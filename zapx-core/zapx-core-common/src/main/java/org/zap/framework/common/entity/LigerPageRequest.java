package org.zap.framework.common.entity;

/**
 * 接收分页参数
 * @author Shin
 *
 */
public class LigerPageRequest {

	String mst_id;
	
	int page;
	
	int pagesize;
	
	String sortname;
	
	String sortorder;

	public String getMst_id() {
		return mst_id;
	}

	public void setMst_id(String mst_id) {
		this.mst_id = mst_id;
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
