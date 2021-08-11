package com.hidebush.roma.util.reporter;

/**
 * Created by htf on 2021/8/11.
 */
public interface Reporter {

    void debug(String msg);

    void info(String msg);

    void warn(String msg);

    void error(String msg);
}
