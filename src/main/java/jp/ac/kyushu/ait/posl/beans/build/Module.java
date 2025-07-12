package jp.ac.kyushu.ait.posl.beans.build;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "module", schema = "buildfile", uniqueConstraints = { @UniqueConstraint(columnNames = { "module_id"}) })
public class Module implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="module_id", columnDefinition="bigint")
    public long moduleId;

    @Column(name = "pom_file")
    String pomFile;

    @Column(name = "parent_pom")//If no module or top directory, it's null
    String parentPom;

    @OneToMany(cascade = CascadeType.ALL, fetch= FetchType.LAZY)
    @JoinColumns({ @JoinColumn(name = "moduleId",updatable=false, nullable=false)})
    public List<Dependency> dependencies;

    @OneToMany(cascade = CascadeType.ALL, fetch= FetchType.LAZY)
    @JoinColumns({ @JoinColumn(name = "moduleId",updatable=false, nullable=false)})
    public List<DependencyManagement> dependencyManagement;

    public Module(String pomFile, String parentPom, List<Dependency> dependencies, List<DependencyManagement> dependencyManagement){
        this.pomFile = pomFile;
        this.parentPom = parentPom;
        this.dependencies = dependencies;
        this.dependencyManagement = dependencyManagement;
    }
    public Module(){
        //For Hibernate
    }

}
