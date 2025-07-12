package jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.impl;

import jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.SetUpPlugin;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;

public class SetUpPiTest extends SetUpPlugin {
    public SetUpPiTest() {
        super("org.pitest", "pitest-maven", "1.13.1");
    }
//<plugin>
//    <groupId>org.pitest</groupId>
//    <artifactId>pitest-maven</artifactId>
//    <version>LATEST</version>
//</plugin>

    @Override
    protected List<PluginExecution> createExecutions() {
        return null;
    }


    @Override
    protected void updateExecutions(Plugin plugin) {

    }


    @Override
    protected Xpp3Dom createConf() {
        return null;
    }

    /**
     * update a configuration element
     * @return
     */
    @Override
    protected void updateConfiguration(Plugin plugin) {
        //Nothing
    }
}
