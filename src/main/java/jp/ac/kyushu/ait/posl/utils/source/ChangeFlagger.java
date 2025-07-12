package jp.ac.kyushu.ait.posl.utils.source;

import jp.ac.kyushu.ait.posl.beans.commit.ChangedFile;
import jp.ac.kyushu.ait.posl.beans.commit.Chunk;
import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;

public class ChangeFlagger {

    /**
     * flag the changed line by a commit
     *
     */
    public static void flagChange(Commit commit, Structure structure, boolean isX_1) {
        for (ChangedFile cf : commit.changedFileList) {
            for (Chunk c : cf.chunks) {
                if(isX_1){
                    flagChange(structure, cf.oldPath, c.getOldStartNo(), c.getOldEndNo());
                }else{
                    flagChange(structure, cf.newPath, c.getNewStartNo(), c.getNewEndNo());
                }
            }
        }
    }
    /**
     * flag the changed line by a commit
     *
     */
    protected static void flagChange(Structure structure, String path, int startNo, int endNo) {
        for (int lineNo = startNo + 1; lineNo <= endNo; lineNo++) {
            MethodDefinition mdX = structure.getMethod(path, lineNo);
            if(mdX!=null){//if out of the method
                mdX.methodInfo.changedLines.put(lineNo, true);
                structure.updateMethod(mdX);
            }

        }
    }
}
