package jp.ac.kyushu.ait.posl.beans.source;

import gr.uom.java.xmi.*;
import gr.uom.java.xmi.decomposition.AbstractStatement;
import gr.uom.java.xmi.decomposition.LeafExpression;
import jp.ac.kyushu.ait.posl.beans.source.db.MethodDefinition4DB;
import jp.ac.kyushu.ait.posl.utils.file.MyPathUtil;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;
import org.refactoringminer.api.Refactoring;

import java.io.Serializable;
import java.util.*;

public class MethodDefinition implements Serializable {

    private static final long serialVersionUID = 1L;
    public Integer id;
    static MyLogger logger = MyLogger.getInstance();

    /**
     * Class information given by Refactoring Miner
     */
    private final UMLAbstractClass umlClass;
    /**
     * Mehtod information given by Refactoring Miner
     */
    private final UMLOperation umlOperation;

    public Map<Integer, Set<Refactoring>> inherentRefactorings;//lineNo, refactorings

    /**
     * variable names used in each line
     */
    public Map<Integer, Set<String>> usedVariablesWithoutLocals;//lineNo, isChange

    public String moduleName;
    public boolean isConstructor = false;
    public MethodDefinition4DB methodInfo;
    /**
     * Copy the contents of class and method information given by RefactoringMiner
     * @param umlClass
     * @param umlOperation
     */
    public MethodDefinition(UMLAbstractClass umlClass, UMLOperation umlOperation, List<UMLClass> classes, String moduleName){
        this.moduleName = moduleName;
        this.umlClass = umlClass;
        this.umlOperation = umlOperation;
        methodInfo = new MethodDefinition4DB();
        methodInfo.fileName = MyPathUtil.join(this.getModuleName(), umlClass.getSourceFile());
        String tmpClassName = MyProgramUtils.getClassNameFromPath(methodInfo.fileName).replace(".java","");
        String tmpPackageName = this.getFullClassName(umlClass);
        //To handle sub-classes
        if(!(tmpPackageName.endsWith("."+tmpClassName)||tmpPackageName.equals(tmpClassName))){
            methodInfo.className = MyProgramUtils.getSubClassAndMethod(tmpPackageName, tmpClassName);
        }else{
            methodInfo.className = tmpClassName;
        }
        methodInfo.packageName = MyProgramUtils.getPackageName(tmpPackageName, tmpClassName);


        //To handle sub-classes
        methodInfo.methodName = umlOperation.getName();

        if (umlOperation.isConstructor()){
            methodInfo.methodName = "<init>";
            isConstructor = true;
        }
        methodInfo.starts = umlOperation.codeRange().getStartLine();
        methodInfo.ends = umlOperation.codeRange().getEndLine();
        this.setAnnotations(umlOperation.getAnnotations());
        this.setArguments(umlOperation);
        this.setGenerics(umlClass, umlOperation);
        this.normalizeArgumentsWithGenerics();
        //initialize
        methodInfo.changedLines = new TreeMap<>();
        usedVariablesWithoutLocals = new TreeMap<>();
        inherentRefactorings = new TreeMap<>();
        for(int i=methodInfo.starts; i <= methodInfo.ends; i++){
            methodInfo.changedLines.put(i, false);
            inherentRefactorings.put(i, new HashSet<>());
        }
        methodInfo.signature = MyProgramUtils.getSignature(this);
        methodInfo.anotherSignature = methodInfo.signature;//this will be changed if this method is renamed
        //find parent
        this.setParent(classes);
        //set fields information
        if(umlClass instanceof UMLClass){
            this.setField((UMLClass)umlClass, classes);
        }

//        System.out.println("++++++++++++++++++++++");
//        System.out.println("fileName: "+fileName);
//        System.out.println("className: "+className);
//        System.out.println("packageName: "+packageName);
//        System.out.println("methodName: "+methodName);
//        System.out.println("arguments: "+arguments);
//        System.out.println("generics: "+generics);
//        System.out.println("++++++++++++++++++++++");

    }

    private String getClassName(UMLOperation umlOperation) {
        String fullPackage = umlOperation.getClassName();
        String[] paths = fullPackage.split("\\.");
        return paths[paths.length-1];
    }

    /**
     * Extract field information
     * @param umlClass
     * @param classes
     */
    private void setField(UMLClass umlClass, List<UMLClass> classes) {
        if(umlOperation.getBody()==null){
            return;
        }
        Map<String, String> importMap = this.getImportMap(umlClass);
        Set<String> fields = this.getFields(umlClass);
        Map<String, String> pFields =this.getParentsFields(umlClass, classes);
        Set<String> localVariables = new HashSet<>(umlOperation.getParameterNameList());
        Map<Integer, Set<String>> scopes = new HashMap<>();
        //find used fields for each line in this method
        for(AbstractStatement s: umlOperation.getBody().getCompositeStatement().getStatements()){
            int lineNo = s.getLocationInfo().getStartLine();
            MyProgramUtils.updateScopes(s, scopes, localVariables);
            Set<String> vars = new HashSet<>();
            for(LeafExpression leafExpression: s.getVariables()){
                String v = leafExpression.toString();
                String v_ = v.replaceAll("^this.", "");//to handle "this."
                if(localVariables.contains(v)){//cannot detect functions.special.B.i because of umlOperation Bug
                }else if(fields.contains(v_)){//determine if this is "this"
                    String f = this.eliminateFirstDot(umlClass.getName()+"."+v_);
                    vars.add(f);
                }else if(pFields.containsKey(v_)){
                    vars.add(pFields.get(v_));
                }else{//find from imports
                    String type = v.split("\\.")[0];
                    String ansImport = importMap.get(type);
                    if(ansImport!=null){
                        vars.add(ansImport+"."+v);
                    }else{
                        //package (e.g., functions.special.B.i)
                        String[] candidates = s.toString().split("[^\\w\\.]");
                        for(String a: candidates){
                            if(a.endsWith("."+v)) {
                                vars.add(a);
                                if(!a.startsWith(umlClass.getPackageName())){
                                    vars.add(umlClass.getPackageName() + "." + a);//This is not good but it can't be helped
                                }
                            }
                        }
                    }
                }
            }
            //If no field is found, we see this as local variable
            usedVariablesWithoutLocals.put(lineNo, vars);
        }
    }

    /**
     * Extract fields if the class uses the parent's fields
     * @param umlClass
     * @param classes
     * @return
     */
    private Map<String, String> getParentsFields(UMLClass umlClass, List<UMLClass> classes) {
        Map<String, String> set = new HashMap<>();
        for (UMLClass cls : classes) {
            if (umlClass.isSubTypeOf(cls)) {
                //parent
                List<UMLAttribute> list = cls.getAttributes();
                for(UMLAttribute a: list){
                    if(a.getVisibility().equals("private")){
                        continue;
                    }
                    String v_ = a.getName();
                    set.put(v_, this.eliminateFirstDot(cls.getName()+"."+v_));
                }
            }
        }
        return set;
    }

    /**
     * get the information about what libraries are used
     * @param umlClass
     * @return
     */
    private Map<String, String> getImportMap(UMLClass umlClass) {
        Map<String, String> importMap = new HashMap<>();
        List<UMLImport> umlImports = umlClass.getImportedTypes();
        List<String> imports = Collections.singletonList(umlImports.toString());
        for(String imp: imports){
            String[] impArray = imp.split("\\.");
            String i = impArray[impArray.length-1];
            List<String> list = Arrays.asList(impArray);
            StringJoiner sb = new StringJoiner(".");
            list.subList(0,list.size()-1).forEach(sb::add);
            importMap.put(i, sb.toString());
        }
        return importMap;
    }

    /**
     * delete redundant dots
     * @param s
     * @return
     */
    private String eliminateFirstDot(String s) {
        return s.replaceAll("^\\.", "");
    }

    /**
     * Extract fields information from UMLAbstractClass
     * @param umlClass
     * @return
     */
    public Set<String> getFields(UMLAbstractClass umlClass) {
        Set<String> list = new HashSet<>();
        List<UMLAttribute> attributes = umlClass.getAttributes();
        attributes.forEach(c->list.add(c.getName()));
        return list;
    }
    /**
     * Get PackageName from UMLAbstractClass
     * @param umlOperation
     * @return
     */
    private String getPackageName(UMLOperation umlOperation) {
        String fullName = umlOperation.getClassName();

        StringJoiner sj = new StringJoiner(".");
        ArrayList<String> li = new ArrayList<String>(Arrays.asList(fullName.split("\\.")));
        li.remove(li.size()-1);
        li.forEach(sj::add);
        return sj.toString();
    }

    private String getFullClassName(UMLAbstractClass umlClass) {
        if(umlClass instanceof UMLClass){//Normal class
            return   ((UMLClass) umlClass).getName();
        }else if(umlClass instanceof UMLAnonymousClass){//if this class is anonymous
            return  ((UMLAnonymousClass) umlClass).getName();
        }else{
            throw new AssertionError();
        }
    }



    /**
     * Sometimes, generics are used in method arguments.
     * When this happens, Object is used as SELogger does.
     */
    private void normalizeArgumentsWithGenerics() {
        List<String> newArguments = new ArrayList<>();
        for(int i=0; i<methodInfo.arguments.size();i++){
            String a = methodInfo.arguments.get(i);
            if(methodInfo.generics.contains(a)){
                newArguments.add("Object");
            }else if(methodInfo.generics.contains(a.replaceAll("\\]",""))){//Array
                newArguments.add("Object]");
            } else{
                newArguments.add(a);
                //TODO: if there is a class that has the same name as generics
            }
        }
        methodInfo.arguments = newArguments;
    }

    /**
     * Set generics information.
     * @param abstractClass
     * @param umlOperation
     */
    private void setGenerics(UMLAbstractClass abstractClass, UMLOperation umlOperation) {
        Set<String> generics = new HashSet<>();
        if(abstractClass instanceof UMLClass) {
            UMLClass umlClass = (UMLClass) abstractClass;
            for (UMLTypeParameter a : umlClass.getTypeParameters()) {
                generics.add(a.getName());
            }
            for (UMLTypeParameter a : umlOperation.getTypeParameters()) {
                generics.add(a.getName());
            }

        }
        methodInfo.generics = generics;
    }

    public MethodDefinition(){
        umlOperation = null;
        umlClass = null;
    }

    private void setArguments(UMLOperation umlOperation) {
        methodInfo.arguments = new ArrayList<>();
        List<UMLParameter> l = umlOperation.getParametersWithoutReturnType();
        logger.trace("umlOperation.getName(): "+umlOperation.getName());
        l.forEach(e->methodInfo.arguments.add(e.getType().getClassType()+this.getArraySign(e)));
    }

    private String getArraySign(UMLParameter e) {
        int dim = e.getType().getArrayDimension();
        String ret="";
        for(int i=0;i<dim;i++){
            ret+="]";
        }
        return ret;
    }




    /**
     * Find the parent and set it in superClassPath
     * @param classes
     */
    public void setParent(List<UMLClass> classes) {
        if(umlClass instanceof UMLClass) {
            UMLClass umlClass2 = (UMLClass) umlClass;
            UMLType parent = umlClass2.getSuperclass();
            if (parent != null) {
                //System.out.println(umlClass.getName());
                for (UMLClass cls : classes) {
                    if (umlClass2.isSubTypeOf(cls)) {
                        methodInfo.superClassPath = cls.getSourceFile();
                        return;
                    }
                }
                //If the class is extended on java.lang or something, we ignore it
            }
        }
    }

    private UMLClass findClass(String superClassPath, String className, List<UMLClass> classes) {
        for(UMLClass u: classes){
            boolean isSameFile = u.getSourceFile().equals(superClassPath);
            boolean isSameClassName = u.getName().equals(className);
            if (isSameFile&&isSameClassName) {
                return u;
            }
        }
        return null;
    }



    public String toString(){
        return methodInfo.fileName + ":" + methodInfo.className + ":" + methodInfo.methodName + ":" + methodInfo.starts + "-" + methodInfo.ends;
    }
    public boolean isIn(int key){
        return methodInfo.starts <= key && key <= methodInfo.ends;
    }

    public boolean isInTest(){
        return false;
    }
    public String getSignature(){
        return methodInfo.signature;
    }
    public String getAnotherSignature(){
        return methodInfo.anotherSignature;
    }

    public String getMethodName() {
        return methodInfo.methodName;
    }
    public String getFileName(){
        return methodInfo.fileName;
    }

    public void setAnnotations(List<UMLAnnotation> annotations) {
        methodInfo.annotations = new ArrayList<>();
        for(UMLAnnotation a: annotations){
            methodInfo.annotations.add(a.getTypeName());
        }
    }

    public boolean isTestCase(){
        return methodInfo.annotations.contains("Test");//changed from annotations.contains("ParameterizedTest")
    }
    public boolean isInSameFile(String signature) {
        String fn = MyProgramUtils.getFileNameFromSignature(signature, false);
        return methodInfo.fileName.equals(fn);
    }

    /**
     * Detect the lines that use refactored attributions
     * @param refactoringType
     * @param fieldTypeFullPaths
     */
    public void setFieldRefactoring(Refactoring refactoringType, Set<String> fieldTypeFullPaths) {
        if (fieldTypeFullPaths==null) return;//To handle a bug that RMiner cannot detect the position of attribution annotation
        for(Integer lineNo: this.usedVariablesWithoutLocals.keySet()){
            Set<String> fieldName = this.usedVariablesWithoutLocals.get(lineNo);
            for(String fieldTypeFullPath: fieldTypeFullPaths){
                if(fieldName.contains(fieldTypeFullPath)){
                    this.inherentRefactorings.get(lineNo).add(refactoringType);
                }
            }

        }
    }

    /**
     * check if this method is initializer
     * @return
     */
    public boolean isTestInitializer() {
        for (String a: methodInfo.annotations){
            switch (a){
                case "Before":
                case "After":
                case "BeforeClass":
                case "AfterClass":
                case "BeforeEach":
                case "AfterEach":
                case "BeforeAll":
                case "AfterAll":
                    return true;
                default:
            }
        }
        return false;
    }


    public String getModuleName() {
        return this.moduleName;
    }


    /**
     * This transform refactoring list into String.
     * To identify refactoring, we append hash code from refactoring instance
     * @param map
     * @return
     */
    private Map<Integer, String> transformRefactoring(Map<Integer, Set<Refactoring>> map){
        Map<Integer, String> out = new HashMap<>();
        for(Integer lineNo: map.keySet()){
            Set<Refactoring> refs = map.get(lineNo);
            if(refs.isEmpty()){
                continue;
            }
            else{
                StringJoiner sj = new StringJoiner(",");
                for(Refactoring r: refs){
                    sj.add(r.getRefactoringType().getDisplayName()+"@"+r.hashCode());
                }
                out.put(lineNo, sj.toString());
            }
        }
        return out;
    }


}