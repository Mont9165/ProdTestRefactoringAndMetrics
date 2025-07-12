package jp.ac.kyushu.ait.posl.modules.source.execution_trace;

import jp.ac.kyushu.ait.posl.beans.source.PassedLine;

import java.util.List;
import java.util.Map;

/**
 * Depend on the setting file, this returns an appropriate tool interface
 */
public interface ExecutionTraceReader {
    Map<String, Map<Integer, List<PassedLine>>>  getPassLinesMap();//testSignature, anySignature, line (in the file)

}
