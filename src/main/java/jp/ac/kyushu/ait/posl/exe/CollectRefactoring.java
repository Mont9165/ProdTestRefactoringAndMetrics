package jp.ac.kyushu.ait.posl.exe;

import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.beans.refactoring.csv.RefactoringForCSV;
import jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range.RefactoringForDatabase;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.modules.refactoring.detect.RefactoringMinerController;
import jp.ac.kyushu.ait.posl.utils.exception.NoParentsException;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import org.refactoringminer.api.Refactoring;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class CollectRefactoring {
    public static void main(String[] args) throws NoParentsException {
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        String branch = sm.getProject().branch;
        List<Commit> commits = gitX.getAllCommits(branch);
        store(sm, gitX, commits);
    }

    public static void store(SettingManager sm, GitController gitX, List<Commit> commits) throws NoParentsException {
        for (Commit commit : commits) {
            List<Refactoring> refactoringResults = RefactoringMinerController.getRefactoringInstanceAtCommit(gitX.getRepo(), commit.commitId);
            System.out.println("Commit: " + commit.commitId + " Refactoring: " + refactoringResults.size());
            for (Refactoring rf : refactoringResults) {
                if (!commit.isMergeCommit()) {   // ignore merge commits
                    registerRefactoring(sm, rf, commit.commitId);
                }
            }
        }
    }


    public static void registerRefactoring(SettingManager sm, Refactoring rf, String commitId) {
        RefactoringForDatabase rfd = new RefactoringForDatabase(rf, commitId, sm.getProject().name);
        RefactoringForCSV rfc = new RefactoringForCSV(rfd);
        String fileDirPath = "output/refactoring/" + sm.getProject().name + "/";
        createOutputDir(fileDirPath);
        String filePath = fileDirPath + "refactoring.csv";
        try {
            rfc.saveToCSV(filePath);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private static void createOutputDir(String outputDirPath) {
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

}
