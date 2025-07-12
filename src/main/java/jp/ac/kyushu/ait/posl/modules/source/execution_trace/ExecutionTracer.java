package jp.ac.kyushu.ait.posl.modules.source.execution_trace;

import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Depend on the setting file, this returns an appropriate tool interface
 */
public interface ExecutionTracer {

    Map<String, Map<Integer, List<PassedLine>>> getPassLinesMap(String moduleName, List<String> testSignatures);

    void clean(String moduleName) throws IOException;

    String getSpecificCommand();

    void verify();
    BuildToolSettingController getBuildToolSettingController();

    default boolean isTracer() {
        return false;
    }
}
