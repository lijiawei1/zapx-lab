package org.zap.framework.orm.sql.criteria;

import org.zap.framework.orm.sql.Column;
import org.zap.framework.orm.sql.Criteria;
import org.zap.framework.orm.sql.Matchable;
import org.zap.framework.orm.sql.output.Output;
import org.zap.framework.orm.sql.Table;
import org.zap.framework.orm.sql.output.Outputable;

import java.util.Set;

public class LeftJoinCriteria extends Criteria {

	private final Outputable srcTable;
    private final Matchable left;
    private final String operator;
    private final Matchable right;
	private final Outputable destTable;
	
    
    public LeftJoinCriteria(Table srcTable, Matchable left, String operator, Table destTable, Matchable right) {
    	this.srcTable = srcTable;
        this.left = left;
        this.operator = operator;
        this.right = right;
        this.destTable = destTable;
    }
    
	@Override
	public void write(Output out) {
		out.print(" LEFT JOIN ");
		destTable.write(out);
		out.print(" ON ")
		.print(((Table)srcTable).getAlias()).print('.').print(((Column)left).getName())
        .print(' ').print(operator).print(' ')
        .print(((Table)destTable).getAlias()).print('.').print(((Column)right).getName());
        
	}

	@Override
	public void addReferencedTablesTo(Set<Table> tables) {
		left.addReferencedTablesTo(tables);
		right.addReferencedTablesTo(tables);
	}

}
