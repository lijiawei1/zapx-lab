package org.zap.framework.orm.sql.criteria;

import org.zap.framework.orm.sql.Criteria;
import org.zap.framework.orm.sql.Table;
import org.zap.framework.orm.sql.output.Output;
import org.zap.framework.orm.sql.output.Outputable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * See OR and AND
 * 
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 */
public abstract class MultiLogicGroup extends Criteria {
    private String operator;
    private Collection<Criteria> criterias;

    public MultiLogicGroup(String operator, Criteria[] criterias) {
        this.criterias = Arrays.asList(criterias);
        this.operator = operator;
    }

    public void write(Output out) {
        out.print("(");
        
        Iterator<? extends Outputable> i = criterias.iterator();
        boolean hasNext = i.hasNext();

        while (hasNext) {
            Outputable curr = (Outputable) i.next();
            hasNext = i.hasNext();
            curr.write(out);
            out.print(' ');
            if (hasNext) {
                out.print(operator);
            }
            out.print(' ');
        }
        out.print(")");
    }

	public void addReferencedTablesTo(Set<Table> tables) {
		
		for (Criteria c : criterias) {
			c.addReferencedTablesTo(tables);
		}
	}
}
