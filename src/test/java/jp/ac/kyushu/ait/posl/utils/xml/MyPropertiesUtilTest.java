package jp.ac.kyushu.ait.posl.utils.xml;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MyPropertiesUtilTest {
    @Test
    public void testExtractValue_N001(){
        String key = "mvn.compile.release";
        String value = "1";
        Properties properties = new Properties();
        properties.put(key,value);
        String ret = MyPropertiesUtil.extractValue(properties, key);
        assertEquals(value, ret);
    }

    /**
     * If it's number
     */
    @Test
    public void testExtractValue_N002(){
        String key = "4.12";
        Properties properties = new Properties();
        String ret = MyPropertiesUtil.extractValue(properties, key);
        assertEquals(key, ret);
    }
    @Test
    public void testExtractValue_N003(){
        Properties properties = new Properties();
        properties.put("jdk.version", "4.12");
        String ret = MyPropertiesUtil.extractValue(properties, "${jdk.version}");
        assertEquals("4.12", ret);
    }
    @Test
    public void testExtractValue_N004(){
        String key = "mvn.compile.release";
        String value = "4.12";
        Properties properties = new Properties();
        properties.put(key,value);
        String ret = MyPropertiesUtil.extractValue(properties, "aaa");
        assertNull(ret);
    }

}
