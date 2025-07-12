package jp.ac.kyushu.ait.posl.modules.build.setting.maven;

import jp.ac.kyushu.ait.posl.beans.build.Dependency;
import jp.ac.kyushu.ait.posl.beans.build.DependencyManagement;
import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.modules.build.commands.BuildCommands;
import jp.ac.kyushu.ait.posl.modules.build.commands.MavenCommands;
import jp.ac.kyushu.ait.posl.modules.build.commands.MavenModuleCommands;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.extractor.MavenJavaVersionExtractor;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.extractor.MavenSurefireExtractor;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.SetElementOfPom;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.utils.exception.InappropriateEnvironmentException;
import jp.ac.kyushu.ait.posl.utils.exception.NoParentsException;
import jp.ac.kyushu.ait.posl.utils.exception.NoSureFireException;
import jp.ac.kyushu.ait.posl.utils.exception.NoTargetBuildFileException;
import jp.ac.kyushu.ait.posl.utils.file.MyFileNameUtils;
import jp.ac.kyushu.ait.posl.utils.file.MyPathUtil;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import jp.ac.kyushu.ait.posl.utils.xml.MyPropertiesUtil;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static jp.ac.kyushu.ait.posl.utils.xml.MyPropertiesUtil.replaceDirectoryName;

public class MavenSettingController implements BuildToolSettingController {

//    public static final String MAVEN_HOME="/usr/local/Cellar/maven/3.6.3_1/";
    private static final String DEFAULT_JAVA_VERSION = "13";
    private static final String DEFAULT_TARGET = "target";
    private static final String DEFAULT_SRC = "src/main/java";
    private static final String DEFAULT_TEST = "src/test/java";
    public GitController gc;
    public String pomFileAbstractPath;

    public Model pomModel;

    public boolean topDirectory;
    //Info in Maven
    public String moduleName;//if this is top directory, it's null
    public Map<String, BuildToolSettingController> modules;
    private Properties properties;
    private Properties inherentProperties;//Don't use this directory.
    private String packageType;
    private String jUnitVersion;
    private String javaVersion;
    private boolean skipTestSurefire;
    private String surefireVersion;
    private String srcDir;
    private String testDir;
    private String targetDir;

    public MavenSettingController(GitController gc) {
        this.gc = gc;
        this.pomFileAbstractPath = MyPathUtil.join(this.getRepositoryDir(), this.getModuleDir(), "pom.xml");
        topDirectory=true;
        this.inherentProperties = null;
    }


    private MavenSettingController(String specifiedModuleName, MavenSettingController parent) {
        this.gc = parent.gc;
        this.inherentProperties = parent.properties;
        this.javaVersion = parent.javaVersion;
        this.jUnitVersion = parent.jUnitVersion;
        this.srcDir = parent.srcDir;
        this.testDir = parent.testDir;
        this.targetDir = parent.targetDir;
        this.moduleName = specifiedModuleName;
        this.pomFileAbstractPath = this.getPomFileLocation(specifiedModuleName, parent.moduleName);
        this.surefireVersion = parent.surefireVersion;
        this.skipTestSurefire = parent.skipTestSurefire;
    }

    public MavenSettingController() {
        //Don't Use
    }

    private String getPomFileLocation(String specifiedModuleName, String parentModuleName) {
        String pomFileName = "pom.xml";
        if (specifiedModuleName!=null){
            if (specifiedModuleName.endsWith(".xml")){//e.g., pom-main.xml
                pomFileName = MyPathUtil.getFileName(specifiedModuleName);;
                if (specifiedModuleName.contains("/")){//e.g., extern/pom.xml
                    this.setModuleName(parentModuleName, MyPathUtil.getDirectoryName(specifiedModuleName));
                }else{//e.g., pom-main.xml
                    this.moduleName = parentModuleName;
                }
            }else{
                this.setModuleName(parentModuleName, specifiedModuleName);
            }
        }
        return MyPathUtil.join(this.getRepositoryDir(), this.getModuleDir(), pomFileName);
    }

    private void setModuleName(String parentModName, String moduleName) {
        if(parentModName == null){
            this.moduleName = moduleName;
        }else{
            this.moduleName = MyPathUtil.join(parentModName, moduleName);
        }
    }
    public String getModuleName(){
        if(this.moduleName==null){
            return null;
        }else{
            return MyFileNameUtils.getDirectoryName(this.moduleName);
        }
    }

    @Override
    public String getJunitVersion() {
        return this.jUnitVersion;
    }

    @Override
    public String getJavaVersion() {
        return this.javaVersion;
    }


    /**
     * Write over pom file
     */
    public void writeSettingFile() {
        try {
            new MavenXpp3Writer().write(Files.newBufferedWriter(new File(this.pomFileAbstractPath).toPath()), pomModel);
        } catch (IOException e) {
            logger.error(e);
            throw new AssertionError();
        }
    }

    @Override
    public GitController getGitController() {
        return gc;
    }


    /**
     * insert plugins
     * @param setter
     * @throws NoSureFireException
     */
    @Override
    public void setupExtraPom(SetElementOfPom setter) throws NoSureFireException {
        this.pomModel = setter.setUpPom(this.pomModel, this.getSettingManager().getProject());
    }

    @Override
    public String getParentCommitId() throws NoParentsException {
        return this.gc.getParentCommitId();
    }

    @Override
    public String getProject() {
        return this.gc.project;
    }

    @Override
    public SettingManager getSettingManager() {
        return gc.sm;
    }

    /**
     * get the output by surefire
     * @return
     */
    public String getSureFireOutputDir() {
       return MyPathUtil.join(this.getTargetDir(), "surefire-reports");
    }

    /**
     * get directory to store JUnit outputs
     * @return
     */
    public String getXMLStoreDir() {
        return MyPathUtil.join(gc.sm.getOutputDir(), "xml");
    }
    public String getHomeDir(){
        return MyPathUtil.join(this.gc.getRepoDir(), this.getModuleDir());
    }

    /**
     * return repository directory. if this maven file is children, returns module directory.
     * @return
     */
    @Override
    public String getRepositoryDir() {
        return gc.getRepoDir();
    }

    @Override
    public Commit getCommit() {
        try{
            return this.gc.getCommit();
        }catch (NoParentsException pe){
            System.err.println("NoParentsException");
            return null;
        }

    }

    public String getCommitId() {
        return gc.commitId;
    }

    /**
     * read pom file
     * @throws NoTargetBuildFileException
     */
    @Override
    public void readBuildFile() throws NoTargetBuildFileException {
        try {
            pomModel = new MavenXpp3Reader().read(new FileReader(this.pomFileAbstractPath));

            this.scanSettings();
            switch (this.packageType){
                case "pom":
                    modules = new LinkedHashMap<>();
                    for (String modStr: pomModel.getModules()){
                        MavenSettingController mc = new MavenSettingController(modStr, this);
                        mc.readBuildFile();
                        //TODO: only use first one
                        //TODO: to put jar
                        //FIXME: Child's module
                        modules.put(modStr, mc);
                    }
                case "jar":
                case "war":
                case "ear":
                case "ejb":
                default:
                    break;
            }
        } catch (IOException | XmlPullParserException e) {
            System.err.println(this.pomFileAbstractPath);
            throw new NoTargetBuildFileException();
        }
    }




    private void scanSettings(){
        this.readProperties();
        String tmp = this.getSrcDirSetting();
        if (tmp != null)
            this.srcDir = tmp;
        tmp = this.getTestDirSetting();
        if (tmp != null)
            this.testDir = tmp;
        tmp = this.getTargetDirSetting();
        if (tmp != null)
            this.targetDir = tmp;

        this.packageType = pomModel.getPackaging();
        //Java
        tmp = MavenJavaVersionExtractor.getJavaVersion(pomModel);
        if(tmp!=null){
            this.javaVersion = tmp;
        }
        //JUnit
        tmp = MavenJavaVersionExtractor.getJUnitVersion(pomModel, properties);
        if(tmp!=null){
            this.jUnitVersion = tmp;
        }
        //Surefire
        tmp = MavenSurefireExtractor.getSurefireVersion(pomModel);
        if (tmp != null){
            this.surefireVersion = tmp;
        }
        Boolean b = MavenSurefireExtractor.isSkipSurefire(pomModel);
        if(b != null){
            this.skipTestSurefire = b;
        }

    }

    private void readProperties() {
        if(this.inherentProperties==null){
            properties = pomModel.getProperties();
            if (pomModel.getArtifactId()!=null)
                properties.put("project.artifactId", pomModel.getArtifactId());
            if (pomModel.getGroupId()!=null)
                properties.put("project.groupId", pomModel.getGroupId());
            if (pomModel.getVersion()!=null)
                properties.put("project.version", pomModel.getVersion());
        }else{
            properties = (Properties) inherentProperties.clone();
            Properties prop = pomModel.getProperties();
            if (prop==null) return;
            if (pomModel.getArtifactId()!=null)
                properties.put("project.artifactId", pomModel.getArtifactId());
            if (pomModel.getGroupId()!=null)
                properties.put("project.groupId", pomModel.getGroupId());
            if (pomModel.getVersion()!=null)
                properties.put("project.version", pomModel.getVersion());
            properties.putAll(prop);
        }
    }

    /**
     * check out repository
     * @param commitId
     */
    @Override
    public void checkout(String commitId) throws NoParentsException {
        this.checkout(commitId, false);
    }

    /**
     * check out repository
     * @param commitId
     */
    @Override
    public void checkout(String commitId, boolean ignoreParents) throws NoParentsException {
        this.gc.checkout(commitId, ignoreParents);
    }

    /**
     * check out repository
     * @param commitId
     * @param parent
     */
    @Override
    public void checkout(String commitId, boolean parent, boolean ignoreParents) {
        try{
            this.gc.checkout(commitId, parent, ignoreParents);
        }catch (NoParentsException pe){
            System.err.println("NoParentsException");
        }
    }

    /**
     * get target directory path including class dir from root
     * @return
     */
    public String getTargetDir(){//${project.build.directory}
        return this.getTargetDir(true);
    }
    /**
     * get target directory path from root (if the argument is true)
     * @return
     */
    public String getTargetDir(boolean absolute){//${project.build.directory}
        String baseDir = "";
        if (absolute){
            baseDir = MyPathUtil.join(this.getRepositoryDir(), this.getModuleDir());
        }
        String dir = this.targetDir;
        if(this.targetDir==null)
            dir = DEFAULT_TARGET;
        return MyPathUtil.join(baseDir, dir);
    }



    /**
     * get source code directory path from root (if the argument is true)
     * @param absolute
     * @return
     */
    public String getSrcDir(boolean absolute){//${project.build.sourceDirectory}
        String baseDir = "";
        if(absolute){
            baseDir = MyPathUtil.join(this.getRepositoryDir(), this.getModuleDir());
        }
        String dir = this.srcDir;
        if(this.srcDir==null)
            dir = DEFAULT_SRC;
        return MyPathUtil.join(baseDir, dir);
    }

    private String getModuleDir() {
        return moduleName;
    }

    /**
     * get source code directory path
     * @return
     */
    public String getSrcDir() {//${project.build.sourceDirectory}
        return this.getSrcDir(true);
    }

    /**
     * get test code directory path from root (if the argument is true)
     * @return
     */
    public String getTestDir(){
        return this.getTestDir(true);
    }

    /**
     * get test code directory path
     * @param absolute
     * @return
     */
    public String getTestDir(boolean absolute){//${project.build.testSourceDirectory}
        String baseDir = "";
        if(absolute){
            baseDir = MyPathUtil.join(this.getRepositoryDir(), this.getModuleDir());
        }
        String dir = this.testDir;
        if(this.testDir==null)
            dir = DEFAULT_TEST;

        return MyPathUtil.join(baseDir, dir);
    }
    private String getDirSetting(String dir){
        if(dir!=null){
            return replaceDirectoryName(properties, dir);
        }
        return null;
    }
    /**
     * get source code directory path according to pom file
     * @return
     */
    private String getSrcDirSetting(){
        try {
            return getDirSetting(pomModel.getBuild().getSourceDirectory());
        }catch (NullPointerException npe){
            return null;// if build is null
        }
    }
    /**
     * get test code directory path according to pom file
     * @return
     */
    private String getTestDirSetting(){
        try {
            return getDirSetting(pomModel.getBuild().getTestSourceDirectory());
        }catch (NullPointerException npe){
            return null;// if build is null
        }
    }
    /**
     * get source code directory path according to pom file
     * @return
     */
    private String getTargetDirSetting(){
        try {
            return getDirSetting(pomModel.getBuild().getDirectory());
        }catch (NullPointerException npe){
            return null;// if build is null
        }
    }


    public Map<String, BuildToolSettingController> getModules(){
        return this.modules;
    }
    public BuildCommands getBuildCommander() throws InappropriateEnvironmentException {
        if (this.getModules()==null){
            return new MavenCommands(this);
        }else{
            return new MavenModuleCommands(this);
        }
    }

    @Override
    public boolean hasModule() {
        return this.getModules() != null;
    }
    @Override
    public boolean isModuleWorker() {
        return this.moduleName != null;
    }
    public void updateJavaVersion() {
    }

    /*********************************************
     * Show Information
     ********************************************/
    public String getArtifactId(){
        return this.pomModel.getArtifactId();
    }
    public String getVersion(){
        return this.pomModel.getVersion();
    }
    public String getGroupId(){
        return this.pomModel.getGroupId();
    }
    public String getPackageType(){return packageType;}
    public boolean isExecutable(){
        if(packageType.equals("jar")){
            return true;
        }
        return false;
    }
    public boolean hasSurefire(){
        return surefireVersion != null;
    }
    public String getSurefireVersion(){
        return surefireVersion;
    }

    public List<Dependency> getDependencies() {
        List<Dependency> list = new ArrayList<>();
        for(org.apache.maven.model.Dependency d: this.pomModel.getDependencies()){
            Dependency dependency = new Dependency(d);
            String versionStr = MyPropertiesUtil.extractValue(properties, dependency.version);
            if(versionStr != null){
                dependency.version = versionStr;
            }
            list.add(dependency);
        }
        return list;
    }

    public List<DependencyManagement> getDependencyManagement() {
        List<DependencyManagement> list = new ArrayList<>();
        org.apache.maven.model.DependencyManagement dm = this.pomModel.getDependencyManagement();
        if(dm==null) return list;
        for(org.apache.maven.model.Dependency d: dm.getDependencies()){
            DependencyManagement dependency = new DependencyManagement(d);
            String versionStr = MyPropertiesUtil.extractValue(properties, dependency.version);
            if(versionStr != null){
                dependency.version = versionStr;
            }
            list.add(dependency);
        }
        return list;
    }
}
