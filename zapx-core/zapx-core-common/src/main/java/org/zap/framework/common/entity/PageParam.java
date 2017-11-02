package org.zap.framework.common.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 *
 * 页面JSP渲染参数
 *
 * @author Shin
 *
 */
public class PageParam implements Serializable{

	/**
	 * 当前模块
	 */
	private String module;
	/**
	 * 当前菜单
	 */
	private String function;
	/**
	 * 当前页面
	 */
	private String page;

	/**
	 * 当前页面编码
	 */
	private String no;
	/**
	 * 父页面编码
	 */
	private String parentPageNo;
	/**
	 * 父页面主键
	 */
	private String parentPageId;
	/**
	 * 
	 */
	private Map<String, Object> params = new HashMap<String, Object>();

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getParentPageNo() {
		return parentPageNo;
	}

	public void setParentPageNo(String parentPageNo) {
		this.parentPageNo = parentPageNo;
	}

	public String getParentPageId() {
		return parentPageId;
	}

	public void setParentPageId(String parentPageId) {
		this.parentPageId = parentPageId;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public String toString() {
		//陷阱
		StringBuffer url = new StringBuffer();

		if (StringUtils.isNotBlank(module)) {
			url.append(module).append("/");
		}
		if (StringUtils.isNotBlank(function)) {
			url.append(function).append("/");
		}
		url.append(page);
		return url.toString();
	}


}
