package jp.ac.kyushu.ait.posl.modules.build.setting;

import jp.ac.kyushu.ait.posl.beans.build.BuildFile;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BuildFileReaderTest {
    @Test
    public void readMavenFile(){
        String project = "TestEffortEstimationTutorial";
        String commitId = "c2dc484c69b059a9a970f5273e171bf2917db678";
        SettingManager sm = new SettingManager(project);
        BuildFileReader reader = new BuildFileReader(sm, commitId);
        BuildFile file = reader.read();
        assertTrue(file.hasMaven);
        assertFalse(file.hasAnt);
        assertFalse(file.hasBazel);
        assertFalse(file.hasGradle);
    }

    @Test
    public void readBazelFile(){
        String project = "closure-compiler";
        String commitId = "0fb7dee06037565372ea46795aee39600396bdee";
        SettingManager sm = new SettingManager(project);
        BuildFileReader reader = new BuildFileReader(sm, commitId);
        BuildFile file = reader.read();
        assertTrue(file.hasBazel);
        assertFalse(file.hasAnt);
        assertFalse(file.hasMaven);
        assertFalse(file.hasGradle);
    }

    @Test
    public void readGradleFile(){
        String project = "kiss";
        String commitId = "4cc60b7c6dcde01115f5fb4a9ccceccf4549cfe4";
        SettingManager sm = new SettingManager(project);
        BuildFileReader reader = new BuildFileReader(sm, commitId);
        BuildFile file = reader.read();
        assertTrue(file.hasGradle);
        assertFalse(file.hasAnt);
        assertFalse(file.hasMaven);
        assertFalse(file.hasBazel);
    }
    @Test
    public void fixReadError1(){//NullPointerException
        String project = "TestEffortEstimationTutorial";
        String commitId = "f478780a32e7a101f96dc12efbbc38f1b4ba5bc0";
        SettingManager sm = new SettingManager(project);
        BuildFileReader reader = new BuildFileReader(sm, commitId);
        BuildFile file = reader.read();
        assertFalse(file.hasAnt);
        assertFalse(file.hasGradle);
        assertFalse(file.hasMaven);
        assertFalse(file.hasBazel);
    }

}
