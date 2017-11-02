package org.zap.framework.orm.criteria;

import org.zap.framework.orm.sql.Criteria;
import org.zap.framework.orm.sql.Table;
import org.zap.framework.orm.sql.output.Output;

import java.util.Set;

public class TempCriteria extends Criteria {

	String type;
	String field;
	Object value;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public TempCriteria(String field, Object value, String type) {
		super();
		this.type = type;
		this.field = field;
		this.value = value;
	}

	@Override
	public void addReferencedTablesTo(Set<Table> tables) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(Output out) {
		// TODO Auto-generated method stub
		
	}

}
