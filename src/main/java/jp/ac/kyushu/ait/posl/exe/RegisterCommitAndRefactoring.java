package jp.ac.kyushu.ait.posl.exe;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.beans.refactoring.csv.RefactoringForCSV;
import jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range.RefactoringForDatabase;
import jp.ac.kyushu.ait.posl.beans.run.Registry;
import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.beans.source.db.MethodDefinition4DB;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.modules.refactoring.detect.RefactoringMinerController;
import jp.ac.kyushu.ait.posl.modules.refactoring.trace.RenameSpecifier;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;
import jp.ac.kyushu.ait.posl.modules.source.structure.StructureScanner;
import jp.ac.kyushu.ait.posl.utils.db.Dao;
import jp.ac.kyushu.ait.posl.utils.exception.NoParentsException;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import jp.ac.kyushu.ait.posl.utils.source.ChangeFlagger;
import org.refactoringminer.api.Refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RegisterCommitAndRefactoring {
    static MyLogger logger = MyLogger.getInstance();
    static Dao<Commit> commitDao = new Dao<>(new Commit[0]);
    static Dao<RefactoringForDatabase> refDao = new Dao<>(new RefactoringForDatabase[0]);
    static Dao<MethodDefinition4DB> methodDao = new Dao<>();


    public static void main(String[] args) throws NoParentsException {
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        String branch = sm.getProject().branch;
        List<Commit> commits = gitX.getAllCommits(branch);
        store(sm, gitX, commits);
    }

    public static void store(SettingManager sm, GitController gitX, List<Commit> commits) throws NoParentsException {
        commitDao.init();
        refDao.init();
        methodDao.init();

        for (Commit commit : commits) {
            registerCommit(commit, commit.project);
            List<Refactoring> refactoringResults = RefactoringMinerController.getRefactoringInstanceAtCommit(gitX.getRepo(), commit.commitId);
            registerMethod(gitX, commit, refactoringResults);

            for (Refactoring rf : refactoringResults) {
                if (!commit.isMergeCommit()) {   // ignore merge commits
                    registerRefactoring(sm, rf, commit.commitId);
                }
            }
        }
        commitDao.close();
        refDao.close();
        methodDao.close();
        logger.info("********Complete the program********");
    }

    public static void registerCommit(Commit commit, String project) {
        commit.project = project;
        commitDao.insert(commit);
    }

    public static void registerRefactoring(SettingManager sm, Refactoring rf, String commitId) {
        RefactoringForDatabase rfd = new RefactoringForDatabase(rf, commitId, sm.getProject().name);
        refDao.insert(rfd);
    }

    private static void registerMethod(GitController gc, Commit commit, List<Refactoring> refactoringResults) throws NoParentsException {
        MavenSettingController maven = new MavenSettingController(gc);

        //X
        maven.checkout(commit.commitId, true);
        StructureScanner analyzerX = new StructureScanner(maven);
        Structure structureX = analyzerX.scan();
        ChangeFlagger.flagChange(commit, structureX, false);
        List<MethodDefinition4DB> methodsX = new ArrayList<>();

        try {
            //X_1
            maven.checkout(commit.parentCommitIds.get(0), true);
            StructureScanner analyzerX_1 = new StructureScanner(maven);
            Structure structureX_1 = analyzerX_1.scan();
            ChangeFlagger.flagChange(commit, structureX_1, true);

            RenameSpecifier.linkMethods(structureX_1, structureX, refactoringResults);
            for (MethodDefinition md: structureX.getAllMethods()){
                methodsX.add(MethodDefinitionStore.transform(commit.project, commit.commitId, "X", md));
            }
            methodDao.insert(methodsX);

        }catch (IndexOutOfBoundsException npe){
            for (MethodDefinition md: structureX.getAllMethods()){
                methodsX.add(MethodDefinitionStore.transform(commit.project, commit.commitId, "X", md));
            }
            methodDao.insert(methodsX);
        }

    }
}
