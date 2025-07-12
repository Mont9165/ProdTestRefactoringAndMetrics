package jp.ac.kyushu.ait.posl.utils.trace;

import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.modules.test.JunitTestResultManager;
import jp.ac.kyushu.ait.posl.beans.trace.ExecutionTrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyTraceUtils {
    /**
     * create ExecutionTrace
     * @return
     */
    public static List<ExecutionTrace>  transform(JunitTestResultManager result){
        List<ExecutionTrace> list = new ArrayList<>();
        for(String testSignature: result.passLinesMap.keySet()){
            setTrace(list, testSignature, result);
        }
        return list;
    }


    public static void setTrace(List<ExecutionTrace> list, String testSignature, JunitTestResultManager result) {
        Map<Integer, List<PassedLine>> lines = result.passLinesMap.get(testSignature);
        for(Integer i: lines.keySet()){
//            ExecutionTrace trace = new ExecutionTrace(result.project, result.commitId, result.isCross(), testSignature, i);
            ExecutionTrace trace = new ExecutionTrace(result.registryId, testSignature, i);
            trace.add(lines.get(i));
            list.add(trace);
        }
    }

}
