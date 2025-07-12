package jp.ac.kyushu.ait.posl.utils.program;

import jp.ac.kyushu.ait.posl.utils.general.MyListUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTypeUtils {
    /**
     *
     * @param s
     * @return
     */
    public static String getArguments(String s) {
        Set set = new HashSet();
        List<String> list = new ArrayList<>();

        String contentsInBlanket = s.split("\\(")[1].split("\\)")[0];

        String[] semicolon = contentsInBlanket.split(";");
        for (String semi : semicolon) {
            if (semi.contains("[L")) {
                String brankets = getBrancketNum(semi);
                String[] l = semi.split("\\[+L", 2);
                assert l.length == 2;
                String s1 = getPrimitiveType(l[0]);
                if (!s1.equals("")) {
                    list.add(s1);
                }
                String s2 = getOwnType(l[1]);
                s2 = getSubclass(s2);
                list.add(s2 + brankets);
            } else if (semi.contains("L")) {
                String[] l = semi.split("L", 2);
                assert l.length == 2;
                String s1 = getPrimitiveType(l[0]);
                if (!s1.equals("")) {
                    list.add(s1);
                }
                String s2 = getOwnType(l[1]);
                s2 = getSubclass(s2);
                list.add(s2);
            } else {
                list.add(getPrimitiveType(semi));
            }
        }
        Pattern pattern = Pattern.compile("\\$[0-9]+");
        List<String> removeList = new ArrayList<>();
        for (String arg: list){
            Matcher m = pattern.matcher(arg);
            if(m.find()){
                removeList.add(arg);
            }
        }
        list.removeAll(removeList);
        return MyListUtils.flatten(list);
    }
    private String getArray(String a) {
        String prefix = "";
        if (a.startsWith("[")) {
            prefix = "]";
            a = a.replace("[", "");
        }
        return prefix;
    }

    private static String getOwnType(String a) {
        String[] tmp = a.split("/");
        String type = tmp[tmp.length - 1];
        return type;
    }

    /**
     * Convert SELogger's expression to general expression (e.g., I will be int)
     * @param a
     * @return
     */
    public static String getPrimitiveType(String a) {
        StringJoiner sj = new StringJoiner(",");
        String prefix = "";
        for (int i = 0; i < a.length(); i++) {
            switch (a.charAt(i)) {
                case 'I':
                    sj.add("int" + prefix);
                    prefix = "";
                    break;
                case 'D':
                    sj.add("double" + prefix);
                    prefix = "";
                    break;
                case 'F':
                    sj.add("float" + prefix);
                    prefix = "";
                    break;
                case 'S':
                    sj.add("short" + prefix);
                    prefix = "";
                    break;
                case 'J':
                    sj.add("long" + prefix);
                    prefix = "";
                    break;
                case 'Z':
                    sj.add("boolean" + prefix);
                    prefix = "";
                    break;
                case 'B':
                    sj.add("byte" + prefix);
                    prefix = "";
                    break;
                case 'C':
                    sj.add("char" + prefix);
                    prefix = "";
                    break;
                case '[':
                    prefix += "]";
                    break;
            }
        }

        return sj.toString();
    }
    /**
     *
     * @param s2
     * @return
     */
    public static String getSubclass(String s2) {
        if(s2.contains("$")){
            String[] tmp = s2.split("\\$");
            s2 = tmp[tmp.length-1];
        }
        return s2;
    }

    public static String getBrancketNum(String semi) {
        String ret = "";
        for (int i = 0; i < semi.length(); i++) {
            if (semi.charAt(i) == '[') {
                ret+="]";
            } else if (semi.charAt(i) == 'L') {
                break;
            } else {
                ret = "";
            }
        }
        return ret;
    }
}
