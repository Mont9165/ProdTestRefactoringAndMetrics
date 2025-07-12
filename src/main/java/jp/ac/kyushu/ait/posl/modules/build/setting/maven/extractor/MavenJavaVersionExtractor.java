package jp.ac.kyushu.ait.posl.modules.build.setting.maven.extractor;

import jp.ac.kyushu.ait.posl.utils.xml.MyPropertiesUtil;
import org.apache.maven.model.*;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static jp.ac.kyushu.ait.posl.utils.xml.MyPropertiesUtil.getValueFromProp;

public class MavenJavaVersionExtractor {
    private static boolean forthJavaVersionUpdate = true;
    private static String defaultJavaVersion = "1.7";
    public boolean isForceUpdated = false;

    static List<String> junits = Arrays.asList("junit-jupiter","junit-jupiter-api", "junit");
    public static String getJUnitVersion(Model pomModel, Properties properties) {
        List<Dependency> depends = pomModel.getDependencies();
        String junitVersion = getJUnitVersion(depends, properties);
        if (junitVersion != null) return junitVersion;
        DependencyManagement dependM = pomModel.getDependencyManagement();
        if (dependM != null){
            junitVersion = getJUnitVersion(dependM.getDependencies(), properties);
            if (junitVersion != null) return junitVersion;
        }
        junitVersion = getValueFromProp(pomModel, "junit.version");
        if (junitVersion != null) return junitVersion;
        junitVersion = getValueFromProp(pomModel, "junit");

        return junitVersion;
    }
    private static String getJUnitVersion(List<Dependency> depends, Properties properties) {
        if(depends==null) return null;
        for(Dependency d: depends){
            if(junits.contains(d.getArtifactId())){
                String versionStr = d.getVersion();
                if (versionStr==null){
                    return "";
                }
                versionStr = MyPropertiesUtil.extractValue(properties, versionStr);
                if(versionStr == null){
                    break;
                }
                return versionStr;
            }
        }
        return null;
    }

    //maven-compiler-plugin will be prioritised
    public static String getJavaVersion(Model pomModel) {
        String version = null;
        Build build = pomModel.getBuild();
        if (build != null) {
            version = getJavaVersionInPlugins(pomModel, build.getPlugins());
            if (version != null) return version;

            PluginManagement pluginManagement = build.getPluginManagement();
            if (pluginManagement != null) {
                version = getJavaVersionInPlugins(pomModel, pluginManagement.getPlugins());
            }
        }
        if (version != null) return version;
        version = getValueFromProp(pomModel, "maven.compiler.release");
        if (version != null) return version;
        version = getValueFromProp(pomModel, "maven.compiler.source");
        if (version != null) return version;
        version = getValueFromProp(pomModel, "maven.compile.source");
        if (version != null) return version;
        version = getValueFromProp(pomModel, "maven.compiler.target");
        if (version != null) return version;
        version = getValueFromProp(pomModel, "jdk.version");

        return version;

    }
    private static String getJavaVersionInPlugins(Model pomModel, List<Plugin> plugins) {
        if (plugins == null) return null;
        for(Plugin p: plugins){
            if(p.getArtifactId().equals("maven-compiler-plugin")){
                Xpp3Dom xml = (Xpp3Dom) p.getConfiguration();
                if (xml == null) return null;
                for(Xpp3Dom c: xml.getChildren()){
                    String name = c.getName();
                    if(name==null) continue;
                    switch (name){
                        case "source":
                        case "target":
                        case "release":
                            return getValueFromProp(pomModel, c.getValue());
                        default:
                            break;
                    }

                }
            }
        }
        return null;
    }
}
