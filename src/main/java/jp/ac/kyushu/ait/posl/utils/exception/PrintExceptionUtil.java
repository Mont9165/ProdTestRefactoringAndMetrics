package jp.ac.kyushu.ait.posl.utils.exception;

public class PrintExceptionUtil {
    static public String toString(Throwable e){
        StringBuilder out = new StringBuilder();
        try{
            out.append(e.getClass().toString()).append("\n");
            for (StackTraceElement el: e.getStackTrace()){
                out.append(el.toString()).append("\n");
            }
        }catch (Exception exc){
        }
        return out.toString();
    }
}
