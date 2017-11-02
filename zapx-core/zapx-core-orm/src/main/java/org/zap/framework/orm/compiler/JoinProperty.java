package org.zap.framework.orm.compiler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JoinProperty {

	private Field[] joinFields;
	
	private String[] joinColumns;
	
	private String[] joinSqls;
	
	private Class<?> clazz;
	
	private Map<String, String> fieldAlias;
	
	private List<MainProp> mainPropList = new ArrayList<MainProp>();
	
	public List<MainProp> getMainPropList() {
		return mainPropList;
	}

	public void setMainPropList(List<MainProp> mainPropList) {
		this.mainPropList = mainPropList;
	}

	public static class MainProp {
		
		String foreignKey;
		String primaryKey;
		String tableName;
		String tableAlias;
		
		List<ColProp> colList;

		public MainProp(String primaryKey, String foreignKey, String tableName, String tableAlias) {
			super();
			this.primaryKey = primaryKey;
			this.foreignKey = foreignKey;
			this.tableName = tableName;
			this.tableAlias = tableAlias;
			this.colList = new ArrayList<ColProp>();
		}
		
		public String getPrimaryKey() {
			return primaryKey;
		}


		public void setPrimaryKey(String primaryKey) {
			this.primaryKey = primaryKey;
		}


		public String getForeignKey() {
			return foreignKey;
		}
		public void setForeignKey(String foreignKey) {
			this.foreignKey = foreignKey;
		}
		public String getTableName() {
			return tableName;
		}
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
		public String getTableAlias() {
			return tableAlias;
		}
		public void setTableAlias(String tableAlias) {
			this.tableAlias = tableAlias;
		}
		public List<ColProp> getColList() {
			return colList;
		}
		public void setColList(List<ColProp> colList) {
			this.colList = colList;
		}
	}
	
	public static class ColProp {
		//tableAlias.columnName as fieldName
		Field field;
		String columnName;
		String fieldName;
		String tableAlias;
		
		public ColProp(Field field, String columnName, String fieldName,
				String tableAlias) {
			super();
			this.field = field;
			this.columnName = columnName;
			this.fieldName = fieldName;
			this.tableAlias = tableAlias;
		}
		public Field getField() {
			return field;
		}
		public void setField(Field field) {
			this.field = field;
		}
		public String getColumnName() {
			return columnName;
		}
		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
		public String getFieldName() {
			return fieldName;
		}
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}
		public String getTableAlias() {
			return tableAlias;
		}
		public void setTableAlias(String tableAlias) {
			this.tableAlias = tableAlias;
		}
	}
	
	public String[] getJoinColumns() {
		return joinColumns;
	}

	public String[] getJoinSqls() {
		return joinSqls;
	}

	public JoinProperty(Class<?> clz) {
		this.clazz = clz;
	}

	public void setJoinColumns(String[] joinColumns) {
		this.joinColumns = joinColumns;
	}

	public void setJoinSqls(String[] joinSqls) {
		this.joinSqls = joinSqls;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Field[] getJoinFields() {
		return joinFields;
	}

	public void setJoinFields(Field[] joinFields) {
		this.joinFields = joinFields;
	}

	public Map<String, String> getFieldAlias() {
		return fieldAlias;
	}

	public void setFieldAlias(Map<String, String> fieldAlias) {
		this.fieldAlias = fieldAlias;
	}

}
