package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.output.Outputable;

import java.util.Set;

/**
 * Something that can be returned from a select query
 * 
 * @author Nat Pryce
 */
public interface Selectable extends Outputable {
	void addReferencedTablesTo(Set<Table> tables);
}
