package jp.ac.kyushu.ait.posl.modules.build.compile;

import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.modules.test.JunitTestResultManager;
import jp.ac.kyushu.ait.posl.beans.test.TestInfo;
import jp.ac.kyushu.ait.posl.utils.Stubs;
import jp.ac.kyushu.ait.posl.utils.exception.DependencyProblemException;
import jp.ac.kyushu.ait.posl.utils.exception.JUnitNotFoundException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Ignore
public class TestRunTest {

    @Ignore//too much time
    @Test
    public void commons_text_N001() throws Exception {
        String project = "commons-text";
        String commitId = "cc9dc64ac8d7b3dc629557187d0a9fcec44f03ee";
        Stubs.runTests(project, commitId, false);
    }

    @Ignore//too much time
    @Test(expected = DependencyProblemException.class)
    public void javapoet_N011() throws Exception {
        String project = "javapoet";
        String commitId = "2fc51db0fcee90f3c9f342ff1caf6ff073a39ad9";
        Stubs.runTests(project, commitId, false);

    }

    @Ignore//too much time
    @Test(expected = DependencyProblemException.class)
    public void javapoet_N012() throws Exception {
        String project = "javapoet";
        String commitId = "0a6ee12960724854a0da33b171bcb06c1121c3e5";
        Stubs.runTests(project, commitId, false);
    }

    /**
     * Dependency Error pattern
     * @throws Exception
     */
    @Ignore//too much time
    @Test(expected = DependencyProblemException.class)
    public void javapoet_E021() throws Exception {
        String project = "javapoet";
        String commitId = "2ddbb90789ae9c8eea9d8264b964748c76703a81";
        Stubs.runTests(project, commitId, false);
    }

    @Ignore//too much time
    @Test
    public void javapoet_N013() throws Exception {
        String project = "javapoet";
        String commitId = "aed50f772034c1235482ecc98fb2c9be794a0449";
        Stubs.runTests(project, commitId, true);
    }
    @Ignore//too much time
    @Test
    public void jsoup_N001() throws Exception {
        String project = "jsoup";
        String commitId = "9297a22afcf1396c54539e0f225d048f794783ab";
        Stubs.runTests(project, commitId, false);
    }
    @Ignore//too much time
    @Test
    public void jsoup_N002() throws Exception {
        String project = "jsoup";
        String commitId = "ebd2a773d67e8d1d042607fd2458f92e18b9203b";
        Stubs.runTests(project, commitId, false);
    }
    @Ignore//too much time
    @Test
    public void jsoup_N003() throws Exception {
        String project = "jsoup";
        String commitId = "105f7bdcdbfffc879d0737cf857c02b6823b1b73";
        Stubs.runTests(project, commitId, true);
    }

    @Test//too much time
    public void tutorial_N001() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "53d2c478232a414a89b9642de81b718d597c44fb";
        Map<String, String> answers = new HashMap<>();
        answers.put("src/test/java/CalculatorTest.java;CalculatorTest.testDivided_N001#", "PASS");
        answers.put("src/test/java/CalculatorTest.java;CalculatorTest.testMinos_N001#", "PASS");
        answers.put("src/test/java/CalculatorTest.java;CalculatorTest.testTimes_N001#", "PASS");
        answers.put("src/test/java/CalculatorTest.java;CalculatorTest.testAdd_N001#", "PASS");
        JunitTestResultManager testResult = Stubs.runTests(project, commitId, false);
        assertEquals(4, testResult.getTestResults().size());
        for(TestInfo t: testResult.getTestResults()){
            System.out.println(t.signature);
            String res = answers.get(t.signature);
            assertEquals(res, t.testResult.type.name());
        }
        System.out.println(testResult.passLinesMap);
        //one time check
        Map<Integer, List<PassedLine>> passes = testResult.passLinesMap.get("src/test/java/CalculatorTest.java;CalculatorTest.testAdd_N001#");
        List<PassedLine> list = passes.get(10);
        assertEquals("src/main/java/Calculator.java;Calculator.plus#int", list.get(0).signature);
        assertEquals(7, (int)list.get(0).lineNo);
        assertEquals("src/main/java/Calculator.java;Calculator.plus#int", list.get(1).signature);
        assertEquals(8, (int)list.get(1).lineNo);

    }
    @Test
    public void tutorial_N002() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "53d2c478232a414a89b9642de81b718d597c44fb";
        Map<String, String> answers = new HashMap<>();
        answers.put("src/test/java/CalculatorTest.java;CalculatorTest.testDivided_N001#", "PASS");
        answers.put("src/test/java/CalculatorTest.java;CalculatorTest.testMinos_N001#", "PASS");
        answers.put("src/test/java/CalculatorTest.java;CalculatorTest.testTimes_N001#", "PASS");
        answers.put("src/test/java/CalculatorTest.java;CalculatorTest.testAdd_N001#", "PASS");

        JunitTestResultManager testResult = Stubs.runTests(project, commitId, false);
        assertEquals(4, testResult.getTestResults().size());
        for(TestInfo t: testResult.getTestResults()){
            System.out.println(t.signature);
            String res = answers.get(t.signature);
            assertEquals(res, t.testResult.type.name());
        }

        //one-time check
        Map<Integer, List<PassedLine>> passes = testResult.passLinesMap.get("src/test/java/CalculatorTest.java;CalculatorTest.testAdd_N001#");
        List<PassedLine> list = passes.get(10);
        assertEquals("src/main/java/Calculator.java;Calculator.plus#int", list.get(0).signature);
        assertEquals(7, (int)list.get(0).lineNo);
        assertEquals("src/main/java/Calculator.java;Calculator.plus#int", list.get(1).signature);
        assertEquals(8, (int)list.get(1).lineNo);

    }

    @Ignore//too much time
    @Test
    public void guice_N001() throws Exception {
        String project = "guice";
        String commitId = "338d0039c1e30038f22f0d5544842c1e87406a8a";
        Stubs.runTests(project, commitId, false);
    }
    @Ignore//too much time
    @Test
    public void testGetSignatureFromFileName001() throws Exception {
        String project = "guice";
        String commitId = "338d0039c1e30038f22f0d5544842c1e87406a8a";

        Stubs.runTests(project, commitId, false);


    }

    @Test
    public void testCross() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "558c0adeae913dd70274aff5bd429e3afec39803";

        Map<String, String> answers = new HashMap<>();
        answers.put("src/test/java/functions/CalculatorTest.java;CalculatorTest.testCalc3_N001#", "COMPILE_ERROR");
        answers.put("src/test/java/functions/CalculatorTest.java;CalculatorTest.testCalc2_N001#", "PASS");
        answers.put("src/test/java/functions/CalculatorTest.java;CalculatorTest.testaho_N002#", "PASS");
        answers.put("src/test/java/functions/CalculatorTest.java;CalculatorTest.testAdd_N001#", "PASS");

        JunitTestResultManager testResult =  Stubs.runTests(project, commitId, true);
        System.out.println(testResult.getTestResults());
        assertEquals(4, testResult.getTestResults().size());
        for(TestInfo t: testResult.getTestResults()){
            System.out.println(t.signature);
            String res = answers.get(t.signature);
            assertEquals(res, t.testResult.type.name());
        }

    }

    @Test
    public void testInitializerErrorHandling() throws Exception{
        String project = "TestEffortEstimationTutorial";
        String commitId = "4593d8031e377d259bd22c8cdf6688b953eb72a1";
        JunitTestResultManager testResult  = Stubs.runTests(project, commitId, true);
        for(TestInfo t: testResult.getTestResults()){
            if (t.getSignature().contains("CalculatorTest")){
                assertEquals("COMPILE_ERROR", t.testResult.type.name());
            }else{
                assertEquals("PASS", t.testResult.type.name());

            }
        }
    }
    @Test
    public void testModuleMode001() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "1b353d7d02d7759b4324e989071a1f8943ccbd44";

        Map<String, String> answers = new HashMap<>();
        answers.put("Calc/src/test/java/work/TestSalaryCalculation.java;TestSalaryCalculation.testCalc001#", "PASS");
        answers.put("Core/src/test/java/function/CalculatorTest.java;CalculatorTest.testDivided_N001#", "PASS");
        answers.put("Core/src/test/java/function/CalculatorTest.java;CalculatorTest.testDivided_N002#", "PASS");

        JunitTestResultManager testResult =  Stubs.runTests(project, commitId, false);
        System.out.println(testResult.getTestResults());
        assertEquals(answers.size(), testResult.getTestResults().size());
        for(TestInfo t: testResult.getTestResults()){
            System.out.println(t.signature);
            String res = answers.get(t.signature);
            assertEquals(res, t.testResult.type.name());
        }

    }
    @Test
    public void testModuleMode002() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "a89de9809af7e641e551dd794464755e35a5f18b";

        Map<String, String> answers = new HashMap<>();
        answers.put("Calc/src/test/java/work/TestSalaryCalculation.java;TestSalaryCalculation.testCalc001#", "PASS");
        answers.put("Core/src/test/java/function/CalculatorTest.java;CalculatorTest.testDivided_N001#", "COMPILE_ERROR");
        answers.put("Core/src/test/java/function/CalculatorTest.java;CalculatorTest.testDivided_N002#", "COMPILE_ERROR");

        JunitTestResultManager testResult = Stubs.runTests(project, commitId, true);
        assertEquals(answers.size(), testResult.getTestResults().size());
        for(TestInfo t: testResult.getTestResults()){
            System.out.println(t.signature);
            String res = answers.get(t.signature);
            assertEquals(res, t.testResult.type.name());
        }

    }
    @Test
    public void testModuleMode003() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "56bd66858d29e17834ee2ab50bdf9a8ae41e8495";

        Map<String, String> answers = new HashMap<>();
        answers.put("Calc/src/test/java/work/TestSalaryCalculation.java;TestSalaryCalculation.testCalc001#", "COMPILE_ERROR");
        answers.put("Core/src/test/java/function/CalculatorTest.java;CalculatorTest.testDivided_N001#", "COMPILE_ERROR");
        answers.put("Core/src/test/java/function/CalculatorTest.java;CalculatorTest.testDivided_N002#", "COMPILE_ERROR");

        JunitTestResultManager testResult  = Stubs.runTests(project, commitId, true);
        System.out.println(testResult.getTestResults());
        assertEquals(answers.size(), testResult.getTestResults().size());
        for(TestInfo t: testResult.getTestResults()){
            System.out.println(t.signature);
            String res = answers.get(t.signature);
            assertEquals(res, t.testResult.type.name());
        }

    }

    /**
     * Using pom.xml and pom-main.xml
     * @throws Exception
     */
    @Test
    public void testModuleMode004() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "d87e56a7325b93edf2982c2af9fb939f9e5249ab";

        Map<String, String> answers = new HashMap<>();
        answers.put("src/test/java/TestA.java;TestA.testA#", "PASS");

        JunitTestResultManager testResult  = Stubs.runTests(project, commitId, false);
        System.out.println(testResult.getTestResults());
        assertEquals(answers.size(), testResult.getTestResults().size());
        for(TestInfo t: testResult.getTestResults()){
            System.out.println(t.signature);
            String res = answers.get(t.signature);
            assertEquals(res, t.testResult.type.name());
        }
    }




    /**
     * jdk version declared in property
     * @throws Exception
     */
    @Ignore
    @Test(expected = JUnitNotFoundException.class)
    public void testErrorCase005() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "c5a1a23baa002848f805aee56ad5bf72000e37d0";
        JunitTestResultManager testResult =  Stubs.runTests(project, commitId, false);
        //PASS
    }

    /**
     * jdk5 but we want not to skip
     * @throws Exception
     */
    @Test//https://github.com/apache/commons-math/commit/4b6b25d558ace75c85e4b977805a066f1f3b7f42
    @Ignore//due to long exe time
    public void testErrorCase007() throws Exception {
        String project = "commons-lang";
        String commitId = "b9e3bc72d55b6eb73a5cc44f5cc2561d9997ef0d";
        JunitTestResultManager testResult =  Stubs.runTests(project, commitId, false);
        //PASS
    }
    @Test
    @Ignore//too long to run
    public void testErrorCase008() throws Exception {
        String project = "commons-lang";
        String commitId = "2244ed9d63e037a85e1ab2ab2bc9b98f12a833dd";
        JunitTestResultManager testResult = Stubs.runTests(project, commitId, false);
        for(TestInfo s: testResult.getTestResults()){
            System.out.println("------");
            System.out.println(s.getSignature());
            System.out.println(testResult.getResult(s.getSignature()));
        }
    }


    @Test
    public void bugfix_N001() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "4593d8031e377d259bd22c8cdf6688b953eb72a1";
        JunitTestResultManager result = Stubs.runTests(project, commitId, false);
        List<? extends TestInfo> tests = result.getTestResults();
        for(TestInfo ti: tests){
            System.out.println(ti.signature);
            System.out.println(ti.testResult);
        }
        assertEquals(1, tests.size());
        //becuase of JUnit specification, the failed test case become CalculatorTest instead of addN001
    }

//    @Test// https://github.com/google/closure-compiler/tree/0ca9454c424697ea9c34d197813d2382922388c2
//    public void testRunMultiplePomFiles() throws Exception {
//        String project = "closure-compiler";
//        String commitId = "0ca9454c424697ea9c34d197813d2382922388c2";
//        AbstractJunitTestResult result = Stubs.runTests(project, commitId, false);
//        List<? extends TestInfo> tests = result.getTestResults();
//        for(TestInfo ti: tests){
//            System.out.println(ti.signature);
//            System.out.println(ti.testResult);
//        }
//        assertEquals(1, tests.size());
//    }

    @Test
    public void testSpark() throws Exception {
        String commit = "1973e402f5d4c1442ad34a1d38ed0758079f7773";
        String project = "spark";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testRxjava() throws Exception {
        String commit = "59f1a8e42828adf1e33f163057f600aa752cd3ca";
        String project = "rxjava-jdbc";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testJoda() throws Exception {
        String commit = "b9a7581818a8f2c428f8e0b427091de2ec2ba038";
        String project = "joda-beans";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testCommonsIo() throws Exception {
        String commit = "22f6525588afe563a895d6c7a70e03ede86610d1";
        String project = "commons-io";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testUrbanAirshipJavaLibrary() throws Exception {
        String commit = "fc42b9aaa3e79825aaf4ffe4ac35efc806f874d6";
        String project = "UrbanAirshipJavaLibrary";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testJsoup() throws Exception {
        String commit = "907d09e28fe9f6df9d39c050d7d476879956574e";
        String project = "jsoup";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testCommonsText() throws Exception {
        String commit = "78fac0f157f74feb804140613e4ffec449070990";
        String project = "commons-text";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testSpoon() throws Exception {
        String commit = "a2665ab4772225de57c23443f053a7cca5eb5b97";
        String project = "spoon";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testCommonsLang() throws Exception {
        String commit = "e3a7399a7d82a5b3a33cc7653107821ce1709f67";
        String project = "commons-lang";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testLambdaj() throws Exception {
        String commit = "bd3afc7c084c3910454a793a872b0a76f92a43fd";
        String project = "lambdaj";
        Stubs.runTests(project, commit, false);
    }
    @Test
    public void testAsterisk() throws Exception {
        String commit = "e4de10deb218f96a2d5507384e9c1fc63328fc2e";
        String project = "Asterisk-Java";
        Stubs.runTests(project, commit, false);
    }

}
