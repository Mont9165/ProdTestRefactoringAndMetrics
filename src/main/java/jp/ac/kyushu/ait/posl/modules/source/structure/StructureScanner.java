package jp.ac.kyushu.ait.posl.modules.source.structure;

import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.beans.source.TestMethodDefinition;
import gr.uom.java.xmi.*;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.utils.file.MyPathUtil;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;
import jp.ac.kyushu.ait.posl.utils.uml.UmlUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructureScanner {
    MyLogger logger = MyLogger.getInstance();
    BuildToolSettingController bc;
    public String prefix="";
    private static final String systemFileSeparator = Matcher.quoteReplacement(File.separator);

    /**
     * whether this is the structure in the parent commit. (This is for Cross build)
     */
    public StructureScanner(BuildToolSettingController maven){
        this.bc = maven;
    }


    /**
     * create anda list of method definitions using umlClass given by AST
     */
    public Structure scan() {
        Structure structure = new Structure(this.bc);
        if (this.bc.getModules()==null){
            scan(structure);
        }else{
            structure.initModules();
            for(String modName: this.bc.getModules().keySet()){
                BuildToolSettingController b = this.bc.getModules().get(modName);
                StructureScanner sc = new StructureScanner(b);
                Structure s = sc.scan();
                structure.addModule(modName, s);
            }
        }

        return structure;
    }


    private void scan(Structure structure){
        File f = new File(structure.homeDir);
        logger.trace("root: "+structure.homeDir);
        UMLModel model = getUmlModel(f);
        List<UMLClass> classes = model.getClassList();

        for (UMLClass umlClass: classes){
            if(umlClass.getSourceFile().startsWith(structure.targetDir)){
                continue;//sometimes class binary files are scanned
            }

            this.addMethods(structure, umlClass, classes);
            for(UMLAnonymousClass a: umlClass.getAnonymousClassList()){
                this.addMethods(structure, a, classes);
            }

        }
    }

    /**
     * read files and returns UMLModels
     * @param f
     * @return
     */
    private UMLModel getUmlModel(File f) {
        UMLModelASTReader reader;
        try {
            List<String> javaFilePaths = getJavaFilePaths(f);
            Map<String, String> javaFileContents = new LinkedHashMap<String, String>();
            Set<String> repositoryDirectories = new LinkedHashSet<String>();
            for(String path : javaFilePaths) {
                String fullPath = f + File.separator + path.replaceAll("/", systemFileSeparator);
                String contents = FileUtils.readFileToString(new File(fullPath));
                javaFileContents.put(path, contents);
                String directory = path;
                while(directory.contains("/")) {
                    directory = directory.substring(0, directory.lastIndexOf("/"));
                    repositoryDirectories.add(directory);
                }
            }
            reader = new UMLModelASTReader(javaFileContents, repositoryDirectories, true);
        } catch (IOException e) {
            logger.error(e);
            throw new AssertionError();
        }
        return reader.getUmlModel();
    }

    private static List<String> getJavaFilePaths(File folder) throws IOException {
        Stream<Path> walk = Files.walk(Paths.get(folder.toURI()));
        List<String> paths = walk.map(Path::toString)
                .filter(f -> f.endsWith(".java"))
                .map(x -> x.substring(folder.getPath().length()+1).replaceAll(systemFileSeparator, "/"))
                .collect(Collectors.toList());
        walk.close();
        return paths;
    }

    /**
     *
     * @param umlClass
     * @param classes
     */
    private void addMethods(Structure structure, UMLAbstractClass umlClass, List<UMLClass> classes) {
        MethodDefinition md;
        List<String> methodsInAClass = new ArrayList<>();
        extractFields(structure, umlClass);
        for(UMLOperation umlOperation: umlClass.getOperations()){
            if(umlClass.getSourceFile().startsWith(structure.testDir)) {
                md = new TestMethodDefinition(umlClass, umlOperation, classes, structure.moduleName);
            }else{
                md = new MethodDefinition(umlClass, umlOperation, classes, structure.moduleName);
            }

            //check just in case
            if(UmlUtils.checkIfMethod(umlOperation)){
                methodsInAClass.add(md.methodInfo.signature);
//                assert (!structure.structureBySignature.containsKey(md.signature));
                structure.structureBySignature.put(md.methodInfo.signature, md);
                structure.moduleBySignature.put(md.methodInfo.signature, structure.moduleName);
                structure.moduleByFileName.put(md.methodInfo.fileName, structure.moduleName);
                structure.moduleNameByFileNameWithoutModuleName.put(umlClass.getSourceFile(), structure.moduleName);
            }else{
                logger.error(md.methodInfo.signature);
                throw new AssertionError();
            }
        }
        String fileKey = this.prefix;
        if(structure.moduleName!=null){
            fileKey = MyPathUtil.join(fileKey, structure.moduleName);
        }
        fileKey = MyPathUtil.join(fileKey, umlClass.codeRange().getFilePath());
        //To deal with a bug in UMLClass
        List<String> tmp = structure.structureByFileName.getOrDefault(fileKey, new ArrayList<String>());
        tmp.addAll(methodsInAClass);//These are subClasses
        structure.structureByFileName.put(fileKey, tmp);

    }

    /**
     * scan attributions (fields) in a class
     * @param umlClass
     */
    private void extractFields(Structure structure, UMLAbstractClass umlClass) {//filePath+lineNo=List<path+variableName>(classies t)
        String fileName = MyProgramUtils.getQualifiedName(umlClass.getSourceFile(), structure.srcDir, structure.testDir);
        if(umlClass instanceof UMLClass){
            List<UMLAttribute> fields = umlClass.getAttributes();
            for(UMLAttribute i: fields){
                String filePath = i.getLocationInfo().getFilePath();
                int startNo = i.getLocationInfo().getStartLine();
                int endNo = i.getLocationInfo().getEndLine();

                String variableType = i.getType().getClassType();
                String variableName = i.getName();
                String variableQualifiedName = MyProgramUtils.getQualifiedName(filePath, structure.srcDir, structure.testDir)+"."+variableName;

                for(int k=startNo;k<=endNo;k++){
//                    System.out.println(k);
                    String key = MyProgramUtils.getFieldSignature(filePath,k);
                    Set<String> set = structure.fieldsPosition.getOrDefault(key, new HashSet<>());
                    set.add(variableQualifiedName);
                    structure.fieldsPosition.put(key, set);
                }
            }
        }

    }


}
