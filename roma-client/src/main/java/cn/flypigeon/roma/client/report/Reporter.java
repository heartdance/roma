package cn.flypigeon.roma.client.report;

/**
 * Created by htf on 2020/10/15.
 */
public interface Reporter {

    void error(Integer code);

    void error(String message);

    void error(Integer code, String message);

    void info(String message);
}
