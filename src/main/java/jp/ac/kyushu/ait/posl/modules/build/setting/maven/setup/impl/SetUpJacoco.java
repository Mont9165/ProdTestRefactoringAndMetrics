package jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.impl;

import jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.SetUpPlugin;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;

public class SetUpJacoco extends SetUpPlugin {

    public SetUpJacoco() {
        super("jacoco-maven-plugin", "org.jacoco", "0.8.7");
    }
//            <plugin>
//                <groupId>org.jacoco</groupId>
//                <artifactId>jacoco-maven-plugin</artifactId>
//                <version>0.8.7</version>
//            </plugin>

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

