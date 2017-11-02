package org.zap.framework.orm.compiler;

import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TableProperty {

	/**
	 * 标识索引
	 */
	private Map<String, Integer> label2IndexesMap = new HashMap<String, Integer>();
	/**
	 * 行类型
	 */
	private int[] columnTypes;
	/**
	 * 行索引
	 */
	private int[] columnIndexes;
	/**
	 * 行标示
	 */
	private String[] columnLabels;

	protected TableProperty(ResultSet rs) {
		try {
			int columnCount = rs.getMetaData().getColumnCount();
			
			columnIndexes = new int[columnCount];
			columnTypes = new int[columnCount];
			columnLabels = new String[columnCount];
			
			for (int i = 0; i < columnCount; i++) {
				columnTypes[i] = rs.getMetaData().getColumnType(i + 1);
				//字段统一大写
				columnLabels[i] = rs.getMetaData().getColumnLabel(i + 1).toUpperCase();
				
				columnIndexes[i] = i + 1;
				
				label2IndexesMap.put(columnLabels[i], columnIndexes[i]);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean containLabel(String label) {
		return label2IndexesMap.containsKey(label);
	}
	
	public int getIndex(String label) {
		Integer index = label2IndexesMap.get(StringUtils.upperCase(label));
		if (index == null)
			return -1;
		
		return index;
	}
	
	public int getType(String label) {
		Integer index = label2IndexesMap.get(StringUtils.upperCase(label));
		if (index == null) {
			return -1;
		}
		return columnTypes[index - 1];
	}
	
	public int[] getColumnTypes() {
		return columnTypes;
	}

	public void setColumnTypes(int[] columnTypes) {
		this.columnTypes = columnTypes;
	}

	public int[] getColumnIndexes() {
		return columnIndexes;
	}

	public void setColumnIndexes(int[] columnIndexes) {
		this.columnIndexes = columnIndexes;
	}

	public String[] getColumnLabels() {
		return columnLabels;
	}

	public void setColumnLabels(String[] columnLabels) {
		this.columnLabels = columnLabels;
	}
	
	
	
}
