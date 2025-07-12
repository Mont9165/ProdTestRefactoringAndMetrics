package jp.ac.kyushu.ait.posl.utils.exception;

public class DatabaseTransactionError extends Error{
    public Exception e;
    public DatabaseTransactionError(Exception e) {
        this.e = e;
    }
}
