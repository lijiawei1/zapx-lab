package org.zap.framework.orm.creator;

import org.apache.commons.lang.StringUtils;
import org.zap.framework.orm.page.PaginationSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 简单关联查询
 * 1.防止sql注入
 * 2.减少sql拼装工作
 * @author Shin
 *
 */
@Deprecated
public class Criteria<T> {
	
	/**
	 * 无查询参数条件
	 */
	protected List<String> noParam;
	/**
	 * 单个参数条件
	 */
	protected List<Condition> singleParam;
	/**
	 * 数组参数，拼接IN语句
	 */
	protected List<Condition> listParam;
	/**
	 * between条件
	 */
	protected List<Condition> betweenParam;
	
	protected CriteriaBuilder<T> builder;
	
	/**
	 * 运算符,默认为AND
	 */
	protected String operator;

	protected Criteria() {
		super();
		noParam = new ArrayList<String>();
		singleParam = new ArrayList<Condition>();
		listParam = new ArrayList<Condition>();
		betweenParam = new ArrayList<Condition>();
	}
	
	public Criteria(CriteriaBuilder<T> criteriaBuilder) {
		this();
		builder = criteriaBuilder;
	}
	
	public T max(String... cols) {
		return builder.max(builder.toSqlString(false), cols, builder.getParamList().toArray(new Object[builder.getParamList().size()]));
	}
	
	public T min(String... cols) {
		return builder.min(builder.toSqlString(false), cols, builder.getParamList().toArray(new Object[builder.getParamList().size()]));
	}
	
	public List<T> tree() {
		return builder.tree(builder.toSqlString(false), builder.getParamList().toArray(new Object[builder.getParamList().size()]));
	}
	
	public List<T> list() {
		return builder.list(builder.toSqlString(false), builder.getParamList().toArray(new Object[builder.getParamList().size()]));
	}
	
	public int count() {
		return builder.count(builder.toSqlString(false), builder.getParamList().toArray(new Object[builder.getParamList().size()]));
	}

	public PaginationSupport<T> page(int page, int pagesize) {
		return builder.page(builder.toSqlString(false), builder.getParamList().toArray(new Object[builder.getParamList().size()]), page, pagesize);
	}
	

	
//	public PaginationSupport<T> page(int page, int pagesize) {
//		return builder.page(builder.toSqlString(false), page, pagesize);
//	}
	
	/**
	 * 判断是否存在条件
	 */
	public boolean isValid() {
		return noParam.size() > 0
				|| singleParam.size() > 0
				|| listParam.size() > 0
				|| betweenParam.size() > 0;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	protected List<String> getNoParam() {
		return noParam;
	}

	protected List<Condition> getSingleParam() {
		return singleParam;
	}

	protected List<Condition> getListParam() {
		return listParam;
	}

	protected List<Condition> getBetweenParam() {
		return betweenParam;
	}

	protected void addCriterion(String condition) {
		if (condition == null || "".equals(condition)) {
			return;
		}
		noParam.add(condition);
	}

	/**
	 * single
	 */
	protected void addCriterion(String condition, Object value, String property) {
		if (value == null || "".equals(value)) {
			return;
		}
		singleParam.add(new Condition(condition, value, StringUtils.upperCase(property)));
	}

	/**
	 * list
	 */
	protected void addCriterion(String condition, List<? extends Object> values, String property) {
		if (values == null || values.size() == 0) {
			return;
		}
		listParam.add(new Condition(condition, values, StringUtils.upperCase(property)));
	}

	/**
	 * between
	 */
	protected Criteria<T> addCriterion(String condition, Object value1, Object value2, String property) {
		if (value1 == null || value2 == null) {
			return this;
		}
		betweenParam.add(new Condition(condition, Arrays.asList(new Object[] { value1, value2 }), StringUtils.upperCase(property)));
		return this;
	}
	
	public Criteria<T> nul(String field) {
		addCriterion(getFieldName(field) + " IS NULL");
		return this;
	}

	public Criteria<T> notNull(String field) {
		addCriterion(getFieldName(field) + " IS NOT NULL");
		return this;
	}

	/**
	 * 等于
	 */
	public Criteria<T> eq(String field, Object value) {
		addCriterion(getFieldName(field) + " = ?", value, field);
		return this;
	}

	/**
	 * 不等于
	 */
	public Criteria<T> notEq(String field, Object value) {
		addCriterion(getFieldName(field) + " <> ?", value, field);
		return this;
	}

	/**
	 * 大于
	 */
	public Criteria<T> lg(String field, Object value) {
		addCriterion(getFieldName(field) + " > ?", value, field);
		return this;
	}

	/**
	 * 大于等于
	 */
	public Criteria<T> lgOrEq(String field, Object value) {
		addCriterion(getFieldName(field) + " >= ?", value, field);
		return this;
	}

	/**
	 * 小于
	 */
	public Criteria<T> ls(String field, Object value) {
		addCriterion(getFieldName(field) + " < ?", value, field);
		return this;
	}

	/**
	 * 小于等于
	 */
	public Criteria<T> lsOrEq(String field, Object value) {
		addCriterion(getFieldName(field) + " <= ?", value, field);
		return this;
	}

	/**
	 * 模糊
	 */
	public Criteria<T> like(String field, String value) {
		addCriterion(getFieldName(field) + " LIKE '%' || ? || '%'", value, field);
		return this;
	}
	
	/**
	 * 自定义条件
	 */
	public Criteria<T> clause(String condition) {
		addCriterion(condition);
		return this;
	}

	/**
	 * 模糊
	 */
	public Criteria<T> notLike(String field, String value) {
		addCriterion(getFieldName(field) + " NOT LIKE '%' || ? || '%'", value, field);
		return this;
	}
	
	/**
	 * 左(前)模糊
	 */
	public Criteria<T> leftLike(String field, String value) {
		addCriterion(getFieldName(field) + " LIKE '%' || ? ", value, field);
		return this;
	}
	
	/**
	 * 右(后)模糊
	 */
	public Criteria<T> rightLike(String field, String value) {
		addCriterion(getFieldName(field) + " LIKE ? || '%'", value, field);
		return this;
	}
	
	public Criteria<T> in(String field, Object[] values) {
		List<String> vs = new ArrayList<String>();
		for (Object value : values) {
			vs.add(value.toString());
		}
		addCriterion(getFieldName(field) + " IN", vs, field);

		return this;
	}
	
	/**
	 * in，支持字符、整数
	 */
	public Criteria<T> in(String field, List<Serializable> values) {
		List<String> vs = new ArrayList<String>();
		for (Serializable value : values) {
			vs.add(value.toString());
		}
		addCriterion(getFieldName(field) + " IN", vs, field);

		return this;
	}

	public Criteria<T> notIn(String field, List<String> values) {
		List<String> vs = new ArrayList<String>();
		for (String string : values) {
			vs.add(string);
		}
		addCriterion(getFieldName(field) + " NOT IN", vs, field);
		return this;
	}
	
	public Criteria<T> or(Criteria<T> criteria) {
		criteria.setOperator(" OR ");
		builder.orCriteria.add(criteria);
		return this;
	}
	
	public Criteria<T> and(Criteria<T> criteria) {
		criteria.setOperator(" AND ");
		builder.orCriteria.add(criteria);
		return this;
	}

	/**
	 * between
	 */
	public Criteria<T> between(String field, Object value1, Object value2) {
		addCriterion(getFieldName(field) + " BETWEEN ", value1, value2, field);
		return this;
	}

	public Criteria<T> notBetween(String field, Object value1, Object value2) {
		addCriterion(getFieldName(field) + " NOT BETWEEN", value1, value2, field);
		return this;
	}
	
	public Criteria<T> sort(String field, boolean asc) {
		if (StringUtils.isBlank(field))
			return this;
		return asc ? asc(field) : desc(field);
	}
	
	/**
	 * 升序
	 */
	public Criteria<T> asc(String field) {
		if (StringUtils.isBlank(field))
			return this;
		builder.orderbyList.add(getFieldName(field));
		return this;
	}
	
	/**
	 * 降序
	 */
	public Criteria<T> desc(String field) {
		if (StringUtils.isBlank(field))
			return this;
		builder.orderbyList.add(getFieldName(field) + " DESC");
		return this;
	}
	

//	private String String str) {
//		if (str == null)
//			return null;
//		return "'" + str + "'";
//	}
	
	private String getFieldName(String field) {

		if (field == null) {
			throw new RuntimeException(field + " cannot be null");
		}
		return field.toUpperCase();
	}
	
	public static class Condition {
		public String clause;
		public Object value;
		public String property;
		public Condition(String clause, Object value, String property) {
			this.clause = clause;
			this.value = value;
			this.property = property;
		}
		public String getClause() {
			return clause;
		}
		public void setClause(String k) {
			this.clause = k;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object v) {
			this.value = v;
		}
		public String getProperty() {
			return property;
		}
		public void setProperty(String p) {
			this.property = p;
		}
	}

}
