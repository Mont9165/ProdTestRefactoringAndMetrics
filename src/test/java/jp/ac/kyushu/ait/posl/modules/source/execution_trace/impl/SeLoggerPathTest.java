package jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl;

import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.stub.SeLoggerStub;
import jp.ac.kyushu.ait.posl.stub.SpecifierStub;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SeLoggerPathTest {
    public static void verify(SpecifierStub stub, Map<String, List<String>> answers){
        Map<String, Map<Integer, List<PassedLine>>> passedLines = stub.result.passLinesMap;
        for(String testSignature: answers.keySet()){
            System.out.println(testSignature);
            List<String> ans = answers.get(testSignature);
            Map<Integer, List<PassedLine>> lines = passedLines.get(testSignature);
            int sum = 0;
            System.out.println(passedLines);
            for(Integer i: lines.keySet()){
                List<PassedLine> passed = lines.get(i);
                for(PassedLine p: passed){
                    System.out.println(p);
                    assertTrue(ans.contains(p.toString()));
                    sum++;
                }
            }
            assertEquals(ans.size(), sum);

        }

    }

    @Test
    public void testModulePath() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "1b353d7d02d7759b4324e989071a1f8943ccbd44";
        Map<String, List<String>> answers = new HashMap<>();
        answers.put("Calc/src/test/java/work/TestSalaryCalculation.java;TestSalaryCalculation.testCalc001#",
                Arrays.asList(
                        "Calc/src/test/java/work/TestSalaryCalculation.java;TestSalaryCalculation.<init>#@8",
                        "Calc/src/main/java/bean/Employee.java;Employee.<init>#String,int@8",
                        "Calc/src/main/java/bean/Employee.java;Employee.<init>#String,int@9",
                        "Calc/src/main/java/bean/Employee.java;Employee.<init>#String,int@10",
                        "Calc/src/main/java/bean/Employee.java;Employee.<init>#String,int@11",
                        "Calc/src/main/java/bean/Employee.java;Employee.<init>#String,int@12",
//                        "Calc/src/main/java/bean/Employee.java;recordWorking#int@13",//!!!
                        "Calc/src/main/java/bean/Employee.java;Employee.recordWorking#int@14",
                        "Calc/src/main/java/bean/Employee.java;Employee.recordWorking#int@16",
//                        "Calc/src/main/java/bean/Employee.java;recordWorking#int@13",//!!!
                        "Calc/src/main/java/bean/Employee.java;Employee.recordWorking#int@14",
                        "Calc/src/main/java/bean/Employee.java;Employee.recordWorking#int@16",
//                        "Calc/src/main/java/bean/Employee.java;recordWorking#int@13",
                        "Calc/src/main/java/bean/Employee.java;Employee.recordWorking#int@14",
                        "Calc/src/main/java/bean/Employee.java;Employee.recordWorking#int@16",
                        "Calc/src/main/java/work/SalaryCalculation.java;SalaryCalculation.<init>#@6",
//                        "Calc/src/main/java/work/SalaryCalculation.java;calc#Employee@7",//!!!
                        "Calc/src/main/java/work/SalaryCalculation.java;SalaryCalculation.calc#Employee@8",
                        "Core/src/main/java/function/Calculator.java;Calculator.<init>#@5",
                        "Core/src/main/java/function/Calculator.java;Calculator.<init>#@6",
                        "Core/src/main/java/function/Calculator.java;Calculator.<init>#@7",
                        "Calc/src/main/java/work/SalaryCalculation.java;SalaryCalculation.calc#Employee@9",
//                        "Core/src/main/java/function/Calculator.java;plus#int@8",//!!!
                        "Core/src/main/java/function/Calculator.java;Calculator.plus#int@9",
                        "Core/src/main/java/function/Calculator.java;Calculator.plus#int@10",
                        "Calc/src/main/java/work/SalaryCalculation.java;SalaryCalculation.calc#Employee@10",
//                        "Core/src/main/java/function/Calculator.java;times#int@14",//!!!
                        "Core/src/main/java/function/Calculator.java;Calculator.times#int@15",
                        "Core/src/main/java/function/Calculator.java;Calculator.times#int@16",
                        "Calc/src/main/java/work/SalaryCalculation.java;SalaryCalculation.calc#Employee@11",
//                        "Core/src/main/java/function/Calculator.java;getAnswer#@23",//!!!
                        "Core/src/main/java/function/Calculator.java;Calculator.getAnswer#@24"
                ));
        SeLoggerStub stub =  new SeLoggerStub(project, commitId, true);
        verify(stub, answers);
    }
    @Test
    public void testSimplePath() throws Exception {
        String project = "TestEffortEstimationTutorial";
        String commitId = "793ce6e21ad234e007a2d27c330dcb13efa35090";
        SeLoggerStub stub =  new SeLoggerStub(project, commitId, true);
        Map<String, List<String>> answers = new HashMap<>();
        answers.put("src/test/java/CalculatorTest.java;CalculatorTest.testDivided_N001#",
                Arrays.asList(
                        "src/test/java/CalculatorTest.java;CalculatorTest.<init>#@5",
                        "src/main/java/Calculator.java;Calculator.<init>#@3",
                        "src/main/java/Calculator.java;Calculator.<init>#@4",
                        "src/main/java/Calculator.java;Calculator.<init>#@5",
                        "src/main/java/Calculator.java;Calculator.divided#int@16",
                        "src/main/java/Calculator.java;Calculator.divided#int@19",
                        "src/main/java/Calculator.java;Calculator.divided#int@20",
                        "src/main/java/Calculator.java;Calculator.getAnswer#@22"
                ));
        //TODO:
        // "src/main/java/Calculator.java;Calculator.divided#int@15",
        // "src/main/java/Calculator.java;Calculator.divided#int@18",
        // "src/main/java/Calculator.java;Calculator.getAnswer#@21",
        verify(stub, answers);
    }

}
