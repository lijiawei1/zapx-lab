package org.zap.framework.common.excel.parser;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Shin on 2017/12/4.
 */
public class ExcelImportRequest implements Serializable {

    /**
     * 业务主键
     */
    String pk_id;
    /**
     * 上传控件列表
     */
    String name;
    /**
     * 列名配置
     */
    Map<String, String> meta;

    /**
     * 是否覆盖，一般是追加
     */
    boolean cover;
    /**
     * 只需要包含
     */
    boolean only_contain;
    /**
     * 忽略错误提示更新
     */
    boolean ignore_error;
    /**
     * 列名起始行
     */
    int header_row = 0;
    /**
     * 数据起始行
     */
    int data_start_row = 1;

    public int getHeader_row() {
        return header_row;
    }

    public void setHeader_row(int header_row) {
        this.header_row = header_row;
    }

    public int getData_start_row() {
        return data_start_row;
    }

    public void setData_start_row(int data_start_row) {
        this.data_start_row = data_start_row;
    }

    public String getPk_id() {
        return pk_id;
    }

    public void setPk_id(String pk_id) {
        this.pk_id = pk_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCover() {
        return cover;
    }

    public void setCover(boolean cover) {
        this.cover = cover;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }

    public boolean isOnly_contain() {
        return only_contain;
    }

    public void setOnly_contain(boolean only_contain) {
        this.only_contain = only_contain;
    }

    public boolean isIgnore_error() {
        return ignore_error;
    }

    public void setIgnore_error(boolean ignore_error) {
        this.ignore_error = ignore_error;
    }
}
