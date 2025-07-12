package jp.ac.kyushu.ait.posl.utils.setting;

import jp.ac.kyushu.ait.posl.utils.exception.ProjectDoesNotExistException;
import org.junit.Assert;
import org.junit.Test;

public class TestSettingManager {
    @Test
    public void testGetProjectFromString(){
        SettingManager sm = new SettingManager("TestEffortEstimationTutorial");
        Assert.assertEquals("TestEffortEstimationTutorial", sm.getProject().name);
    }
    @Test
    public void testGetProjectFromInt(){
        SettingManager sm = new SettingManager("1");
        Assert.assertNotNull(sm.getProject().name);//Because different JDKs return different list
    }
    @Test(expected = ProjectDoesNotExistException.class)
    public void testGetProjectFromIntWhenWrongNumber(){
        SettingManager sm = new SettingManager("11111");
        Assert.fail();
    }
    @Test(expected = ProjectDoesNotExistException.class)
    public void testGetProjectFromIntWhenWrongString(){
        SettingManager sm = new SettingManager("BAKA_AHO_MANUKE");
        Assert.fail();
    }
}
