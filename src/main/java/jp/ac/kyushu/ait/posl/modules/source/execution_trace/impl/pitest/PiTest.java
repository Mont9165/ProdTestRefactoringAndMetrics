package jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.pitest;

import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.impl.SetUpPiTest;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.ExecutionTracer;
import jp.ac.kyushu.ait.posl.utils.exception.NoSureFireException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PiTest extends SetUpPiTest implements ExecutionTracer {
    BuildToolSettingController mc;

    public PiTest(BuildToolSettingController mc) throws NoSureFireException {
        this.mc = mc;
        mc.setupExtraPom(this);
        mc.writeSettingFile();
    }
    @Override
    public Map<String, Map<Integer, List<PassedLine>>> getPassLinesMap(String moduleName, List<String> testSignatures) {
        return null;
    }

    @Override
    public void clean(String moduleName) throws IOException {

    }

    @Override
    public String getSpecificCommand() {
        return null;
    }

    @Override
    public void verify() {

    }

    @Override
    public BuildToolSettingController getBuildToolSettingController() {
        return this.mc;
    }
}
