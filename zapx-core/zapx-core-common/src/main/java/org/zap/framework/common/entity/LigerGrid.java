package org.zap.framework.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/6.
 */
public class LigerGrid {
    /**
     * 数据行
     */
    @JsonProperty(value = "Rows")
    Object rows;
    /**
     * 记录总数
     */
    @JsonProperty(value = "Total")
    int total = 0;

    @JsonIgnore
    Map<String, Object> params = new HashMap<>();

    public LigerGrid() {}

    public LigerGrid(Object rows, int total) {
        this.rows = rows;
        this.total = total;
    }

    public LigerGrid(Object rows, int total, Map<String, Object> params) {
        this.rows = rows;
        this.total = total;
        this.params = params;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Object getRows() {
        return rows;
    }

    public void setRows(Object rows) {
        this.rows = rows;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
