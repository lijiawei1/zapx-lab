package org.zap.framework.orm.page;

import java.util.ArrayList;
import java.util.List;

public class PaginationSupport<T> {
	
	public static final int DEFAULT_PAGESIZE = 50;	
	/**
	 * 数据对象
	 */
	private T entity;
	/**
	 * 数据对象
	 */
	private List<T> data = new ArrayList<T>();
	/**
	 * 当前页，0代表第一页
	 */
	private int currentPage = 0;
	/**
	 * 总页数
	 */
	private int pageCount = 0;
	/**
	 * 页大小
	 */
	private int pageSize = DEFAULT_PAGESIZE;
	/**
	 * 总记录数
	 */
	private int totalCount;
	/**
	 * 起始记录索引
	 */
	private int start = 0;
	/**
	 * 结束记录索引
	 */
	private int end = 0;
	/**
	 * 排序字段
	 */
	private String sortname;
	/**
	 * 排序顺序
	 */
	private String sortorder;
	
	public PaginationSupport<T> count() {
		start = currentPage * pageSize + 1;
		end = (currentPage + 1) * pageSize;
		return this;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getPageCount() {
		return pageCount;
	}
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
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
