package org.zap.framework.common.excel.jxls;

import java.util.Collection;

public class GridData {
	Object item;
	Collection items;
	public GridData(Object item, Collection headItems) {
		// TODO Auto-generated constructor stub
	    this.item = item;
	    this.items = headItems;
	}
	public Object getItem() {
		return item;
	}
	public void setItem(Object item) {
		this.item = item;
	}
	public Collection getItems() {
		return items;
	}
	public void setItems(Collection items) {
		this.items = items;
	}

	 	
}
