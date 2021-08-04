package com.hidebush.roma.client.report;

/**
 * Created by htf on 2020/10/15.
 */
public class ConsoleReporter implements Reporter {

    @Override
    public void error(Integer code) {
        System.out.println("error " + code);
    }

    @Override
    public void error(String message) {
        System.out.println("error: " + message);
    }

    @Override
    public void error(Integer code, String message) {
        System.out.println("error " + code + ": " + message);
    }

    @Override
    public void info(String message) {
        System.out.println(message);
    }
}
