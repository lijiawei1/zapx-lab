package org.zap.framework.common.entity.pagination;

/**
 * 分页数据请求
 *
 */
public class PaginationRequest {
	
	int page;
	
	int pageSize = 65535;
	
	String sortName;
	
	String sortOrder;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	

}
