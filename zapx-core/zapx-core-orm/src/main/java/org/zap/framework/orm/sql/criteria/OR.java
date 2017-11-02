package org.zap.framework.orm.sql.criteria;

import org.zap.framework.orm.sql.Criteria;

public class OR extends MultiLogicGroup {

	public OR(Criteria... criterias) {
		super("OR", criterias);
	}
	
}
