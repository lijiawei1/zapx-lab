package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.criteria.MatchCriteria;
import org.zap.framework.orm.sql.output.ToStringer;
import org.zap.framework.orm.sql.criteria.LeftJoinCriteria;
import org.zap.framework.orm.sql.output.Output;
import org.zap.framework.orm.sql.output.Outputable;

import java.util.*;

/**
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 * @author Nat Pryce
 */
public class SelectQuery implements Outputable, ValueSet {
    public static final int indentSize = 4;
    
    private final List<Selectable> selection = new ArrayList<Selectable>();
    private final List<Criteria> leftCriteria = new ArrayList<Criteria>();
    private final List<Criteria> criteria = new ArrayList<Criteria>();
    private final List<Order> order = new ArrayList<Order>();
    private final List<Group> group = new ArrayList<Group>();
    
    private boolean isDistinct = false;

    private Table mainTable = null;
    
    public SelectQuery() {}
    
    public SelectQuery(Table mainTable) {
    	this.mainTable = mainTable;
    }
    
	public List<Table> listTables() {
    	LinkedHashSet<Table> tables = new LinkedHashSet<Table>();
    	addReferencedTablesTo(tables);
    	return new ArrayList<Table>(tables);
    }

    public void addToSelection(Selectable selectable) {
        selection.add(selectable);
    }

    /**
     * Syntax sugar for addToSelection(Column).
     */
    public void addColumn(Table table, String columname) {
        addToSelection(table.getColumn(columname));
    }
    
    public void addColumn(Column column) {
        addToSelection(column);
    }
    
    public void addColumn(String columname) {
    	addToSelection(getMainTable().getColumn(columname));
    }

    public void removeFromSelection(Selectable selectable) {
        selection.remove(selectable);
    }
    
    public void clearSelection() {
    	selection.clear();
    }

    /**
     * @return a list of {@link Selectable} objects.
     */
    public List<Selectable> listSelection() {
        return Collections.unmodifiableList(selection);
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    public void setDistinct(boolean distinct) {
        isDistinct = distinct;
    }

    public void addCriteria(Criteria criteria) {
        this.criteria.add(criteria);
    }
    
    public void addLeftCriteria(Criteria criteria) {
    	this.leftCriteria.add(criteria);
    }

    public void removeCriteria(Criteria criteria) {
        this.criteria.remove(criteria);
    }

    public List<Criteria> listCriteria() {
        return Collections.unmodifiableList(criteria);
    }

    public void addLeftJoin(Table srcTable, String srcColumnname, Table destTable, String destColumnname) {
    	addLeftCriteria(new LeftJoinCriteria(srcTable, srcTable.getColumn(srcColumnname), MatchCriteria.EQUALS, destTable, destTable.getColumn(destColumnname)));
    }
    
    /**
     * Syntax sugar for addCriteria(JoinCriteria)
     */
    public void addJoin(Table srcTable, String srcColumnname, Table destTable, String destColumnname) {
        addCriteria(new MatchCriteria(srcTable.getColumn(srcColumnname), MatchCriteria.EQUALS, destTable.getColumn(destColumnname)));
    }

    /**
     * Syntax sugar for addCriteria(JoinCriteria)
     */
    public void addJoin(Table srcTable, String srcColumnName, String operator, Table destTable, String destColumnName) {
        addCriteria(new MatchCriteria(srcTable.getColumn(srcColumnName), operator, destTable.getColumn(destColumnName)));
    }

    public void addOrder(Order order) {
        this.order.add(order);
    }
    
    public void addGroup(Group group) {
        this.group.add(group);
    }

    /**
     * Syntax sugar for addOrder(Order).
     */
    public void addOrder(Table table, String columnname, boolean ascending) {
        addOrder(new Order(table.getColumn(columnname), ascending));
    }

    public void removeOrder(Order order) {
        this.order.remove(order);
    }

    public List<Order> listOrder() {
        return Collections.unmodifiableList(order);
    }

    public String toString() {
        return ToStringer.toString(this);
    }

    public void write(Output out) {
        out.print("SELECT");
        if (isDistinct) {
            out.print(" DISTINCT");
        }
        out.println();

        appendIndentedList(out, selection, ",");

        Set<Table> tables = findAllUsedTables();
        if (!tables.isEmpty()) {
	        out.println("FROM");

	        if (leftCriteria.size() > 0) {
		        mainTable.write(out);
	        	appendList(out, leftCriteria, " ");
	        } else {
	        	appendIndentedList(out, tables, ",");
	        }
        }

        // Add criteria
        if (criteria.size() > 0) {
            out.println("WHERE");
            appendIndentedList(out, criteria, "AND");
        }

        //Add group
        if (group.size() > 0) {
        	out.print("GROUP BY ");
        	appendIndentedList(out, group, ",");
        }
        
        // Add order
        if (order.size() > 0) {
            out.println("ORDER BY");
            appendIndentedList(out, order, ",");
        }
    }

    private void appendIndentedList(Output out, Collection<? extends Outputable> things, String seperator) {
        out.indent();
        appendList(out, things, seperator);
        out.unindent();
    }

    /**
     * Iterate through a Collection and append all entries (using .toString()) to
     * a StringBuffer.
     */
    private void appendList(Output out, Collection<? extends Outputable> collection, String seperator) {
        Iterator<? extends Outputable> i = collection.iterator();
        boolean hasNext = i.hasNext();

        while (hasNext) {
            Outputable curr = (Outputable) i.next();
            hasNext = i.hasNext();
            curr.write(out);
            out.print(' ');
            if (hasNext) {
                out.print(seperator);
            }
            out.println();
        }
    }
    
    /**
     * Find all the tables used in the query (from columns, criteria and order).
     *
     * @return Set of {@link Table}s
     */
    private Set<Table> findAllUsedTables() {
        Set<Table> tables = new LinkedHashSet<Table>();
        addReferencedTablesTo(tables);
        return tables;
    }
    
    public void addReferencedTablesTo(Set<Table> tables) {
        for (Selectable s : selection) {
            s.addReferencedTablesTo(tables);
        }
        for (Criteria c : leftCriteria) {
            c.addReferencedTablesTo(tables);
        }
        for (Criteria c : criteria) {
            c.addReferencedTablesTo(tables);
        }
        for (Order o : order) {
            o.addReferencedTablesTo(tables);
        }
    }
    
    public Table getMainTable() {
		return mainTable;
	}

	public void setMainTable(Table mainTable) {
		this.mainTable = mainTable;
	}
}
