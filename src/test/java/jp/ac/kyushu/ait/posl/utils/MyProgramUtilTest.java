package jp.ac.kyushu.ait.posl.utils;

import org.junit.Assert;
import org.junit.Test;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;

public class MyProgramUtilTest {
    @Test
    public void removeUnRelevantFolderName(){
        String target = "core/test/com/google/inject/ErrorHandlingTest.java";
        String reg = "${project.basedir}/test/";
        String ans = MyProgramUtils.removeUnRelevantDirectoryName(reg, target);
        Assert.assertEquals("com/google/inject/ErrorHandlingTest.java", ans);
    }
    @Test
    public void removeUnRelevantFolderName2(){
        String target = "test/org/apache/commons/io/test/ThrowOnCloseWriter.java";
        String reg = "${project.basedir}/test/";
        String ans = MyProgramUtils.removeUnRelevantDirectoryName(reg, target);
        Assert.assertEquals("org/apache/commons/io/test/ThrowOnCloseWriter.java", ans);
    }
    @Test
    public void testGetClassNamesFromSignatureN001(){
        String a = MyProgramUtils.getClassNamesFromSignature("src/main/java/functions/Calculator.java;Calculator.SubClass.SayHello#");
        Assert.assertEquals("Calculator.SubClass", a);
    }
    @Test
    public void testGetClassNamesFromSignatureN002(){
        String a = MyProgramUtils.getClassNamesFromSignature("src/main/java/functions/Calculator.java;Calculator.SayHello#");
        Assert.assertEquals("Calculator", a);
    }
    @Test
    public void testGetClassNamesFromSignatureN003(){
        String a = MyProgramUtils.getClassNamesFromSignature("src/main/java/functions/Calculator.java;Calculator.SubClass1.SubClass2.SubClass3.SayHello#");
        Assert.assertEquals("Calculator.SubClass1.SubClass2.SubClass3", a);
    }
    @Test
    public void getClassSignatureFromFullSignatureN001(){
        String a = MyProgramUtils.getClassSignatureFromFullSignature("src/main/java/functions/Calculator.java;Calculator.SubClass1.SayHello#");
        Assert.assertEquals("src/main/java/functions/Calculator.java;Calculator.SubClass1", a);
    }
    @Test
    public void getClassSignatureFromFullSignatureN002(){
        String a = MyProgramUtils.getClassSignatureFromFullSignature("src/main/java/functions/Calculator.java;Calculator.SayHello#");
        Assert.assertEquals("src/main/java/functions/Calculator.java;Calculator", a);
    }
    @Test
    public void getClassSignatureFromFullSignatureN003(){
        String a = MyProgramUtils.getClassSignatureFromFullSignature("src/main/java/functions/Calculator.java;Calculator.SubClass1.SubClass2.SubClass3.SayHello#");
        Assert.assertEquals("src/main/java/functions/Calculator.java;Calculator.SubClass1.SubClass2.SubClass3", a);
    }
}
