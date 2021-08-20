package com.hidebush.roma.util.exception;

/**
 * Created by htf on 2021/8/20.
 */
public enum ErrorCode {

    UNKNOWN(0, "unknown error"),

    TIMEOUT(101, "time out"),
    INTERRUPTED(102, "interrupted"),

    PORT_OCCUPIED(201, "port occupied"),
    NO_FREE_PORT(202, "no free port");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public static ErrorCode ofCode(int code) {
        switch (code) {
            case 101:
                return TIMEOUT;
            case 102:
                return INTERRUPTED;
            case 201:
                return PORT_OCCUPIED;
            case 202:
                return NO_FREE_PORT;
            case 0:
            default:
                return UNKNOWN;
        }
    }
}
