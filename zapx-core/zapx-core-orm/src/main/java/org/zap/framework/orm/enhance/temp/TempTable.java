package org.zap.framework.orm.enhance.temp;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JDBC增强，临时表
 * @author Shin
 *
 */
public class TempTable {

	static Logger logger  = Logger.getLogger(TempTable.class);
	
	private JdbcTemplate jdbcTemplate;
	
	public TempTable() {
		
	}
	
	public  String createTempTable(String tableName, String[] columns, String[] indexs) {
		
		if (StringUtils.isBlank(tableName) || columns == null || indexs == null)
			return null;
		
		try {
			jdbcTemplate.update("DELECT FROM " + tableName);
		} catch (DataAccessException e) {
			//表不存在
			create(tableName, columns, indexs);
		}
		return tableName;
	}
	
	public  String create(String tableName, String[] columns, String[] indexs) {
		try {
			//建立临时表
			StringBuilder sqlBuilder= new StringBuilder("CREATE GLOBAL TEMPORARY TABLE ");
			sqlBuilder.append(tableName);
			sqlBuilder.append("(").append(columns[0]);
			
			if (columns.length > 1) {
				for (int i = 1; i < columns.length; i++) {
					sqlBuilder.append(",").append(columns[i]);
				}
			}
			sqlBuilder.append(") ON COMMIT DELETE ROWS ");
			
			jdbcTemplate.execute(sqlBuilder.toString());
		} catch (DataAccessException ex) {
			logger.error("Error creating temporary table：" + tableName , ex);
		}
		return tableName;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * drop临时表
	 * @param tempTable
	 */
	public void dropTempTable(Object tempTable) {
		jdbcTemplate.execute( "DROP TABLE " + tempTable);
	}
	
	
	
}
