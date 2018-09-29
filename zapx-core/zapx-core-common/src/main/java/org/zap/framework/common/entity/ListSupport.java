package org.zap.framework.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class ListSupport {

    Object data;

    int total = 0;

    @JsonIgnore
    Map<String, Object> params = new HashMap<>();

    public ListSupport() {}

    public ListSupport(Object data, int total) {
        this.data = data;
        this.total = total;
    }

    public ListSupport(Object data, int total, Map<String, Object> params) {
        this.data = data;
        this.total = total;
        this.params = params;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
