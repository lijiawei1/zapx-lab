package org.zap.framework.common.excel.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.zap.framework.util.SqlUtils;

import java.io.Serializable;

/**
 *
 * EXCEL报表请求内容
 *
 * Created by Shin on 2017/12/4.
 */
public class ExcelDataRequest implements Serializable {

    /**
     * 业务字段，可拓展，来源公司
     */
    @JsonIgnore
    String from_company;
    /**
     * 文件名称
     */
    @JsonIgnore
    String file_name;
    /**
     * 报表列名字段，逗号隔开
     */
    @JsonIgnore
    String headers;
    /**
     * 实体、数据表对应的值字段， 逗号隔开，和headers的顺序一一对应
     */
    @JsonIgnore
    String names;
    /**
     * 报表尾业务规则，一般是合计，可以自定义，# 表示不做任何处理
     */
    @JsonIgnore
    String footers;
    /**
     * 过滤条件，JSON化
     */
    String where;
    /**
     * 排序字段名，逗号分隔
     */
    String sortname;
    /**
     * 每个字段的排序规则，逗号分隔
     */
    String sortorder;
    /**
     * 当前页码，从0开始
     */
    int page = 0;
    /**
     * 导出60000条数据
     * 低版本EXCEL最大值
     */
    int pagesize = 60000;

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

    public String getFooters() {
        return footers;
    }

    public void setFooters(String footers) {
        this.footers = footers;
    }
}
