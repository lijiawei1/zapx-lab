package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.output.Output;
import org.zap.framework.orm.sql.output.Outputable;

import java.util.Set;

/**
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 * @author Nat Pryce
 */
public abstract class Criteria implements Outputable {
    public abstract void write(Output out);
	public abstract void addReferencedTablesTo(Set<Table> tables);
}
