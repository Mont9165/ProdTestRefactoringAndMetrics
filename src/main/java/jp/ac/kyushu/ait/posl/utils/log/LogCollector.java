package jp.ac.kyushu.ait.posl.utils.log;
import org.apache.commons.exec.LogOutputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for Maven to write log into arrays.
 */
public class LogCollector extends LogOutputStream {
    private final List<String> info;
    private final List<String> warn;
    private final List<String> error;
    private final List<String> crash;
    public LogCollector(){
        info = new ArrayList<>();
        warn = new ArrayList<>();
        error = new ArrayList<>();
        crash = new ArrayList<>();

    }

    @Override
    protected void processLine(String line, int level) {
        if(line.startsWith("[INFO]")){
            info.add(line);
        } else if(line.startsWith("[WARNING]")){
            warn.add(line);
        } else if(line.startsWith("[WARN]")){
            warn.add(line);
        } else if(line.startsWith("[ERROR]")){
            error.add(line);
        }else if(line.startsWith("The forked VM terminated without properly saying goodbye.")){
            crash.add(line);
        }else{
//            System.err.println(line);
        }
//        System.out.println(line); //一時的に全てのログが表示されるように
    }
    public List<String> getError() {
        return error;
    }
    public List<String> getWarn() {
        return warn;
    }
    public List<String> getInfo() {
        return info;
    }
    public List<String> getCrash() {
        return crash;
    }
}
