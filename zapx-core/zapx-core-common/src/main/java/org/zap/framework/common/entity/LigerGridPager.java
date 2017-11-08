package org.zap.framework.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.zap.framework.orm.page.PaginationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页
 *
 * @param <T>
 * @author Shin
 */
public class LigerGridPager<T> {

    /**
     * 当前页码 默认不分页
     */
    int page = 1;
    /**
     * 当前每页条数 默认不分页
     */
    int pagesize = 9999;
    /**
     * 数据行
     */
    @JsonProperty(value = "Rows")
    List<T> rows = new ArrayList<T>();
    /**
     * 记录总数
     */
    @JsonProperty(value = "Total")
    int total = 0;

    String sortname;

    String sortorder;

    public LigerGridPager() {
    }

    public LigerGridPager(PaginationSupport<T> queryPage) {
        page = queryPage.getCurrentPage() + 1;
        pagesize = queryPage.getPageSize();
        total = queryPage.getTotalCount();
        rows = queryPage.getData();

    }

    public LigerGridPager(List<T> rows, int total) {
        this.total = total;
        this.rows = rows;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
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

}
