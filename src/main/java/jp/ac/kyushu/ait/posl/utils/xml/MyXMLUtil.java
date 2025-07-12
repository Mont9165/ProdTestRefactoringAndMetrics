package jp.ac.kyushu.ait.posl.utils.xml;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class MyXMLUtil {
    /**
     * Append Maven XML (e.g., plugins) to pom.xml
     * @param xml
     * @param key
     * @param value
     */
    public static void addChild(Xpp3Dom xml, String key, String value) {
        Xpp3Dom x = xml.getChild(key);
        if(x==null) {
            Xpp3Dom testFailureIgnore = new Xpp3Dom(key);
            testFailureIgnore.setValue(value);
            xml.addChild(testFailureIgnore);
        }else{
            x.setValue(value);
        }
    }
    public static void addChild(Xpp3Dom xml, String key, String value1, String value2) {
        Xpp3Dom x = xml.getChild(key);
        if(x==null) {
            Xpp3Dom testFailureIgnore = new Xpp3Dom(key);
            testFailureIgnore.setAttribute(value1, value2);
            xml.addChild(testFailureIgnore);
        }else{
            x.setAttribute(value1, value2);
        }
    }
}
