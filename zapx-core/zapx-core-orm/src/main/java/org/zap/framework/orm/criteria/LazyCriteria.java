package org.zap.framework.orm.criteria;

import org.zap.framework.orm.sql.Criteria;
import org.zap.framework.orm.sql.Table;
import org.zap.framework.orm.sql.output.Output;

import java.util.List;
import java.util.Set;

public abstract class LazyCriteria extends Criteria {
	
	public abstract Criteria toCriteria(Table table, List<Object> paramList);
	
	public abstract String getColumn();

	@Override
	public void write(Output out) {
	}

	@Override
	public void addReferencedTablesTo(Set<Table> tables) {
		
	}
	
}
