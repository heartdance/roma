package cn.flypigeon.roma.client;

import cn.flypigeon.roma.client.report.ConsoleReporter;

/**
 * Created by htf on 2020/10/15.
 */
public class Reporter {

    private static final cn.flypigeon.roma.client.report.Reporter reporter = new ConsoleReporter();

    public static void error(String message) {
        reporter.error(message);
    }

    public static void info(String message) {
        reporter.info(message);
    }
}
