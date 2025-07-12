package jp.ac.kyushu.ait.posl.utils;

import jp.ac.kyushu.ait.posl.utils.file.MyPathUtil;

public class Utils {
    public static String getResources(){
        String homeDir = System.getProperty("user.dir");
        return homeDir+"/src/test/resources/";
    }
    public static String getFileInResources(String fileName) {
        String f = MyPathUtil.join(getResources(), fileName);
        return f;
    }
}
