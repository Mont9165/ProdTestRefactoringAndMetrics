package jp.ac.kyushu.ait.posl.beans.test.rowdata;

import jp.ac.kyushu.ait.posl.beans.test.TestInfo;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static jp.ac.kyushu.ait.posl.beans.test.TestResult.ResultType.PASS;

public class TestInfoTest {

    @Test
    public void readN001(){
        String fileName = "src/test/resources/TestInfo/TEST-org.apache.commons.text.similarity.ParameterizedLevenshteinDistanceTest.xml";
        NodeList nodeList = getNodeList(fileName);
        String project = "commons-text";
        String commitId = "0b55205025fcec1bf5b6317e79dba63a7bed33b3";
        SettingManager sm = new SettingManager(new String[]{project});
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        TestInfo tr = new TestInfo(0, nodeList.item(0), mc);
        Assert.assertEquals("src/test/java/org/apache/commons/text/similarity/ParameterizedLevenshteinDistanceTest.java;ParameterizedLevenshteinDistanceTest.test#Integer,CharSequence,CharSequence,Integer", tr.getSignature());
    }
    @Test
    public void readN002(){
        String fileName = "src/test/resources/TestInfo/TEST-org.apache.commons.text.diff.ReplacementsFinderTest.xml";
        NodeList nodeList = getNodeList(fileName);
        String project = "commons-text";
        String commitId = "0b55205025fcec1bf5b6317e79dba63a7bed33b3";
        SettingManager sm = new SettingManager(new String[]{project});
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        TestInfo tr = new TestInfo(0, nodeList.item(0), mc);
        Assert.assertEquals("src/test/java/org/apache/commons/text/diff/ReplacementsFinderTest.java;ReplacementsFinderTest.testReplacementsHandler#String,String,int,Character],Character]", tr.getSignature());
    }

    @Test
    public void readN003(){
        String fileName = "src/test/resources/TestInfo/TEST-org.apache.commons.text.TextStringBuilderTest.xml";
        NodeList nodeList = getNodeList(fileName);
        String project = "commons-text";
        String commitId = "0b55205025fcec1bf5b6317e79dba63a7bed33b3";
        SettingManager sm = new SettingManager(new String[]{project});
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        TestInfo tr = new TestInfo(0, nodeList.item(0), mc);
        Assert.assertEquals("src/test/java/org/apache/commons/text/TextStringBuilderTest.java;TextStringBuilderTest.testAsTokenizer#", tr.getSignature());
    }

    @Test
    public void ExecutionTimeTest001(){
        String fileName = "src/test/resources/TestInfo/TEST-org.apache.commons.text.TextStringBuilderTest.xml";
        NodeList nodeList = getNodeList(fileName);
        String project = "commons-text";
        String commitId = "0b55205025fcec1bf5b6317e79dba63a7bed33b3";
        SettingManager sm = new SettingManager(new String[]{project});
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        TestInfo tr = new TestInfo(0, nodeList.item(0), mc);
        Double expected = 0.001;
        Assert.assertEquals(expected, tr.getExecutionTime());
    }

    @Test
    public void ExecutionTimeTest002(){
        String fileName = "src/test/resources/TestInfo/TEST-org.apache.commons.text.TextStringBuilderTest.xml";
        NodeList nodeList = getNodeList(fileName);
        String project = "commons-text";
        String commitId = "0b55205025fcec1bf5b6317e79dba63a7bed33b3";
        SettingManager sm = new SettingManager(new String[]{project});
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        TestInfo tr = new TestInfo(0, nodeList.item(1), mc);
        Double expected = 1234.567;
        Assert.assertEquals(expected, tr.getExecutionTime());
    }

    @Test
    public void ExecutionTimeTest003(){
        String fileName = "src/test/resources/TestInfo/TEST-org.apache.commons.text.TextStringBuilderTest.xml";
        NodeList nodeList = getNodeList(fileName);
        String project = "commons-lang";
        String commitId = "0b55205025fcec1bf5b6317e79dba63a7bed33b3";
        SettingManager sm = new SettingManager(new String[]{project});
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        TestInfo tr = new TestInfo(0, nodeList.item(2), mc);
        Double expected = 1234567.89;
        Assert.assertEquals(expected, tr.getExecutionTime());
    }

    @Test
    public void UnknownTagTest001(){
        String fileName = "src/test/resources/TestInfo/TEST-org.apache.commons.lang3.time.DateUtilsTest.xml";
        NodeList nodeList = getNodeList(fileName);
        String project = "commons-lang";
        String commitId = "3eb4be60cf00c20e46b75fb72366dd0649700c2f";
        SettingManager sm = new SettingManager(new String[]{project});
        GitController git = new GitController(sm, "/test/");
        MavenSettingController mc = new MavenSettingController(git);
        TestInfo tr = new TestInfo(0, nodeList.item(0), mc);
        Assert.assertEquals(PASS, tr.testResult.getType());
    }


    private NodeList getNodeList(String fileName) {
        File xml = new File(fileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        Document document = null;
        try {
            documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.parse(xml);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            throw new AssertionError(fileName);
        }
        Element root = document.getDocumentElement();
        NodeList nodeList = root.getElementsByTagName("testcase");
        return nodeList;
    }
}
