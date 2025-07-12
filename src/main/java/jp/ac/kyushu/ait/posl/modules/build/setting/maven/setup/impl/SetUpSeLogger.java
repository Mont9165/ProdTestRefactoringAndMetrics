package jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.impl;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import static jp.ac.kyushu.ait.posl.utils.xml.MyXMLUtil.addChild;

public class SetUpSeLogger extends SetUpSureFire {//Selogger is a sure-fire based
    public static String outputFile;
    /**
     * top directory in the repository
     */
    public String homeDir;
    /**
     * source code directory shown by pom file
     */
    public String srcDir;
    /**
     * test code directory shown by pom file
     */
    public String testDir;
    /**
     * output directory for selogger
     */
    public String outputDir = "selogger-output";
    public String exlocation = "/.m2/";
    protected String except = ",e=org/junit,e=com/intellij,e=jdk/nashorn,e=jdk/";
    protected String weave = "";//",weave=CALL";/
    protected static String bufferSize = "32";
    protected static String commonMemorySize = "8g";
    protected static String maxMemorySize = "12g";

    /**
     * to handle the existing setting in the pom file
     * @param conf
     */
    @Override
    public void setInheritanceOptions(Xpp3Dom conf) {
        Xpp3Dom x = conf.getChild("argLine");
        if(x==null){
            x = new Xpp3Dom("argLine");
        }
        String val = x.getValue();
        if(val==null){
            addChild(conf, "argLine", this.getSeloggerCommand());
        }else if(val.contains("-Xmx") || val.contains("-Xms")){
            //TODO: extract contents except -Xms and Xmx
            addChild(conf, "argLine", this.getSeloggerCommand());
        }else{
            addChild(conf, "argLine", val + " " + this.getSeloggerCommand());
        }
    }

    public String getSeloggerCommand() {//+" -verbose:gc" +
        return "-Xms"+this.commonMemorySize+ " -Xmx"+this.maxMemorySize + " -javaagent:" + outputFile + "="+"format=nearomni,size="+this.bufferSize + ",weave="+weave + except + ",exlocation="+exlocation;
//        return " -verbose:gc" + " -javaagent:" + outputFile + "="+"format=nearomni,size="+this.bufferSize + ",weave="+weave + except + ",exlocation="+exlocation;
    }
}
