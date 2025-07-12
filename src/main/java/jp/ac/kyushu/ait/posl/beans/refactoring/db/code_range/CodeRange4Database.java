package jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range;

import gr.uom.java.xmi.diff.CodeRange;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
/**
 * This class gives the location information of refactoring.
 * This class will be extended by LeftCodeRange4Database
 * This class is used to store/get data from Database (hibernate)
 */
@MappedSuperclass
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class CodeRange4Database implements Serializable {
    /**
     * The first line of the refactoring
     */
    @Column(name = "start_")
    public int start;
    /**
     * The last line of the refactoring
     */
    @Column(name = "end_")
    public int end;
    /**
     * The file path that the refactoring happens
     */
    @Column(name = "file_path")
    public String filepath;

    /**
     * This constructor copy the contents of CodeRange given by RefactoringMiner to this class
     * @param cr
     */
    public CodeRange4Database(CodeRange cr){
        this.start = cr.getStartLine();
        this.end = cr.getEndLine();
        this.filepath = cr.getFilePath();
    }
    public CodeRange4Database(){}//For Hibernate

}
