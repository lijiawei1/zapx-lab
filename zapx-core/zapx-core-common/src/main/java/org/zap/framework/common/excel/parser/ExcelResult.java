package org.zap.framework.common.excel.parser;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * excel错误
 *
 * Created by Shin on 2017/5/4.
 */
public class ExcelResult<T> implements Serializable {

    /**
     * 错误提示
     */
    String message;
    /**
     * 临时文件URL
     */
    String url;
    /**
     * 返回结果
     */
    List<ExcelResultWrapper<T>> data;

    /**
     * 返回值
     */
    Object value;

    boolean error = false;

    public ExcelResult() {
    }

    public ExcelResult(String message, boolean error) {
        this.message = message;
        this.error = error;
    }

    public ExcelResult(String message) {
        this.message = message;
    }

    public List<ExcelResultWrapper<T>> getData() {
        return data;
    }

    public void setData(List<ExcelResultWrapper<T>> data) {
        this.data = data;
    }

    public boolean isError() {
        if (!error && data != null) {
            for (ExcelResultWrapper<T> wrapper : data) {
                if (wrapper.getMsg() != null &&
                        wrapper.getMsg().getTemp() != null &&
                        wrapper.getMsg().getTemp().size() > 0) {
                    return (error = true);
                }
            }
        }
        return error;
    }

    public String getDetailMsg() {
        for (ExcelResultWrapper<T> wrapper : data) {

            if (wrapper.getMsg() != null &&
                    wrapper.getMsg().getTemp() != null &&
                    wrapper.getMsg().getTemp().size() > 0) {
                return StringUtils.join(wrapper.getMsg().getTemp(), ";");
            }
        }

        return "";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
