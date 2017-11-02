package org.zap.framework.orm.sql.criteria;

import org.zap.framework.orm.sql.Criteria;
import org.zap.framework.orm.sql.Matchable;
import org.zap.framework.orm.sql.Table;
import org.zap.framework.orm.sql.output.Output;

import java.util.Set;

public class IsNotNullCriteria extends Criteria {
	private final Matchable matched;
	
	public IsNotNullCriteria(Matchable matched) {
		this.matched = matched;
	}

	@Override
	public void write(Output out) {
		matched.write(out);
		out.print(" IS NOT NULL");
	}

	public void addReferencedTablesTo(Set<Table> tables) {
		matched.addReferencedTablesTo(tables);
	}
}
