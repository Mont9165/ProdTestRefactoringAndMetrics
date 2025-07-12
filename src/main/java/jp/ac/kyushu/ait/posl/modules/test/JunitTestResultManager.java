package jp.ac.kyushu.ait.posl.modules.test;

import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.beans.test.TestInfo;
import jp.ac.kyushu.ait.posl.beans.test.TestResult;
import jp.ac.kyushu.ait.posl.modules.build.compile.CompileErrorEliminator;
import jp.ac.kyushu.ait.posl.modules.build.compile.CompileErrorFinder;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.program.MyProgramUtils;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.*;

/**
 * This class is used to store/get data from Database (hibernate)
 */
/**
 * This class is used to store/get data from Database (hibernate)
 */
public class JunitTestResultManager implements Serializable {
    private static final long serialVersionUID = 1L;
    protected MyLogger logger = MyLogger.getInstance();

    public long registryId;
    public List<TestInfo> results;//signature, result



    /**
     * The lines that are exercised by test methods
     */
    public Map<String, Map<Integer, List<PassedLine>>> passLinesMap;//testSignature, <testLine, <signature, lineNo>>




    /**
     * Deleted lines due to compile errors
     */
    public Map<String, Map<String, List<CompileErrorEliminator.Line>>> deletedLinesMap;
    public Map<Integer, List<PassedLine>> getPassLines(String signature) {
        return passLinesMap.get(signature);
    }

    public JunitTestResultManager(long registryId, BuildToolSettingController mc){
        this.registryId = registryId;
        deletedLinesMap = new LinkedHashMap<>();
        passLinesMap = new HashMap<>();
        results = new ArrayList<>();
    }
    public JunitTestResultManager(){
    }

    /**
     * add test results
     * @param item
     * @param mc
     * @return
     */
    public String add(Node item, BuildToolSettingController mc) {
        TestInfo tr = this.createInstance(item, mc);
        if(!tr.testResult.type.equals(TestResult.ResultType.SKIPPED)){
            this.add(tr);
        }

        return tr.signature;
    }

    protected TestInfo createInstance(Node item, BuildToolSettingController mc) {
        return new TestInfo(registryId, item, mc);
    }

    protected TestInfo createInstance(TestResult.ResultType tr) {
        return new TestInfo(registryId, tr);
    }

    protected <T extends TestInfo> void add(T tr) {
        this.results.add(tr);
    }

    public List<? extends TestInfo> getTestResults() {
        return this.results;
    }

    /**
     * extract compiler error information and put it into this class
     * @param em
     */
    public void add(CompileErrorFinder.ErrorMethod em) {
        TestInfo tr = this.createInstance(TestResult.ResultType.COMPILE_ERROR);
        tr.testResult.setErrorMessage(em);
        MethodDefinition md = em.getDefinition();
        if(md==null){
            //outside of the method
            return;
        }
        tr.setSignature(MyProgramUtils.getSignature(md));
        tr.className = em.getClassName();
        tr.executionTime = null;
        tr.methodName = em.getMethodName();
        this.add(tr);
    }




    public TestInfo getResult(String sig){
        List<? extends TestInfo> list = getTestResults();
        for(TestInfo t: list){
            if(t.signature.equals(sig)){
                return t;
            }
        }
        return null;
    }



}
