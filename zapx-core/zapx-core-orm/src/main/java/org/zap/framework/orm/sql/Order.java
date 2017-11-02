package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.output.Output;
import org.zap.framework.orm.sql.output.Outputable;

import java.util.Set;

/**
 * ORDER BY clause. See SelectQuery.addOrder(Order).
 * 
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 */
public class Order implements Outputable {
    public static final boolean ASCENDING = true;
    public static final boolean DESCENDING = false;
    
    private Column column;
    private boolean ascending;
    
    /**
     * @param column    Column to order by.
     * @param ascending Order.ASCENDING or Order.DESCENDING
     */
    public Order(Column column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
    }

    public Projection getColumn() {
        return column;
    }

    public void write(Output out) {
        column.write(out);
        if (!ascending) {
            out.print(" DESC");
        }
    }

	public void addReferencedTablesTo(Set<Table> tables) {
		column.addReferencedTablesTo(tables);
	}
}
