package jp.ac.kyushu.ait.posl.utils.xml;

import org.apache.maven.model.Model;

import java.util.Properties;

public class MyPropertiesUtil {
    public static String getValueFromProp(Model pomModel, String key) {
        Properties props = pomModel.getProperties();
        if (props==null) return null;
        return MyPropertiesUtil.extractValue(props, key);
    }
    /**
     * Pattern
     * 1. mvn.compile.release
     * 2. ${jdk.version}
     * 3. 4.12
     */
    public static String extractValue(Properties properties, String key){
        if (key==null){
            return null;
        }
        if(isDigitValue(key)){
            return key;
        }
        if(key.contains("$")){
            key = key.strip().replace("$","")
                    .replace("{","").replace("}","");
            return properties.getProperty(key);
        }
        //Properties
        return properties.getProperty(key);
    }
    public static boolean isDigitValue(String key){
        if(key==null) return false;
        try {
            String firstDigit = key.split("\\.")[0];
            Double.parseDouble(firstDigit);
            return true;
        }catch (NumberFormatException nfe){
            return false;
        }
    }
    public static String replaceDirectoryName(Properties properties, String dir){
        if(dir.contains("$")){
            for(Object key:  properties.keySet()){
                Object value = properties.get(key);
                if(value !=null){
                    String k = "${"+(String) key+"}";
                    String v = (String) value;
                    dir = dir.replace(k, v);
                }

            }
            if(dir.contains("${basedir}")){
                dir = dir.replace("${basedir}/", "");
            }else if(dir.contains("${project.basedir}")){
                dir = dir.replace("${project.basedir}/", "");
            }
        }

        return dir;
    }
}
