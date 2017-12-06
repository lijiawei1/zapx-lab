package org.zap.framework.common.excel.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shin on 2017/5/10.
 */
public class ExcelBusiHolder<T> {

    List<T> appendList = new ArrayList<>();

    List<T> updateList = new ArrayList<>();

    public List<T> getAppendList() {
        return appendList;
    }

    public void setAppendList(List<T> appendList) {
        this.appendList = appendList;
    }

    public List<T> getUpdateList() {
        return updateList;
    }

    public void setUpdateList(List<T> updateList) {
        this.updateList = updateList;
    }
}
