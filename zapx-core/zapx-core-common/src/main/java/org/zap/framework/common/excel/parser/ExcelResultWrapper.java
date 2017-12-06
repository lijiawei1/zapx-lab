package org.zap.framework.common.excel.parser;

/**
 * Created by Shin on 2017/5/4.
 */
public class ExcelResultWrapper<T> {

    public ExcelResultWrapper() {}

    public ExcelResultWrapper(T data, ExcelResultMsg msg) {
        this.data = data;
        this.msg = msg;
    }

    T data;

    ExcelResultMsg msg;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ExcelResultMsg getMsg() {
        return msg;
    }

    public void setMsg(ExcelResultMsg msg) {
        this.msg = msg;
    }
}
