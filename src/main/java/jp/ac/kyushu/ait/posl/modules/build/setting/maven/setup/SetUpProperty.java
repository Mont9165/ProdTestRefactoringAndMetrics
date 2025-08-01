package jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup;

import java.util.Properties;

public abstract class SetUpProperty implements SetElementOfPom{

    public Properties getProperties(Properties p, String key, String value){
        if(p==null){
            p = new Properties();
        }
        String source = p.getProperty(key);
        if(source==null){
            p.setProperty(key, value);
        }
        return p;
    }
}
