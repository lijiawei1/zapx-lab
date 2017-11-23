package org.zap.framework.orm.creator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.orm.annotation.JdbcTable;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.exception.DaoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DeleteSqlCreator extends BaseCreator {

	private static Logger logger = LoggerFactory.getLogger(DeleteSqlCreator.class);
	
	protected static DeleteSqlCreator instance;
	
	public static DeleteSqlCreator getInstance() {
		if (instance == null) {
			instance = new DeleteSqlCreator();
		}
		return instance;
	}
	
	private DeleteSqlCreator() {}
	
	public StringBuilder createSql(Class<?> clazz) {
		
		checkBase(clazz);
		
		BeanProperty beanProperty = compiler.getBeanProperty(clazz);
		StringBuilder del = new StringBuilder("DELETE FROM ");
		del.append(beanProperty.getTableName());
		
		return del;
	}
	
	public StringBuilder createByPrimaryKeySql(Class<?> clazz, boolean withVersion) {
		
		StringBuilder del = createSql(clazz);
		
		BeanProperty beanProperty = compiler.getBeanProperty(clazz);
		String[] primaryKeys = beanProperty.getPrimaryKeys();

		del.append(" WHERE ");
		for (int i = 0; i < primaryKeys.length; i++) {
			del.append(primaryKeys[i]).append(" = ? AND ");
		}

		//版本更新字段
		if (beanProperty.isVersionControl() && withVersion) {
			del.append(beanProperty.getVersionColumn()).append(" = ?");
		} else {
			del.replace(del.length() - 4, del.length(), " ");
		}

		return del;
	}
	
	public StringBuilder createByClauseSql(Class<?> clazz, String clause) {
		StringBuilder createSql = createSql(clazz);
		if (StringUtils.isNotBlank(clause)) {
			createSql.append(" WHERE ").append(clause);
		}
		return createSql;
	}

	public List<Object[]> createParamList(Class<? extends Object> clazz, Object[] pojos, boolean withVersion) {
		List<Object[]> paramList = new ArrayList<>();
		//
		BeanProperty beanProperty = compiler.getBeanProperty(clazz);
		//
		Field[] primaryFields = beanProperty.getPrimaryFields();
		
		try {
			for (int i =0; i < pojos.length; i++) {
				Object[] params = new Object[primaryFields.length + (withVersion ? 1 : 0)];
				int k;
				for (k = 0; k < primaryFields.length; k++) {
					params[k] = primaryFields[k].get(pojos[i]);
					if (params[k] == null)
						throw new DaoException("Primary key not found");
				}

				//版本更新条件
				if (beanProperty.isVersionControl() && withVersion) {
					params[k] = beanProperty.getVersionField().get(pojos[i]);
				}

				paramList.add(params);
			}
		} catch (Exception e) {
			logger.debug(e.toString());
		}
		return paramList;
	}

	/**
	 * 暂时只提供单引号的转义
	 *
	 * @param clazz
	 * @param pojos
	 * @param withVersion
	 * @return
	 */
	public String[] createSqlWithParam(Class<? extends Object> clazz, Object[] pojos, boolean withVersion) {

		//检查是否标识注解
		JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
		if (annTable == null) {
			throw new DaoException("类型没有注解");
		}

		String[] sqls = new String[pojos.length];

		//从缓存中获取
		BeanProperty beanProperty = compiler.getBeanProperty(clazz);

		Field[] primaryFields = beanProperty.getPrimaryFields();
		String[] primaryKeys = beanProperty.getPrimaryKeys();

		try {
			for (int i = 0; i < pojos.length; i++) {

				StringBuilder del = new StringBuilder("DELETE FROM ").append(beanProperty.getTableName()).append(" WHERE ");

				//主键更新条件
				for (int k = 0; k < primaryKeys.length; k++) {
					Object o = primaryFields[k].get(pojos[i]);
					del.append(primaryKeys[k]).append(" = '").append(StringEscapeUtils.escapeSql(o == null ? "null" : o.toString())).append("' AND ");
				}

				//版本更新字段
				if (beanProperty.isVersionControl() && withVersion) {
					Object version = beanProperty.getVersionField().get(pojos[i]);
					del.append(beanProperty.getVersionColumn()).append(" = ").append(version);
				} else {
					del.replace(del.length() - 4, del.length(), " ");
				}

				sqls[i] = del.toString();

				logger.debug("DELETE SQL {}", sqls[i]);
			}
		} catch (IllegalAccessException e) {
			logger.error("", e);
		}

		return sqls;
	}

//	public static void main(String[] args) {
//		DeleteSqlCreator creator = DeleteSqlCreator.getInstance();
//		logger.debug(creator.createByPrimaryKeySql(TestVo.class).toString());
//		
//		TestVo[] vos = TestVoFactory.getInstance().create(10);
//		List<Object[]> createParamList = creator.createParamList(TestVo.class, vos);
//		for (Object[] obj : createParamList) {
//			logger.debug(Arrays.toString(obj));
//		}
//	}
	
}
