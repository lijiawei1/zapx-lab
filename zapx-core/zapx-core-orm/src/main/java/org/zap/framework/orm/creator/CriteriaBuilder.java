package org.zap.framework.orm.creator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.compiler.JoinProperty;
import org.zap.framework.orm.compiler.PreCompiler;
import org.zap.framework.orm.converter.DefaultConverter;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.page.PaginationSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询条件
 * @author Shin
 *
 * @param <T>
 */
@Deprecated
public class CriteriaBuilder<T> {

	static Logger logger = Logger.getLogger(CriteriaBuilder.class);
	
	BaseDao baseDao;
	
	Class<T> clazz;
	
	BeanProperty bp;
	
	JoinProperty jp;
	
	public CriteriaBuilder(BaseDao baseDao, Class<T> clazz) {
		this.baseDao = baseDao;
		this.clazz = clazz;
		this.bp = PreCompiler.getInstance().getBeanProperty(clazz);
		this.jp = PreCompiler.getInstance().getJoinProperty(clazz);
		this.orCriteria = new ArrayList<Criteria<T>>();
		this.paramList = new ArrayList<Object>();
		this.orderbyList = new ArrayList<String>();
		
	}
	
	/**
	 * sql语句
	 */
	private String command;
	/**
	 * sql条件
	 */
	protected List<Criteria<T>> orCriteria;
	/**
	 * 查询参数
	 */
	protected List<Object> paramList;
	/**
	 * 排序列表
	 */
	protected List<String> orderbyList;
	
	/**
	 */
	public String toSqlString(boolean debug) {
		if (orCriteria == null || orCriteria.size() <= 0)
			return "";

		StringBuffer sqlAll = new StringBuffer();
		boolean addOp = false;
		for (Criteria<T> cri : orCriteria) {
			if (!cri.isValid())
				continue;
			if (addOp) {
				sqlAll.append(cri.getOperator());
			} else {
				addOp = true;
			}
			
			sqlAll.append("(");
			//TODO 修改一下拼接
			StringBuffer sqlTemp = new StringBuffer();
			appendNoParam(sqlTemp, cri.getNoParam(), debug);
			appendSingleParam(sqlTemp, cri.getSingleParam(), debug);
			appendListParam(sqlTemp, cri.getListParam(), debug);
			appendBetweenParam(sqlTemp, cri.getBetweenParam(), debug);
			sqlAll.append(sqlTemp.toString());
			
			sqlAll.append(")");
//			sqlAll.append(" OR ");
		}
		
		//替换最后的OR
//		if (sqlAll.length() > 0)
//			sqlAll.replace(sqlAll.length() - 4, sqlAll.length(), "");
		
		//增加排序条件
		if (orderbyList != null && orderbyList.size() > 0) {
			sqlAll.append(" ORDER BY ").append(bp.getTableAlias()).append(".").append(StringUtils.join(orderbyList, "," + bp.getTableAlias() + "."));
		}
		return sqlAll.toString();
	}
	
	private StringBuffer appendNoParam(StringBuffer sql, List<String> list, boolean debug) {
		if (list == null)
			return sql;

		int n = list.size();
		for (int i = 0; i < n; i++) {
			sql.append(list.get(i));
			if (i < n - 1)
				sql.append(" AND ");
		}

		return sql;
	}
	
	private StringBuffer appendSingleParam(StringBuffer sql, List<Criteria.Condition> list, boolean debug) {
		if (list == null)
			return sql;
		
		if (sql.length() > 0 && list.size() > 0)
			sql.append(" AND ");
		
		for (int i =0; i < list.size(); i++) {
			Criteria.Condition p = list.get(i);
			if (bp.getFieldMap().containsKey(p.getProperty())) {
				sql.append(bp.getTableAlias()).append(".");
				if (debug) {
					//debug模式插入值
					sql.append(p.getClause().replaceFirst("[?]", convertDebug(p.getProperty(), p.getValue()).toString()));
				} else {
					paramList.add(convert(p.getProperty(), p.getValue()));
					sql.append(p.getClause());
				}
				if (i < list.size() - 1) {
					sql.append(" AND ");
				}
			} else if (jp != null && jp.getFieldAlias().containsKey(p.getProperty())) {
				sql.append(jp.getFieldAlias().get(p.getProperty()));
			}
		}
		return sql;
	}
	
	private StringBuffer appendListParam(StringBuffer sql, List<Criteria.Condition> list, boolean debug) {
		if (list == null)
			return sql;
		
		if (sql.length() > 0 && list.size() > 0)
			sql.append(" AND ");
		
		int n = list.size();
		for (int i = 0; i < n; i++) {
			
			Criteria.Condition p = list.get(i);
			List<Object> values = (List<Object>)p.getValue();
			
			if (bp.getFieldMap().containsKey(p.getProperty())) {
				sql.append(bp.getTableAlias()).append(".").append(p.getClause()).append("(");
				if (debug) {
					List<Object> joinlist = new ArrayList<Object>();
					for (Object v : values) {
						joinlist.add(convertDebug(p.getProperty(), v));
					}
					sql.append(StringUtils.join(joinlist, ","));
				} else {
					if (values.size() < 1000) {
						sql.append(StringUtils.repeat("?", ",", values.size()));
					} else {
						throw new DaoException("超过IN语句最大参数值1000");
					}
					for (Object v : values) {
						paramList.add(convert(p.getProperty(), v));
					}
				}
				sql.append(")");
				
				if (i < list.size() - 1) {
					sql.append(" AND ");
				}
			}
		}
		return sql;
	}
	
	private String appendBetweenParam(StringBuffer sql, List<Criteria.Condition> list, boolean debug) {
		if (list == null)
			return "";
		if (sql.length() > 0 && list.size() > 0)
			sql.append(" AND ");
		
		int n = list.size();
		for (int i = 0; i < n; i++) {
			
			Criteria.Condition p = list.get(i);
			if (bp.getFieldMap().containsKey(p.getProperty())) {
				sql.append(bp.getTableAlias()).append(".").append(p.getClause());
				
				List<Object> values = (List<Object>)p.getValue();
				
				if (debug) {
					//debug模式插入值
					sql.append(convertDebug(p.getProperty(), values.get(0)).toString())
					.append(" AND ")
					.append(convertDebug(p.getProperty(), values.get(1)).toString());
				} else {
					sql.append("?  AND ? ");
					for (Object v : values) {
						paramList.add(convert(p.getProperty(), v));
					}
				}
				if (i < list.size() - 1) {
					sql.append(" AND ");
				}
			} else {
				throw new DaoException("不存在字段：" + p.getProperty());
			}
		}
		return sql.toString();
	}
	
	private Object convert(String property, Object value) {
		return DefaultConverter.getInstance().convertValue(bp.getFieldMap().get(property).getType(), -9999, value);
	}
	
	private Object convertDebug(String property, Object value) {
		Class<?> type = bp.getFieldMap().get(property).getType();
		
		try {
			if (Integer.class.equals(type) || int.class.equals(type)) {
				if (value == null)
					return 0;
			} else if (Double.class.equals(type) || double.class.equals(type)) {
				if (value == null)
					return 0.0;
			} else if (Long.class.equals(type) || long.class.equals(type)) {
				if (value == null)
					return 0L;
			} else if (Short.class.equals(type) || short.class.equals(type)) {
				if (value == null) 
					return 0;
			} else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
				if (value != null && (Boolean)value) {
					return "Y";
				} else {
					return "N";
				}
			} else if (BigDecimal.class.equals(type)) {
				if (value == null) 
					return BigDecimal.ZERO;
			} else if (String.class.equals(type)) {
				if (value == null) {
					return "";
				} else {
					return "'" + value + "'";
				}
			} else if (LocalDate.class.equals(type)) {
				if (value != null) {
					return "'" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format((LocalDate)value) + "'";
				}
			} else if (LocalDateTime.class.equals(type)) {
				if (value != null) {
					return "'" + DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format((LocalDateTime)value) + "'";
				}
			} else if (LocalTime.class.equals(type)) {
				if (value != null) {
					return "'" + DateTimeFormatter.ofPattern("hh:mm:ss").format((LocalTime)value) + "'";
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return value;
	
	}
	
	public Criteria<T> createCriteria() {
		Criteria<T> criteria = new Criteria<T>(this);
		if (orCriteria.size() == 0) {
			orCriteria.add(criteria);
		}
		return criteria;
	}
	
	protected List<Object> getParamList() {
		return paramList;
	}

	protected void setParamList(List<Object> paramList) {
		this.paramList = paramList;
	}
	
	protected T max(String clause, String[] cols, Object[] params) {
		return baseDao.querySortByClause(clazz, cols, clause, params, false);
	}
	
	protected T min(String clause, String[] cols, Object[] params) {
		return baseDao.querySortByClause(clazz, cols, clause, params, true);
	}

	protected List<T> tree(String clause, Object[] params) {
		return baseDao.queryTreeByClause(clazz, clause, params);
	}
	
	protected List<T> list(String clause, Object[] params) {
		return baseDao.queryByClause(clazz, clause, params);
	}
	
	public int count(String clause, Object[] params) {
		return baseDao.queryCount(clazz, clause, params);
	}

	protected PaginationSupport<T> page(String clause, Object[] params, int page, int pagesize) {
		return baseDao.queryPage(clazz, clause, params, page, pagesize);
	}
	
	protected PaginationSupport<T> page(String clause, int page, int pagesize) {
		return baseDao.queryPage(clazz, clause, page, pagesize);
	}

	protected String getCommand() {
		return command;
	}

	protected void setCommand(String command) {
		this.command = command;
	}

}
