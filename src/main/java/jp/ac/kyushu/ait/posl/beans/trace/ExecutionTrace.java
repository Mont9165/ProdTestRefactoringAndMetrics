package jp.ac.kyushu.ait.posl.beans.trace;

import jp.ac.kyushu.ait.posl.beans.run.Registry;
import jp.ac.kyushu.ait.posl.beans.source.PassedLine;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to store/get data from Database (hibernate)
 */
@Entity
@Table(name = "trace_in_test", schema = "trace",
        indexes = { @Index( name="trace_idx", columnList="test_trace_id,registry_id,signature") })
public class ExecutionTrace implements Serializable {
    /**
     * Project name
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_trace_id", columnDefinition="bigint")
    public long id;

    @Column(name = "registry_id", columnDefinition = "bigint")
    public long registryId;

    @Column(name = "signature", columnDefinition="TEXT")
    public String signature;
    /**
     * Line number in the file (absolute number)
     */
    @Column(name = "line_no")
    public Integer lineNo;

    /**
     * List of the path exercised by this test
     */
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "trace_in_source", schema = "trace",
            joinColumns = {
                @JoinColumn(name = "test_trace_id", referencedColumnName = "test_trace_id", nullable = false)
    }
    ,indexes = { @Index( name="path_idx", columnList="test_trace_id")}
    )
    @Column(name = "path", columnDefinition="TEXT")
    @OrderColumn(name="invoked_order")
    public List<String> passes;

    public ExecutionTrace(Long registryId, String testSignature, Integer i) {
        this.signature = testSignature;
        this.registryId = registryId;
        this.lineNo = i;
        this.passes = new ArrayList<>();
    }
    public ExecutionTrace(){}
    public void add(List<PassedLine> lines) {
        for(PassedLine p: lines){
            passes.add(p.toString());
        }
    }
    public String toString(){
        return this.id+","+this.registryId+","+this.signature+", "+this.lineNo+", "+this.passes;
    }
}