package org.zap.framework.orm.criteria;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.compiler.JoinProperty;
import org.zap.framework.orm.compiler.PreCompiler;
import org.zap.framework.orm.converter.DefaultConverter;
import org.zap.framework.orm.dao.dialect.DBType;
import org.zap.framework.orm.dao.dialect.PaginatorFactory;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.exception.ExEnum;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.orm.extractor.MapListExtractor;
import org.zap.framework.orm.helper.BeanHelper;
import org.zap.framework.orm.page.PaginationSupport;
import org.zap.framework.orm.sql.*;
import org.zap.framework.orm.sql.criteria.*;
import org.zap.framework.orm.criteria.call.CountFunction;
import org.zap.framework.orm.sql.literal.ParamLiteral;

import java.lang.reflect.Field;
import java.util.*;

public class Query<T> {

    /************************* 获取结果的方法 ***********************/
    public List<Map<String, Object>> mapList() {
        projection();
        return baseDao.query(toString(), toParams(), new MapListExtractor());
    }

    public List<T> list() {
        projection();
        return baseDao.query(toString(), toParams(), new BeanListExtractor<T>(clazz));
    }

    public List<T> tree() {
        projection();
        return BeanHelper.buildTree(baseDao.query(toString(), toParams(), new BeanListExtractor<T>(clazz)));
    }

    public int count() {

        function = true;
        select.addToSelection(new CountFunction());

        return baseDao.getJdbcTemplate().queryForObject(toString(), toParams(), Integer.class);
    }

    /**
     * 分页
     *
     * @param page     当前页
     * @param pageSize 每页数量
     * @return
     */
    public PaginationSupport<T> page(int page, int pageSize) {
        return PaginatorFactory.getInstance(baseDao).queryPage(toString(), page, pageSize, toParams());
    }

    /*******************************************************/

    public static Criteria AND(Criteria... criterias) {
        return new LazyAND(criterias);
    }

    public static Criteria OR(Criteria... criterias) {
        return new LazyOR(criterias);
    }

    public static Criteria EQ(String column, Object value) {
        return value == null ? null : new LazyMatchCriteria(column, value, MatchCriteria.EQUALS);
    }

    public static Criteria NOT_EQ(String column, Object value) {
        return value == null ? null : new LazyMatchCriteria(column, value, MatchCriteria.NOTEQUAL);
    }

    public static Criteria GT(String column, Object value) {
        return value == null ? null : new LazyMatchCriteria(column, value, MatchCriteria.GREATER);
    }

    public static Criteria GT_EQ(String column, Object value) {
        return value == null ? null : new LazyMatchCriteria(column, value, MatchCriteria.GREATEREQUAL);
    }

    public static Criteria LS(String column, Object value) {
        return value == null ? null : new LazyMatchCriteria(column, value, MatchCriteria.LESS);
    }

    public static Criteria LS_EQ(String column, Object value) {
        return value == null ? null : new LazyMatchCriteria(column, value, MatchCriteria.LESSEQUAL);
    }

    public static Criteria LIKE(String column, Object value) {
        return value == null ? null : new LazyMatchCriteria(column, value, MatchCriteria.LIKE);
    }

    public static Criteria LEFT_LIKE(String column, Object value) {
        return value == null ? null : new LazyMatchCriteria(column, value, MatchCriteria.LEFTLIKE);
    }

    public static Criteria RIGHT_LIKE(String column, Object value) {
        return value == null ? null : new LazyMatchCriteria(column, value, MatchCriteria.RIGHTLIKE);
    }

    public static Criteria IN(String column, Object... values) {
        return values == null || values.length == 0 ? null : new LazyInCriteria(column, values);
    }

    public static Criteria NOT_NUL(String column) {
        return new LazyNotNullCriteria(column);
    }

    public static Criteria NUL(String column) {
        return new LazyNullCriteria(column);
    }

    public static Criteria BETWEEN(String column, Object lower, Object upper) {
        return new LazyBetweenCriteria(column, lower, upper);
    }

    public Query<T> eq(String column, Object value) {
        return match(column, value, MatchCriteria.EQUALS);
    }

    public Query<T> notEq(String column, Object value) {
        return match(column, value, MatchCriteria.NOTEQUAL);
    }

    public Query<T> gt(String column, Object value) {
        return match(column, value, MatchCriteria.GREATER);
    }

    public Query<T> gtOrEq(String column, Object value) {
        return match(column, value, MatchCriteria.GREATEREQUAL);
    }

    public Query<T> ls(String column, Object value) {
        return match(column, value, MatchCriteria.LESS);
    }

    public Query<T> lsOrEq(String column, Object value) {
        return match(column, value, MatchCriteria.LESSEQUAL);
    }

    public Query<T> like(String column, Object value) {
        return match(column, value, MatchCriteria.LIKE);
    }

    public Query<T> leftLike(String column, Object value) {
        return match(column, value, MatchCriteria.LEFTLIKE);
    }

    public Query<T> rightLike(String column, Object value) {
        return match(column, value, MatchCriteria.RIGHTLIKE);
    }

    public Query<T> in(String column, Object[] values) {

        List<Literal> pllist = new ArrayList<Literal>();
        for (int i = 0; i < values.length; i++) {
            pllist.add(ParamLiteral.instance);
        }
        this.select.addCriteria(new InCriteria(retrieveColumn(column), new LiteralValueSet(pllist)));
//		paramList.add(values);
        Collections.addAll(paramList, convert(column, values));
        return this;
    }

    public Query<T> notNul(String column) {
        this.select.addCriteria(new IsNotNullCriteria(retrieveColumn(column)));
        return this;
    }

    public Query<T> nul(String column) {
        this.select.addCriteria(new IsNullCriteria(retrieveColumn(column)));
        return this;
    }

    public Query<T> between(String column, Object lower, Object upper) {
        Collections.addAll(paramList, convert(column, lower)[0], convert(column, upper)[0]);
        this.select.addCriteria(new BetweenCriteria(retrieveColumn(column), ParamLiteral.instance, ParamLiteral.instance));
        return this;
    }

    public Query<T> and(Criteria criteria) {
        select.addCriteria(lazy(criteria));
        return this;
    }

    public Criteria or(Criteria... criterias) {
        if (criterias == null || criterias.length == 0) {
            throw new DaoException(ExEnum.PARAMS_IS_NULL.toString());
        }
        for (int i = 0; i < criterias.length; i++) {
            criterias[i] = lazy(criterias[i]);
        }
        return new OR(criterias);
    }

    public Criteria and(Criteria... criterias) {
        if (criterias == null || criterias.length == 0) {
            throw new DaoException(ExEnum.PARAMS_IS_NULL.toString());
        }
        for (int i = 0; i < criterias.length; i++) {
            criterias[i] = lazy(criterias[i]);
        }

        return new AND(criterias);
    }

    public Query<T> sort(String column, boolean ascending) {
        if (StringUtils.isNotBlank(column)) {
            this.select.addOrder(new Order(retrieveColumn(column), ascending));
        }
        return this;
    }

    public Query<T> asc(String column) {
        return sort(column, true);
    }

    public Query<T> desc(String column) {
        return sort(column, false);
    }

    public Query<T> groupby(String... columns) {
        if (columns != null && columns.length > 0) {

            group = true;

            for (int i = 0; i < columns.length; i++) {
                this.select.addGroup(new Group(retrieveGroupColumn(columns[i])));
                this.select.addColumn(retrieveColumn(columns[i]));
            }
        }
        return this;
    }

    public Query<T> having(Criteria criteria) {
        //TODO
        return this;
    }

    /************************查询方法***********************/
//	protected T max(String clause, String[] cols, Object[] params) {
//		return baseDao.querySortByClause(clazz, cols, clause, params, false);
//	}
//	protected T min(String clause, String[] cols, Object[] params) {
//		return baseDao.querySortByClause(clazz, cols, clause, params, true);
//	}
//	protected List<T> list(String clause, Object[] params) {
//		return baseDao.queryByClause(clazz, clause, params);
//	}
//	public PaginationSupport<T> page(String clause, Object[] params, int page, int pagesize) {
//		return baseDao.queryPage(clazz, clause, params, page, pagesize);
//	}
//	public PaginationSupport<T> page(String clause, int page, int pagesize) {
//		return baseDao.queryPage(clazz, clause, page, pagesize);
//	}
//	public int count(String clause, Object[] params) {
//		return baseDao.queryCount(clazz, clause, params);
//	}

    /*******************************************************/

    private Object[] convert(String property, Object... values) {
//		property = retrieveColumn(property);
        property = StringUtils.upperCase(property);
        if (values != null) {
//			if (values.length == 1) {
//				
//				Field field = bp.getFieldMap().get(property);
//				if (field == null)
//					return new Object[] { values[0] };
//				
//				return new Object[] { DefaultConverter.getInstance().convertValue(field.getType(), values[0]) };
//			} else {
            Object[] result = new Object[values.length];

            Field field = bp.getFieldMap().get(property);
            for (int i = 0; i < result.length; i++) {
                if (field == null) {
                    return new Object[]{values[i]};
                } else {
                    result[i] = DefaultConverter.getInstance().convertValue(field.getType(), -9999, values[i]);
                }
            }
            return result;
        }
//		}
        return null;
    }

    private Criteria lazy(Criteria criteria) {
        if (criteria instanceof LazyCriteria) {
            LazyCriteria tc = (LazyCriteria) criteria;
            return tc.toCriteria(this.retrieveTable(tc.getColumn()), paramList);
        }
        return criteria;
    }

    private Query<T> match(String column, Object value, String matchType) {
        this.select.addCriteria(new MatchCriteria(retrieveColumn(column), matchType, ParamLiteral.instance));
        //转换参数类型
//		paramList.add(convert(column, value));

        CollectionUtils.addAll(paramList, convert(column, value));

        return this;
    }

    /**
     * 获取
     *
     * @param column
     * @return
     */
    private Column retrieveGroupColumn(String column) {

        Table table = null;

        //1.使用corp_name获取形如    OC1.NAME
        //2.使用OC1.NAME获取到形如 OC1.NAME
        //3.使用NAME 获取到形如       AU.NAME
        //1.3常用

        //参照字段，使用corp_name获取OC1.NAME
        String original = colsAliasCache.get(StringUtils.upperCase(column));

        if (StringUtils.isNotEmpty(original)) {
            //原始字段
            table = retrieveTable(original);

            return table.getColumn(StringUtils.split(original, ".")[1]);

        } else {
            table = retrieveTable(column);
        }

        if (column.indexOf(".") > -1) {
            return table.getColumn(StringUtils.split(column, ".")[1]);
        }

        return table.getColumn(column);
    }


    /**
     * 获取
     *
     * @param column
     * @return
     */
    private Column retrieveColumn(String column) {

        Table table = null;

        //1.使用corp_name获取形如    OC1.NAME
        //2.使用OC1.NAME获取到形如 OC1.NAME
        //3.使用NAME 获取到形如       AU.NAME
        //1.3常用

        //参照字段，使用corp_name获取OC1.NAME
        String original = colsAliasCache.get(StringUtils.upperCase(column));

        if (StringUtils.isNotEmpty(original)) {
            //原始字段
            table = retrieveTable(original);

            return table.getColumn(StringUtils.split(original, ".")[1], column);

        } else {
            table = retrieveTable(column);
        }

        if (column.indexOf(".") > -1) {
            return table.getColumn(StringUtils.split(column, ".")[1]);
        }

        return table.getColumn(column);
    }

    /**
     * 获取Table对象
     *
     * @param column
     * @return
     */
    private Table retrieveTable(String column) {

        column = StringUtils.upperCase(column);

        Table table = null;

        if (column.indexOf(".") > -1) {
            String[] split = StringUtils.split(column, ".");

            String alias = split[0];
//			String name = split[1];

            //通过外键别名获取
            table = aliasCache.get(alias);

            if (table == null) {
                throw new DaoException("Table alias [" + alias + "] not exist");
            }

        } else {
            if (bp.getFieldMap().get(column) == null) {
                throw new DaoException("Field [" + column + "] not exist");
            }
            table = aliasCache.get(column);
        }


        if (table == null) {
            table = select.getMainTable();
        }
        return table;
    }

    private SelectQuery select;

    private Class<T> clazz;

    private BaseDao baseDao;

    private BeanProperty bp;

    private JoinProperty jp;

    //
    private boolean function = false;

    private boolean group = false;

    /**
     *
     */
    private List<FunctionCall> callList = new ArrayList<FunctionCall>();

    private List<Object> paramList = new ArrayList<Object>();

    /**
     * 表别名缓存
     */
    private Map<String, Table> aliasCache = new HashMap<String, Table>();

    /**
     * 外键字段别名缓存
     */
    private Map<String, String> colsAliasCache = new HashMap<String, String>();

    public Query(BaseDao baseDao, Class<T> clazz) {
        this(clazz);
        this.baseDao = baseDao;
    }

    public Query(Class<T> clazz) {
        this.clazz = clazz;
        //1.添加column
        bp = PreCompiler.getInstance().getBeanProperty(clazz);

        Table mainTable = new Table(bp.getTableName(), bp.getTableAlias());
        select = new SelectQuery(mainTable);

        //外键列
        jp = PreCompiler.getInstance().getJoinProperty(clazz);

        //外键信息
        List<JoinProperty.MainProp> mainPropList = jp.getMainPropList();

        if (mainPropList != null && mainPropList.size() > 0) {
            for (JoinProperty.MainProp mp : mainPropList) {
                List<JoinProperty.ColProp> colPropList = mp.getColList();

                Table foreignTable = new Table(mp.getTableName(), mp.getTableAlias());
                select.addLeftCriteria(new LeftJoinCriteria(mainTable, mainTable.getColumn(mp.getForeignKey()), "=", foreignTable, foreignTable.getColumn(mp.getPrimaryKey())));

                for (JoinProperty.ColProp cp : colPropList) {
//					select.addColumn(foreignTable.getColumn(cp.getColumnName(), cp.getFieldName()));

                    //CORP_NAME=OC1.NAME
                    colsAliasCache.put(StringUtils.upperCase(cp.getFieldName()), StringUtils.upperCase(mp.getTableAlias() + "." + cp.getColumnName()));
                }
            }
        }

        //5.缓存主表字段与别名
        List<Table> listTables = select.listTables();
        for (Table t : listTables) {
            //注意字段名与表别名一样
            aliasCache.put(t.getName(), t);
            aliasCache.put(t.getAlias(), t);
        }

    }

    public static <T> Query<T> getBuilder(Class<T> clazz) {
        return new Query<T>(clazz);
    }

    protected void projection() {

        if (function || group) {
            for (FunctionCall fc : callList) {
                select.addToSelection(fc);
            }

        } else {
            //
            String[] primaryKeys = bp.getPrimaryKeys();
            String[] columns = bp.getColumns();
            //2.主键
            for (int i = 0; i < primaryKeys.length; i++) {
                select.addColumn(primaryKeys[i]);
            }
            //3.版本
            if (bp.isVersionControl()) {
                select.addColumn(bp.getVersionColumn());
            }
            //4.业务字段
            for (int i = 0; i < columns.length; i++) {
                select.addColumn(columns[i]);
            }

            //外键列
            jp = PreCompiler.getInstance().getJoinProperty(clazz);

            //外键信息
            List<JoinProperty.MainProp> mainPropList = jp.getMainPropList();

            if (mainPropList != null && mainPropList.size() > 0) {
                for (JoinProperty.MainProp mp : mainPropList) {
                    List<JoinProperty.ColProp> colPropList = mp.getColList();

                    Table foreignTable = aliasCache.get(mp.getTableAlias());

                    for (JoinProperty.ColProp cp : colPropList) {
                        select.addColumn(foreignTable.getColumn(cp.getColumnName(), cp.getFieldName()));
                    }
                }
            }
        }
    }


    public String toString() {

        projection();

        return select.toString();
    }

    public String toCountString() {
        return "";
    }

    public String toPageString() {

        //获取数据库类型
        DBType dbType = DBType.ORACLE;
        if (baseDao != null) {
            dbType = baseDao.getDbType();
        }

        if (DBType.ORACLE.equals(dbType)) {
            StringBuilder pageSql = new StringBuilder("SELECT * FROM (");
            pageSql.append("SELECT ROWNUM RW, MT.* FROM (")
                    .append(select.toString())
                    .append(") MT WHERE ROWNUM <= ? ")
                    .append(") MST WHERE MST.RW >= ? ");

            return pageSql.toString();

        } else if (DBType.MYSQL.equals(dbType)) {
            return select.toString() + " LIMIT ?, ? ";
        }

        return select.toString();
    }

    public Object[] toParams() {
        return paramList.toArray(new Object[paramList.size()]);
    }

    public List<Object> getParamList() {
        return paramList;
    }

    public void setParamList(List<Object> paramList) {
        this.paramList = paramList;
    }

    static class LazyAND extends LazyCriteria {

        Criteria[] criterias;

        public LazyAND(Criteria[] criterias) {
            super();
            this.criterias = criterias;
        }

        @Override
        public Criteria toCriteria(Table table, List<Object> paramList) {
            for (int i = 0; i < criterias.length; i++) {
                criterias[i] = lazy(criterias[i], table, paramList);
            }
            return new AND(criterias);
        }

        private Criteria lazy(Criteria criteria, Table table, List<Object> paramList) {
            if (criteria instanceof LazyCriteria) {
                LazyCriteria tc = (LazyCriteria) criteria;
                return tc.toCriteria(table, paramList);
            }
            return criteria;
        }

        @Override
        public String getColumn() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    static class LazyOR extends LazyCriteria {

        Criteria[] criterias;

        public LazyOR(Criteria[] criterias) {
            super();
            this.criterias = criterias;
        }

        @Override
        public Criteria toCriteria(Table table, List<Object> paramList) {
            for (int i = 0; i < criterias.length; i++) {
                criterias[i] = lazy(criterias[i], table, paramList);
            }
            return new OR(criterias);
        }

        private Criteria lazy(Criteria criteria, Table table, List<Object> paramList) {
            if (criteria instanceof LazyCriteria) {
                LazyCriteria tc = (LazyCriteria) criteria;
                return tc.toCriteria(table, paramList);
            }
            return criteria;
        }

        @Override
        public String getColumn() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    static class LazyMatchCriteria extends LazyCriteria {

        String column;
        Object value;
        String matchType;

        public LazyMatchCriteria(String column, Object value, String matchType) {
            this.column = column;
            this.value = value;
            this.matchType = matchType;
        }

        @Override
        public Criteria toCriteria(Table table, List<Object> paramList) {
            paramList.add(value);
            return new MatchCriteria(table.getColumn(column), matchType, ParamLiteral.instance);
        }

        @Override
        public String getColumn() {
            return column;
        }
    }

    static class LazyInCriteria extends LazyCriteria {
        String column;
        Object[] values;

        public LazyInCriteria(String column, Object[] values) {
            super();
            this.column = column;
            this.values = values;
        }

        @Override
        public Criteria toCriteria(Table table, List<Object> paramList) {
            Collections.addAll(paramList, values);
            List<Literal> pllist = new ArrayList<Literal>();
//			Collections.fill(pllist, ParamLiteral.instance);
            for (int i = 0; i < values.length; i++) {
                pllist.add(ParamLiteral.instance);
            }
            return new InCriteria(table.getColumn(column), new LiteralValueSet(pllist));
        }

        @Override
        public String getColumn() {
            return column;
        }
    }

    static class LazyNullCriteria extends LazyCriteria {
        String column;

        public LazyNullCriteria(String column) {
            this.column = column;
        }

        @Override
        public Criteria toCriteria(Table table, List<Object> paramList) {
            return new IsNullCriteria(table.getColumn(column));
        }

        @Override
        public String getColumn() {
            return column;
        }

    }

    static class LazyNotNullCriteria extends LazyCriteria {
        String column;

        public LazyNotNullCriteria(String column) {
            this.column = column;
        }

        @Override
        public Criteria toCriteria(Table table, List<Object> paramList) {
            return new IsNotNullCriteria(table.getColumn(column));
        }

        @Override
        public String getColumn() {
            return column;
        }

    }

    static class LazyBetweenCriteria extends LazyCriteria {

        String column;
        Object lower, upper;

        public LazyBetweenCriteria(String column, Object lower, Object upper) {
            this.column = column;
            this.lower = lower;
            this.upper = upper;
        }

        @Override
        public Criteria toCriteria(Table table, List<Object> paramList) {
            paramList.add(lower);
            paramList.add(upper);
            return new BetweenCriteria(table.getColumn(column), ParamLiteral.instance, ParamLiteral.instance);
        }

        @Override
        public String getColumn() {
            return column;
        }
    }

}
