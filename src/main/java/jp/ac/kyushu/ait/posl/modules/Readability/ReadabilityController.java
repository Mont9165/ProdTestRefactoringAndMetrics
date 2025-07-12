package jp.ac.kyushu.ait.posl.modules.Readability;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadabilityController {

    /**
     *
     *
     * @param fileMetrics
     * @param outputPath
     */
    public static void writeFileMetricsToCSV(Map<String, Map<String, String>> fileMetrics, String outputPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            List<String> headers = getHeaders(fileMetrics);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader(headers.toArray(new String[0])));

            for (Map.Entry<String, Map<String, String>> fileEntry : fileMetrics.entrySet()) {
                Map<String, String> metrics = fileEntry.getValue();
                Object[] row = new Object[headers.size()];
                row[0] = fileEntry.getKey();

                for (int i = 1; i < headers.size(); i++) {
                    row[i] = metrics.getOrDefault(headers.get(i), "");
                }
                csvPrinter.printRecord(row);
            }
            csvPrinter.flush();

            System.out.println("Metrics written to " + outputPath);

        } catch (IOException e) {
            System.err.println("Error writing metrics to CSV: " + e.getMessage());
        }
    }

    private static List<String> getHeaders(Map<String, Map<String, String>> fileMetrics) {
        List<String> headers = new java.util.ArrayList<>(List.of("Filename"));

        for (Map<String, String> metrics : fileMetrics.values()) {
            for (String metric : metrics.keySet()) {
                if (!headers.contains(metric)) {
                    headers.add(metric);
                }
            }
        }
        return headers;
    }

    public static Map<String, String> executeReadabilityCommand(String jarPath, String javaFile, String commitId) {
        Map<String, String> metrics = new HashMap<>();
        String command = String.format("java -cp %s it.unimol.readability.metric.runnable.ExtractMetrics %s", jarPath, javaFile);

        try {
            Process process = Runtime.getRuntime().exec(command);

            try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                while ((line = stdInput.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        metrics.put(parts[0].trim(), parts[1].trim());
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("Failed to calculate Readability metrics for file " + javaFile + " in commit " + commitId);
                }

            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing readability JAR for file " + javaFile + ": " + e.getMessage());
        }
        return metrics;
    }

    public static Map<String, String> executeOnlyReadabilityCommand(String jarPath, String javaFile, String commitId, String outputCsvPath) {
        Map<String, String> metrics = new HashMap<>();
        String command = String.format("java -jar %s %s", jarPath, javaFile);

        try {
            System.out.println("Executing command: " + command);
            Process process = Runtime.getRuntime().exec(command);

            try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = stdInput.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains(".java")) {
                        String[] parts = line.split("\t");
                        if (parts.length == 2) {
                            String filePath = normalizePath(parts[0].trim());
                            String readability = parts[1].trim();
                            metrics.put(filePath, readability);
                        }
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("Failed to calculate Readability metrics for file " + javaFile + " in commit " + commitId);
                    return metrics;
                }
            }

            // Append metrics to CSV file
            appendMetricsToCSV(metrics, outputCsvPath);
            System.out.println("Metrics successfully saved to " + outputCsvPath);

        } catch (IOException e) {
            System.err.println("I/O error while executing readability JAR: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Process was interrupted: " + e.getMessage());
        }

        return metrics;
    }

    private static String normalizePath(String path) {
        try {
            Path normalizedPath = Paths.get(path).normalize();
            return normalizedPath.toString();
        } catch (Exception e) {
            return path;
        }
    }

    private static void appendMetricsToCSV(Map<String, String> metrics, String outputCsvPath) {
        File file = new File(outputCsvPath);
        boolean isNewFile = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsvPath, true))) {
            // Write header if it's a new file
            if (isNewFile) {
                writer.write("Filename,Readability\n");
            }

            // Append metrics
            for (Map.Entry<String, String> entry : metrics.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }

        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }
}