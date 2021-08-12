package com.hidebush.roma.util.config;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by htf on 2021/8/12.
 */
public class StringList extends ArrayList<String> {

    public StringList(int initialCapacity) {
        super(initialCapacity);
    }

    public StringList() {
    }

    public StringList(Collection<? extends String> c) {
        super(c);
    }
}
