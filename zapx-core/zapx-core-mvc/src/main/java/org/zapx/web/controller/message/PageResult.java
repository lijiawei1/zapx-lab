package org.zapx.web.controller.message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/25 0025.
 */
@ApiModel("响应")
public class PageResult<T> implements Serializable {

    /**
     * 状态码
     */
    private int code;
    /**
     * 是否异常
     */
    private boolean error;
    /**
     * 调用返回消息
     */
    private String message;
    /**
     * 成功响应数据
     */
    private T data;
    /**
     * 时间戳
     */
    private long timestamp;

    @ApiModelProperty(value = "状态码", required = true)
    public int getCode() {
        return code;
    }

    @ApiModelProperty(value = "是否异常", required = true)
    public boolean isError() {
        return error;
    }

    @ApiModelProperty(value = "调用返回消息", required = true)
    public String getMessage() {
        return message;
    }

    @ApiModelProperty(value = "成功响应数据", required = true)
    public T getData() {
        return data;
    }

    @ApiModelProperty(value = "时间戳", required = true, dataType = "long")
    public long getTimestamp() {
        return timestamp;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
