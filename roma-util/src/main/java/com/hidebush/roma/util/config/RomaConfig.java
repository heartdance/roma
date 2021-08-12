package com.hidebush.roma.util.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by htf on 2021/8/12.
 */
public class RomaConfig {

    private final Map<Option<?>, Object> config = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(Option<T> option) {
        return (T) config.getOrDefault(option, option.defaultValue());
    }

    public <T> void set(Option<T> option, Object value) {
        check(option.type(), value);
        config.put(option, value);
    }

    private static void check(Class<?> type, Object value) {
        if (!type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(value.getClass().getName() + " not a " + type.getName());
        }
    }
}
