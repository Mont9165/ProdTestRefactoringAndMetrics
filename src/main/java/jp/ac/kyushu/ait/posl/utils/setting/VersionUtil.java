package jp.ac.kyushu.ait.posl.utils.setting;

public class VersionUtil {
    public static String getApplicationVersion(){
        String version = VersionUtil.class.getPackage().getImplementationVersion();
        return (version == null)? "unable to reach": version;
    }
}