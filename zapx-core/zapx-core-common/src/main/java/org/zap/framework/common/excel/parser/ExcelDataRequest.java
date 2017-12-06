package org.zap.framework.common.excel.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.zap.framework.util.SqlUtils;

import java.io.Serializable;

/**
 * Created by Shin on 2017/12/4.
 */
public class ExcelDataRequest implements Serializable {

    /**
     * 文件名称
     */
    @JsonIgnore
    String file_name;
    /**
     * 来源公司
     */
    @JsonIgnore
    String from_company;
    /**
     * 头字段
     */
    @JsonIgnore
    String headers;
    /**
     * 值字段
     */
    @JsonIgnore
    String names;
    /**
     * 过滤条件
     */
    String where;
    /**
     * 排序
     */
    String sortname;
    /**
     * 排序
     */
    String sortorder;
    /**
     * 页码
     */
    int page = 0;
    /**
     * 导出65534条数据
     */
    int pagesize = 65534;

    public String getOrderBy() {
        return SqlUtils.getSortPart(sortname, sortorder);
//        return StringUtils.isNotBlank(sortname) ? String.format(" ORDER BY %s %s ", sortname, sortorder) : "";

    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getFrom_company() {
        return from_company;
    }

    public void setFrom_company(String from_company) {
        this.from_company = from_company;
    }

    public String getWhere() {
        return where;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getSortname() {
        return sortname;
    }

    public void setSortname(String sortname) {
        this.sortname = sortname;
    }

    public String getSortorder() {
        return sortorder;
    }

    public void setSortorder(String sortorder) {
        this.sortorder = sortorder;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }
}
