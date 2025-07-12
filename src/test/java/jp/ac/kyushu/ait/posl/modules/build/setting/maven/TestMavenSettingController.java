package jp.ac.kyushu.ait.posl.modules.build.setting.maven;

import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.utils.exception.NoParentsException;
import jp.ac.kyushu.ait.posl.utils.exception.NoTargetBuildFileException;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestMavenSettingController {
    @Test
    public void testReadBasicFile_N001() throws Exception {
        SettingManager sm = new SettingManager("commons-io");
        GitController gc = new GitController(sm, "repos/test/mvn");
        MavenSettingController mc = new MavenSettingController(gc);
        mc.checkout("63af628e71da85841b48cddbc2ae83d180a040d6");
        mc.readBuildFile();
        String artifactId = mc.getArtifactId();
        String groupId = mc.getGroupId();
        String version = mc.getVersion();
        assertEquals("commons-io", groupId);
        assertEquals("commons-io", artifactId);
        assertEquals("2.12.0-SNAPSHOT", version);
        String moduleName = mc.getModuleName();
        assertNull(moduleName);
        assertNull(mc.getModules());

    }
    @Test
    public void testReadModuleFile_N001() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("commons-math-core");
        list.add("commons-math-legacy-core");
        list.add("commons-math-legacy");
        list.add("commons-math-legacy-exception");
        list.add("commons-math-transform");
        list.add("commons-math-examples");
        list.add("commons-math-neuralnet");
        SettingManager sm = new SettingManager("commons-math");
        GitController gc = new GitController(sm, "repos/test/mvn");
        MavenSettingController mc = new MavenSettingController(gc);
        mc.checkout("e85dd59990a3b47793f5a26ac27e88a9f5ee9e53");
        mc.readBuildFile();
        String artifactId = mc.getArtifactId();
        String groupId = mc.getGroupId();
        String version = mc.getVersion();
        assertNull(groupId);
        assertEquals("commons-math-parent", artifactId);
        assertEquals("4.0-SNAPSHOT", version);
        String moduleName = mc.getModuleName();
        assertNull(moduleName);
        assertEquals(7, mc.getModules().size());

        for(String name: mc.getModules().keySet()){
            MavenSettingController mChild = (MavenSettingController) mc.getModules().get(name);
            assertTrue(list.contains(mChild.moduleName));
        }
    }

    @Test
    public void testReadModuleFile_N002() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("pom.xml");//"externs/pom.xml"
        list.add("pom-gwt.xml");
        list.add("pom-main.xml");
        list.add("pom-linter.xml");
        list.add("pom-main-unshaded.xml");
        list.add("pom-main-shaded.xml");
        SettingManager sm = new SettingManager("closure-compiler");
        GitController gc = new GitController(sm, "test");
        MavenSettingController mc = new MavenSettingController(gc);
        mc.checkout("0ca9454c424697ea9c34d197813d2382922388c2");
        mc.readBuildFile();
        String artifactId = mc.getArtifactId();
        String groupId = mc.getGroupId();
        String version = mc.getVersion();
        assertEquals("com.google.javascript", groupId);
        assertEquals("closure-compiler-parent", artifactId);
        assertEquals("1.0-SNAPSHOT", version);
        String moduleName = mc.getModuleName();
        assertNull(moduleName);
        assertEquals(3, mc.getModules().size());
        System.out.println("------------");
        for(String name: mc.getModules().keySet()){
            MavenSettingController mChild = (MavenSettingController) mc.getModules().get(name);
            System.out.println(mChild.pomFileAbstractPath);
            String[] array = mChild.pomFileAbstractPath.split("/");
            assertTrue(list.contains(array[array.length-1]));
        }
    }

    @Test
    public void extractJavaVersionInPluginManagement_N001() throws NoTargetBuildFileException {
        MavenSettingController mc = new MavenSettingControllerStub("POM/PluginManagement.xml");
        mc.readBuildFile();
        assertEquals("1.8", mc.getJavaVersion());
        assertEquals("2.19.1", mc.getSurefireVersion());
        assertTrue(mc.hasSurefire());
        assertEquals("4.12", mc.getJunitVersion());

    }
    @Test
    public void extractJavaVersionInPluginManagement_N002() throws NoTargetBuildFileException {
        MavenSettingController mc = new MavenSettingControllerStub("POM/Null.xml");
        mc.readBuildFile();
        assertEquals("1.8", mc.getJavaVersion());
        assertNull(mc.getSurefireVersion());
        assertFalse(mc.hasSurefire());
        assertEquals("4.12", mc.getJunitVersion());
    }
    @Test
    public void extractJavaVersionInPlugin() throws NoTargetBuildFileException {
        MavenSettingController mc = new MavenSettingControllerStub("POM/Plugin.xml");
        mc.readBuildFile();
        assertEquals("15", mc.getJavaVersion());
        assertEquals("100", mc.getSurefireVersion());
        assertTrue(mc.hasSurefire());
        assertEquals("4.12", mc.getJunitVersion());

    }
    @Test
    public void testModules_N001() throws NoParentsException, NoTargetBuildFileException {
        String project = "TestEffortEstimationTutorial";
        String commitId = "3a33fb8efb0eb813a889a5bbbbfd0c8604804e52";
        SettingManager sm = new SettingManager(project);
        GitController gc = new GitController(sm, "test");
        MavenSettingController mc = new MavenSettingController(gc);
        mc.checkout(commitId);
        mc.readBuildFile();
        assertEquals(2, mc.getModules().size());
        String rtn = extractNameAfterRepos(mc.getModules().get("Core").getTestDir());
        assertEquals("test/TestEffortEstimationTutorial/Core/src/test/java", rtn);
        rtn = extractNameAfterRepos(mc.getModules().get("Calc").getTestDir());
        assertEquals("test/TestEffortEstimationTutorial/Calc/src/test/java", rtn);


    }
//    @Test
//    public void testModules_N002() throws NoParentsException, NoTargetBuildFileException {
//        String project = "closure-compiler";
//        String commitId = "0ca9454c424697ea9c34d197813d2382922388c2";
//        SettingManager sm = new SettingManager(project);
//        GitController gc = new GitController(sm, "test");
//        MavenSettingController mc = new MavenSettingController(gc);
//        mc.checkout(commitId);
//        mc.readBuildFile();
//        assertEquals(3, mc.getModules().size());
//        for(String key: mc.getModules().keySet()){
//            BuildToolSettingController m = mc.getModules().get(key);
//            System.out.println(key+":"+ m.getTestDir());
//
//        }
//        assertEquals(3, mc.getModules().size());
//        String rtn = extractNameAfterRepos(mc.getModules().get(null).getTestDir());
//        assertEquals("pom-main.xml", rtn);
//        assertEquals("pom-main.xml", rtn);
//        assertEquals("pom-main.xml", rtn);
//        //TODO: Shaded
//        //TODO: MethodDefinitionのモジュールが全てNullであること
//
//
//
//    }


    @Test
    public void testTestDirectory() throws NoParentsException, NoTargetBuildFileException {
        String project = "closure-compiler";
        String commitId = "0ca9454c424697ea9c34d197813d2382922388c2";
        SettingManager sm = new SettingManager(project);
        GitController gc = new GitController(sm, "");
        MavenSettingController mc = new MavenSettingController(gc);
        mc.checkout(commitId);
        mc.readBuildFile();
        assertEquals(3, mc.getModules().size());
        List<String> ans = Arrays.asList("closure-compiler/externs/src/test/java","closure-compiler/test");
        for (BuildToolSettingController m: mc.getModules().values()){
            String ret = extractNameAfterRepos(m.getTestDir());
            assertTrue(ans.contains(ret));
        }

    }

    private String extractNameAfterRepos(String testDir) {
        return testDir.split("repos/")[1];
    }
    @Ignore//commentout validate()
    @Test
    public void TestNoJUnitVersion() throws NoParentsException, NoTargetBuildFileException {
        String project = "commons-io";
        String commitId = "22f6525588afe563a895d6c7a70e03ede86610d1";
        SettingManager sm = new SettingManager(project);
        GitController gc = new GitController(sm, "");
        MavenSettingController mc = new MavenSettingController(gc);
        mc.checkout(commitId);
        mc.readBuildFile();
        assertEquals("", mc.getJunitVersion());
    }
}
