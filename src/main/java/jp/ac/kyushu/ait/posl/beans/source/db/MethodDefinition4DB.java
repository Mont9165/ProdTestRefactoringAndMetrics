package jp.ac.kyushu.ait.posl.beans.source.db;


import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.beans.source.TestMethodDefinition;
import org.refactoringminer.api.Refactoring;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

/**
 * This class is used for storing data into database
 */
@Entity
@Table(name = "method", schema = "source", uniqueConstraints = { @UniqueConstraint(columnNames = { "project", "commit_id", "type", "signature"}) })
public class MethodDefinition4DB implements Serializable{

    @Id
    @Column(name = "project")
    public String project;
    @Id
    @Column(name = "commit_id")
    public String commitId;
    @Id
    @Column(name = "type")
    public String type;
    /**
     * This is method's signature (i.e., filename, class name, method name, parameter types)
     */
    @Id
    @Column(name="signature", columnDefinition="TEXT")
    public String signature;
    /**
     * This is method's signature (i.e., filename, class name, method name, parameter types) after/before refactoring
     * Sometimes, signature is modified by Rename, Move refactoring
     * This makes us difficult to link the method between two snapshots.
     * Thus, we link them with refactoring miner
     */
    @Column(name="another_signature", columnDefinition="TEXT")
    public String anotherSignature;
    /**
     * file name
     */
    @Column(name="file_name", columnDefinition="TEXT")
    public String fileName;
    /**
     * class name
     */
    @Column(name="class_name", columnDefinition="TEXT")
    public String className;
    /**
     * package name
     */
    @Column(name="package_name", columnDefinition="TEXT")
    public String packageName;
    /**
     * method name
     */
    @Column(name="method_name", columnDefinition="TEXT")
    public String methodName;
    /**
     * annotations (e.g., @Test)
     */
    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "annotations", schema = "source",joinColumns = {@JoinColumn(name = "project", nullable = false),@JoinColumn(name = "commit_id", nullable = false),@JoinColumn(name = "type", nullable = false),@JoinColumn(name = "signature", nullable = false)})
    @Column(name="annotations", columnDefinition="TEXT")
    public List<String> annotations;

    /**
     * Parameters
     */
    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "arguments", schema = "source",joinColumns = {@JoinColumn(name = "project", nullable = false),@JoinColumn(name = "commit_id", nullable = false),@JoinColumn(name = "type", nullable = false),@JoinColumn(name = "signature", nullable = false)})
    @Column(name="arguments", columnDefinition="TEXT")
    public List<String> arguments;
    /**
     * Generics
     */
    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "generics", schema = "source", joinColumns = {@JoinColumn(name = "project", nullable = false),@JoinColumn(name = "commit_id", nullable = false),@JoinColumn(name = "type", nullable = false),@JoinColumn(name = "signature", nullable = false)})
    @Column(name="generics", columnDefinition="TEXT")
    public Set<String> generics;
    public Integer starts;
    public Integer ends;

    /**
     * Super class
     */
    @Column(name="super_class_path", columnDefinition="TEXT")
    public String superClassPath;
    @Column(name="is_in_test")
    public Boolean isInTest;
    @Column(name="is_testcase")
    public Boolean isTestCase;

    /**
     * changed lines. Line number starts from the top of the file.
     * e.g.,
     * Line 10: True (= modified)
     * Line 11: False (= not modified)
     */
    @ElementCollection(targetClass = Boolean.class, fetch=FetchType.EAGER)
    @CollectionTable(name="changeLine",schema = "source",joinColumns = {@JoinColumn(name = "project", referencedColumnName = "project", nullable = false),@JoinColumn(name = "commit_id", referencedColumnName = "commit_id", nullable = false),@JoinColumn(name = "type", referencedColumnName = "type", nullable = false),@JoinColumn(name = "signature", referencedColumnName = "signature", nullable = false)})
    @MapKeyColumn(name="changeLine_key")
    @Column(name="changedLine_value")
    public Map<Integer, Boolean> changedLines;//lineNo, isChange

    // @Transient
    /**
     * refactorings that happens in this method
     */
    @ElementCollection(targetClass = String.class, fetch=FetchType.EAGER)
    @CollectionTable(name="inherent_refactoring",schema = "ia",joinColumns = {@JoinColumn(name = "project", referencedColumnName = "project", nullable = false),@JoinColumn(name = "commit_id", referencedColumnName = "commit_id", nullable = false),@JoinColumn(name = "type", referencedColumnName = "type", nullable = false),@JoinColumn(name = "signature", referencedColumnName = "signature", nullable = false)})
    @MapKeyColumn(name="inherent_refactoring_key")
    @Column(name="inherent_refactoring_value", columnDefinition="TEXT")
    public Map<Integer, String> inherentRefactorings;//lineNo, refactorings
    //Test only
    // @Transient
    /**
     * refactorings affecting this method (outside of this method)
     */
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name="impacted_refactoring",schema = "ia",joinColumns = {@JoinColumn(name = "project", referencedColumnName = "project", nullable = false),@JoinColumn(name = "commit_id", referencedColumnName = "commit_id", nullable = false),@JoinColumn(name = "type", referencedColumnName = "type", nullable = false),@JoinColumn(name = "signature", referencedColumnName = "signature", nullable = false)})
    @MapKeyColumn(name="impacted_refactoring_key")
    @Column(name="impacted_refactoring_value", columnDefinition="TEXT")
    public Map<Integer, String> impactedRefactorings;//lineNo, refactorings


    public MethodDefinition4DB(){
    }


}
