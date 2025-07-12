package jp.ac.kyushu.ait.posl.utils.exception;

public class JavaVersionTooOldException extends Exception {
    public JavaVersionTooOldException(String errors){
        super(errors);
    }
}
