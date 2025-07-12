package jp.ac.kyushu.ait.posl.modules.build.commands;

import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.ExecutionTracer;
import jp.ac.kyushu.ait.posl.utils.exception.*;
import jp.ac.kyushu.ait.posl.utils.file.MyFileUtils;
import jp.ac.kyushu.ait.posl.utils.log.LogCollector;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

public class MavenCommands implements BuildCommands {
    private final Invoker invoker;
    private LogCollector logCollector;
    protected MavenSettingController maven;
    protected final String RUN_OPTION = "-Drat.skip=true -Dmaven.test.failure.ignore=true -DfailIfNoTests=false ";//-Dmaven.main.skip=true
    private ExecutionTracer dynamicAnalyzer;


    public MavenCommands(MavenSettingController maven) throws InappropriateEnvironmentException {
        this.maven = maven;
        this.invoker = new DefaultInvoker();
        String m2Home = System.getenv("M2_HOME");

        if (m2Home==null){
            System.err.println("M2_HOME needs to be set up");
            throw new InappropriateEnvironmentException();
        }
        File file = new File(m2Home);
        if (!file.exists()){
            System.err.println("Incorrect M2_HOME");
            throw new InappropriateEnvironmentException();
        }
        this.invoker.setMavenHome(file);

    }
    /**
     * to run maven
     * @param goal
     * @return
     */
    protected InvocationResult execute(String goal, String option) {
        //InvocationResult ir0 = mc.run("clean");//Just in case
        System.out.println("mvn "+goal+"  "+ option);
        return doTest(goal, option);
    }

    public List<String> getErrors(){
        return this.logCollector.getError();
    }

    @Override
    public void setUpDynamicAnalyzer(ExecutionTracer tracer) {
        dynamicAnalyzer = tracer;
    }




    protected InvocationResult doTest(String signature){
        String option = "-Dtest="+signature;
        if (signature==null){
            option = "";
        }
        String goal = null;
        if (dynamicAnalyzer!=null){
            goal= dynamicAnalyzer.getSpecificCommand();
        }
        if (goal==null){
            goal = "test";
        }
        return execute(goal, RUN_OPTION + option);
    }
    /**
     * Meven execution. This method receives commands.\
     * e.g., clean, compile, test
     * @param goal
     * @param option
     * @return
     */
    public InvocationResult doTest(String goal, String option){
        InvocationRequest request = new DefaultInvocationRequest();
        System.out.println("-f "+maven.pomFileAbstractPath);
        request.setPomFile(new File(maven.pomFileAbstractPath));
        request.setGoals( Collections.singletonList(goal) );
        if(option!=null){
            request.setMavenOpts(option);
        }
        this.logCollector = new LogCollector();
        this.invoker.setOutputHandler(new PrintStreamHandler(new PrintStream(this.logCollector), true));
        try {
            return this.invoker.execute(request);
        } catch (MavenInvocationException e) {
            logger.error(e);
            throw new RuntimeException();
        }
    }

    @Override
    public void cleanFiles(ExecutionTracer dynamicAnalyzer) {
        //TODO: execute("clean", null);
        try {
            MyFileUtils.deleteDirectory(this.maven.getSureFireOutputDir());
        }catch (IOException nsf){
            //nothing
        }
        try {
            dynamicAnalyzer.clean(null);//Don't delete this
        }catch (IOException | NullPointerException nsf){
            //nothing
        }
    }

    @Override
    public InvocationResult install() {
        //Do nothing
        return null;
    }



    @Override
    public InvocationResult run(String targetName) throws TestUnknownFailureException, IOException, BuildExecutionError, CrushHappensException {

        //delete the results of tests and SeLogger during previous run
        this.cleanFiles(dynamicAnalyzer);

        //run
        InvocationResult result = this.doTest(targetName);
        if (result.getExitCode() != 0) {
            throw new BuildExecutionError();
        }else if(this.logCollector.getCrash().size()>0){
            throw new CrushHappensException();
        }
        return result;
    }
    public InvocationResult testCompile(){
        return execute( "test-compile", "-Drat.skip=true");
    }


    public InvocationResult dependencyCheck() throws DependencyProblemException, JavaVersionTooOldException {
        StringBuilder errors = new StringBuilder();
        InvocationResult preResult = execute( "test-compile dependency:resolve", "-Drat.skip=true");
        if(preResult.getExitCode() == 0)
            return preResult;
        throwErrors(errors);
        return null;//Never pass there
    }

    private void throwErrors(StringBuilder errors) throws DependencyProblemException, JavaVersionTooOldException {
        boolean isOldJava = false;
        for(String a: this.getErrors()){
            errors.append(" \n").append(a);
            if(a.contains("Use 7 or later.")){
                isOldJava = true;
            }
        }
        System.out.println(errors);
        if (isOldJava){
            throw new JavaVersionTooOldException(errors.toString());
        }else {
            throw new DependencyProblemException(errors.toString());
        }
    }

    /**
     * check if the compiler returns pass
     * @return
     */
    public InvocationResult productionCheck() throws ProductionProblemException {
        InvocationResult preResult = execute( " compile dependency:resolve test-compile", "-Dmaven.test.skip=true -Drat.skip=true");
        if(preResult.getExitCode() != 0){
            StringBuilder errors = new StringBuilder();
            for(String a: this.getErrors()){
                errors.append(" ").append(a);
            }
            // errors = maven.getErrors().get(0).split("¥n")[0];
            throw new ProductionProblemException(errors.toString());
        }
        return preResult;
    }



}
