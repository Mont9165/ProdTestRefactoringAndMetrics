package jp.ac.kyushu.ait.posl.modules.build;

import jp.ac.kyushu.ait.posl.modules.build.compile.CompileErrorEliminator;
import jp.ac.kyushu.ait.posl.modules.build.compile.CompileErrorFinder;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.ExecutionTracer;
import jp.ac.kyushu.ait.posl.utils.exception.*;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.InvocationResult;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BuildRunnerCross extends BuildRunner {
    public final BuildToolSettingController mavenX_1;
    public final BuildToolSettingController mavenX;

    /**
     * initialize maven
     */
    public BuildRunnerCross(long registryId, ExecutionTracer dynamicAnalyzer) throws DependencyProblemException, ProductionProblemException, NoTargetBuildFileException, IOException, NoParentsException, NoSureFireException, JavaVersionTooOldException, JUnitNotFoundException, JUnitVersionUnsupportedException, InappropriateEnvironmentException {
        super(registryId, dynamicAnalyzer);
        mavenX = mavenSetting;
        mavenX_1 = new MavenSettingController(new GitController(mavenSetting.getGitController().sm, "/x_1/"));
        mavenX_1.checkout(mavenSetting.getCommitId(), true, false);
        mavenX_1.readBuildFile();
    }

    @Override
    public void deploy() throws IOException, TestUnknownFailureException {
        if(mavenX.hasModule()){
            for(String modName: mavenX.getModules().keySet()){
                BuildToolSettingController childX = mavenX.getModules().get(modName);
                BuildToolSettingController childX_1 = mavenX_1.getModules().get(modName);
                if(childX_1==null){
                    throw new AssertionError("Module Not Found: Module Name might be modified");
                }
                this.clean(childX);
                this.deploy(childX_1, childX);
            }
        }else{
            this.clean(mavenX);
            this.deploy(mavenX_1, mavenX);
        }
        this.removeCompileError();
        //TODO: STORE

    }
    public void removeCompileError() throws TestUnknownFailureException, IOException {
        int attempt=1;
        while(true) {//repeat until the test passed
            logger.trace(attempt+"th attempt");
            InvocationResult result = mavenCommander.testCompile();
            if (result.getExitCode() == 0) {
                break;
            } else {//delete test methods that have compiler errors
                List<CompileErrorFinder.ErrorMethod> list = new CompileErrorFinder().getErrorMethods(mavenX, mavenCommander.getErrors());
                if(list.size()==0){
                    throw new TestUnknownFailureException();//when errors happen except compiler errors
                }
                recorder.recordCompileErrorResults(list);
                CompileErrorEliminator eliminator = new CompileErrorEliminator(list);//Modify source code to eliminate errors
                Map<String, List<CompileErrorEliminator.Line>> removedLines = eliminator.deleteLines();
                recorder.recordDeletedLines(attempt, removedLines);
                attempt++;
            }
        }
    }




    /**
     * delete existing test files for cross builds
     * @param mc
     * @throws IOException
     */
    protected void clean(BuildToolSettingController mc) {
        try {
            FileUtils.deleteDirectory(new File(mc.getTargetDir()));
            FileUtils.deleteDirectory(new File(mc.getTestDir()));
        } catch (IOException e) {
        }
    }
    /**
     * Copy the test code in the previous revision to the directory the target revision
     * @param mavenX_1
     * @param mavenX
     * @throws IOException
     */
    private void deploy(BuildToolSettingController mavenX_1, BuildToolSettingController mavenX) throws IOException {
        //copy
        FileUtils.copyDirectory(new File(mavenX_1.getTestDir()), new File(mavenX.getTestDir()));
    }


}
