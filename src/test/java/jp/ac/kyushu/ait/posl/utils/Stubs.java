package jp.ac.kyushu.ait.posl.utils;

import jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.selogger.SeLogger;
import jp.ac.kyushu.ait.posl.modules.test.JunitTestResultManager;
import jp.ac.kyushu.ait.posl.modules.build.BuildRunner;
import jp.ac.kyushu.ait.posl.modules.build.BuildRunnerCross;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;

import java.util.Set;

public class Stubs {
    public static JunitTestResultManager runTests(String project, String commitId, boolean isCross) throws Exception {
        SettingManager sm = new SettingManager(project);
        sm.changeProperty("tracer", "se_logger");
        sm.changeProperty("testRun", "m");
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        mc.checkout(commitId, false, false);
        mc.readBuildFile();
//        mc.validate();
        BuildRunner buildRunner;
        if (isCross){
            buildRunner = new BuildRunnerCross(0, new SeLogger(mc));
        }else{
            buildRunner = new BuildRunner(0, new SeLogger(mc));
        }
        buildRunner.deploy();
        Set<String> targets = buildRunner.getTargets();
        System.out.println("targets:"+targets);
        for(String target: targets){
            buildRunner.run(target);
        }
        return buildRunner.getResults();
    }
}
