package jp.ac.kyushu.ait.posl.utils.setting;


import jp.ac.kyushu.ait.posl.modules.build.commands.select.ClassRunSelector;
import jp.ac.kyushu.ait.posl.modules.build.commands.select.MethodRunSelector;
import jp.ac.kyushu.ait.posl.modules.build.commands.select.TestSuiteRunSelector;
import jp.ac.kyushu.ait.posl.modules.build.commands.select.TestTargetSelector;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.setting.inner.PositionManager;
import jp.ac.kyushu.ait.posl.utils.setting.inner.Project;
import jp.ac.kyushu.ait.posl.utils.setting.inner.ProjectManager;
import jp.ac.kyushu.ait.posl.utils.setting.inner.PropertyManager;

import java.io.Serializable;

/**
 * This class manage three managers about property, position (directory), project.
 */
public class SettingManager implements Serializable {
    /**
     * This class loads setting file.
     */
    public PropertyManager propManager;
    /**
     * This class returns directly information using PropertyManager.
     */
    public PositionManager posManager;
    /**
     * This class returns project information using PropertyManager.
     */
    protected ProjectManager pjManager;
    String arg;
    MyLogger logger = MyLogger.getInstance();


    public SettingManager(String... args){
        if(args.length==1){
            arg=args[0];
        }
        String[] fullPath = new Exception().getStackTrace()[1].getClassName().split("\\.");

        String executedMethodName = fullPath[fullPath.length-1];
        logger.trace("executed by "+executedMethodName);
        propManager = new PropertyManager(executedMethodName);
        pjManager = new ProjectManager(propManager, arg);
        posManager = new PositionManager(propManager, pjManager);
    }
    public Project getProject(){
        return pjManager.getProject();
    }

    public String getOutputDir(){
        return this.posManager.getDataDirAbsPath();
    }

    public String getSetting(String set){
        return propManager.getProperty(set);
    }

    public String getBranch() {
        return propManager.getProperty("branch");
    }

    public String getLogDir() {
        return "logs";
    }

    public static String repoDir="repos";
    public String getRepoDir() {
        return repoDir;
    }

    public TestTargetSelector getTargetSelector() {
        String mode = propManager.getProperty("testRun");
        switch (mode){
            case "c":
                return new ClassRunSelector();
            case "m":
                return new MethodRunSelector();
            case "w":
                return new TestSuiteRunSelector();
            default:
                throw new AssertionError();
        }
    }


    public enum RunType{
        Method, Class
    }
    public RunType getRunType() {
        switch (this.propManager.getProperty("runType")){
            case "m":
                return RunType.Method;
            case "c":
                return RunType.Class;
            default:
                throw new AssertionError();
        }
    }
    public void changeProperty(String key, String value){
        this.propManager.addProperty(key, value);
    }
}
