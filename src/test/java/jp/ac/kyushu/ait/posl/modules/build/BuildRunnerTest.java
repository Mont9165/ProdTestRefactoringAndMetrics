package jp.ac.kyushu.ait.posl.modules.build;

import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.selogger.SeLogger;
import jp.ac.kyushu.ait.posl.utils.exception.*;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import org.junit.Ignore;
import org.junit.Test;

public class BuildRunnerTest {
    @Test
    public void testPass() throws JavaVersionTooOldException, NoSureFireException, JUnitNotFoundException, JUnitVersionUnsupportedException, InappropriateEnvironmentException, DependencyProblemException, NoParentsException, ProductionProblemException, NoTargetBuildFileException {
        String project = "TestEffortEstimationTutorial";
        String commitId = "c2dc484c69b059a9a970f5273e171bf2917db678";
        SettingManager sm = new SettingManager(project);
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        mc.checkout(commitId);
        mc.readBuildFile();
        BuildRunner br = new BuildRunner(-1, new SeLogger(mc));



    }
    @Ignore
    @Test(expected = JavaVersionTooOldException.class)
    public void testJava5() throws JavaVersionTooOldException, NoSureFireException, JUnitNotFoundException, JUnitVersionUnsupportedException, InappropriateEnvironmentException, DependencyProblemException, NoParentsException, ProductionProblemException, NoTargetBuildFileException {
        String project = "lambdaj";
        String commitId = "bd3afc7c084c3910454a793a872b0a76f92a43fd";
        SettingManager sm = new SettingManager(project);
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        mc.checkout(commitId);
        mc.readBuildFile();
        BuildRunner br = new BuildRunner(-1, new SeLogger(mc));
    }
}
