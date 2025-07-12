package jp.ac.kyushu.ait.posl.utils.file;

import org.junit.Assert;
import org.junit.Test;

public class TestMyPathUtil {
    @Test
    public void testJoinN001(){
        String ans = "/Users/yutarokashiwa/Documents/200_Development/210_Git/TestEffortEstimationOnRefactoring/pom.xml";
        String input1 = "/Users/yutarokashiwa/Documents/200_Development/210_Git/TestEffortEstimationOnRefactoring";
        String input2 = "pom.xml";

        String rtn = MyPathUtil.join(input1, input2);
        Assert.assertEquals(ans, rtn);
    }
    @Test
    public void testJoinN002(){
        String ans = "/Users/yutarokashiwa/Documents/200_Development/210_Git/TestEffortEstimationOnRefactoring/repos/aaa/pom.xml";
        String input1 = "/Users/yutarokashiwa/Documents/200_Development/210_Git/TestEffortEstimationOnRefactoring";
        String input2 = "repos/aaa/pom.xml";

        String rtn = MyPathUtil.join(input1, input2);
        Assert.assertEquals(ans, rtn);
    }
    @Test
    public void testJoinN003(){
        String ans = "/Users/yutarokashiwa/Documents/200_Development/210_Git/TestEffortEstimationOnRefactoring/pom.xml";
        String input1 = "/Users/yutarokashiwa/Documents/200_Development/210_Git/TestEffortEstimationOnRefactoring/";
        String input2 = "pom.xml";

        String rtn = MyPathUtil.join(input1, input2);
        Assert.assertEquals(ans, rtn);
    }
    @Test
    public void testJoinN004(){
        String ans = "/Users/yutarokashiwa/Documents/200_Development/210_Git/TestEffortEstimationOnRefactoring/pom.xml";
        String input1 = "/Users/yutarokashiwa/Documents/200_Development/210_Git/TestEffortEstimationOnRefactoring/";
        String input2 = "/pom.xml";

        String rtn = MyPathUtil.join(input1, input2);
        Assert.assertEquals(ans, rtn);
    }
    @Test
    public void testJoinN005(){
        String ans = "/Users/yutarokashiwa/Documents/200_Development/210_Git/TestEffortEstimationOnRefactoring/pom.xml";
        String input1 = "/Users/yutarokashiwa/Documents/200_Development/210_Git/";
        String input2 = "/TestEffortEstimationOnRefactoring/";
        String input3 = "/pom.xml";

        String rtn = MyPathUtil.join(input1, input2, input3);
        System.out.println(rtn);
        Assert.assertEquals(ans, rtn);
    }
    @Test
    public void testJoinN006(){
        String ans = "repos/aaa/pom.xml";
        String input1 = null;
        String input2 = "repos/aaa/pom.xml";

        String rtn = MyPathUtil.join(input1, input2);
        Assert.assertEquals(ans, rtn);
    }
    @Test
    public void testGetDirectoryName(){
        String ans = "repos/aaa";
        String input = "repos/aaa/pom.xml";

        String rtn = MyPathUtil.getDirectoryName(input);
        Assert.assertEquals(ans, rtn);
    }
    @Test
    public void testGetFileName_N001(){
        String ans = "pom.xml";
        String input = "repos/aaa/pom.xml";

        String rtn = MyPathUtil.getFileName(input);
        Assert.assertEquals(ans, rtn);
    }
    @Test
    public void testGetFileName_N002(){
        String ans = "pom.xml";
        String input = "pom.xml";

        String rtn = MyPathUtil.getFileName(input);
        Assert.assertEquals(ans, rtn);
    }
}
