package jp.ac.kyushu.ait.posl.beans.test;

import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.utils.file.MyPathUtil;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * This class is used to store/get data from Database (hibernate)
 */
@Entity
@Table(name = "test", schema = "test")//,  uniqueConstraints = { @UniqueConstraint(columnNames = { "test_info_id"}) })
public class TestInfo implements Serializable {
    @Transient
    private static final long serialVersionUID = 1L;
    @Transient
    MyLogger logger = MyLogger.getInstance();
    /**
     * this ID is automatically generated for each table by hibernate
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="test_info_id", columnDefinition="bigint")
    public long id;

    @Column(name="registry_id", columnDefinition="bigint")
    public long registryId;

    /**
     * Method signature (i.e., file path+class+method name+parameter type)
     */
    @Column(name = "signature")
    public String signature;

    @Transient
    public String signatureParameterized;

    /**
     * class name
     */
    @Column(name = "className")
    public String className;

    /**
     * method name
     */
    @Column(name = "methodName")
    public String methodName;

    /**
     * execution time measured by JUnit
     */
    @Column(name = "executionTime")
    public Double executionTime;
    /**
     * test result
     */
    @Embedded
    public TestResult testResult;

    public TestInfo(long registryId, Node node, BuildToolSettingController bc) {
        setContents(registryId, node, bc);
    }


    /**
     * extract data from XML file provided by JUnit and set the data in this class
     * @param node
     * @param bc
     */
    private void setContents(long registryId, Node node, BuildToolSettingController bc) {
        this.registryId = registryId;
        this.setTestResult(node);
        Element e = (Element) node;

        if(this.testResult.getType().equals(TestResult.ResultType.SKIPPED)){
            return;
        }
        this.setMethodName(e);
        String module = bc.getModuleName();
        String testPath = MyPathUtil.join(module, bc.getTestDir(false));
        //TODO: handle subclass
        String[] className = e.getAttribute("classname").split("\\.");
        this.className= className[className.length-1];
        String path =  MyPathUtil.join(testPath, e.getAttribute("classname").replaceAll("\\.", "/")+".java");
        String arguments = getArguments(e);
        this.setSignature(MyProgramUtils.getSignature(path, this.className, this.methodName, arguments));


        // nullじゃなければリプレイス
        if (e.getAttribute("time") != null) {
            this.executionTime = Double.parseDouble(e.getAttribute("time").replaceAll(",", ""));
        }
        else{
            this.executionTime = null;
        }
    }

    /**
     * extract method name
     * @param e
     * @return
     */
    private static String getArguments(Element e) {
        String methodName = e.getAttribute("name");
        if(methodName.contains("{")){
            methodName = methodName.split("\\{")[1].split("}")[0];
            methodName = methodName.replaceAll(" ", "").replaceAll("\\[", "");
            return methodName;
        }else {
            return "";
        }

    }

    private void setTestResult(Node node) {
        this.testResult = new TestResult(node);
    }

    public String toString(){
        return testResult.toString()+": "+methodName+"#"+className;
    }
    public TestInfo(long registryId, TestResult.ResultType rs){
        this.registryId = registryId;
        this.testResult = new TestResult(rs);
    }
    public TestInfo(){
    }

    public String getSignatureParameterized() {
        return signatureParameterized;
    }
    public String getSignature() {
        return signature;
    }

    public Double getExecutionTime(){
        return executionTime;
    }

    /**
     * This is for parameterized test (but is not used in this study)
     * @param signature
     */
    public void setSignature(String signature) {
        this.signatureParameterized = signature;
        String sig = signature;

        String regex = "\\[[0-9]+\\]#";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(sig);
        if(m.find()){
            String parameterizedNumber =m.group();
            sig = sig.replace(parameterizedNumber, "#");
        }

        this.signature = sig;
    }

    /**
     * extract name from XML file
     * @param e
     */
    public void setMethodName(Element e) {
        this.methodName = e.getAttribute("name").split("\\{")[0];
        if(this.methodName.contains(".")){
            List<String> l = new ArrayList<String>();
            for(String s:e.getAttribute("name").split("\\.")){
                l.add(s);
            }
            this.methodName = l.get(l.size() - 1);
        }
    }

    /**
     * check if this result is compiler error
     * @return
     */
    public boolean isCompileError(){
        return testResult.type.equals(TestResult.ResultType.COMPILE_ERROR);
    }
    /**
     * check if this result is compiler error
     * @return
     */
    public boolean isPass(){
        return testResult.type.equals(TestResult.ResultType.PASS);
    }

}
