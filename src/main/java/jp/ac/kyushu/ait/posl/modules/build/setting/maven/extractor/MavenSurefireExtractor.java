package jp.ac.kyushu.ait.posl.modules.build.setting.maven.extractor;

import org.apache.maven.model.*;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;

public class MavenSurefireExtractor {

    /**
     * not exist = null
     * exist = version
     * @param pomModel
     * @return
     */
    public static String getSurefireVersion(Model pomModel) {
        Build build = pomModel.getBuild();
        if (build==null) return null;

        String result = getSurefireVersion(build.getPlugins());
        if (result!=null) return result;
        PluginManagement pm = build.getPluginManagement();
        if (pm==null) return null;
        result = getSurefireVersion(pm.getPlugins());
        return result;
    }
    private static String getSurefireVersion(List<Plugin> plugins) {
        Plugin p = getSurefirePlugin(plugins);
        if(p==null) return null;
        return p.getVersion();
    }

    private static Plugin getSurefirePlugin(List<Plugin> plugins) {
        for(Plugin p: plugins){
            if(p.getArtifactId().equals("maven-surefire-plugin")){
                return p;
            }
        }
        return null;
    }

    public static Boolean isSkipSurefire(Model pomModel) {
        Build build = pomModel.getBuild();
        if (build==null) return null;

        Boolean result = isSkipSurefire(build.getPlugins());
        if (result!=null) return result;

        PluginManagement pm = build.getPluginManagement();
        if (pm==null) return null;
        result = isSkipSurefire(pm.getPlugins());

        if(result==null) {
            return null;
        }else return result;
    }
    private static Boolean isSkipSurefire(List<Plugin> plugins) {
        Plugin p = getSurefirePlugin(plugins);
        if(p==null) return null;
        Xpp3Dom conf = ((Xpp3Dom)p.getConfiguration());
        if (conf==null) return null;
        Xpp3Dom skipConf = conf.getChild("skipTests");
        if (skipConf==null) return null;
        switch (skipConf.getValue()){
            case "true":
                return true;
            case "false":
                return false;
            default:
                return null;
        }
    }
    //      <plugin>
    //        <groupId>org.apache.maven.plugins</groupId>
    //        <artifactId>maven-surefire-plugin</artifactId>
    //        <version>2.17</version>
    //        <configuration>
    //          <skipTests>true</skipTests>
    //        </configuration>
    //      </plugin>
    //     <plugin>


}
