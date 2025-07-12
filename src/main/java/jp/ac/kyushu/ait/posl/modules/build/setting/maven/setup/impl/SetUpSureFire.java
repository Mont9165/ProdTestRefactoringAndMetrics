package jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.impl;

import jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.SetUpPlugin;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;

import static jp.ac.kyushu.ait.posl.utils.xml.MyXMLUtil.addChild;

public class SetUpSureFire extends SetUpPlugin {
    public SetUpSureFire(){
        super("maven-surefire-plugin", "org.apache.maven.plugins","3.0.0-M5" );
    }






    /**
     * create a configuration element
     * @return
     */
    @Override
    protected Xpp3Dom createConf() {
        Xpp3Dom conf = new Xpp3Dom("configuration");
        addChild(conf, "testFailureIgnore", "true");
//        addChild(conf, "reuseForks", "false");
        addChild(conf, "forkNode", "implementation", "org.apache.maven.plugin.surefire.extensions.SurefireForkNodeFactory");
        addChild(conf, "useSystemClassLoader", "false");
//        addChild(conf, "forkCount", "0");//SELoggerが動かなくなる. 1なら動く
        this.setInheritanceOptions(conf);
        return conf;
    }

    protected void setInheritanceOptions(Xpp3Dom conf) {
        //Used for only SELogger
    }

    /**
     * update a configuration element
     * @return
     */
    @Override
    protected void updateConfiguration(Plugin plugin) {
        Xpp3Dom conf = ((Xpp3Dom)plugin.getConfiguration());
//        addChild(conf, "skip", "false");
        addChild(conf, "testFailureIgnore", "true");
//        addChild(conf, "reuseForks", "false");
        addChild(conf, "forkNode", "implementation", "org.apache.maven.plugin.surefire.extensions.SurefireForkNodeFactory");
        addChild(conf, "useSystemClassLoader", "false");
//        addChild(conf, "forkCount", "0");//SELoggerが動かなくなる. 1なら動く
        this.setInheritanceOptions(conf);
        plugin.setConfiguration(conf);
    }


    @Override
    protected List<PluginExecution> createExecutions() {
        return null;
    }

    @Override
    protected void updateExecutions(Plugin plugin) {
        //Nothing
    }


}
