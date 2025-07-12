package jp.ac.kyushu.ait.posl.beans.run;


import jp.ac.kyushu.ait.posl.utils.setting.HostUtil;
import jp.ac.kyushu.ait.posl.utils.setting.VersionUtil;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This class is the base for all registry class.
 * This class is used to store/get data from Database (hibernate)
 */

@Entity
@Table(name = "registry", schema = "run",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project", "commit_id", "execution_id", "execution_type"}))
public class Registry implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registry_id", columnDefinition = "bigint")
    public long registryId;

    /**
     * This is id to identify the aim of execution
     */
    @Column(name = "execution_id")
    public String executionId;

    @Column(name = "execution_type")
    @Enumerated(EnumType.STRING)
    public ExecutionType executionType;

    @Column(name = "trace_tool_version")
    public String traceToolVersion;

    @Column(name = "core_tool_version")
    public String coreToolVersion;

    @Column(name = "executed_host_name")
    public String executedHostName;
    /**
     * Project name
     */
    @Column(name = "project")
    public String project;
    /**
     * Commit sha
     */
    @Column(name = "commit_id")
    public String commitId;
    /**
     * The status of build/analysis
     * 0: Waiting
     * 1: Running
     * 2: Complete
     * 3: Normal Terminate due to Exception
     * -1: Error termination
     */
    @Column(name = "result_code")
    public int resultCode = 0;

    /**
     * Exception/Error name
     */
    @Column(name = "result_message", columnDefinition="TEXT")
    public String resultMessage = null;
    /**
     * Exception/Error message
     */
    @Column(name = "error_message", columnDefinition="TEXT")
    public String errorMessage = null;

    /**
     * Date that analysis/build starts
     */
     @Column(name = "start_date")
     public LocalDateTime startDate;
    /**
     * Date that analysis/build ends
     */
     @Column(name = "end_date")
     public LocalDateTime endDate;




    public Registry(String project, String commitId, ExecutionType executionType, String executionId){
        this.project = project;
        this.commitId = commitId;
        this.executionType = executionType;
        this.executionId = executionId;
        this.coreToolVersion = Registry.class.getPackage().getImplementationVersion();

    }
    public Registry(){
    }

    public boolean isCross() {
        return executionType==ExecutionType.CROSS;
    }

    public void init(){
        this.resultCode = 0;
        this.resultMessage = null;
        this.errorMessage = null;
        this.startDate = null;
        this.endDate = null;
//        this.coreToolVersion = null;
//        this.traceToolVersion = null;
        this.executedHostName = null;
    }

    public void startNow() {
        startDate = LocalDateTime.now();
        this.executedHostName= HostUtil.getHostName();
    }

    protected void finishNow() {
        endDate = LocalDateTime.now();
    }

    public void error(Exception e) {
        resultCode = 3;
        resultMessage = "Exception";
        errorMessage = e.getMessage();
        finishNow();
    }

    public void success() {
        resultCode = 2;
        finishNow();
    }

    public enum ExecutionType {
        STRAIGHT, CROSS, PURE
    }
}
