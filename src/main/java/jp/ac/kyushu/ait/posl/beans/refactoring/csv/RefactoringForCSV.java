package jp.ac.kyushu.ait.posl.beans.refactoring.csv;

import jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range.LeftCodeRange4Database;
import jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range.RefactoringForDatabase;
import jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range.RightCodeRange4Database;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RefactoringForCSV {
    private final String commitId;
    private final String project;
    private final int hash;
    private final String refactoringType;
    private String leftFilepath;
    private String rightFilepath;
    private int leftStartLine;
    private int leftEndLine;
    private int rightStartLine;
    private int rightEndLine;

    public RefactoringForCSV(RefactoringForDatabase refactoring) {
        this.commitId = refactoring.commitId;
        this.project = refactoring.project;
        this.hash = refactoring.hash;
        this.refactoringType = refactoring.RefactoringType.name();

        if (!refactoring.leftside.isEmpty()) {
            LeftCodeRange4Database left = refactoring.leftside.get(0);
            this.leftFilepath = left.filepath;
            this.leftStartLine = left.start;
            this.leftEndLine = left.end;
        }

        if (!refactoring.rightside.isEmpty()) {
            RightCodeRange4Database right = refactoring.rightside.get(0);
            this.rightFilepath = right.filepath;
            this.rightStartLine = right.start;
            this.rightEndLine = right.end;
        }
    }

    public void saveToCSV(String filePath) throws IOException {
        boolean fileExists = Files.exists(Paths.get(filePath));

        try (FileWriter writer = new FileWriter(filePath, true)) {
            // ファイルが存在しない場合にヘッダーを書き込む
            if (!fileExists) {
                writer.write("commitId,project,hash,refactoringType,left_filepath,right_filepath,left_startLine,left_endLine,right_startLine,right_endLine\n");
            }

            // データをCSV形式で追記
            writer.write(String.format("%s,%s,%d,%s,%s,%s,%d,%d,%d,%d\n",
                    commitId,
                    project,
                    hash,
                    refactoringType,
                    leftFilepath,
                    rightFilepath,
                    leftStartLine,
                    leftEndLine,
                    rightStartLine,
                    rightEndLine
            ));
        }
    }
}