package jp.ac.kyushu.ait.posl.exe.not_use;

import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range.RefactoringForDatabase;
import jp.ac.kyushu.ait.posl.utils.db.Dao;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;

public class DeleteRefactoringCSV {
    static Dao<Commit> commitDao = new Dao<>(new Commit());
    static Dao<RefactoringForDatabase> refactoringDao = new Dao<>(new RefactoringForDatabase());

    public static void main(String[] args) {
        SettingManager sm = new SettingManager(args);
        String projectName = sm.getProject().name;

        // 初期化
        commitDao.init();
        refactoringDao.init();

        // 指定プロジェクトのデータを削除
        deleteProjectData(projectName);

        // DBリソースの解放
        commitDao.close();
        refactoringDao.close();
    }

    private static void deleteProjectData(String projectName) {
        // Commitのデータを削除
        commitDao.setWhere("project", projectName);
        for (Commit commit : commitDao.select()) {
            commitDao.remove(commit);
        }
        System.out.println("Deleted all commits for project: " + projectName);

        // Refactoringのデータを削除
        refactoringDao.setWhere("project", projectName);
        for (RefactoringForDatabase refactoring : refactoringDao.select()) {
            refactoringDao.remove(refactoring);
        }
        System.out.println("Deleted all refactoring data for project: " + projectName);
    }
}