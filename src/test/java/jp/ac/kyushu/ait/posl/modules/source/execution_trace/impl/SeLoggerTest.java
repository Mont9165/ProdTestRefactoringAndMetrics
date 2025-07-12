package jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl;

import com.opencsv.CSVReader;
import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.beans.source.TestMethodDefinition;
import jp.ac.kyushu.ait.posl.beans.test.TestInfo;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.selogger.SeLogger;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.selogger.SeLoggerReader;
import jp.ac.kyushu.ait.posl.stub.SeLoggerReaderStub;
import jp.ac.kyushu.ait.posl.stub.SeLoggerStub;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SeLoggerTest {

	@Test
    public void testGetPassLinesMap_N001() {
        String homeDir = System.getProperty("user.dir")+"/";

        SeLogger seLogger = new SeLoggerReaderStub(homeDir,"SeLogger/getPassLinesMap");
        List<String> list = new ArrayList<>();
        list.add("src/test/java/functions/CalculatorTest.java;CalculatorTest.testAdd_N001#");
        list.add("src/test/java/functions/CalculatorTest.java;CalculatorTest.testAdd_N002#");
        list.add("src/test/java/functions/CalculatorTest.java;CalculatorTest.testMinus_N001#");
        list.add("src/test/java/functions/CalculatorTest.java;CalculatorTest.testMinus_N002#");
        list.add("src/test/java/functions/CalculatorTest.java;CalculatorTest.testTimes_N001#");
        list.add("src/test/java/functions/CalculatorTest.java;CalculatorTest.testTimes_N002#");
        list.add("src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.testPower_N001#");
        list.add("src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.testPower_N002#");
        list.add("src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.special_N001#");

        Map<String, Map<Integer, List<PassedLine>>> map = seLogger.getPassLinesMap(null, list);
        System.out.println("keys:"+map.keySet());
        for(String sig: list){
            System.out.println(sig);
            Map<Integer, List<PassedLine>> exp = map.get(sig);
            System.out.println(exp);
            Assert.assertNotNull(exp);
        }
    }

    @Test
    public void testGetPassLinesMap_N002() {

        List<String> testSignatures = new ArrayList<>();
        testSignatures.add("src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.all#");
        String homeDir = System.getProperty("user.dir")+"/";
        SeLogger seLogger = new SeLoggerReaderStub(homeDir,"SeLogger/getPassLinesMap2");
        Map<String, Map<Integer,List<PassedLine>>> map = seLogger.getPassLinesMap(null, testSignatures);
        System.out.println(map.keySet());
        for(String sig: testSignatures){
            System.out.println(sig);
            Map<Integer, List<PassedLine>> exp = map.get(sig);
            Assert.assertNotNull(exp);
        }
    }

    @Test
    public void getArgumentsTest() throws IOException {
        String[] answers = {"Double", "int", "double", "float", "short",
                "long", "boolean", "byte", "char", "byte]",
                "Integer]", "Integer]", "Object", "", "",
                "Double,Double", "String]", "SubClass", "Double,Double,Double,Double", "int,double", "int,double,Double"};
        String homeDir = System.getProperty("user.dir")+"/";
        SeLoggerReaderStub seLogger = new SeLoggerReaderStub(homeDir, "SeLogger/getArgumentsTest");
        String methodsFile = seLogger.getMethodFile();

        int i = 0;
        CSVReader reader = new CSVReader(new FileReader(methodsFile));
        for (String[] csv : reader) {
            //make signature with class name
            String arguments = seLogger.getArguments(csv[SeLoggerReader.HEADER_METHODS_METHOD_ARG_RETURN]);
            Assert.assertEquals(answers[i], arguments);
            i++;
        }

    }


//    //Check no Error
//    @Test
//    public void test24f3a020a4f3ad00bc07b4b6784841d66a4eed64() throws FinishException, NoParentsException, JavaVersionTooOldException, NoSureFireException, TestUnknownFailureException, JUnitVersionUnsupportedException, DependencyProblemException, BuildExecutionError, IOException, ProductionProblemException, JUnitNotFoundException, CrushHappensException, NoTargetBuildFileException, JavaVersionNotFoundException, InappropriateEnvironmentException {
//        String commitId = "24f3a020a4f3ad00bc07b4b6784841d66a4eed64";
//        String project = "TestEffortEstimationTutorial";
//        Registry registry = new CrossRegistry(project, commitId);
//        RefactoringEffortAnalyzerCrossStub analyzer = new RefactoringEffortAnalyzerCrossStub(registry);
//        AbstractJunitTestResult result = analyzer.analyze2();
//        Assert.assertEquals(0, result.passLinesMap.size());
//    }

    @Test
    public void N003()  {//39737d42f581d8e69af4b1b023c5ea39780ea90a
        String homeDir = System.getProperty("user.dir")+"/";
        SeLoggerReaderStub seLogger = new SeLoggerReaderStub(homeDir, "SeLogger/N003");
        List<String> list = Arrays.asList(
                "src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.special_N001#",
                "src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.testPower_N001#",
                "src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.testPower_N002#",
                "src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.all#"
        );
        Map<Integer, String> ans4testPower_N002 = new TreeMap<>();
        ans4testPower_N002.put(19, "[src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.<init>#@9, src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.<init>#@10]");
        ans4testPower_N002.put(20, "[src/main/java/functions/distributions/Calculator2.java;Calculator2.<init>#@7, src/main/java/functions/Calculator.java;Calculator.<init>#@9, src/main/java/functions/Calculator.java;Calculator.<init>#@10, src/main/java/functions/Calculator.java;Calculator.<init>#@11, src/main/java/functions/Calculator.java;Calculator.<init>#@12]");
        ans4testPower_N002.put(21, "[src/main/java/functions/Calculator.java;Calculator.plus#Double@17, src/main/java/functions/Calculator.java;Calculator.plus#Double@18, src/main/java/functions/Calculator.java;Calculator.isNull#Double,Double@75, src/main/java/functions/Calculator.java;Calculator.isNull#Double,Double@76, src/main/java/functions/Calculator.java;Calculator.isNull#Double,Double@78, src/main/java/functions/Calculator.java;Calculator.plus#Double@19, src/main/java/functions/Calculator.java;Calculator.plus#Double@21, src/main/java/functions/Calculator.java;Calculator.plus#Double@22]");
        ans4testPower_N002.put(22, "[src/main/java/functions/distributions/Calculator2.java;Calculator2.power#Double@9, src/main/java/functions/distributions/Calculator2.java;Calculator2.power#Double@10, src/main/java/functions/distributions/Calculator2.java;Calculator2.power#Double@11]");
        ans4testPower_N002.put(23, "[src/main/java/functions/distributions/Calculator2.java;Calculator2.getAnswer#@15, src/main/java/functions/Calculator.java;Calculator.getAnswer#@69]");
        ans4testPower_N002.put(24, "[src/main/java/functions/Calculator.java;Calculator.aho#@72, src/main/java/functions/Calculator.java;Calculator.aho#@73]");

        Map<Integer, String> ans4special_N001 = new TreeMap<>();
        ans4special_N001.put(27, "[src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.<init>#@9, src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.<init>#@10]");
        ans4special_N001.put(28, "[src/main/java/functions/special/B.java;B.echo#@5, src/main/java/functions/special/B.java;B.echo#@6]");

        Map<String, Map<Integer, List<PassedLine>>> map = seLogger.getPassLinesMap(null, list);

        Map<Integer, List<PassedLine>> a1 = map.get("src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.special_N001#");
        for (Integer i: a1.keySet()) System.out.println(i +":"+a1.get(i));
        Assert.assertEquals(ans4special_N001.size(), a1.size());
        for(Integer key: a1.keySet()){
            System.out.println(key);
            String act = a1.get(key).toString();
            String ans = ans4special_N001.get(key);
            Assert.assertEquals(ans, act);
        }
        Map<Integer, List<PassedLine>> a2 = map.get("src/test/java/functions/distributions/Calculator2Test.java;Calculator2Test.testPower_N002#");
        Assert.assertEquals(ans4testPower_N002.size(), a2.size());
        for(Integer key: a2.keySet()){
            System.out.println(key);
            String act = a2.get(key).toString();
            String ans = ans4testPower_N002.get(key);
            Assert.assertEquals(ans, act);
        }


    }

    @Test
    public void SignatureNullTest()  {
        String homeDir = System.getProperty("user.dir")+"/";

        SeLoggerReaderStub seLogger = new SeLoggerReaderStub(homeDir, "SeLogger/SignatureNullMap");
        List<String> list = List.of(
                "src/test/java/org/apache/commons/lang3/EnumUtilsTest.java;EnumUtilsTest.test_generateBitVectors_nullClass#"
        );

        Map<Integer, String> ans4testPower_SigNullMap = new TreeMap<>();
        ans4testPower_SigNullMap.put(103, "[src/test/java/org/apache/commons/lang3/EnumUtilsTest.java;EnumUtilsTest.<init>#@38]");
        ans4testPower_SigNullMap.put(104, "[src/test/java/org/apache/commons/lang3/Traffic.java;Traffic.<init>#@370, src/test/java/org/apache/commons/lang3/Traffic.java;Traffic.<init>#String,int@369, src/test/java/org/apache/commons/lang3/Traffic.java;Traffic.<init>#String,int@369, src/test/java/org/apache/commons/lang3/Traffic.java;Traffic.<init>#String,int@369, src/test/java/org/apache/commons/lang3/Traffic.java;Traffic.<init>#@369, src/test/java/org/apache/commons/lang3/Traffic.java;Traffic.values#@369, src/main/java/org/apache/commons/lang3/EnumUtils.java;EnumUtils.generateBitVectors#Class,Iterable@170, src/main/java/org/apache/commons/lang3/EnumUtils.java;EnumUtils.asEnum#Class@307, src/main/java/org/apache/commons/lang3/Validate.java;Validate.notNull#Object,String,Object]@221, src/main/java/org/apache/commons/lang3/Validate.java;Validate.notNull#Object,String,Object]@222]");
        Map<String, Map<Integer, List<PassedLine>>> map = seLogger.getPassLinesMap(null, list);
        for(Map<Integer, List<PassedLine>> a: map.values()){
            System.out.println(a);
        }
        Map<Integer, List<PassedLine>> a1 = map.get("src/test/java/org/apache/commons/lang3/EnumUtilsTest.java;EnumUtilsTest.test_generateBitVectors_nullClass#");
        for(Integer key: a1.keySet()){
            String act = a1.get(key).toString();
            String ans = ans4testPower_SigNullMap.get(key);
            Assert.assertEquals(ans, act);
        }
        //FIXME: Do we need this?
//        map.forEach((k, v) -> {
//            Assert.assertNotNull(k);
//        });


    }

    @Test
    public void twoByteDeletion() {

        String reg = "\\W";
        String en = "hello".replaceAll(reg, "");
        Assert.assertEquals ("hello", en);

        String ja = "こんにちは".replaceAll(reg, "");
        Assert.assertEquals ("", ja);

        String ch = "你好".replaceAll(reg, "");
        Assert.assertEquals ("", ch);

        String al = "سپین".replaceAll(reg, "");
        Assert.assertEquals ("", al);

        String other = "႗၇𝟐?𑇕?５৮௫7꤆𝟖?".replaceAll(reg, "");
        Assert.assertEquals ("7", other);

    }

    @Test
    public void doubleDimension() throws IOException {
        String homeDir = System.getProperty("user.dir")+"/";
        SeLoggerReaderStub seLogger = new SeLoggerReaderStub(homeDir,"SeLogger/doubleDimension");
        String methodsFile = seLogger.getMethodFile();
        String classesFile = seLogger.getClassFile();

        Map<String, String> methods = seLogger.getMethods(classesFile, methodsFile);
        System.out.println(methods);
        Assert.assertEquals("src/main/java/functions/Calculator.java;Calculator.aaa1#int]]", methods.get("10"));
        Assert.assertEquals("src/main/java/functions/Calculator.java;Calculator.aaa12#Double]]", methods.get("22"));

    }

    @Test
    public void N002() throws IOException {
        String homeDir = System.getProperty("user.dir")+"/";
        SeLoggerReaderStub seLogger = new SeLoggerReaderStub(homeDir,"SeLogger/N002");
        String methodsFile = seLogger.getMethodFile();
        String classesFile = seLogger.getClassFile();

        Map<String, String> methods = seLogger.getMethods(classesFile, methodsFile);
        System.out.println(methods);
        Assert.assertEquals(
                "src/main/java/org/apache/commons/text/RandomStringGenerator.java;RandomStringGenerator.<init>#int,int,Set,TextRandomProvider,List",
                methods.get("1894"));

    }



    @Test
    public void checkSeloggerContainsSignature() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "0dbb25e7d119f6bc10b601a2df2dde5e18adb4ab";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for (final TestInfo a : specifier.result.getTestResults()) {
            if(MyProgramUtils.isConstructor(a.getSignature())) continue;
            TestMethodDefinition tmd = (TestMethodDefinition) specifier.structure.getMethod(a.getSignature());
            Assert.assertNotNull(tmd);
        }
    }

    public static List<String> exeptions = Arrays.asList("values#");//ToString?
    @Test
    public void checkPassLinesMapContainsSignature() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "0dbb25e7d119f6bc10b601a2df2dde5e18adb4ab";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        System.out.println(specifier.structure.getAllSignatures());
        System.out.println("------");
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            System.out.println(specifier.result.passLinesMap);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.signature);
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    boolean exempt = false;
                    for (String e: exeptions){
                        if(s2.signature.endsWith(e)){
                            exempt=true;
                            break;
                        };
                    }
                    if (!exempt){
                        Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                    }
                }
            }
        }
    }

    @Test
    public void testIfLines() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "793ce6e21ad234e007a2d27c330dcb13efa35090";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        System.out.println(specifier.structure.getTestSignature());
        for (String s : specifier.structure.getTestSignature()) {
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if (eachLinesPass == null) continue;//This is due to abstract method
            for (Integer i : eachLinesPass.keySet()) {
                System.out.println(" " + i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for (PassedLine s2 : passedLines) {
                    System.out.println("  " + s2.toString());
                    if (MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
            break;
        }
    }
}