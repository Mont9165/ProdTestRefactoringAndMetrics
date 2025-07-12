package jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.selogger;

import com.opencsv.CSVReader;
import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.utils.file.MyPathUtil;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static jp.ac.kyushu.ait.posl.utils.program.MyTypeUtils.getArguments;

/**
 * This class read the output of SELogger
 */
public class SeLoggerReader {
    public static MyLogger logger = MyLogger.getInstance();

    public static int HEADER_CLASSES_NO = 0;
    public static int HEADER_CLASSES_PATH = 1;//not used
    public static int HEADER_CLASSES_CLASSNAME = 2;

    public static int HEADER_METHODS_CLASS_NO = 0;
    public static int HEADER_METHODS_METHOD_NO = 1;
    public static int HEADER_METHODS_CLASS_NAME = 2;
    public static int HEADER_METHODS_METHOD_NAME = 3;
    public static int HEADER_METHODS_METHOD_ARG_RETURN = 4;

    public static int HEADER_DATA_ID_NO = 0;
    public static int HEADER_DATA_CLASS_NO = 1;
    public static int HEADER_DATA_METHOD_NO = 2;
    public static int HEADER_DATA_LINE_NO = 3;
    public static int HEADER_DATA_TYPE = 5;

    public static int HEADER_CALL_DATA_NO = 0;
    public static int HEADER_CALL_ORDER = 4;

    /**
     * API to get lines where each line of the test execution exercised
     * @param homeDir
     * @param srcDir
     * @param testDir
     * @param seloggerOutputDir
     * @param testSignatures
     * @return
     */
    public static Map<String, Map<Integer, List<PassedLine>>> getPassLinesMap(String homeDir, String srcDir, String testDir, String seloggerOutputDir, List<String> testSignatures) {
        Map<Long, PassedLine> calls = calcPassLinesMap(homeDir, srcDir, testDir, seloggerOutputDir);
        return groupByTestLine(testDir, calls, testSignatures);
    }

    /**
     * Implements to get lines where each line of the test execution exercised
     * @param testDir
     * @param allCalls
     * @param testSignatures
     * @return
     */
    private static Map<String, Map<Integer, List<PassedLine>>> groupByTestLine(String testDir, Map<Long, PassedLine> allCalls, List<String> testSignatures) {
        Map<String, Map<Integer, List<PassedLine>>> map = new HashMap<>();

        int testLineNo = -1;
        String target = null;
        Set<String> invokedMethods = new HashSet();

        Map<Integer, List<PassedLine>> callsByTestLine = new TreeMap<>();
        List<PassedLine> passedLines = new ArrayList<>();
        for (PassedLine passedLine : allCalls.values()) {
            if (MyProgramUtils.isTestConstructor(passedLine.signature, testDir)){//&&testSignatures.contains(passedLine.signature)
                if(target!=null){
                    map.put(target, callsByTestLine);
                    target = null;
                    callsByTestLine = new TreeMap<>();
                    passedLines = new ArrayList<>();
                    invokedMethods = new HashSet();
                }
            }

            if(testSignatures.contains(passedLine.signature)){
                if(target==null){
                    target = passedLine.signature;
                    testLineNo = passedLine.lineNo;
                    callsByTestLine.put(testLineNo-1, passedLines);
                    passedLines = new ArrayList<>();
                    invokedMethods.add(passedLine.signature.split(";")[0]);
                }else if(target.equals(passedLine.signature)){
                    assert passedLines !=null;
                    callsByTestLine.put(testLineNo, passedLines);
                    passedLines = new ArrayList<>();
                    testLineNo = passedLine.lineNo;
                }else{//a test method invokes test methods
                    passedLines.add(passedLine);
                }
            }else{
                passedLines.add(passedLine);
            }
            invokedMethods.add(passedLine.signature.split(";")[0]);
        }
        if(testLineNo!=-1){
            callsByTestLine.put(testLineNo, passedLines);
            map.put(target, callsByTestLine);
        }
        return map;
    }


    /**
     * get lines where the test executions exercised
     * @param homeDir
     * @param srcDir
     * @param testDir
     * @param seloggerOutputDir
     * @return
     */
    private static Map<Long, PassedLine> calcPassLinesMap(String homeDir, String srcDir, String testDir, String seloggerOutputDir) {

        try {
            //file read
            String classesFile = MyPathUtil.join(homeDir, seloggerOutputDir, "classes.txt");
            String methodsFile = MyPathUtil.join(homeDir, seloggerOutputDir,  "methods.txt");
            String dataIdsFile = MyPathUtil.join(homeDir, seloggerOutputDir, "dataids.txt");
            String callsFile = MyPathUtil.join(homeDir, seloggerOutputDir, "recentdata.txt");
            //read method file
            Map<String, String> methods = getMethods(homeDir, srcDir, testDir, classesFile, methodsFile);
            //read data file
            Map<String, String> process = getProcess(dataIdsFile);
            //read calls
            Map<Long, PassedLine> passes = getPasses(callsFile, methods, process);

            return passes;
        } catch (IOException e) {
            logger.error(e);
            System.out.println(e);
            throw new AssertionError();
        }
    }

    /**
     *
     * @param callsFile
     * @param methods
     * @param programs
     * @return
     * @throws IOException
     */
    public static Map<Long, PassedLine> getPasses(String callsFile, Map<String, String> methods, Map<String, String> programs) throws IOException {
        Map<Long, PassedLine> calls = new TreeMap<>();
        Set<String> unique = new HashSet<>();
        CSVReader reader = new CSVReader(new FileReader(callsFile));
        for (String[] record : reader) {
            String called_no = record[HEADER_CALL_DATA_NO];
            String[] called_contents = programs.get(called_no).split(":");
            String methodNo = called_contents[0];
            String lineNo = called_contents[1];
            String type = called_contents[2];
            if (lineNo.equals("0")) {
                continue;
            }
            PassedLine passedLine = null;
            for (int i = HEADER_CALL_ORDER; i < record.length; i += 3) {//patterns that the files has many right side
                Long order = Long.parseLong(record[i]);
                String methodName = methods.get(methodNo);
                if (methodName.contains("$")) {
                    continue;//to deal with access$100
                }
                passedLine = new PassedLine(methodName, Integer.parseInt(lineNo));
                passedLine.type=type;
                if (!unique.contains(passedLine.toString())) {
                    calls.put(order, passedLine);
                } else {
                    break;
                }
            }
            if (passedLine != null) {
                unique.add(passedLine.toString());
            }
        }
        return calls;
    }

    /**
     *
     * @param dataIdsFile
     * @return
     * @throws IOException
     */
    public static Map<String, String> getProcess(String dataIdsFile) throws IOException {
        Map<String, String> programs = new HashMap<>();
        CSVReader reader = new CSVReader(new FileReader(dataIdsFile));
        for (String[] line : reader) {
            programs.put(line[HEADER_DATA_ID_NO], line[HEADER_DATA_METHOD_NO] + ":" + line[HEADER_DATA_LINE_NO] + ":" + line[HEADER_DATA_TYPE]);
        }
        return programs;
    }

    /**
     *
     * @param srcDir
     * @param testDir
     * @param classesFile
     * @param methodsFile
     * @return
     * @throws IOException
     */
    public static Map<String, String> getMethods(String homeDir, String srcDir, String testDir, String classesFile, String methodsFile) throws IOException {
        //class
        Map<String, String> classes = new HashMap<>();
        Map<String, String> paths = new HashMap<>();
        CSVReader reader = new CSVReader(new FileReader(classesFile));
        for (String[] csv : reader) {
            String classPath = csv[HEADER_CLASSES_CLASSNAME];
            String basePath = csv[HEADER_CLASSES_PATH];
            String srcOrTest;
            String moduleName = getModuleName(basePath, homeDir, "target");
            if (basePath.endsWith("test-classes/")) {
                srcOrTest = MyPathUtil.join(moduleName, testDir);
            } else if (basePath.endsWith("classes/")) {
                srcOrTest = MyPathUtil.join(moduleName, srcDir);
            } else if (basePath.contains("/.m2/repository/")) {
                //srcOrTest = srcDir;
                throw new AssertionError();
            } else {
//                System.out.println(basePath);
                logger.error(basePath);
//                throw new AssertionError();
                continue;
            }

            String filePath = classPath.split("\\$")[0]+".java";
            String[] dirs = classPath.split("/");
            String className = dirs[dirs.length-1].replaceAll("\\$", MyProgramUtils.SEPARATOR_CLASS_REGEX);
            //                className = className.replaceAll("\\$[0-9]+","");//無名クラス対応

            classes.put(csv[HEADER_CLASSES_NO], className);
            paths.put(csv[HEADER_CLASSES_NO], MyPathUtil.join(srcOrTest, filePath));
        }
        //method
        Map<String, String> methods = new HashMap<>();
        CSVReader reader2 = new CSVReader(new FileReader(methodsFile));
        for (String[] csv : reader2) {
            //make signature with class
            String no =csv[HEADER_METHODS_CLASS_NO];
            String className = classes.get(no);
            String path = paths.get(no);
            String methodName = csv[HEADER_METHODS_METHOD_NAME];
            String arguments = getArguments(csv[HEADER_METHODS_METHOD_ARG_RETURN]);
            logger.trace(className+":"+methodName);
            //to handle constructor
            if (methodName.equals("<init>")||methodName.equals("<clinit>")) {
                methodName = "<init>";
                //subclass
//                if (className.contains("$")) {
//                    String[] tmp = className.replace(".java", "").split("/");
//                    String c = tmp[tmp.length - 1];
//                    String[] a = c.split("\\$");
//                    for (int i = 0; i < a.length - 1; i++) {
//                        arguments = arguments.replaceFirst(a[i], "");
//                        arguments = arguments.replaceFirst("\\$", "");
//                    }
//                }
            }
//            methodName = methodName.replaceAll("\\$[0-9]+","");
            methodName = methodName.replaceAll("lambda\\$","");


            String signature = MyProgramUtils.getSignature(path, className, methodName, arguments);
            methods.put(csv[HEADER_METHODS_METHOD_NO], signature);
            //HEADER_METHODS_METHOD_ARG_RETURN

        }
        return methods;
    }





    static String getModuleName(String path, String homeDir, String targetDirName){
        path = path.replace("file:", "");
        path = path.replace(homeDir+"/", "");//Don't remove slash.
        path = path.split(targetDirName)[0];

        return path;
    }
}
