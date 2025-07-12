package jp.ac.kyushu.ait.posl.modules.build.setting;


import jp.ac.kyushu.ait.posl.beans.build.BuildFile;
import jp.ac.kyushu.ait.posl.beans.build.Module;
import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.utils.exception.NoParentsException;
import jp.ac.kyushu.ait.posl.utils.exception.NoTargetBuildFileException;
import jp.ac.kyushu.ait.posl.utils.file.MyPathUtil;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class BuildFileReader {
    MavenSettingController mc;
    String commitId;

    private BuildFile bf;

    public BuildFileReader(SettingManager sm, Commit c){
        //Delete old files
        this(sm, c.commitId);
    }
    public BuildFileReader(SettingManager sm, String commitId){
        //Delete old files
        this.commitId = commitId;
        mc = new MavenSettingController(new GitController(sm, "/dependency/"));
        try {
            mc.checkout(commitId, true);
        } catch (NoParentsException e) {//Should not happen
            throw new RuntimeException(e);
        }
        bf = new BuildFile(sm.getProject().name, commitId);
    }

    public BuildFile read() {
        //Maven
        String path = MyPathUtil.join(mc.getRepositoryDir(), "pom.xml");
        if(Files.exists(Path.of(path))){
            readMaven();
        }
        //Bazel
        path = MyPathUtil.join(mc.getRepositoryDir(), "BUILD.bazel");
        if(Files.exists(Path.of(path))){
            readBazel();
        }
        //GRADLE
        String pattern1 = MyPathUtil.join(mc.getRepositoryDir(), "build.gradle");//sometimes there is not this file
        String pattern2 = MyPathUtil.join(mc.getRepositoryDir(), "gradlew");
        if(Files.exists(Path.of(pattern1))||Files.exists(Path.of(pattern2))){
            readGradle();
        }
        //ANT
        path = MyPathUtil.join(mc.getRepositoryDir(), "build.xml");
        if(Files.exists(Path.of(path))){
            readAnt();
        }
        return bf;
    }

    private void readMaven() {
        List<Module> modules = new ArrayList<>();
        try {
            mc.checkout(this.commitId, true);
            mc.readBuildFile();
            setModulesRecursively(modules, mc, null);
        }catch (NoTargetBuildFileException|NoParentsException e){
            //OK
        }
        bf.setInfo(mc.pomModel.getParent(), modules);
    }
    private void readBazel(){
        //TODO read file
//        bf.setInfo(null);
        bf.hasBazel=true;
    }
    private void readGradle(){
        //TODO read file
//        bf.setInfo(null);
        bf.hasGradle=true;
    }
    private void readAnt(){
        //TODO read file
//        bf.setInfo(null);
        bf.hasAnt=true;
    }
    private void setModulesRecursively(List<Module> modules, MavenSettingController mc, MavenSettingController parentMc) {
        String pomFileLocation = identifyPomLocation(mc);
        String parentPomFileLocation = identifyPomLocation(parentMc);
        Module module = new Module(pomFileLocation, parentPomFileLocation, mc.getDependencies(), mc.getDependencyManagement());
        modules.add(module);
        if(!mc.hasModule()) return;
        for(BuildToolSettingController childMc: mc.getModules().values()){
            setModulesRecursively(modules, (MavenSettingController) childMc, mc);
        }
    }

    private String identifyPomLocation(MavenSettingController mc) {
        if(mc==null){
            return null;
        }
        String absolutePath = mc.pomFileAbstractPath;
        return absolutePath.split("/"+mc.getProject()+"/", 2)[1];
    }


}
