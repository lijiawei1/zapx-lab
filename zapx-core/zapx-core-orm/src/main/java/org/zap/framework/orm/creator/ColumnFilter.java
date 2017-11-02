package org.zap.framework.orm.creator;

import org.apache.commons.lang.StringUtils;
import org.zap.framework.orm.exception.ExEnum;
import org.zap.framework.orm.exception.DaoException;

import java.util.HashSet;
import java.util.Set;

/**
 * 字段过滤器
	include 并且 filter.contain(col)      包含模式 且 校验字段在包含范围里
	exclude 并且 filter.contain(col)      排除模式 且 校验字段不在排除范围里
 *
 */
public class ColumnFilter {

	private String[] columns;
	
	private boolean include = false;
	
	private Set<String> filter = new HashSet<String>(); 
	
	public ColumnFilter(String[] columns, boolean include) {
		
		if (columns == null)
			throw new DaoException(ExEnum.PARAMS_IS_NULL.toString());
		
		this.columns = columns;
		this.include = include;
		
		if (columns != null && columns.length > 0) {
			for (int i =0 ; i < columns.length; i++) {
				filter.add(StringUtils.upperCase(columns[i]));
			}
		}
	}
	
	/**
	 * 过滤器包含的属性都被过滤掉
	 * @param col
	 */
	public boolean contain(String col) {
		return (include ? !filter.contains(col) : filter.contains(col));
	}

	
	public int length () {
		return columns == null ? 0 : (include ? columns.length : -columns.length);
	}

	public void validate(String[] validColumns) {
		if (filter.size() > 0) {
			HashSet<String> validSet = new HashSet<String>();
			for (int i = 0; i < validColumns.length; i++) {
				if (filter.contains(validColumns[i])) {
					validSet.add(validColumns[i]);
				}
			}
			filter.clear();
			filter.addAll(validSet);
			columns = validSet.toArray(new String[validSet.size()]);
		}
	}

	public boolean isInclude() {
		return include;
	}

}
