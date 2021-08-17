package com.hidebush.roma.util.config;

/**
 * Created by htf on 2020/9/28.
 */
public class TypeConstant {

    public static final int PING = 0;
    public static final int PONG = 1;

    public static final int SUCCESS = 2;
    public static final int FAILED = 3;

    public static final int CREATE_TCP_PROXY = 4;
    public static final int CANCEL_TCP_PROXY = 5;

    public static final int CREATE_UDP_PROXY = 6;
    public static final int CANCEL_UDP_PROXY = 7;

    public static final int ON_VISITOR_CONNECT = 10;
    public static final int ON_VISITOR_SEND_MSG = 11;
    public static final int ON_VISITOR_DISCONNECT = 12;

    public static final int ON_SERVICE_SEND_MSG = 20;
    public static final int ON_SERVICE_DISCONNECT = 21;
}
