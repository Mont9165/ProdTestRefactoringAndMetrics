package jp.ac.kyushu.ait.posl.beans.build;

import org.apache.maven.model.Parent;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "buildfile", schema = "buildfile")
public class BuildFile implements Serializable {
    @Id
    @Column(name = "project")
    public String project;

    @Id
    @Column(name = "commit_id")
    public String commitId;


    @OneToMany(cascade = CascadeType.ALL, fetch= FetchType.EAGER)
    @JoinColumns({ @JoinColumn(name = "project",updatable=false, nullable=false),
            @JoinColumn(name = "commit_id",updatable=false,nullable=false) })
    List<Module> modules;//This includes top directory

    @Column(name = "is_no_build_file")
    boolean isNoBuildFile;
    @Column(name = "artifact_id")
    public String artifact;

    @Column(name = "group_id")
    public String group;

    @Column(name = "version")
    public String version;

    @Column(name = "has_maven")
    public boolean hasMaven;
    @Column(name = "has_bazel")
    public boolean hasBazel;
    @Column(name = "has_ant")
    public boolean hasAnt;
    @Column(name = "has_gradle")
    public boolean hasGradle;

    public BuildFile(String project, String commitId){
        this.project = project;
        this.commitId = commitId;
    }

    /**
     * For maven
     * @param parent
     */
    public void setInfo(Parent parent, List<Module> modules){
        this.hasMaven = true;
        this.modules = modules;
        isNoBuildFile = (this.modules==null);
        if(parent!=null){
            this.artifact = parent.getArtifactId();
            this.group = parent.getGroupId();
            this.version = parent.getVersion();
        }
    }
    public BuildFile(){
        //For Hibernate
    }


}
