package com.inmobi.template.context;

import java.util.HashMap;
import java.util.Map;

public class CreativeBean {

    private transient Map<String, Object> params = new HashMap<String, Object>();

    public CreativeBean(final String key, final Object value) {
        set(key, value);
    }

    public Object get(final String key) {
        return params.get(key);
    }

    public void set(final String key, final Object value) {
        params.put(key, value);
    }
}
