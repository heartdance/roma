package com.hidebush.roma.util.exception;

/**
 * Created by htf on 2021/8/9.
 */
public class RomaException extends RuntimeException {

    private final ErrorCode errorCode;

    public RomaException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return errorCode.message();
    }
}
