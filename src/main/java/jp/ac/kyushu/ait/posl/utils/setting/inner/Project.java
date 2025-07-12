package jp.ac.kyushu.ait.posl.utils.setting.inner;

import java.util.Properties;

public class Project {
    /**
     * project name
     */
    public String name;
    /**
     * Abbreviation (not used)
     */
    public String abb;
    /**
     * Repository url
     */
    public String url;
    public String owner;
    public String classPlacement;
    public String methodPlacement;
    public String branch;
//    public String srcDir;
//    public String testDir;
//    public String classDir;
    public Project(Properties prop){
        name = prop.getProperty("name");
        abb = prop.getProperty("abb");
        url = prop.getProperty("url");
        owner = url.split("/")[3];
        branch = prop.getProperty("branch");
//        classPlacement = prop.getProperty("class");
//        methodPlacement = prop.getProperty("method");
//        srcDir = prop.getProperty("src_dir");
//        testDir = prop.getProperty("test_dir");
//        classDir = prop.getProperty("class_dir");
    }

}
