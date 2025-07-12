package jp.ac.kyushu.ait.posl.modules.build;

import jp.ac.kyushu.ait.posl.modules.test.JunitTestResultManager;
import jp.ac.kyushu.ait.posl.modules.build.commands.BuildCommands;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.ExecutionTracer;
import jp.ac.kyushu.ait.posl.utils.exception.*;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;

import java.io.IOException;
import java.util.Set;

/**
 * this class manipulate build tool
 */
public class BuildRunner {
    static MyLogger logger = MyLogger.getInstance();
    protected final BuildToolSettingController mavenSetting;
    protected BuildCommands mavenCommander;
    protected BuildResultRecorder recorder;

    public long registryId;
    /**
     * initialize maven repository
     */
    public BuildRunner(long registryId, ExecutionTracer dynamicAnalyzer) throws DependencyProblemException, ProductionProblemException, NoTargetBuildFileException, NoSureFireException, JavaVersionTooOldException, JUnitNotFoundException, JUnitVersionUnsupportedException, NoParentsException, InappropriateEnvironmentException {
        this.registryId = registryId;
        this.mavenSetting = dynamicAnalyzer.getBuildToolSettingController();
        mavenCommander = mavenSetting.getBuildCommander();
        mavenCommander.install();
        mavenCommander.dependencyCheck();
        mavenCommander.productionCheck();
        dynamicAnalyzer.verify();
        mavenCommander.setUpDynamicAnalyzer(dynamicAnalyzer);
        recorder = new BuildResultRecorder(mavenSetting, dynamicAnalyzer, getJunitResultManager());

    }

    /**
     * deploy files which is used in only CrossBuildRunner
     *
     * @throws NoSureFireException
     */
    public void deploy() throws IOException, TestUnknownFailureException {

    }


//    public void setUpDynamicAnalyzer(ExecutionTracer dynamicAnalyzer) throws NoSureFireException {
//        mavenCommander.setUpDynamicAnalyzer(dynamicAnalyzer);
//    }

    /**
     * Run tests and return the results. If compiler errors are detected, they will be deleted.
     * @throws IOException
     */
    public void run(String targetName) throws IOException, NoParentsException, TestUnknownFailureException, NoSureFireException, BuildExecutionError, CrushHappensException {
        mavenCommander.run(targetName);
        recorder.recordResults(targetName);
    }

    public JunitTestResultManager getResults(){
        return recorder.getResults();
    }


    public Set<String> getTargets() {
        return recorder.getTargets();
    }


    public JunitTestResultManager getJunitResultManager() {
        return new JunitTestResultManager(this.registryId, mavenSetting);
    }

}
