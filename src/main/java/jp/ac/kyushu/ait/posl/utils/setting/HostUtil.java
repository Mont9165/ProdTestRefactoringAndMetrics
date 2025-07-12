package jp.ac.kyushu.ait.posl.utils.setting;

import java.net.InetAddress;

public class HostUtil {
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }catch (Exception e) {
            e.printStackTrace();
            return "UnknownHost";
        }
    }
}
