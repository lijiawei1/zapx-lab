package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.output.Output;

/**
 * Special column to represent For SELECT * FROM ...
 * 
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 * @author Nat Pryce
 */
public class WildCardColumn extends Projection {
    public WildCardColumn(Table table) {
        super(table);
    }

	public void write(Output out) {
        out.print(getTable().getAlias()).print(".*");
	}
}
