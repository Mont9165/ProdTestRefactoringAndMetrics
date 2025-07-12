package jp.ac.kyushu.ait.posl.stub;

import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;

public class JacocoStub extends SpecifierStub{

    public JacocoStub(String project, String commitId) throws Exception {
        super(project, commitId);
    }
    public JacocoStub(String project, String commitId, boolean run) throws Exception {
        super(project, commitId, run);
    }

    @Override
    protected void initSettingManager(String project) {
        sm = new SettingManager(project);
        sm.changeProperty("tracer", "jacoco");
        sm.changeProperty("testRun", "m");
    }
}