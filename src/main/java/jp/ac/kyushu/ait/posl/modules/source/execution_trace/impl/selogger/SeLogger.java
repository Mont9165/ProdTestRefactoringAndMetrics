package jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.selogger;

import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.modules.build.commands.select.TestSuiteRunSelector;
import jp.ac.kyushu.ait.posl.modules.build.commands.select.TestTargetSelector;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.impl.SetUpSeLogger;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.ExecutionTracer;
import jp.ac.kyushu.ait.posl.utils.exception.NoSureFireException;
import jp.ac.kyushu.ait.posl.utils.file.MyFileUtils;
import jp.ac.kyushu.ait.posl.utils.file.MyPathUtil;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SeLogger extends SetUpSeLogger implements ExecutionTracer {
    BuildToolSettingController mc;
    /**
     * read setting file
     * @param mc
     */
    public SeLogger(BuildToolSettingController mc) throws NoSureFireException {
        this.mc = mc;
        homeDir = mc.getHomeDir();
        srcDir = mc.getSrcDir(false);
        srcDir = MyProgramUtils.addLastSlash(srcDir);
        testDir = mc.getTestDir(false);
        testDir = MyProgramUtils.addLastSlash(testDir);
        SettingManager sm = mc.getSettingManager();
        String seloggerDir = MyPathUtil.join(sm.posManager.getHomeDirAbsPath() , sm.propManager.getProperty("seloggerDir"));
        outputFile = sm.propManager.getProperty("seloggerFile");
        outputDir = sm.propManager.getProperty("seloggerOutputDir");
        exlocation = sm.propManager.getProperty("exlocation");
        except = sm.propManager.getProperty("except");
        String excpt = sm.propManager.getProperty("selogger_exception");
        if(excpt!=null){
            except+=excpt;
        }
        weave = sm.propManager.getProperty("weave");
        bufferSize = sm.propManager.getProperty("bufferSize");
        commonMemorySize = sm.propManager.getProperty("commonMemorySize");
        maxMemorySize = sm.propManager.getProperty("maxMemorySize");
        if(mc.getModules()==null){
            this.deployJarFile(seloggerDir, homeDir);
        }else{
            for(BuildToolSettingController bc: mc.getModules().values()){
                this.deployJarFile(seloggerDir, bc.getHomeDir());
            }
        }
        mc.setupExtraPom(this);
        mc.writeSettingFile();
        if (mc.getModules()==null) return;
        for(BuildToolSettingController bc: mc.getModules().values()){
            MavenSettingController m = (MavenSettingController) bc;
            if(m.moduleName==null){//to address a specific case (pom-main.xml)
                m.setupExtraPom(this);
                m.writeSettingFile();
            }
        }//TODO: extern/pom-main.xml (corner case)
    }

    private void deployJarFile(String seloggerDir, String dir) {
        File original = new File(MyPathUtil.join(seloggerDir, outputFile));
        File copied = new File(MyPathUtil.join(dir, outputFile));
        try {
            FileUtils.copyFile(original, copied);
        } catch (IOException e) {
            logger.error(e);
            System.out.println(e);
            throw new AssertionError();
        }
    }


    /**
     * this is for testing
     * TODO: make a beans.test.rowdata.stub for test
     * @param dir
     */
    public SeLogger(String dir) {
        //this is for test
        homeDir = "";
        srcDir = "src/main/java/";
        testDir = "src/test/java/";
        outputDir = MyPathUtil.join("src/test/resources/", dir);
    }



    @Override
    public void clean(String moduleName) throws IOException {
        MyFileUtils.deleteDirectory(getSeloggerOutputDir(moduleName));
    }

    @Override
    public String getSpecificCommand() {
        return null;//Not needed
    }

    @Override
    public void verify() {
        TestTargetSelector targetSelector = this.mc.getSettingManager().getTargetSelector();
        if (targetSelector instanceof TestSuiteRunSelector)
            throw new UnsupportedOperationException();
    }

    @Override
    public BuildToolSettingController getBuildToolSettingController() {
        return this.mc;
    }

    public String getSeloggerOutputDir(String moduleName){
        String dir = "";
        if (moduleName!=null){
            dir =  moduleName;
        }
        return MyPathUtil.join(dir, outputDir);
    }
    @Override
    public Map<String, Map<Integer, List<PassedLine>>> getPassLinesMap(String modName, List<String> testSignatures) {//testSignature, anySignature, line (in the file)
        return SeLoggerReader.getPassLinesMap(homeDir, srcDir, testDir, getSeloggerOutputDir(modName), testSignatures);
    }

    @Override
    public boolean isTracer(){
        return true;
    }




}
