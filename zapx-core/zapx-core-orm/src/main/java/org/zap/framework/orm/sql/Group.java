package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.output.Output;
import org.zap.framework.orm.sql.output.Outputable;

import java.util.Set;

/**
 * GROUP BY clause. See SelectQuery.addGroup(Group).
 * 
 * @author shin
 */
public class Group implements Outputable {
    
    private Column column;
    
    /**
     * @param column    Column to order by. ascending Order.ASCENDING or Order.DESCENDING
     */
    public Group(Column column) {
        this.column = column;
    }

    public Projection getColumn() {
        return column;
    }

    public void write(Output out) {
        column.write(out);
    }

	public void addReferencedTablesTo(Set<Table> tables) {
		column.addReferencedTablesTo(tables);
	}
}
