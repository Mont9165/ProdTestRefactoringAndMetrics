package jp.ac.kyushu.ait.posl.utils.file;

import java.io.File;
import java.util.StringJoiner;

public class MyPathUtil {
    public static String join(String... dir){
        String rtn = null;
        for(int i=0;i<dir.length;i++){
            if (dir[i]==null) continue;
            if (rtn==null){
                if(!dir[i].equals("")){
                    rtn = dir[i];
                }
                continue;
            }
            //append
            rtn = new File(rtn, dir[i]).getPath();
        }
        return rtn;
    }

    public static String getDirectoryName(String moduleName) {
        StringJoiner sj = new StringJoiner("/");
        String[] arrays = moduleName.split("/");
        for (int i=0;i<arrays.length-1;i++){
            sj.add(arrays[i]);
        }
        return sj.toString();
    }

    public static String getFileName(String moduleName) {
        String[] arrays = moduleName.split("/");
        return arrays[arrays.length-1];
    }
}
