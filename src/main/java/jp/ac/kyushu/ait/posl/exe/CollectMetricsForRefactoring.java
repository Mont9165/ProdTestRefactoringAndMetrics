package jp.ac.kyushu.ait.posl.exe;

import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range.RefactoringForDatabase;
import jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range.LeftCodeRange4Database;
import jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range.RightCodeRange4Database;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.utils.db.Dao;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static jp.ac.kyushu.ait.posl.modules.CK.CkController.executeCkCommand;
import static jp.ac.kyushu.ait.posl.modules.Readability.ReadabilityController.*;

public class CollectMetricsForRefactoring {

    public static void main(String[] args) {
        // Parse command line arguments
        List<String> settingArgs = new ArrayList<>();
        boolean onlyWithRefactoring = false;
        boolean onlyWithoutRefactoring = false;

        for (String arg : args) {
            if (arg.equals("--with-refactoring")) {
                onlyWithRefactoring = true;
            } else if (arg.equals("--without-refactoring")) {
                onlyWithoutRefactoring = true;
            } else {
                settingArgs.add(arg);
            }
        }

        // Initialize SettingManager with filtered arguments
        SettingManager sm = new SettingManager(settingArgs.toArray(new String[0]));

        // Get refactorings from database
        List<RefactoringForDatabase> refactoringList = getRefactoringsFromDB(sm);
        // Get all commits
        List<String> allCommits = getAllCommits(sm);
        
        // Process metrics for commits based on filter
        processMetricsForCommits(sm, refactoringList, allCommits, onlyWithRefactoring, onlyWithoutRefactoring);
    }

    private static List<RefactoringForDatabase> getRefactoringsFromDB(SettingManager sm) {
        Dao<RefactoringForDatabase> refDao = new Dao<>(new RefactoringForDatabase[0]);
        refDao.init();

        try {
            // Set where condition for the project
            refDao.setWhere("project", sm.getProject().name);
            return refDao.select();
        } catch (Exception e) {
            System.err.println("Error reading refactorings from database: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            refDao.close();
        }
    }

    private static List<String> getAllCommits(SettingManager sm) {
        GitController gitX = new GitController(sm, "/x/");
        List<Commit> commits = gitX.getAllCommits(sm.getProject().branch);
        return commits.stream()
                .map(commit -> commit.commitId)
                .collect(Collectors.toList());
    }

    private static void processMetricsForCommits(SettingManager sm, List<RefactoringForDatabase> refactoringList, 
            List<String> allCommits, boolean onlyWithRefactoring, boolean onlyWithoutRefactoring) {
        GitController gitX = new GitController(sm, "/x/");
        Set<String> processedCKCommits = new HashSet<>();
        Map<String, Map<String, Map<String, String>>> readabilityMetrics = new HashMap<>();
        Set<String> commitsWithRefactoring = new HashSet<>();

        // Collect commits with refactoring
        for (RefactoringForDatabase ref : refactoringList) {
            commitsWithRefactoring.add(ref.commitId);
        }

        // Filter commits based on refactoring presence
        List<String> filteredCommits = allCommits.stream()
                .filter(commitId -> {
                    boolean hasRefactoring = commitsWithRefactoring.contains(commitId);
                    if (onlyWithRefactoring) {
                        return hasRefactoring;
                    } else if (onlyWithoutRefactoring) {
                        return !hasRefactoring;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Process filtered commits
        for (String commitId : filteredCommits) {
            try {
                gitX.checkout(commitId);
                String projectDir = "repos/x/" + sm.getProject().name;

                // Calculate CK metrics
                if (!processedCKCommits.contains(commitId)) {
                    calculateCKMetrics(sm, commitId, projectDir);
                    processedCKCommits.add(commitId);
                }

                // Get all Java files in the project
                List<String> javaFiles = getAllJavaFiles(projectDir);
                
                // Calculate readability metrics
                processReadabilityMetrics(sm, commitId, javaFiles, "lib/readability/rsm.jar", readabilityMetrics);

                // Write metrics to CSV with refactoring flag
                boolean hasRefactoring = commitsWithRefactoring.contains(commitId);
                writeMetricsToCSV(sm, commitId, hasRefactoring);

            } catch (Exception e) {
                System.err.println("Error processing commit " + commitId + ": " + e.getMessage());
            }
        }
    }

    private static List<String> getAllJavaFiles(String projectDir) {
        List<String> javaFiles = new ArrayList<>();
        File dir = new File(projectDir);
        if (dir.exists() && dir.isDirectory()) {
            findJavaFiles(dir, javaFiles);
        }
        return javaFiles;
    }

    private static void findJavaFiles(File dir, List<String> javaFiles) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJavaFiles(file, javaFiles);
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file.getAbsolutePath());
                }
            }
        }
    }

    private static void writeMetricsToCSV(SettingManager sm, String commitId, boolean hasRefactoring) {
        String outputDir = "output/metrics/" + sm.getProject().name + "/" + commitId + "/";
        createOutputDir(outputDir);
        String outputFile = outputDir + "metrics.csv";
        String fileMetricsOutputFile = outputDir + "file_metrics.csv";

        try (FileWriter writer = new FileWriter(outputFile, true);
             FileWriter fileWriter = new FileWriter(fileMetricsOutputFile, true)) {
            
            // Write header if file is empty
            if (new File(outputFile).length() == 0) {
                writer.write("commit_id,has_refactoring,ck_metrics,avg_readability,min_readability,max_readability,file_count\n");
            }
            if (new File(fileMetricsOutputFile).length() == 0) {
                fileWriter.write("commit_id,has_refactoring,file_path,readability_score\n");
            }

            // Read CK metrics
            String ckMetricsFile = "output/ck/" + sm.getProject().name + "/" + commitId + "/class.csv";
            String ckMetrics = readCKMetrics(ckMetricsFile);

            // Read and aggregate readability metrics
            String readabilityMetricsFile = "output/readability/" + sm.getProject().name + "/" + commitId + "/readability.csv";
            ReadabilityMetrics readabilityMetrics = aggregateReadabilityMetrics(readabilityMetricsFile, fileWriter, commitId, hasRefactoring);

            // Write metrics to CSV
            writer.write(String.format("%s,%b,%s,%.4f,%.4f,%.4f,%d\n",
                    commitId,
                    hasRefactoring,
                    ckMetrics,
                    readabilityMetrics.average,
                    readabilityMetrics.min,
                    readabilityMetrics.max,
                    readabilityMetrics.fileCount));
        } catch (IOException e) {
            System.err.println("Error writing metrics to CSV: " + e.getMessage());
        }
    }

    private static class ReadabilityMetrics {
        double average;
        double min;
        double max;
        int fileCount;

        ReadabilityMetrics(double average, double min, double max, int fileCount) {
            this.average = average;
            this.min = min;
            this.max = max;
            this.fileCount = fileCount;
        }
    }

    private static ReadabilityMetrics aggregateReadabilityMetrics(String metricsFilePath, FileWriter fileWriter, String commitId, boolean hasRefactoring) {
        List<Double> scores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(metricsFilePath))) {
            // Skip header
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        String filePath = parts[0].trim();
                        double score = Double.parseDouble(parts[1].trim());
                        scores.add(score);
                        
                        // Write file metrics to CSV
                        fileWriter.write(String.format("%s,%b,%s,%.4f\n",
                                commitId,
                                hasRefactoring,
                                filePath,
                                score));
                    } catch (NumberFormatException e) {
                        // Skip invalid scores
                    }
                }
            }
        } catch (IOException e) {
            return new ReadabilityMetrics(0.0, 0.0, 0.0, 0);
        }

        if (scores.isEmpty()) {
            return new ReadabilityMetrics(0.0, 0.0, 0.0, 0);
        }

        double sum = scores.stream().mapToDouble(Double::doubleValue).sum();
        double min = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double average = sum / scores.size();

        return new ReadabilityMetrics(average, min, max, scores.size());
    }

    private static String readCKMetrics(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header
            reader.readLine();
            // Read first line of metrics
            String line = reader.readLine();
            return line != null ? line : "";
        } catch (IOException e) {
            return "";
        }
    }

    private static void calculateCKMetrics(SettingManager sm, String commitId, String projectDir) {
        try {
            String ckJarPath = "lib/ck/ck-0.7.1-SNAPSHOT-jar-with-dependencies.jar";
            String ckOutputDir = "output/ck/" + sm.getProject().name + "/" + commitId + "/";
            createOutputDir(ckOutputDir);
            executeCkCommand(ckJarPath, projectDir, ckOutputDir);
        } catch (Exception e) {
            System.err.println("Error calculating CK metrics for " + commitId + ": " + e.getMessage());
        }
    }

    private static void processReadabilityMetrics(SettingManager sm, String commitId, List<String> filePaths, String readabilityJarPath, Map<String, Map<String, Map<String, String>>> readabilityMetrics) {
        try {
            if (!readabilityMetrics.containsKey(commitId)) {
                String outputDir = "output/readability/" + sm.getProject().name + "/" + commitId + "/";
                createOutputDir(outputDir);
                String outputCsvPath = outputDir + "readability.csv";
                Map<String, Map<String, String>> metrics = new HashMap<>();

                // Write header to readability CSV
                try (FileWriter writer = new FileWriter(outputCsvPath)) {
                    writer.write("file_path,readability_score\n");
                }

                // Process each file and write results immediately
                for (String filePath : filePaths) {
                    try {
                        // Execute readability command
                        Map<String, String> fileMetrics = executeOnlyReadabilityCommand(readabilityJarPath, filePath, commitId, outputCsvPath);
                        metrics.put(filePath, fileMetrics);

                        // Extract readability score from metrics
                        String score = fileMetrics.get("readability");
                        if (score != null) {
                            // Append to CSV file
                            try (FileWriter writer = new FileWriter(outputCsvPath, true)) {
                                writer.write(String.format("%s,%s\n", filePath, score));
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing file " + filePath + ": " + e.getMessage());
                    }
                }

                readabilityMetrics.put(commitId, metrics);
            }
        } catch (Exception e) {
            System.err.println("Error calculating Readability metrics for " + commitId + ": " + e.getMessage());
        }
    }

    private static void createOutputDir(String outputDirPath) {
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }
}
