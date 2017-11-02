package org.zap.framework.common.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *  对应前台 ligerFilter 的检索规则数据
 * @author haojc
 *
 */
public class FilterGroup {

	public FilterGroup(){
		rules = new ArrayList<FilterRule>();
		op = "and";
	}
	
	public List<FilterRule> rules;
	public String op ;
	public List<FilterGroup> groups;
	
	public List<FilterRule> getRules() {
		return rules;
	}
	public void setRules(List<FilterRule> rules) {
		this.rules = rules;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public List<FilterGroup> getGroups() {
		return groups;
	}
	public void setGroups(List<FilterGroup> groups) {
		this.groups = groups;
	}
}
