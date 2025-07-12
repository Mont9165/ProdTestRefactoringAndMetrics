package jp.ac.kyushu.ait.posl.stub;

import jp.ac.kyushu.ait.posl.beans.source.PassedLine;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.selogger.SeLogger;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.selogger.SeLoggerReader;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.utils.exception.NoSureFireException;
import jp.ac.kyushu.ait.posl.utils.file.MyPathUtil;
import jp.ac.kyushu.ait.posl.utils.program.MyTypeUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SeLoggerReaderStub extends SeLogger {
    public SeLoggerReaderStub(MavenSettingController mc) throws NoSureFireException {
        super(mc);
    }
    public SeLoggerReaderStub(String homeDir, String s){
        super(s);
        this.homeDir = homeDir;

    }
    public String getArguments(String s){
        return MyTypeUtils.getArguments(s);
    }
    public String getMethodFile(){
        return MyPathUtil.join(homeDir, outputDir, "methods.txt");
    }
    public String getDataFile(){
        return MyPathUtil.join(homeDir, outputDir, "dataids.txt");
    }
    public String getCallFile(){
        return MyPathUtil.join(homeDir, outputDir, "recentdata.txt");
    }
    public Map<String, String>  getMethods(String classesFile, String methodsFile) throws IOException {
        return SeLoggerReader.getMethods(homeDir, srcDir, testDir, classesFile, methodsFile);
    }
    public Map<String, String> getProcess(String dataIdsFile) throws IOException {
        return SeLoggerReader.getProcess(dataIdsFile);
    }
    public Map<Long, PassedLine> getPasses(String callsFile, Map<String, String> methods, Map<String, String> programs) throws IOException {
        return SeLoggerReader.getPasses(callsFile, methods, programs);
    }
    @Override
    public Map<String, Map<Integer, List<PassedLine>>> getPassLinesMap(String modName, List<String> testSignatures) {//testSignature, anySignature, line (in the file)
        return SeLoggerReader.getPassLinesMap(homeDir, srcDir, testDir, getSeloggerOutputDir(modName), testSignatures);
    }
    public String getClassFile() {
        return MyPathUtil.join(homeDir, outputDir, "classes.txt");

    }
}
