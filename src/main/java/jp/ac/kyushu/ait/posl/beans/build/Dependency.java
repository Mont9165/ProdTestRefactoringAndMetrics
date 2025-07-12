package jp.ac.kyushu.ait.posl.beans.build;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "dependency", schema = "buildfile", uniqueConstraints = { @UniqueConstraint(columnNames = { "dependency_id"}) })
public class Dependency implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="dependency_id", columnDefinition="bigint")
    public long dependencyId;

    @Column(name = "artifact_id")
    public String artifact;

    @Column(name = "group_id")
    public String group;

    @Column(name = "version")
    public String version;
    @Column(name = "scope")
    public String scope;

    public Dependency(org.apache.maven.model.Dependency d) {
        this.artifact = d.getArtifactId();
        this.group = d.getGroupId();
        this.version = d.getVersion();
        this.scope = d.getScope();
    }
    public Dependency(){
        //For Hibernate
    }
}
