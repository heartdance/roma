package com.hidebush.roma.util.reporter;

import java.util.function.BiFunction;

/**
 * Created by htf on 2021/8/11.
 */
public class ReporterFactory {

    private static BiFunction<String, Integer, Reporter> factory = ConsoleReporter::new;

    public static Reporter createReporter(String role, int id) {
        return factory.apply(role, id);
    }

    public static void change(BiFunction<String, Integer, Reporter> factory) {
        ReporterFactory.factory = factory;
    }
}
