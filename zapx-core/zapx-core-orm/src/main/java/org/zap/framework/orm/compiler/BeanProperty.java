package org.zap.framework.orm.compiler;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Shin
 *
 */
public class BeanProperty implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4890540553088645447L;
	
	/** bean class*/
	private Class<?> clazz;
	
	private Map<String, Field> fieldMap;
	
	/** bean fields */
	private Field[] fields;
	/**
	 * 表字段名
	 */
	private String[] columns;
	/**
	 * 字段数据库类型
	 */
	private Integer[] types;
	/**
	 * 是否受版本控制
	 */
	private boolean versionControl;
	/**
	 * 版本字段
	 */
	private Field versionField;
	/**
	 * 版本字段名称
	 */
	private String versionColumn;
	/**
	 * 主键字段
	 */
	private Field[] primaryFields;
	/**
	 * 主键名称
	 */
	private String[] primaryKeys;
	/**
	 * 默认值
	 */
	private Object[] defaultValues;
	/**
	 * 表名称
	 */
	private String tableName;
	/**
	 * 表别名
	 */
	private String tableAlias;
	/**
	 * 是否视图
	 */
	private boolean view;
	
	/**
	 * 类级注解
	 */
	private List<Annotation> classAnnotations;
	/**
	 * 字段级注解
	 */
	private Map<Field, Annotation[]> fieldAnnotations;
	
	/**
	 * @param clz 
	 * 
	 */
	public BeanProperty(Class<?> clz) {
		this.clazz = clz;
	}

	/**
	 * @return the clazz
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 */
	public Field[] getFields() {
		return fields;
	}

	/**
	 * @return the classAnnotations
	 */
	public List<Annotation> getClassAnnotations() {
		if (classAnnotations == null) {
			classAnnotations = new ArrayList<Annotation>();
		}
		return classAnnotations;
	}

	/**
	 * @param classAnnotations the classAnnotations to set
	 */
	public void setClassAnnotations(List<Annotation> classAnnotations) {
		this.classAnnotations = classAnnotations;
	}

	/**
	 * @return the fieldAnnotations
	 */
	public Map<Field, Annotation[]> getFieldAnnotations() {
		if (fieldAnnotations == null) {
			fieldAnnotations = new HashMap<Field, Annotation[]>();
		}
		return fieldAnnotations;
	}

	/**
	 * @param fieldAnnotations the fieldAnnotations to set
	 */
	public void setFieldAnnotations(Map<Field, Annotation[]> fieldAnnotations) {
		this.fieldAnnotations = fieldAnnotations;
	}

	public String getTableName() {
		return tableName;
	}

	public String[] getColumns() {
		return columns;
	}

	public Object[] getDefaultValues() {
		return defaultValues;
	}

	public Field[] getPrimaryFields() {
		return primaryFields;
	}

	public String[] getPrimaryKeys() {
		return primaryKeys;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
	}

	public void setPrimaryFields(Field[] primaryFields) {
		this.primaryFields = primaryFields;
	}

	public void setPrimaryKeys(String[] primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	public void setDefaultValues(Object[] defaultValues) {
		this.defaultValues = defaultValues;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Field getVersionField() {
		return versionField;
	}

	public void setVersionField(Field versionField) {
		this.versionField = versionField;
	}

	public String getVersionColumn() {
		return versionColumn;
	}

	public void setVersionColumn(String versionColumn) {
		this.versionColumn = versionColumn;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	public Map<String, Field> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, Field> fieldMap) {
		this.fieldMap = fieldMap;
	}

	public boolean isVersionControl() {
		return versionControl;
	}

	public void setVersionControl(boolean versionControl) {
		this.versionControl = versionControl;
	}

	public Integer[] getTypes() {
		return types;
	}

	public void setTypes(Integer[] types) {
		this.types = types;
	}

	public boolean isView() {
		return view;
	}

	public void setView(boolean view) {
		this.view = view;
	}
}
