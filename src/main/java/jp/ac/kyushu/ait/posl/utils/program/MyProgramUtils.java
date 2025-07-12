package jp.ac.kyushu.ait.posl.utils.program;

import gr.uom.java.xmi.decomposition.AbstractStatement;
import gr.uom.java.xmi.decomposition.VariableDeclaration;
import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.utils.general.MyListUtils;

import java.util.*;

public class MyProgramUtils {
    public static final String SEPARATOR_CLASS = ".";
    public static final String SEPARATOR_CLASS_REGEX = "\\.";
    public static final String SEPARATOR_FILE =";";
    public static final String SEPARATOR_ARG="#";

    public static final String SEPARATOR_FIELD_LINE = "@";
    /**
     * to create method signature from String
     * @param relativePath
     * @param method
     * @param arguments
     * @return
     */
    public static String getSignature(String relativePath, String className, String method, String arguments){
        return relativePath+ SEPARATOR_FILE +className+SEPARATOR_CLASS+method+SEPARATOR_ARG+arguments;
    }

    /**
     * to create method signature from string and lists
     * @param relativePath
     * @param method
     * @param arguments
     * @return
     */
    public static String getSignature(String relativePath, String className, String method, List<String> arguments){
        String args = MyListUtils.flatten(arguments);
        return getSignature(relativePath, className, method, args);
    }

    /**
     * to create method signature from MethodDefinition
     * @param md
     * @return
     */
    public static String getSignature(MethodDefinition md) {
        String arguments = MyListUtils.flatten(md.methodInfo.arguments);
        return getSignature(md.getFileName(), md.methodInfo.className, md.getMethodName(), arguments);
    }
    /**
     * Extract the file name
     * @param signature
     * @param removeJava
     * @return
     */
    public static String getFilePathFromSignature(String signature, boolean removeJava) {
        String tmp = signature.split(SEPARATOR_FILE)[0];
        if(removeJava){
            return tmp.replace(".java","");
        }
        return tmp;
    }
    /**
     * Extract the file name
     * @param filePath
     * @param removeJava
     * @return
     */
    public static String getFileNameFromFilePath(String filePath, boolean removeJava) {
        String[] tmp = filePath.split("/");
        String fileName = tmp[tmp.length-1];
        if(removeJava){
            return fileName.replace(".java","");
        }
        return fileName;
    }

    /**
     * Extract the file name
     * @param signature
     * @param removeJava
     * @return
     */
    public static String getFileNameFromSignature(String signature, boolean removeJava) {
        String tmp = signature.split(SEPARATOR_FILE)[0];
        String[] arr = tmp.split("/");
        String fileName = arr[arr.length-1];
        if(removeJava){
            return fileName.replace(".java","");
        }
        return fileName;
    }
    /**
     * Extract the class name
     * @param signature
     * @return
     */
    public static String getLastClassNameFromSignature(String signature) {
        String classAndMethod = signature.split(SEPARATOR_FILE)[1];
        String[] classes = classAndMethod.split(SEPARATOR_CLASS_REGEX);

        return classes[classes.length-2];
    }



    /**
     * Extract the method name
     * @param signature
     * @param isNeedArguments
     * @return
     */
    public static String getMethodNameFromSignature(String signature, boolean isNeedArguments) {
        String classAndMethod = signature.split(SEPARATOR_FILE)[1];
        String[] classesAndMethodName = classAndMethod.split(SEPARATOR_CLASS_REGEX);
        String methodName = classesAndMethodName[classesAndMethodName.length-1];
        if(isNeedArguments){
            return methodName;
        }
        return methodName.split(SEPARATOR_ARG)[0];
    }



    /**
     * Check if this method is a constructor
     */
    public static boolean isConstructor(String signature) {
//        String fileName = MyProgramUtils.getClassNameFromSignature(signature);
        String methodName = MyProgramUtils.getMethodNameFromSignature(signature, false);
//        return fileName.equals(methodName);
        return methodName.equals("<init>");

    }
    /**
     * Check if this method is a constructor in test code
     */
    public static boolean isTestConstructor(String sig, String testDir) {
        if(sig.startsWith(testDir)){
            return isConstructor(sig);
        }
        return false;
    }
    /**
     * Get sub class name
     */
    public static String getClassSubClassAndMethod(String packageName, String fileName) {
        boolean flg = false;
        StringJoiner sj = new StringJoiner(SEPARATOR_CLASS);
        for(String s: packageName.split("\\.")){
            if(flg){
                sj.add(s);
            }
            if(s.equals(fileName)){
                flg = true;
                sj.add(s);
            }
        }
        return sj.toString();
    }
    /**
     * Get sub class name
     */
    public static String getClassAndSubClasses(String packageName, String fileName) {
        String[] classAndMethod = getClassSubClassAndMethod(packageName, fileName).split(SEPARATOR_CLASS_REGEX);
        ArrayList<String> classes =new ArrayList<>(Arrays.asList(classAndMethod));
        StringJoiner sj = new StringJoiner(SEPARATOR_CLASS);
        classes.forEach(sj::add);
        return sj.toString();
    }

    /**
     * to create field signature
     * @param filePath
     * @param lineNo
     * @return
     */
    public static String getFieldSignature(String filePath, Integer lineNo) {
        return filePath+SEPARATOR_FIELD_LINE+lineNo;
    }

    /**
     * Update scopes of variables
     */
    public static void updateScopes(AbstractStatement s, Map<Integer, Set<String>> scopes, Set<String> localVariables) {
        int lineNo = s.getLocationInfo().getStartLine();
        Set<String> scope = scopes.get(lineNo);
        if(scope!=null){
            localVariables.removeAll(scope);
        }
        for(VariableDeclaration v: s.getVariableDeclarations()){
            String[] arr = v.getScope().toString().split("[:\\-]");
            assert arr.length == 4;
            int expireLineNo = Integer.parseInt(arr[2]);
            Set<String> vars = scopes.getOrDefault(expireLineNo, new HashSet<>());
            vars.add(v.getVariableName());
            scopes.put(expireLineNo, vars);
            localVariables.add(v.getVariableName());
        }
    }


    /**
     * To get qualified name
     */
    public static String getQualifiedName(String fileName, String srcDir, String testDir) {
        srcDir = MyProgramUtils.addLastSlash(srcDir);
        fileName = MyProgramUtils.removeUnRelevantDirectoryName(srcDir, fileName);
        fileName = MyProgramUtils.removeUnRelevantDirectoryName(testDir, fileName);
        fileName = fileName.replace(".java", "");
        fileName = fileName.replaceAll("/", "\\.");
        return fileName;
    }
    /**
     * Append slash after directory's name
     */
    public static String addLastSlash(String dir) {
        if (dir==null||dir.equals("")) return "";
        if(dir.endsWith("/")){
            return dir;
        }
        return dir +"/";
    }



    public static String removeUnRelevantDirectoryName(String reg, String target){
        reg = reg.replaceAll("\\$\\{.+\\}/", "");
        reg = MyProgramUtils.addLastSlash(reg);
        String[] tmp = target.split(reg, 2);
        if(tmp.length == 1){
            return target;
        }else if(tmp.length == 2) {
            return tmp[1];
        }else{
            System.out.println("reg: "+reg);
            System.out.println("target: "+target);
            System.out.println("length: "+tmp.length);
            throw new AssertionError();
        }
    }
    public static String transform2MavenSignature(String m, String srcDir, String testDir) {
        m = getQualifiedName(m, srcDir, testDir);
        String a = m.split(SEPARATOR_ARG)[0];
        a = a.replace(SEPARATOR_FILE, SEPARATOR_ARG);
        return a;
    }

    /**
     * This is only for test methods
     * @param signature
     * @return
     */
    public static String transform2SignatureFromMavenSignature(String signature) {
        String a = signature.replace(".", "/");
        a = a.replace(SEPARATOR_ARG, ".java;");
        return a+SEPARATOR_ARG;
    }



    public static String getClassSignatureFromFullSignature(String sig) {
        String fileName = getFilePathFromSignature(sig, false);
        String classNames = getClassNamesFromSignature(sig);
        return fileName+ SEPARATOR_FILE +classNames;
    }

    public static String getClassNamesFromSignature(String sig) {
        String[] array = sig.split(SEPARATOR_FILE)[1].split(SEPARATOR_ARG)[0].split(SEPARATOR_CLASS_REGEX);
        ArrayList<String> subclasses =new ArrayList<>(Arrays.asList(array));
        if (subclasses.size() == 1) {
            throw new RuntimeException("No way");
        }else if(subclasses.size() >= 2){//Class.method
            subclasses.remove(subclasses.size()-1);
            StringJoiner sj = new StringJoiner(SEPARATOR_CLASS);
            subclasses.forEach(sj::add);
            return sj.toString();
        }
        throw new RuntimeException("No way");
    }









    //-----------
    /**
     * to get class name
     */
    public static String getClassNameFromPath(String path) {
        String[] tmp = path.split("/");
        String className = tmp[tmp.length-1];
        if(className.contains("$")){
            tmp = className.split("\\$");
            className = tmp[tmp.length-1];
        }
        return className;

    }
    /**
     * Get sub class name
     */
    public static String getSubClassAndMethod(String packageName, String parentClassName) {
        boolean flg = false;
        StringJoiner sj = new StringJoiner(SEPARATOR_CLASS);
        for(String s: packageName.split("\\.")){
            if(flg){
                sj.add(s);
            }
            if(s.equals(parentClassName)){
                flg = true;
                sj.add(s);
            }
        }
        return sj.toString();
    }

    public static String getPackageName(String packageName, String className) {
        boolean flg = false;
        StringJoiner sj = new StringJoiner(SEPARATOR_CLASS);
        for(String s: packageName.split("\\.")){
            if(s.equals(className)){
                break;
            }
            sj.add(s);
        }
        return sj.toString();
    }
}
