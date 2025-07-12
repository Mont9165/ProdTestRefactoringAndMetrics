package jp.ac.kyushu.ait.posl.utils;

import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import org.junit.Test;
import jp.ac.kyushu.ait.posl.utils.exception.NoParentsException;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;

import static org.junit.Assert.assertEquals;

public class TestCommitUtils {
    @Test
    public void testDays() throws NoParentsException {
        GitController git = new GitController(new SettingManager(new String[]{"TestEffortEstimationTutorial"}), "/main/");
        Commit parent = git.getCommit("bf8d6ad0d1dae2e407680db01db6e16884559e29");
        Commit child = git.getCommit("2833f1d5157877675801271a326bb2ba9b9b43ca");
        long days = GitController.getDays(parent, child);
        assertEquals(4, (int) days);
    }

    @Test
    public void testRevisions(){
        GitController git = new GitController(new SettingManager(new String[]{"TestEffortEstimationTutorial"}), "/main/");
        Integer i = git.countCommitsBetween("bf8d6ad0d1dae2e407680db01db6e16884559e29", "2833f1d5157877675801271a326bb2ba9b9b43ca");
        assertEquals(6, (int) i);
    }
}
