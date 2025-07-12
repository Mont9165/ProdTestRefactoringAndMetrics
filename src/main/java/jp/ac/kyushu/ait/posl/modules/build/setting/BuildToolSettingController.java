package jp.ac.kyushu.ait.posl.modules.build.setting;

import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup.SetElementOfPom;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import jp.ac.kyushu.ait.posl.modules.build.commands.BuildCommands;
import jp.ac.kyushu.ait.posl.utils.exception.*;

import java.util.Map;

public interface BuildToolSettingController {
    MyLogger logger = MyLogger.getInstance();

    /**
     * check out the target revision
     * @param commitId
     */
    void checkout(String commitId) throws NoParentsException;
    /**
     * check out the parent revision
     * @param commitId
     */
    void checkout(String commitId, boolean parent, boolean ignoreParents);
    void checkout(String commitId, boolean ignoreParents) throws NoParentsException;
    /**
     * get test code directory path from root
     * @return
     */
    String getTestDir();
    /**
     * get test code directory path from root (if the argument is true)
     * @return
     */
    String getTestDir(boolean absolutePath);
    /**
     * get source code directory path from root
     * @return
     */
    String getSrcDir();
    /**
     * get source code directory path from root (if the argument is true)
     * @param absolutePath
     * @return
     */
    String getSrcDir(boolean absolutePath);
    /**
     * read pom file
     * @throws NoTargetBuildFileException
     */
    void readBuildFile() throws NoTargetBuildFileException, JavaVersionTooOldException, JUnitNotFoundException, JUnitVersionUnsupportedException;
    /**
     * get target directory path from root
     * @return
     */
    String getTargetDir();
    /**
     * get target directory path from root (if the argument is true)
     * @return
     */
    String getTargetDir(boolean absolutePath);

    /**
     * get the output by surefire
     * @return
     */
    String getSureFireOutputDir();

    /**
     * get commit id using GitController
     * @return
     */
    String getCommitId();

    /**
     * get directory to store JUnit outputs
     * @return
     */
    String getXMLStoreDir();


    /**
     * get home directory
     * @return
     */
    String getHomeDir();
    /**
     * get git repository directory
     * @return
     */
    String getRepositoryDir();
    /**
     * get Commits provided by GitController
     * @return
     */
    Commit getCommit();
    /**
     * insert plugins
     * @param dynamicAnalyzer
     * @throws NoSureFireException
     */
    void setupExtraPom(SetElementOfPom dynamicAnalyzer) throws NoSureFireException;

    /**
     * get parent commit id
     * @return
     * @throws NoParentsException
     */
    String getParentCommitId() throws NoParentsException;

    String getProject();
    SettingManager getSettingManager();
    default Map<String, BuildToolSettingController> getModules(){
        return null;
    };

    BuildCommands getBuildCommander() throws InappropriateEnvironmentException;

    boolean hasModule();
    boolean isModuleWorker();

    String getModuleName();
    String getJunitVersion();
    String getJavaVersion();

    void writeSettingFile();
    GitController getGitController();

    boolean isExecutable();

 }
