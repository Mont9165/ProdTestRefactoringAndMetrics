package jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range;

import javax.persistence.*;

import org.refactoringminer.api.Refactoring;
import gr.uom.java.xmi.diff.CodeRange;

import org.refactoringminer.api.RefactoringType;

import java.io.Serializable;
import java.util.*;
/**
 * This class is used to store/get data from Database (hibernate)
 */
@Entity
@Table(name = "refactoring", schema = "refactoring")
public class RefactoringForDatabase implements Serializable{

	@Id
    @Column(name = "commit_id")
	public String commitId;

	/**
	 * Project name
	 */
	@Id
	@Column(name = "project")
	public String project;

	/**
	 * this hash is generated based on the contents
	 */
	@Id
	@Column(name = "hash")
	public int hash;

	@Column(name = "Refactoring_type")
	@Enumerated(EnumType.STRING)
	public RefactoringType RefactoringType;
	/**
	 * Changes in the previous file
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name = "commit_id", referencedColumnName = "commit_id", updatable=false,nullable=false),
		@JoinColumn(name = "project", referencedColumnName = "project", updatable=false, nullable=false),
		@JoinColumn(name = "hash", referencedColumnName = "hash", updatable=false,nullable=false) 	}
	)
	public List<LeftCodeRange4Database> leftside = new ArrayList<>();
	/**
	 * Changes in the new file
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name = "commit_id", referencedColumnName = "commit_id", updatable=false,nullable=false),
		@JoinColumn(name = "project", referencedColumnName = "project", updatable=false, nullable=false),
		@JoinColumn(name = "hash", referencedColumnName = "hash", updatable=false,nullable=false) 	}
	)
	public List<RightCodeRange4Database> rightside = new ArrayList<>();


	public RefactoringForDatabase(Refactoring rf, String commitId, String project){
		this.commitId = commitId;
		this.project = project;
		this.RefactoringType = rf.getRefactoringType();
		this.hash = this.generateHash(rf);
		for(CodeRange cr : rf.leftSide()){
			LeftCodeRange4Database crd = new LeftCodeRange4Database(cr);
			this.leftside.add(crd);
		}
		 for(CodeRange cr : rf.rightSide()){
		 	RightCodeRange4Database crd = new RightCodeRange4Database(cr);
		 	this.rightside.add(crd);
		 }
	}

	private int generateHash(Refactoring rf) {
		int result = 17;
		final int prime = 31; // 奇数の素数
		result = prime * result + commitId.hashCode();
		result = prime * result + project.hashCode();
		result = prime * result + rf.getRefactoringType().hashCode();
		result = prime * result + rf.hashCode();
		return result;
	}

	public RefactoringForDatabase(){
	}

	public int hashCode(){
		return hash;
	}

}