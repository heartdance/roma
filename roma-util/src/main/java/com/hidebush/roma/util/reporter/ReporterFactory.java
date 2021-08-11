package com.hidebush.roma.util.reporter;

import java.util.function.Function;

/**
 * Created by htf on 2021/8/11.
 */
public class ReporterFactory {

    private static Function<String, Reporter> factory = ConsoleReporter::new;

    public static Reporter createReporter(String role) {
        return factory.apply(role);
    }

    public static void change(Function<String, Reporter> factory) {
        ReporterFactory.factory = factory;
    }
}
