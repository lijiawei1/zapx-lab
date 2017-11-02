package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.output.Outputable;

import java.util.Set;

/**
 * A literal value, such as a number, string or boolean.
 * 
 * @author Nat Pryce
 * 
 */
public abstract class Literal implements Outputable, Matchable, Selectable {
	public void addReferencedTablesTo(Set<Table> tables) {
	}
}
