package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.output.Output;

import java.util.Set;

public class Parameter implements Matchable {
	public void write(Output out) {
		out.print("?");
	}

	public void addReferencedTablesTo(Set<Table> tables) {
	}
}
