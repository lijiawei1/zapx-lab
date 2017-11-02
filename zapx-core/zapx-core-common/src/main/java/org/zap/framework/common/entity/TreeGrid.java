package org.zap.framework.common.entity;

import java.util.ArrayList;
import java.util.List;

public class TreeGrid {

	private List<TreeGrid> children = new ArrayList<TreeGrid>();

	public List<TreeGrid> getChildren() {
		return children;
	}

	public void setChildren(List<TreeGrid> children) {
		this.children = children;
	}
}
