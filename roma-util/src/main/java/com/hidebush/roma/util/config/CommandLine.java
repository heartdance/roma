package com.hidebush.roma.util.config;

import java.util.*;

/**
 * Created by htf on 2021/8/12.
 */
public class CommandLine {

    private final Option<?>[] options;
    private final Map<String, Option<?>> optionMap = new HashMap<>();

    public CommandLine(Option<?>... options) {
        this.options = options;
        for (Option<?> option : options) {
            if (option == null) {
                throw new NullPointerException("option");
            }
            String[] keys = option.keys();
            for (String key : keys) {
                Option<?> existed = optionMap.get(key);
                if (existed != null) {
                    throw new IllegalArgumentException("same key of " + existed.name() + " and " + option.name());
                }
                optionMap.put(key, option);
            }
        }
    }

    public RomaConfig parse(String... parameters) {
        RomaConfig romaConfig = new RomaConfig();
        for (int i = 0; i < parameters.length; i++) {
            String parameterName = parameters[i];
            if (parameterName.startsWith("--")) {
                parameterName = parameterName.substring(2);
            } else if (parameterName.startsWith("-")) {
                parameterName = parameterName.substring(1);
            } else {
                throw new IllegalArgumentException("parameter name '" + parameterName + "' not format");
            }
            Option<?> option = optionMap.get(parameterName);
            if (option == null) {
                throw new IllegalArgumentException("unknown parameter name '" + parameterName + "'");
            }
            if (option.type() == Boolean.class) {
                romaConfig.set(option, true);
            } else {
                String parameterValue = parameters[++i];
                Object value = castType(option.type(), parameterValue);
                romaConfig.set(option, value);
            }
        }
        for (Option<?> option : options) {
            if (romaConfig.get(option) == null) {
                throw new NullPointerException("not found " + option.name());
            }
        }
        return romaConfig;
    }

    private static Object castType(Class<?> type, String parameter) {
        if (type == String.class) {
            return parameter;
        }
        if (type == Integer.class) {
            return Integer.parseInt(parameter);
        }
        if (type == Long.class) {
            return Long.parseLong(parameter);
        }
        if (type == StringList.class) {
            return new StringList(Arrays.asList(parameter.split(",")));
        }
        throw new IllegalArgumentException("not support cast type " + type.getName());
    }
}
