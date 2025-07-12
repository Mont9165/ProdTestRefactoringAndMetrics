package jp.ac.kyushu.ait.posl.beans.refactoring.db.code_range;

import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.modules.refactoring.detect.RefactoringMinerController;
import jp.ac.kyushu.ait.posl.utils.exception.NoParentsException;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.refactoringminer.api.Refactoring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestRefactoringForDatabase3 {
    static List<Refactoring> refactoringResults;
    static String project = "commons-lang";
    static String commitId = "debc02c6d9b94c717b4182ae4dd7a97d47293a52";//

    @BeforeClass
    public static void before() throws NoParentsException {
        SettingManager sm = new SettingManager(project);
        GitController gc = new GitController(sm, "repos");
//        gc.checkout("cee7f2c9ef6bc7a6aaa7f4e66d4d1267d4264ba6");
        refactoringResults = RefactoringMinerController.getRefactoringInstanceAtCommit(gc.getRepo(), commitId);
    }
    @Test
    public void hashN001(){
        Set<Integer> hashSet = new HashSet<>();
        for (Refactoring r: refactoringResults){
            System.out.println("----------");
            System.out.println(r.toJSON());
            RefactoringForDatabase db = new RefactoringForDatabase(r, project, commitId);
            Assert.assertFalse(hashSet.contains(db.hash));
            hashSet.add(db.hash);
        }

    }
    @Test
    public void hashN002(){
        Refactoring a = refactoringResults.get(0);
        Refactoring b = refactoringResults.get(0);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        RefactoringForDatabase db1 = new RefactoringForDatabase(a, project, commitId);
        RefactoringForDatabase db2 = new RefactoringForDatabase(b, project, commitId);
        Assert.assertEquals(db1.hashCode(), db2.hashCode());
        Assert.assertEquals(db1.hash, db2.hash);
    }


}
