package jp.ac.kyushu.ait.posl.modules.build.commands;

import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.ExecutionTracer;
import jp.ac.kyushu.ait.posl.utils.exception.InappropriateEnvironmentException;
import jp.ac.kyushu.ait.posl.utils.file.MyFileUtils;
import org.apache.maven.shared.invoker.InvocationResult;

import java.io.IOException;

public class MavenModuleCommands extends MavenCommands{

    public MavenModuleCommands(MavenSettingController maven) throws InappropriateEnvironmentException {
        super(maven);
    }


    @Override
    public void cleanFiles(ExecutionTracer dynamicAnalyzer) {
        for (String modName: this.maven.getModules().keySet()){
            BuildToolSettingController bc = this.maven.getModules().get(modName);
            try {
                MyFileUtils.deleteDirectory(bc.getSureFireOutputDir());
            }catch (IOException nsf){
                //nothing
            }
            try {
                dynamicAnalyzer.clean(modName);
            }catch (IOException | NullPointerException nsf){
                //nothing
            }
        }

    }

    /**
     * To use -Dtest, we need to create snapshots.
     * @return
     */
    @Override
    public InvocationResult install() {//package?
        return execute("clean install", "-DskipTests");
    }





}
