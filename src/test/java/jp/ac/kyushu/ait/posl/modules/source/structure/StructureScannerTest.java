package jp.ac.kyushu.ait.posl.modules.source.structure;

import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.modules.refactoring.detect.RefactoringMinerController;
import jp.ac.kyushu.ait.posl.stub.SeLoggerStub;
import jp.ac.kyushu.ait.posl.utils.exception.DependencyProblemException;
import jp.ac.kyushu.ait.posl.utils.exception.NoParentsException;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import jp.ac.kyushu.ait.posl.utils.source.ChangeFlagger;
import jp.ac.kyushu.ait.posl.utils.source.RefactoringFlagger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;

import java.beans.Transient;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class StructureScannerTest {


    @Test
    public void checkGenerics() throws NoParentsException {
        String[] args = {"TestEffortEstimationTutorial"};
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        gitX.checkout("dc139e40f994b297d18227d21d9cd164751f1deb");
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner analyzerX = new StructureScanner(mavenX);
        Structure structure = analyzerX.scan();
        Assert.assertTrue(structure.hasMethod("src/main/java/functions/Calculator.java;Calculator.tmp#Object"));
    }

    @Test
    public void checkSignatures() throws NoParentsException {
        String[] args = {"TestEffortEstimationTutorial"};
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        gitX.checkout("cceeb8d5fec9bfc3b1014ccb5cb8ed962e3e572c");
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner analyzerX = new StructureScanner(mavenX);
        Structure structure = analyzerX.scan();
        Assert.assertEquals(7, structure.getAllFiles().size());
        Assert.assertTrue(structure.hasFile("src/test/java/functions/CalculatorTest.java"));
        Assert.assertTrue(structure.hasFile("src/test/java/functions/distributions/Calculator2Test.java"));
        Assert.assertTrue(structure.hasFile("src/main/java/OpenCloverController.java"));
        Assert.assertTrue(structure.hasFile("src/main/java/functions/special/B.java"));
        Assert.assertTrue(structure.hasFile("src/main/java/functions/Calculator.java"));
        Assert.assertTrue(structure.hasFile("src/main/java/functions/distributions/Calculator2.java"));
        Assert.assertTrue(structure.hasFile("src/main/java/CloverRead.java"));
    }

    @Test
    public void checkMethods() throws NoParentsException {
        String[] args = {"TestEffortEstimationTutorial"};
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        gitX.checkout("cceeb8d5fec9bfc3b1014ccb5cb8ed962e3e572c");
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner analyzerX = new StructureScanner(mavenX);
        Structure structure = analyzerX.scan();

        Assert.assertEquals(23, structure.getSignatures("src/main/java/functions/Calculator.java").size());
        Assert.assertEquals(6, structure.getSignatures("src/test/java/functions/CalculatorTest.java").size());
        Assert.assertEquals(4, structure.getSignatures("src/test/java/functions/distributions/Calculator2Test.java").size());
        Assert.assertEquals(4, structure.getSignatures("src/main/java/OpenCloverController.java").size());
        Assert.assertEquals(1, structure.getSignatures("src/main/java/functions/special/B.java").size());
        Assert.assertEquals(3, structure.getSignatures("src/main/java/functions/distributions/Calculator2.java").size());
        Assert.assertEquals(1, structure.getSignatures("src/main/java/CloverRead.java").size());
    }

    @Test
    public void checkSubclass() throws NoParentsException {
        String[] args = {"TestEffortEstimationTutorial"};
        Map<String, List<String>> answers = new HashMap<>();
        answers.put("src/main/java/OpenCloverController.java",
                Arrays.asList(
                        "src/main/java/OpenCloverController.java;OpenCloverController.<init>#",
                        "src/main/java/OpenCloverController.java;OpenCloverController.getPath#String",
                        "src/main/java/OpenCloverController.java;OpenCloverController.getTestCaseInfo#String",
                        "src/main/java/OpenCloverController.java;OpenCloverController.setMap#"));
        answers.put("src/main/java/CloverRead.java",
                Collections.singletonList(
                        "src/main/java/CloverRead.java;CloverRead.main#String]"));
        answers.put("src/main/java/functions/Calculator.java",
                Arrays.asList(
                        "src/main/java/functions/Calculator.java;Calculator.<init>#",
                        "src/main/java/functions/Calculator.java;Calculator.plus#Double",
                        "src/main/java/functions/Calculator.java;Calculator.minus#Double",
                        "src/main/java/functions/Calculator.java;Calculator.times#Double",
                        "src/main/java/functions/Calculator.java;Calculator.divided#Double",
                        "src/main/java/functions/Calculator.java;Calculator.aaa1#int",
                        "src/main/java/functions/Calculator.java;Calculator.aaa2#double",
                        "src/main/java/functions/Calculator.java;Calculator.aaa3#float",
                        "src/main/java/functions/Calculator.java;Calculator.aaa5#short",
                        "src/main/java/functions/Calculator.java;Calculator.aaa6#long",
                        "src/main/java/functions/Calculator.java;Calculator.aaa4#boolean",
                        "src/main/java/functions/Calculator.java;Calculator.aaa7#byte",
                        "src/main/java/functions/Calculator.java;Calculator.aaa8#char",
                        "src/main/java/functions/Calculator.java;Calculator.aaa9#byte]",
                        "src/main/java/functions/Calculator.java;Calculator.aaa10#Integer]",
                        "src/main/java/functions/Calculator.java;Calculator.aaa11#Integer]",
                        "src/main/java/functions/Calculator.java;Calculator.aaa12#Object",
                        "src/main/java/functions/Calculator.java;Calculator.getAnswer#",
                        "src/main/java/functions/Calculator.java;Calculator.aho#",
                        "src/main/java/functions/Calculator.java;Calculator.isNull#Double,Double",
                        "src/main/java/functions/Calculator.java;Calculator.isStaticNull#Double,Double",
                        "src/main/java/functions/Calculator.java;Calculator.aaa#Double,Double,Double,Double",
                        "src/main/java/functions/Calculator.java;Calculator.main#String]",
                        "src/main/java/functions/Calculator.java;Calculator.SubClass.SayHello#"
                ));


        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        gitX.checkout("8c64ccc68f96a7abffe5de057c10f9e6554d0b73");
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner analyzerX = new StructureScanner(mavenX);
        Structure structure = analyzerX.scan();
        for (MethodDefinition md: structure.getAllMethods()){
            System.out.println(md.methodInfo.signature);
        }
        System.out.println("----");
        for(String key: answers.keySet()){
            List<String> ans = answers.get(key);
            List<String> act = structure.getSignatures(key);
            assertEquals(ans.size(), act.size());
            System.out.println(act);
            for(String a: ans){
                System.out.println(a);
                Assert.assertTrue(act.contains(a));
            }
        }

    }
    @Test
    public void checkMethods2() throws NoParentsException {
        String[] args = {"TestEffortEstimationTutorial"};
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        gitX.checkout("24f3a020a4f3ad00bc07b4b6784841d66a4eed64");
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner analyzerX = new StructureScanner(mavenX);
        analyzerX.scan();

    }
    @Test
    public void checkAnotherSignature() throws NoParentsException {
        String[] args = {"TestEffortEstimationTutorial"};
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        gitX.checkout("46404c26c70d2615f7f6dc27ccfe5273d3464491");
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner sa = new StructureScanner(mavenX);
        Structure structure = sa.scan();
        for(String sig: structure.getAllSignatures()){
            System.out.println("-----------------");
            MethodDefinition md = structure.getMethod(sig);
            System.out.println(md.methodInfo.signature);
//            System.out.println(md.anotherSignature);
        }
    }
    @Ignore
    @Test
    public void testtt() throws NoParentsException {
        String[] args = {"javapoet"};
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/test/");
        gitX.checkout("b1e1a088321da708d3299138fc55c0a9976a6291", true, true);
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner analyzerX = new StructureScanner(mavenX);
        Structure structure = analyzerX.scan();
        for(MethodDefinition md: structure.getAllMethods()){
            System.out.println(md.methodInfo.signature+":"+md.methodInfo.starts+"-"+md.methodInfo.ends);
        }
    }

    @Transient
    public void testChangeParameter() throws NoParentsException {
        String[] args = {"TestEffortEstimationTutorial"};
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        gitX.checkout("8fccdf79ce37cc42a738127f259c80d1cfe65542");
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner analyzerX = new StructureScanner(mavenX);
        Structure structure = analyzerX.scan();
        for(MethodDefinition md: structure.getAllMethods()){
            System.out.println(md.methodInfo.signature);
            for(Set<String> a: md.usedVariablesWithoutLocals.values()){
                if(a.size()>0){
                    System.out.println("  "+a);
                }
            }
        }
        System.out.println("--------");
        System.out.println(structure.fieldsPosition.values());

    }

    @Test @Ignore
    public void testSetField_N001() throws NoParentsException {
        String project = "TestEffortEstimationTutorial";
        String commitId = "57846b464acc55b9a95dcb5e29ead2f0e992edb4";
        String signature = "src/main/java/function/Calculator.java;Calculator.add#Double";

        Structure analyzerX = getStructureAnalyzer(project, commitId);
        MethodDefinition md = analyzerX.getMethod(signature);
        System.out.println(md.methodInfo.signature);
        Set<String> usedVals = md.usedVariablesWithoutLocals.get(19);
        assertEquals(1, usedVals.size()); // actual 0
        System.out.println(usedVals);
        Assert.assertTrue(usedVals.contains("function.Calculator.answer"));
    }

    @Test @Ignore
    public void testSetField_N002() throws NoParentsException {
        String project = "TestEffortEstimationTutorial";
        String commitId = "89196baddfe20532222e86c9b7875f9440d3416c";
        String signature = "src/main/java/function/Calculator.java;Calculator.plus#Double";

        Structure analyzerX = getStructureAnalyzer(project, commitId);
        MethodDefinition md = analyzerX.getMethod(signature);
        System.out.println(md.methodInfo.signature);
        Set<String> usedVals = md.usedVariablesWithoutLocals.get(11);
        assertEquals(1, usedVals.size()); // actual 0
        System.out.println(usedVals);
        Assert.assertTrue(usedVals.contains("function.Base.intval"));
    }


    @Ignore
    @Test
    public void tess() throws NoParentsException {
        String project = "javapoet";
        String commitId = "c5b6b36b2e98b59f0711c1bfc8486c32eb3b482a";

        SettingManager sm = new SettingManager(new String[]{project});
        GitController gitX = new GitController(sm, "/x/");
        gitX.checkout(commitId);
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner analyzerX = new StructureScanner(mavenX);
        Structure structure = analyzerX.scan();
        for(MethodDefinition md: structure.getAllMethods()){
            System.out.println(md.methodInfo.signature);
            for(Set<String> a: md.usedVariablesWithoutLocals.values()){
                if(a.size()>0){
                    System.out.println("  "+a);
                }
            }
        }
        System.out.println("--------");
        System.out.println(structure.fieldsPosition.values());


    }
    @Ignore
    @Test
    public void checkGenerics2() throws NoParentsException {
        String project = "joda-beans";
        String commitId = "1eec635172a5c053ff34e4f7c95c0c05ef93fcc2";
        SettingManager sm = new SettingManager(new String[]{project});
        GitController gitX = new GitController(sm, "/x/");
        gitX.checkout(commitId);
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner analyzerX = new StructureScanner(mavenX);
        Structure structure = analyzerX.scan();
        List<String> sigs = structure.getSignatures("src/main/java/org/joda/beans/impl/direct/DirectBeanBuilder.java");
        MethodDefinition md = structure.getMethod(sigs.get(0));
        System.out.println(md.methodInfo.generics);
    }
    @Test
    public void testSetChangeX_1_N001() throws NoParentsException {
        String project = "TestEffortEstimationTutorial";
        String commitId = "57846b464acc55b9a95dcb5e29ead2f0e992edb4";
        String signature = "src/test/java/function/CalculatorTest.java;CalculatorTest.testAdd_N001#";

        Structure analyzerX_1 = getStructureAnalyzer(project, commitId);
        MethodDefinition md = analyzerX_1.getMethod(signature);
        Assert.assertNotNull(md);
        Assert.assertFalse(md.methodInfo.changedLines.get(15));
        Assert.assertTrue(md.methodInfo.changedLines.get(16));
        Assert.assertFalse(md.methodInfo.changedLines.get(17));


    }
    @Test
    public void testMethods1() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "53d2c478232a414a89b9642de81b718d597c44fb";
        SeLoggerStub stub =  new SeLoggerStub(project, commitId, false);
        assertEquals(4, stub.structure.getTestSignature().size());
        assertEquals(6, stub.structure.getProductionSignature().size());

    }
    @Test
    public void testMethods2() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "cceeb8d5fec9bfc3b1014ccb5cb8ed962e3e572c";
        SeLoggerStub stub =  new SeLoggerStub(project, commitId, false);
        assertEquals(10, stub.structure.getTestSignature().size());
        assertEquals(32, stub.structure.getProductionSignature().size());

    }

    @Test
    public void testModuleMode001() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "1b353d7d02d7759b4324e989071a1f8943ccbd44";
        SeLoggerStub stub =  new SeLoggerStub(project, commitId, false);
        assertEquals(12, stub.structure.getAllSignatures().size());
        assertEquals(3, stub.structure.getTestSignature().size());
        assertEquals(9, stub.structure.getProductionSignature().size());
    }

    public int MethodsOfModule(String module, List<String> path_list){
        return path_list.stream().filter(y->y.contains(module)).collect(Collectors.toList()).size();
    }

    /**
     * check the number of test methods in commons-math
     * @throws Exception
     */
    @Test @Ignore
    public void testCommonsMathScan001() throws Exception {
        String project = "commons-math";
        String commitId = "142dcaa92109648d69b06694d80b377ab7fd8424";
        SeLoggerStub stub =  new SeLoggerStub(project, commitId, false);
        assertThat(stub.structure.getProductionSignature().size(), is(5811));//5813
        assertThat(stub.structure.getALLTestMethods().size(), is(3073));
        assertThat(stub.structure.getAllMethods().size(), is(11226));//11191
        List<MethodDefinition> list = stub.structure.getALLTestMethods().stream().collect(Collectors.toList());
        List<String> path_list = list.stream().map(MethodDefinition::getFileName).collect(Collectors.toList());
        Set<String> module_set = list.stream().map(MethodDefinition::getModuleName).collect(Collectors.toSet());
        assertThat(MethodsOfModule("commons-math-core/", path_list), is(1));
        assertThat(MethodsOfModule("commons-math-legacy-exception/", path_list), is(25));
        assertThat(MethodsOfModule("commons-math-neuralnet/", path_list), is(69));
        assertThat(MethodsOfModule("commons-math-legacy-core/", path_list), is(225));
        assertThat(MethodsOfModule("commons-math-transform/", path_list), is(31));

    }
    @Test(expected = DependencyProblemException.class)//SNAPSHOT問題で解決できない
    public void testModuleMode002() throws Exception {
        String project = "commons-math";
        String commitId = "839f8b45a3c7ce6e61e5864513eb74c1f36ea669";
        SeLoggerStub stub =  new SeLoggerStub(project, commitId, true);
    }

    @Test
    public void testModuleMode003() throws Exception {
        String project = "commons-lang";
        String commitId = "f20df89b6f8afbdf175ced0270593b826d228f27";
        SeLoggerStub stub =  new SeLoggerStub(project, commitId, false);
        assertThat(stub.structure.srcDir, is("src/java"));
        assertThat(stub.structure.testDir, is("src/test"));
    }
    public Structure getStructureAnalyzer(String project, String commitId) throws NoParentsException {
        SettingManager sm = new SettingManager(new String[]{project});
        GitController gitX = new GitController(sm, "/test/");
        gitX.checkout(commitId);
        List<Refactoring> refactorings = RefactoringMinerController.getRefactoringInstanceAtCommit(gitX.getRepo(), commitId);
        Commit commit = gitX.getCommit();
        BuildToolSettingController mavenX = new MavenSettingController(gitX);
        StructureScanner sc = new StructureScanner(mavenX);
        Structure structure = sc.scan();
        ChangeFlagger.flagChange(commit, structure, false);
        RefactoringFlagger.setRefactoring(refactorings, structure, false);

        return structure;
    }
}
