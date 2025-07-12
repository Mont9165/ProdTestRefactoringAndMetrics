package jp.ac.kyushu.ait.posl.modules.build.commands;

import jp.ac.kyushu.ait.posl.modules.source.execution_trace.ExecutionTracer;
import jp.ac.kyushu.ait.posl.utils.exception.*;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import org.apache.maven.shared.invoker.InvocationResult;

import java.io.IOException;
import java.util.List;

public interface BuildCommands {
    static MyLogger logger = MyLogger.getInstance();

    InvocationResult dependencyCheck() throws DependencyProblemException, JavaVersionTooOldException;

    InvocationResult productionCheck() throws ProductionProblemException;


    InvocationResult run(String target) throws TestUnknownFailureException, IOException, BuildExecutionError, CrushHappensException;

    void cleanFiles(ExecutionTracer dynamicAnalyzer);

    InvocationResult install();

    /**
     * get errors
     * @return
     */
    List<String> getErrors();

    void setUpDynamicAnalyzer(ExecutionTracer tracer);

    InvocationResult testCompile();
}
