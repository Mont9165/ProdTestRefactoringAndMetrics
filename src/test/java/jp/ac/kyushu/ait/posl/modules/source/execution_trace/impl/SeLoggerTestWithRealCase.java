package jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl;

import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.modules.test.JunitTestResultManager;
import jp.ac.kyushu.ait.posl.beans.test.TestInfo;
import jp.ac.kyushu.ait.posl.stub.SeLoggerStub;
import jp.ac.kyushu.ait.posl.utils.Stubs;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeLoggerTestWithRealCase {
    @Ignore
    @Test
    public void checkPassLines() throws Exception {
        String project = "joda-beans";
        String commitId = "e5234b9277a93e567fd9b0b8e04c1a5e81b44aba";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }

    @Ignore
    @Test
    public void checkSizeBehavior() throws Exception {
        String project = "joda-beans";
        String commitId = "787e5f2fb9b29cccb4542a3ee0e4db7b69d1ce32";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        Map<String,  Map<Integer, Integer>> passedLineSizewithSig50 = new HashMap<String, Map<Integer, Integer>>();
        // Map<String,  Map<Integer, Integer>> passedLineSizewithSig60 = new HashMap<String, Map<Integer, Integer>>();
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            Map<Integer, Integer> passedLineSize = new HashMap<Integer, Integer>();
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer line : eachLinesPass.keySet()){
                passedLineSize.put(line, eachLinesPass.get(line).size());
            }
            passedLineSizewithSig50.put(s, passedLineSize);
        }
        System.out.println(passedLineSizewithSig50);
    }
    @Ignore
    @Test
    public void checkSureFireCorrectN001() throws Exception {
        String project = "joda-beans";
        String commitId = "0df15f7f09e93a042a173f35ec51afa93ac6f7ca";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void checkSureFireCorrectN002() throws Exception {
        String project = "joda-beans";
        String commitId = "34a1b291ad260f456ef2a1e2eaaebc7d09484fd5";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void checkSureFireCorrect2() throws Exception {
        String project = "joda-beans";
        String commitId = "34a1b291ad260f456ef2a1e2eaaebc7d09484fd5";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }

    @Ignore
    @Test
    public void checkSureFireCorrect3() throws Exception {
        String project = "joda-beans";
        String commitId = "41be9f9dad2ef3bee1dd730da2c0077b9d6dc6cc";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }

    @Ignore
    @Test
    public void testSeLoggerInjsoupN001() throws Exception {
        String project = "jsoup";
        String commitId = "d65510c8ed0f10561372838b1c15b9d8af658d8b";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testSeLoggerInjsoupN002() throws Exception {
        String project = "jsoup";
        String commitId = "5f0714329e2763d330460efee8ccd7f69acc8e7c";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testSeLoggerInjsoupN003() throws Exception {
        String project = "jsoup";
        String commitId = "140b48a58568c9614ad91773598b56891bb70bac";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testSeLoggerInjunit4N004() throws Exception {
        String project = "junit4";
        String commitId = "467c3f8efe1a87e3029df282e4df60ad98bc4142";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testSeLoggerInguice4N001() throws Exception {
        String project = "guice";
        String commitId = "7d9991e6354f9a97c191c09e21a8e62f60ae9ce9";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testSeLoggerInguice4N002() throws Exception {
        String project = "guice";
        String commitId = "338d0039c1e30038f22f0d5544842c1e87406a8a";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testSeLoggerIncommonsioN001() throws Exception {
        String project = "commons-io";
        String commitId = "b803066005e1244932146aa904f05b420ca689a3";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testSeLoggerIncommonsioN002() throws Exception {
        String project = "commons-io";
        String commitId = "68a73b54d6fd08ea2951ea1911e035a2390119bc";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testSeLoggerIncommonsioN003() throws Exception {
        String project = "commons-io";
        String commitId = "01f92b184933bf8f333676f0b872cecb8e23466d";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testcommonsloggingIncommonsioN001() throws Exception {
        String project = "commons-logging";
        String commitId = "5063d2387588e98605dc6d1b9f5206499a09b592";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testcommonsloggingIncommonsioN002() throws Exception {
        String project = "commons-logging";
        String commitId = "ae02acf389b6475017147d991bfafcc446a3c8a9";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testInswaggercoreN001() throws Exception {
        String project = "swagger-core";
        String commitId = "e555f472fa523c40dd0f4addeffad85ece98c447";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testInpippoN001() throws Exception {
        String project = "pippo";
        String commitId = "6e5ce99a12ebd246e5c8330697f24f36aae0a4f0";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testIntruthN001() throws Exception {
        String project = "truth";
        String commitId = "db853e680c150ef9907ef936874d15a6fe29031e";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testIntraccarN001() throws Exception {
        String project = "traccar";
        String commitId = "5b3ee0a9666a9c7fe3b3c04d1fe637b7d7b935da";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testIntraccarN002() throws Exception {
        String project = "traccar";
        String commitId = "457c6a29077ebb143e40c29fdc37863b8d46357a";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testIntraccarN003() throws Exception {
        String project = "traccar";
        String commitId = "9cc1c29ec08cdc4369b010ac17b270bf3c3d7ead";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testIntraccarN004() throws Exception {
        String project = "traccar";
        String commitId = "b7d48127e60bcaa5d01f45d8df5203f28f9a1667";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testInSpoonN001() throws Exception {
        String project = "spoon";
        String commitId = "adb7890225e8915470606f88c5ce87f1869e0368";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testInRestheartN001() throws Exception {
        String project = "restheart";
        String commitId = "254dbd3fe8fd57876a1796bd29c1e83c7bdd30ef";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        System.out.println(specifier.structure.getTestSignature());
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }
    @Ignore
    @Test
    public void testInLog4jN001() throws Exception {
        String project = "log4j";
        String commitId = "c6b4fcb791c4d0f46974a1515f317858e6eeab55";

        SeLoggerStub specifier = new SeLoggerStub(project, commitId);
        System.out.println(specifier.structure.getTestSignature());
        for(String s: specifier.structure.getTestSignature()){
            System.out.println(s);
            Map<Integer, List<PassedLine>> eachLinesPass = specifier.result.passLinesMap.get(s);
            if(eachLinesPass==null) continue;//This is due to abstract method
            for(Integer i : eachLinesPass.keySet()){
                System.out.println(" "+i);
                List<PassedLine> passedLines = eachLinesPass.get(i);
                for(PassedLine s2 : passedLines){
                    System.out.println("  "+s2.toString());
                    if(MyProgramUtils.isConstructor(s2.signature)) continue;
                    Assert.assertTrue(specifier.structure.hasMethod(s2.signature));
                }
            }
        }
    }



    @Test
    @Ignore//TODO: need to be fixed after fixing a bug 162
    public void testClassInClass() throws Exception {
        String project = "commons-math";
        String commitId = "a9fdcd64bf0a982901d298596151d13e56442a11";
        JunitTestResultManager testResult =  Stubs.runTests(project, commitId, false);
        for(TestInfo t: testResult.getTestResults()){
            System.out.println(t);
        }
        TestInfo ti = testResult.getResult("org.apache.commons.math4.analysis.integration.gauss.BaseRuleFactoryTest.java;BaseRuleFactoryTest.testConcurrentCreation#");
        Assert.assertNotNull(ti);

    }
}
