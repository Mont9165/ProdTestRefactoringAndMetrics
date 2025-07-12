package jp.ac.kyushu.ait.posl.exe.not_use;

//import com.github.mauricioaniche.ck.util.FileUtils;
import com.github.mauricioaniche.ck.util.FileUtils;
import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.utils.exception.NoParentsException;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;

import java.io.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static jp.ac.kyushu.ait.posl.modules.CK.CkController.executeCkCommand;
import static jp.ac.kyushu.ait.posl.modules.Readability.ReadabilityController.executeReadabilityCommand;
import static jp.ac.kyushu.ait.posl.modules.Readability.ReadabilityController.writeFileMetricsToCSV;

public class CollectCkAndReadabilityPerCommits {

    public static void main(String[] args) throws NoParentsException {
        SettingManager sm = new SettingManager(args);
        GitController gitX = new GitController(sm, "/x/");
        String branch = sm.getProject().branch;
        List<Commit> commits = gitX.getAllCommits(branch);
        processCommits(sm, gitX, commits);
    }

    private static void processCommits(SettingManager sm, GitController gitX, List<Commit> commits) {
        for (Commit commit : commits) {
            try {
                gitX.checkout(commit.commitId);
                String projectDirPath = "repos/x/" + sm.getProject().name;
                storeCommitToCSV(commit);
                calculateCK(sm, commit, projectDirPath);
//                calculateReadability(sm, commit, projectDirPath);
            } catch (Exception e) {
                System.err.println("Error processing commit " + commit.commitId + ": " + e.getMessage());
            }
        }
    }

    private static void storeCommitToCSV(Commit commit) {
        String outputDirPath = "output/commit/" + commit.project + "/";
        createOutputDir(outputDirPath);
        String csvFilePath = outputDirPath + "commit.csv";
        boolean fileExists = new File(csvFilePath).exists();
        List<String> newFiles = commit.changedFileList.stream().map(changedFile -> changedFile.newPath).toList();
        List<String> oldFiles = commit.changedFileList.stream().map(changedFile -> changedFile.oldPath).toList();

        try (FileWriter csvWriter = new FileWriter(csvFilePath, true)) {
            // ファイルが存在しない場合、ヘッダー行を追加
            if (!fileExists) {
                csvWriter.append("Project,CommitId,oldFileList,newFileList,CommitterEmail,AddedLines,DeletedLines,CommitComment\n");
            }

            csvWriter.append(commit.project).append(",");
            csvWriter.append(commit.commitId).append(",");
            csvWriter.append(oldFiles.toString().replaceAll(",", "")).append(",");
            csvWriter.append(newFiles.toString().replaceAll(",", "")).append(",");
            csvWriter.append(commit.committerEmail).append(",");
            csvWriter.append(commit.addedLines.toString()).append(",");
            csvWriter.append(commit.deletedLines.toString()).append(",");
            csvWriter.append(commit.commitComment.replaceAll(",", "")).append(",");
            csvWriter.append("\n");

            csvWriter.flush();
        } catch (IOException e) {
            System.err.println("Error writing commit to CSV: " + e.getMessage());
        }
    }

    private static void calculateReadability(SettingManager sm, Commit commit, String projectDirPath) {
        String jarPath = "lib/readability/rsm.jar";
        String outputDirPath = "output/readability/" + sm.getProject().name + "/" + commit.commitId + "/";
        String[] javaFiles = FileUtils.getAllJavaFiles(projectDirPath);
        Map<String, Map<String, String> > metrics = new HashMap<>();

        createOutputDir(outputDirPath);
        for (String javaFile : javaFiles) {
            metrics.put(javaFile, executeReadabilityCommand(jarPath, javaFile, commit.commitId));
        }
        writeFileMetricsToCSV(metrics, outputDirPath + "readability.csv");
    }

    private static void calculateCK(SettingManager sm, Commit commit, String projectDirPath) {
        String jarPath = "lib/ck/ck-0.7.1-SNAPSHOT-jar-with-dependencies.jar";
        String outputDirPath = "output/ck/" + sm.getProject().name + "/" + commit.commitId + "/";

        createOutputDir(outputDirPath);
        executeCkCommand(jarPath, projectDirPath, outputDirPath);
    }

    private static void createOutputDir(String outputDirPath) {
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }
}