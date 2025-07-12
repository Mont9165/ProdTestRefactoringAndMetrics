package jp.ac.kyushu.ait.posl.modules.source.structure;

import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.beans.source.TestMethodDefinition;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.utils.file.MyFileNameUtils;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;

import java.util.*;

public class Structure {
    /**
     * the top directory path of the production code.
     */
    public String srcDir;
    /**
     * the top directory path of the test code.
     */
    public String testDir;
    /**
     * the top directory path of the target files (e.g., classes, META-INF).
     */
    public String targetDir;
    /**
     * Map to return MethodDefinition by method signature
     */
    public Map<String, MethodDefinition> structureBySignature;
    /**
     * Map to return moduleName by method signature
     */
    public Map<String, String> moduleBySignature;
    /**
     * Map to return moduleName by method signature
     */
    public Map<String, String> moduleByFileName;
    /**
     * Map to return moduleName by method signature
     */
    public Map<String, String> moduleByPureFileName;
    /**
     * Map to return moduleName by file name without Module.
     *
     */
    public Map<String, String> moduleNameByFileNameWithoutModuleName;
    /**
     * Map to return MethodDefinitions by file name
     */
    public Map<String, List<String>> structureByFileName;//filename, signature
    HashMap<String, Structure> modules = new HashMap<String, Structure>();
    public String homeDir;
    /**
     * Map for variables name.
     * Receive filePath@lineNo and return path.variableName
     */
    public Map<String, Set<String>> fieldsPosition;//filePath+lineNo->path+variableName

    public String moduleName;
    public Structure(BuildToolSettingController bc){
        srcDir = MyFileNameUtils.getDirectoryName(bc.getSrcDir(false));
        testDir = MyFileNameUtils.getDirectoryName(bc.getTestDir(false));//do not end with slash
        targetDir = MyFileNameUtils.getDirectoryName(bc.getTargetDir(false));//do not end with slash
        homeDir = bc.getHomeDir();
        structureByFileName = new TreeMap<>();
        moduleNameByFileNameWithoutModuleName = new TreeMap<>();
        structureBySignature = new TreeMap<>();
        moduleBySignature = new TreeMap<>();
        moduleByFileName = new TreeMap<>();
        moduleByPureFileName = new TreeMap<>();
        fieldsPosition = new TreeMap<>();
        moduleName = bc.getModuleName();
    }


    public Collection<String> getAllSignatures() {
        return this.structureBySignature.keySet();
    }

    public Collection<MethodDefinition> getAllMethods() {
        return this.structureBySignature.values();
    }
    public Collection<String> getAllFiles() {
        return this.structureByFileName.keySet();
    }
    public boolean hasMethod(String signature) {
        return this.structureBySignature.containsKey(signature);
    }

    public boolean hasFile(String path) {
        return this.structureByFileName.containsKey(path);
    }

    public List<String> getSignatures(String filePath){
        return this.structureByFileName.get(filePath);
    }

    public MethodDefinition getMethod(String signature) {
        return structureBySignature.get(signature);
    }
    /**
     * receive file path and line no and return method
     * @param path
     * @param line
     * @return
     */
    public MethodDefinition getMethod(String path, Integer line) {
        if(!path.endsWith(".java")) return null;
        if(path.endsWith("package-info.java")) return null;
        List<String> signatures = this.structureByFileName.get(path);
        if(signatures!=null) {
            for(String s: signatures){
                MethodDefinition m = this.getMethod(s);
                if(m.isIn(line)){
                    return m;
                }
            }
        }
        return null;
    }



    /**
     * receive file path and returns the methods in the file
     * @return
     */
    private List<MethodDefinition> getMethods(String filename) {
        List<String> signatures = this.structureByFileName.get(filename);
        if(signatures==null) return null;
        List<MethodDefinition> methods = this.getMethods(signatures);
        return methods;
    }
    /**
     * receive a list of method signatures and returns method definitions
     * @return
     */
    private List<MethodDefinition> getMethods(List<String> signatures) {
        List<MethodDefinition> methods = new ArrayList<>();
        for(String signature: signatures){
            methods.add(structureBySignature.get(signature));
        }
        return methods;
    }

    /**
     * returns a list of all the signature in the production code
     * @return
     */
    public Set<String> getProductionSignature() {
        Set<String> set = new HashSet<>();
        for(MethodDefinition md: this.structureBySignature.values()){
            if(!md.isInTest()){
                set.add(md.getSignature());
            }
        }
        return set;
    }

    /**
     * returns a list of all the signature in the test code
     * @return
     */
    public Set<String> getTestSignature() {
        Set<String> set = new HashSet<>();
        for(MethodDefinition md: this.structureBySignature.values()){
            if(md.isInTest()) {
                TestMethodDefinition tmd = (TestMethodDefinition) md;
                if(tmd.isTestCase()){
                    set.add(md.getSignature());
                }
            }
        }
        return set;
    }
    public TestMethodDefinition getTestMethod(String s) {
        return (TestMethodDefinition) this.getMethod(s);
    }

    /**
     * receive file path and line no, and then return field name
     * @param lineNo
     * @return
     */
    public Set<String> getFields(String filePath, Integer lineNo) {
        return fieldsPosition.get(MyProgramUtils.getFieldSignature(filePath, lineNo));
    }

    /**
     * returns all the test files
     * @return
     */
    public Set<String> getTestFiles() {
        Set<String> files = new HashSet<>();
        for(MethodDefinition md: this.structureBySignature.values()){
            if(md.isInTest()){
                files.add(md.getFileName().split("\\$")[0]);
            }
        }
        return files;
    }
    public void updateMethod(MethodDefinition md) {
        this.structureBySignature.put(md.methodInfo.signature, md);
    }

    public void initModules() {
        this.modules = new HashMap<String, Structure>();
    }

    public void addModule(String modName, Structure s) {
        this.modules.put(modName, s);
        structureBySignature.putAll(s.structureBySignature);
        structureByFileName.putAll(s.structureByFileName);
        moduleBySignature.putAll(s.moduleBySignature);
        moduleByFileName.putAll(s.moduleByFileName);
        moduleByPureFileName.putAll(s.moduleByPureFileName);
        moduleNameByFileNameWithoutModuleName.putAll(s.moduleNameByFileNameWithoutModuleName);
    }



    /**
     * returns a list of all the method in the test code
     * @return
     */
    public List<MethodDefinition> getALLTestMethods() {
        List<MethodDefinition> collection = new ArrayList<>();
        for(MethodDefinition md: this.structureBySignature.values()){
            if(md.isInTest()) {
                TestMethodDefinition tmd = (TestMethodDefinition) md;
                if(tmd.isTestCase()){
                    collection.add(md);
                }
            }
        }
        return collection;
    }

    public String getModuleNameByMethodName(String signature) {
        return this.moduleBySignature.get(signature);
    }

    public String getModuleNameByClassName(String targetName) {
        return this.moduleByFileName.get(targetName);
    }

    public String findModule(String fileName) {
        if (modules.size()==0){
            return "";
        }
        String rtn = moduleNameByFileNameWithoutModuleName.get(fileName);
        if (rtn==null) throw new AssertionError();
        return rtn;
    }
}
