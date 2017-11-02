package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.output.Outputable;

import java.util.Set;

/**
 * Something that can be part of a match expression in a where clause
 * 
 * @author Nat Pryce
 */
public interface Matchable extends Outputable {
	void addReferencedTablesTo(Set<Table> tables);
}
