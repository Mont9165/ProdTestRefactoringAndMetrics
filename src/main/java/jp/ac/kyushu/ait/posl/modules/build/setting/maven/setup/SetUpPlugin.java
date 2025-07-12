package jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup;

import jp.ac.kyushu.ait.posl.utils.exception.NoSureFireException;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import org.apache.maven.model.*;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.ArrayList;
import java.util.List;


public abstract class SetUpPlugin implements SetElementOfPom{
    public MyLogger logger = MyLogger.getInstance();
    public List<String> surefireList = new ArrayList<String>();
    public String homeDir;
    public String srcDir;
    public String testDir;
    public String targetDir;
    public final String ARTIFACT;
    public final String GROUP_ID;
    public final String VERSION;
    public SetUpPlugin(String artifact, String groupId, String version){
        ARTIFACT = artifact;
        GROUP_ID = groupId;
        VERSION = version;
    }



    protected abstract List<PluginExecution> createExecutions();
    protected abstract void updateExecutions(Plugin plugin);
    protected abstract Xpp3Dom createConf();
    protected abstract void updateConfiguration(Plugin plugin);
//    protected abstract void setInheritanceOptions(Xpp3Dom conf);
    /**
     * Find existing surefire setting. If the repository does not have surefire, we add it.
     * @param model
     * @param project
     * @return
     * @throws NoSureFireException
     */
    public Model setUp(Model model, String project) throws NoSureFireException {
        boolean isExistSurefire=false;
        // find surefire in build element
        isExistSurefire = this.hasPluginInPlugins(model, isExistSurefire);
        isExistSurefire = this.hasPluginInPluginManagement(model, isExistSurefire);
        // find surefire in profile element
        isExistSurefire = this.hasPluginInProfile(model, isExistSurefire);
        //Otherwise, make it
        if(!isExistSurefire){
            this.defaultActionIfNotExist(model);
        }
        return model;
    }
    /**
     * set up default surefire
     * @param model
     */
    protected void defaultActionIfNotExist(Model model) {
        Build b = model.getBuild();
        List<Plugin> list = b.getPlugins();
        if(list==null){
            list = new ArrayList<>();
        }
        Plugin p = new Plugin();
        p.setArtifactId(this.getArtifact());
        p.setGroupId(this.getGroupID());
        p.setVersion(this.getVersion());
        Xpp3Dom c = this.createConf();
        if(c!=null) {
            p.setConfiguration(c);
        }
        List<PluginExecution> pe = this.createExecutions();
        if(pe!=null){
            p.setExecutions(pe);
        }
        list.add(p);
        b.setPlugins(list);
        //TODO: the following actions are needed but redundant for each plugins
        //source directory setting
        String src = this.getDirectory(model.getBuild().getSourceDirectory(), srcCandidates);
        b.setSourceDirectory(src);
        //test directory setting
        String test = this.getDirectory(model.getBuild().getTestSourceDirectory(), testCandidates);
        b.setTestSourceDirectory(test);
    }


    String[] srcCandidates = {"src/main/java/", "src/java/", "src/"};//should order by longer name
    String[] testCandidates = {"src/test/java/", "test/java/", "tests/"};
    private String getDirectory(String sourceDirectory, String[] candidates) {
        if(sourceDirectory!=null){
            return sourceDirectory;
        }
        return candidates[0];
        //TODO: it crushed because no repository dir info
//        for(String c: candidates){
//            File f = new File(c);
//            if(f.exists()){
//                return c;
//            }
//        }
//        throw new RuntimeException();
    }
    /**
     * find sure fire in the Plugin element
     * @param model
     * @param isExist
     * @return
     */
    private boolean hasPluginInPlugins(Model model, boolean isExist) {
        Build b = model.getBuild();
        if(b!=null){
            int originalSize = b.getPlugins().size();
            for(int i=0;i<originalSize;i++){
                isExist = isExist|this.hasPlugin(b, b.getPlugins().get(i));
            }
            model.setBuild(b);
        }else{
            b = new Build();
            model.setBuild(b);
        }
        return isExist;
    }

    /**
     * find sure fire in the PluginManagement element
     * @param model
     * @param isExistSurefire
     * @return
     */
    private boolean hasPluginInPluginManagement(Model model, boolean isExistSurefire) {
        Build b = model.getBuild();
        PluginManagement pm = b.getPluginManagement();
        if(pm!=null){
            int originalSize = b.getPluginManagement().getPlugins().size();
            for(int i=0;i<originalSize;i++){
                isExistSurefire = isExistSurefire|this.hasPlugin(pm, b.getPluginManagement().getPlugins().get(i));
            }
            b.setPluginManagement(pm);
        }
        return isExistSurefire;
    }
    /**
     * find sure fire in the Profile element
     * @param model
     * @param isExist
     * @return
     */
    private boolean hasPluginInProfile(Model model, boolean isExist) {
        for(int i = 0; i<model.getProfiles().size();i++){
            Profile profile = model.getProfiles().get(i);
            if(profile.getBuild() == null) continue;
            int originalSize = profile.getBuild().getPlugins().size();
            for(int j=0; j < originalSize;j++){
                isExist = isExist|this.hasPlugin(profile.getBuild(), profile.getBuild().getPlugins().get(j));
            }
            profile.setBuild(profile.getBuild());
            model.getProfiles().set(i, profile);
        }
        return isExist;
    }


    /**
     * insert into xml file
     * @param b
     * @param plugin
     */
    private boolean hasPlugin(Object b, Plugin plugin) {
        if(plugin.getArtifactId().equals(getArtifact())){//if surefire exists
            Plugin p = this.updatePlugin(plugin);
            return true;
        }else{
            return false;
        }
    }



    public Plugin updatePlugin(Plugin plugin) {
        plugin.setVersion(this.getVersion());
        if(plugin.getConfiguration()==null){//if configure does not exist
            plugin.setConfiguration(this.createConf());
        }else{
            this.updateConfiguration(plugin);
        }
        if(plugin.getExecutions()==null) {
            plugin.setExecutions(this.createExecutions());
        }else {
            this.updateExecutions(plugin);
        }
        return plugin;
    }





    public String getArtifact() {
        return ARTIFACT;
    }

    public String getGroupID() {
        return GROUP_ID;
    }

    public String getVersion() {
        return VERSION;
    }





}
