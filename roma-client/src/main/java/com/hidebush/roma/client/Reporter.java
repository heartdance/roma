package com.hidebush.roma.client;

import com.hidebush.roma.client.report.ConsoleReporter;

/**
 * Created by htf on 2020/10/15.
 */
public class Reporter {

    private static final com.hidebush.roma.client.report.Reporter reporter = new ConsoleReporter();

    public static void error(String message) {
        reporter.error(message);
    }

    public static void info(String message) {
        reporter.info(message);
    }
}
