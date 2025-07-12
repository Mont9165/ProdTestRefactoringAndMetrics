package jp.ac.kyushu.ait.posl.stub;

import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;


public class SeLoggerStub extends SpecifierStub{

    public SeLoggerStub(String project, String commitId) throws Exception {
        super(project, commitId);
    }

    public SeLoggerStub(String project, String commitId, boolean run) throws Exception {
        super(project, commitId, run);
    }

    @Override
    protected void initSettingManager(String project) {
        sm = new SettingManager(project);
        sm.changeProperty("tracer", "se_logger");
        sm.changeProperty("testRun", "m");
    }
}
