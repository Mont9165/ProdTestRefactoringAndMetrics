package jp.ac.kyushu.ait.posl.modules.build;

import jp.ac.kyushu.ait.posl.beans.test.TestInfo;
import jp.ac.kyushu.ait.posl.beans.trace.ExecutionTrace;
import jp.ac.kyushu.ait.posl.modules.build.commands.select.TestTargetSelector;
import jp.ac.kyushu.ait.posl.modules.build.compile.CompileErrorEliminator;
import jp.ac.kyushu.ait.posl.modules.build.compile.CompileErrorFinder;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.ExecutionTracer;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;
import jp.ac.kyushu.ait.posl.modules.source.structure.StructureScanner;
import jp.ac.kyushu.ait.posl.modules.test.JunitTestResultManager;
import jp.ac.kyushu.ait.posl.utils.db.Dao;
import jp.ac.kyushu.ait.posl.utils.file.MyFileReadWriteUtils;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.trace.MyTraceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class BuildResultRecorder {
    JunitTestResultManager result;
    ExecutionTracer dynamicAnalyzer;
    TestTargetSelector targetSelector;
    Structure structure;
    BuildToolSettingController mavenSetting;
    protected static MyLogger logger = MyLogger.getInstance();
    /**
     * to run method by method
     */
    List<String> testDoneList;
    public BuildResultRecorder(BuildToolSettingController mavenSetting, ExecutionTracer dynamicAnalyzer, JunitTestResultManager result){
        this.mavenSetting = mavenSetting;
        this.dynamicAnalyzer = dynamicAnalyzer;
        structure = new StructureScanner(mavenSetting).scan();
        targetSelector = mavenSetting.getSettingManager().getTargetSelector();
        testDoneList = new ArrayList<>();
        this.result = result;
    }




    public Set<String> getTargets(){
        return targetSelector.getTestTargets(structure);
    }

    public static Exception storeJUnitResult(JunitTestResultManager result) {
        try {//TODO: Should be a transaction
            Dao<TestInfo> dao = new Dao<>();
            dao.init();
            for (TestInfo t: result.getTestResults()){
                dao.insert(t);
            }
            dao.close();
            return null;
        }catch (Exception e){
            return e;
        }
    }

    public static Exception storeTraces(JunitTestResultManager result) {
        try {//TODO: Should be a transaction
            Dao<ExecutionTrace> dao = new Dao<>();
            dao.init();
            for(ExecutionTrace et: MyTraceUtils.transform(result)){
                dao.insert(et);
            }
            dao.close();
            result.passLinesMap = new HashMap<>();
            return null;
        }catch (Exception e){
            return e;
        }
    }


    public void recordResults(String targetName) {
        String modName = targetSelector.getModuleName(structure, targetName);
        BuildToolSettingController modBC = targetSelector.findBuildControllerForModule(mavenSetting, modName);
        this.recordSucceedTest(result, modBC);//read test results
        this.setPassLinesMap(result, modName, dynamicAnalyzer);
    }
    public JunitTestResultManager getResults(){
        return result;
    }

    /**
     * Read result's XML file given by JUnit and record the results of methods.
     */
    public void recordSucceedTest(JunitTestResultManager result, BuildToolSettingController bc) {
        List<String> xmlFiles = this.readXMLFile(bc);


        for(String fileName: xmlFiles){
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
            for(int i=0;i<nodeList.getLength();i++){
                String added = result.add(nodeList.item(i), bc);
                if (added!=null){
                    testDoneList.add(added);
                }
            }
        }
    }

    private List<String> readXMLFile(BuildToolSettingController bc) {
        List<String> xmlFiles = new ArrayList<>();
        if (!bc.hasModule()){
            List<String> file = MyFileReadWriteUtils.getFileList(bc.getSureFireOutputDir(), ".xml");
            //Home directory
            if (file!=null){
                xmlFiles.addAll(file);
            }
        }else if (bc.hasModule()){//Case where test whole run
            for(BuildToolSettingController b: bc.getModules().values()){
                List<String> tmp  = MyFileReadWriteUtils.getFileList(b.getSureFireOutputDir(), ".xml");
                if(tmp!=null){
                    xmlFiles.addAll(tmp);
                }
            }
        }else {
            throw new AssertionError();
        }
        return xmlFiles;
    }

//    /**
//     * copy the output file
//     * @param m
//     * @param li
//     */
//    private void storeXMLFiles(String m, List<String> li, String outputDir) {
//        try {
//            if(li==null){
//                FileUtils.touch(new File(MyPathUtil.join(this.mc.getXMLStoreDir(), this.result.commitId, this.result.type.toString(), "no_methods_"+m+".txt")));
//            }else {
//                FileUtils.copyDirectory(new File(outputDir), new File(MyPathUtil.join(this.mc.getXMLStoreDir(), this.result.commitId+"_"+m, this.result.type.toString())));
//            }
//        } catch (IOException e) {
//            System.err.println(e);
//            throw new AssertionError();
//        }
//    }

    /**
     * add the compiler error to result variable
     * @param list
     */
    public void recordCompileErrorResults(List<CompileErrorFinder.ErrorMethod> list) {
        for (CompileErrorFinder.ErrorMethod em:list) {
            result.add(em);
        }
    }

    /**
     * record deleted lines
     * @param attempt
     * @param removedLines
     */
    public void recordDeletedLines(int attempt, Map<String, List<CompileErrorEliminator.Line>> removedLines) {
        result.deletedLinesMap.put(Integer.toString(attempt), removedLines);
    }


    /**
     * show lines (for debug)
     */
    public void showDeletedLines(JunitTestResultManager result) {
        for (String key1:result.deletedLinesMap.keySet()){
            logger.trace("******"+key1+"******");
            Map<String, List<CompileErrorEliminator.Line>> val1 = result.deletedLinesMap.get(key1);
            for (String key2:val1.keySet()){
                logger.trace("-----"+key2+"-----");
                List<CompileErrorEliminator.Line> val2 = val1.get(key2);
                for(CompileErrorEliminator.Line line:val2){
                    if(line.delete){
                        System.out.println(line.contents);
                    }
                }
            }
        }
    }

    /**
     * set the test methods that passed
     * @param dynamicAnalyzer
     */
    public void setPassLinesMap(JunitTestResultManager result, String moduleName, ExecutionTracer dynamicAnalyzer) {
        if (!dynamicAnalyzer.isTracer()) return;
        if(testDoneList.size()>0){//TODO: for OpenClover
            result.passLinesMap.putAll(dynamicAnalyzer.getPassLinesMap(moduleName, testDoneList));
            testDoneList.removeAll(result.passLinesMap.keySet());
        }
    }


}
