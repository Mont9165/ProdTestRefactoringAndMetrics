package jp.ac.kyushu.ait.posl.modules.CK;

import java.io.*;

public class CkController {

    public static void executeCkCommand(String jarPath, String projectDirPath, String outputDirPath) {
        // Command to execute the CK tool
        String command = String.format("java -jar %s %s true 0 true %s", jarPath, projectDirPath, outputDirPath);

        try {
            // Execute the CK tool
            Process process = Runtime.getRuntime().exec(command);

            // Capture and display error output (if any)
            try (BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = stdError.readLine()) != null) {
                    System.err.println("CK Error Output: " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("CK tool execution failed with exit code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing CK tool: " + e.getMessage());
        }
    }
}