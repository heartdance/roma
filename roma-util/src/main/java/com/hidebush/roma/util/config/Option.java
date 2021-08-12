package com.hidebush.roma.util.config;

/**
 * Created by htf on 2021/8/12.
 */
public final class Option<T> {

    private final String name;
    private final T defaultValue;
    private final Class<T> type;
    private final String[] keys;

    public Option(String name, T defaultValue, Class<T> type, String... keys) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name");
        }
        if (type == null) {
            throw new IllegalArgumentException(new NullPointerException("type"));
        }
        this.name = name;
        this.defaultValue = defaultValue;
        this.type = type;
        if (keys == null || keys.length == 0) {
            this.keys = new String[] {name};
        } else {
            for (String key : keys) {
                if (key == null || key.isEmpty()) {
                    throw new IllegalArgumentException("keys");
                }
            }
            this.keys = keys;
        }
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public String[] keys() {
        return keys;
    }
}
