package org.zap.framework.orm.creator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.compiler.JoinProperty;
import org.zap.framework.orm.compiler.PreCompiler;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.exception.ExEnum;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 选择语句生成器
 *
 * @author Shin
 */
public class SelectSqlCreator extends BaseCreator {

    private static Logger logger = LoggerFactory.getLogger(SelectSqlCreator.class);

    protected PreCompiler compiler = PreCompiler.getInstance();

    protected static SelectSqlCreator instance;

    public static SelectSqlCreator getInstance() {
        if (instance == null) {
            instance = new SelectSqlCreator();
        }
        return instance;
    }

    private SelectSqlCreator() {
    }

    /**
     * 构建查询总数
     *
     * @param clazz 实体类型
     * @return 语句
     */
    public StringBuilder createCountSql(Class<?> clazz) {
        //检查是否标识注解
        checkBase(clazz);

        StringBuilder sel = new StringBuilder("SELECT COUNT(1) FROM ");

        BeanProperty beanProperty = compiler.getBeanProperty(clazz);
        //关联字段
        JoinProperty joinProperty = compiler.getJoinProperty(clazz);

        //1拼主表
        sel.append(beanProperty.getTableName()).append(" ").append(beanProperty.getTableAlias()).append(" ");

        //2外键连接表
        if (joinProperty != null) {
            String[] joinSqls = joinProperty.getJoinSqls();
            for (int i = 0; i < joinSqls.length; i++) {
                sel.append(joinSqls[i]).append(" ");
            }
        }

        return sel;
    }


    /**
     * 构建普通查询
     *
     * @param clazz  实体类型
     * @param filter 列过滤器
     * @param join   是否关联字段
     * @return
     */
    public StringBuilder createSql(Class<?> clazz, ColumnFilter filter, boolean join) {
        //检查是否标识注解
        checkBase(clazz);

        //获取Class的orm信息
        BeanProperty beanProperty = compiler.getBeanProperty(clazz);

        String[] primaryKeys = beanProperty.getPrimaryKeys();
        String[] columns = beanProperty.getColumns();

        //关联字段
        JoinProperty joinProperty = compiler.getJoinProperty(clazz);

        StringBuilder sel = new StringBuilder("SELECT ");

        String tableAlias = beanProperty.getTableAlias();

        if (!beanProperty.isView()) {
            //1.主键字段
            for (int i = 0; i < primaryKeys.length; i++) {
                sel.append(tableAlias).append(".").append(primaryKeys[i]).append(",");
            }
            //2.版本字段
            if (beanProperty.isVersionControl()) {
                sel.append(tableAlias).append(".").append(beanProperty.getVersionColumn()).append(",");
            }
        }

        //3.业务字段
        for (int i = 0; i < columns.length; i++) {

            if (filter != null && filter.contain(columns[i]))
                continue;

            sel.append(tableAlias).append(".").append(columns[i]).append(",");
        }

        //4.1外键关联字段
        if (join && joinProperty != null) {
            String[] joinColumns = joinProperty.getJoinColumns();
            for (int i = 0; i < joinColumns.length; i++) {
                sel.append(joinColumns[i]).append(",");
            }
        }

        sel.replace(sel.length() - 1, sel.length(), "");
        sel.append(" FROM ").append(beanProperty.getTableName()).append(" ").append(tableAlias).append(" ");

        //4.2外键连接表
        if (join && joinProperty != null) {
            String[] joinSqls = joinProperty.getJoinSqls();
            for (int i = 0; i < joinSqls.length; i++) {
                sel.append(joinSqls[i]).append(" ");
            }
        }

//		logger.debug(sel.toString());
        return sel;


    }


    /**
     * 构建普通查询
     *
     * @param clazz  实体
     * @param filter 列过滤器
     * @return 语句
     */
    public StringBuilder createSql(Class<?> clazz, ColumnFilter filter) {
        return createSql(clazz, filter, true);
    }

    /**
     * 构建查询语句
     *
     * @param clazz  实体
     * @param filter 列过滤器
     * @param clause 条件从句
     * @return 语句
     */
    public StringBuilder createByClauseSql(Class<?> clazz, ColumnFilter filter, String clause) {
        return createSql(clazz, filter).append(processClause(clause, false));
    }

    /**
     * 条件从句处理ORDER 和 WHERE
     *
     * @param clause 条件从句
     * @return 语句
     */
    private String processClause(String clause, boolean append) {

        String s = StringUtils.trimToEmpty(clause);
        if (s.toUpperCase().startsWith("ORDER")) {
            return " " + s;
        } else if (s.length() > 0) {
            if (append) {
                return " AND " + s;
            } else {
                return " WHERE " + s;
            }
        } else {
            return "";
        }
    }


    /**
     * 版本查询
     *
     * @param clazz  持久化类
     * @param filter 列过滤器
     * @param size   主键数组长度
     * @param clause 条件从句
     * @return 语句
     */
    public StringBuilder createVersionForUpdate(Class<?> clazz, ColumnFilter filter, int size, String clause) {
        return createForVersionSql(clazz, filter, size, clause).append(" FOR UPDATE ");
    }


    /**
     * 构建版本查询
     *
     * @param clazz  视图
     * @param filter 列过滤器
     * @param size   主键数组长度
     * @param clause 条件从句
     * @return 语句
     */
    public StringBuilder createForVersionSql(Class<?> clazz, ColumnFilter filter, int size, String clause) {

        checkBase(clazz);

        BeanProperty beanProperty = compiler.getBeanProperty(clazz);

        if (beanProperty.isView()) {
            //视图不支持查询
            throw new DaoException(ExEnum.VIEW_VERSION_NOT_SUPPORT.toString());
        }

        if (!beanProperty.isVersionControl()) {
            //实体不支持版本查询
            throw new DaoException(ExEnum.ENTITY_VERSION_NOT_SUPPORT.toString());
        }

        String[] primaryKeys = beanProperty.getPrimaryKeys();

        if (primaryKeys.length > 1)
            throw new DaoException(ExEnum.MULTIPLE_KEY_FOUND.toString());

        StringBuilder createSql = createSql(clazz, filter, false);

        createSql.append(" WHERE ").append(beanProperty.getTableAlias()).append(".").append(primaryKeys[0]).append(" IN (");
        createSql.append(StringUtils.repeat("?", ",", size));
        createSql.append(")");

        if (clause != null && clause.length() > 0) {
            createSql.append(" AND ").append(clause);
        }

//		logger.debug(createSql.toString());
        return createSql;
    }

    /**
     *
     * @param clazz
     * @param entities
     */
    public StringBuilder createForVersionSql(Class<?> clazz, Object[] entities) {

        checkBase(clazz);

        BeanProperty beanProperty = compiler.getBeanProperty(clazz);

        if (beanProperty.isView()) {
            //视图不支持查询
            throw new DaoException(ExEnum.VIEW_VERSION_NOT_SUPPORT.toString());
        }

        if (!beanProperty.isVersionControl()) {
            //实体不支持版本查询
            throw new DaoException(ExEnum.ENTITY_VERSION_NOT_SUPPORT.toString());
        }

        String[] primaryKeys = beanProperty.getPrimaryKeys();
        String versionColumn = beanProperty.getVersionColumn();
        String tableAlias = beanProperty.getTableAlias();

        if (primaryKeys.length > 1)
            throw new DaoException(ExEnum.MULTIPLE_KEY_FOUND.toString());

//        beanProperty.getVersionField()

        StringBuilder countSql = createCountSql(clazz);

        countSql.append(" WHERE (");

        String[] clauses = new String[entities.length];
        for (int i = 0; i < entities.length; i++) {
            clauses[i] = (tableAlias + "." + primaryKeys[0] + " A= ? AND " + tableAlias + "." + versionColumn + " = ?" );
        }

        countSql.append(StringUtils.join(clauses, " OR "));
        countSql.append(")");

        return countSql;
    }

    /**
     *
     * @param clazz
     * @param entities
     * @return
     */
    public Object[] createParams(Class<? extends Object> clazz, Object[] entities) {
        //
        BeanProperty beanProperty = compiler.getBeanProperty(clazz);
        //
        Field[] primaryFields = beanProperty.getPrimaryFields();
        Field versionField = beanProperty.getVersionField();

        Object[] params = new Object[entities.length * 2];
        try {
            int k = 0;
            for (int i = 0; i < entities.length; i++) {
                params[k++] = primaryFields[0].get(entities[i]);
                params[k++] = versionField.get(entities[i]);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }

        return params;
    }

    /**
     * 构建主键查询
     *
     * @param clazz  实体
     * @param filter 列过滤器
     * @param size   主键长度
     * @param clause 条件语句
     * @return 语句
     */
    public StringBuilder createByPrimaryKeySql(Class<?> clazz, ColumnFilter filter, int size, String clause) {
        StringBuilder createSql = createSql(clazz, filter);

        BeanProperty beanProperty = compiler.getBeanProperty(clazz);
        String[] primaryKeys = beanProperty.getPrimaryKeys();

        if (primaryKeys.length > 1)
            throw new DaoException(ExEnum.MULTIPLE_KEY_FOUND.toString());

        createSql.append(" WHERE ").append(beanProperty.getTableAlias()).append(".").append(primaryKeys[0]).append(" IN (");
        createSql.append(StringUtils.repeat("?", ",", size));
        createSql.append(")").append(processClause(clause, true));

        return createSql;
    }


    /**
     * 构建主键查询
     *
     * @param clazz  实体
     * @param filter 列过滤器
     * @param clause 条件语句
     * @return 语句
     */
    public StringBuilder createByPrimaryKeySql(Class<?> clazz, ColumnFilter filter, String clause) {

        StringBuilder createSql = createSql(clazz, filter);

        BeanProperty beanProperty = compiler.getBeanProperty(clazz);
        String[] primaryKeys = beanProperty.getPrimaryKeys();

        if (primaryKeys.length > 1)
            throw new DaoException(ExEnum.MULTIPLE_KEY_FOUND.toString());

        createSql.append(" WHERE ").append(beanProperty.getTableAlias()).append(".").append(primaryKeys[0]).append(" = ?")
                .append(processClause(clause, true));

        return createSql;
    }

    /**
     * ORACLE 版本
     * 构建排序查询
     *
     * @param clazz  实体
     * @param cols   列
     * @param clause 条件从句
     * @param asc    升序降序
     * @return 语句
     */
    public StringBuilder createSortByClauseSql(Class<?> clazz, String[] cols, String clause, boolean asc) {

        StringBuilder createSql = createSql(clazz, null);
        if (clause != null) {

            if (clause.trim().startsWith("ORDER")) {
                //自带order by
                createSql.append(clause);
            } else {
                if (!"".equals(clause))
                    createSql.append(" WHERE 1 = 1 AND ").append(clause);

                if (clause.indexOf("ORDER") == -1) {
                    String tableAlias = PreCompiler.getInstance().getBeanProperty(clazz).getTableAlias();

                    if (cols != null && cols.length > 0) {
                        createSql.append(" ORDER BY ").append(tableAlias).append(".").append(cols[0].toUpperCase()).append(asc ? " " : " DESC ");
                        for (int i = 1; i < cols.length; i++) {
                            createSql.append(tableAlias).append(".").append(cols[i].toUpperCase()).append(asc ? " " : " DESC ");
                        }
                    }
                }

                /**
                 * 使用orderby排序
                 * SELECT * FROM (
                 SELECT * FROM zap_auth_menu
                 ORDER BY morder DESC
                 ) WHERE ROWNUM=1;
                 */
                StringBuilder sortSql = new StringBuilder("SELECT * FROM (");
                sortSql.append(createSql);
                sortSql.append(") WHERE ROWNUM = 1");
                return sortSql;
            }
        }
//		logger.debug(createSql.toString());
        return createSql;
    }


    /**
     * MSSQL版本，处理自定义语句中的ORDER BY
     *
     * @param clazz  实体
     * @param clause 条件从句
     * @return 语句
     */
    public StringBuilder createCountByClauseSqlForMssql(Class<?> clazz, String clause) {
        StringBuilder createCountSql = createCountSql(clazz);
        if (StringUtils.isNotBlank(clause)) {

            int orderIndex = StringUtils.lastIndexOf(clause, "ORDER");

            if (orderIndex == 0) {
                //do nothing
            } else if (orderIndex > 0) {
                createCountSql.append(" WHERE 1 = 1 AND ").append(StringUtils.substring(clause, 0, orderIndex - 1));
            } else {
                createCountSql.append(" WHERE 1 = 1 AND ").append(clause);
            }
        }
        return createCountSql;
    }

    /**
     * 构建总数查询
     *
     * @param clazz  实体
     * @param clause 条件从句
     * @return 语句
     */
    public StringBuilder createCountByClauseSql(Class<?> clazz, String clause) {
        return createCountSql(clazz).append(processClause(clause, false));
    }

    /**
     * ORACLE版本
     *
     * @param clazz  映射类
     * @param clause 条件
     * @return 语句
     */
    public StringBuilder createPageSql(Class<?> clazz, String clause) {

        StringBuilder createByClauseSql = createByClauseSql(clazz, null, clause);

        /**
         有ORDER BY排序的写法。(效率最高)(经过测试，此方法随着查询范围的扩大，速度也会越来越慢哦！)
         SELECT *
         FROM (SELECT tt.*, ROWNUM AS rowno
         FROM (  SELECT t.* FROM emp t
         WHERE hire_date BETWEEN TO_DATE ('20060501', 'yyyymmdd')
         AND TO_DATE ('20060731', 'yyyymmdd')
         ORDER BY create_time DESC, emp_no) tt
         WHERE ROWNUM <= 20) table_alias
         WHERE table_alias.rowno >= 10;
         */

        StringBuilder pageSql = new StringBuilder("SELECT * FROM (");
        pageSql.append("SELECT ROWNUM RW, MT.* FROM (")
                .append(createByClauseSql)
                .append(") MT WHERE ROWNUM <= ? ");
        pageSql.append(") MST WHERE MST.RW >= ? ");

//		logger.debug(pageSql.toString());
        return pageSql;
    }

    /**
     * ORACLE版本
     *
     * @param sql 查询SQL
     * @return 语句
     */
    public StringBuilder createPageSql(String sql) {

        /**
         有ORDER BY排序的写法。(效率最高)(经过测试，此方法随着查询范围的扩大，速度也会越来越慢哦！)
         SELECT *
         FROM (SELECT tt.*, ROWNUM AS rowno
         FROM (  SELECT t.* FROM emp t
         WHERE hire_date BETWEEN TO_DATE ('20060501', 'yyyymmdd')
         AND TO_DATE ('20060731', 'yyyymmdd')
         ORDER BY create_time DESC, emp_no) tt
         WHERE ROWNUM <= 20) table_alias
         WHERE table_alias.rowno >= 10;
         */

        StringBuilder pageSql = new StringBuilder("SELECT * FROM (");
        pageSql.append("SELECT ROWNUM RW, MT.* FROM (")
                .append(sql)
                .append(") MT WHERE ROWNUM <= ? ");
        pageSql.append(") MST WHERE MST.RW >= ? ");

//		logger.debug(pageSql.toString());
        return pageSql;
    }

    /**
     * ORACLE版本
     *
     * @param sql 查询SQL
     * @param startPageParmName 最大页码参数
     * @param endPageParmName 最小页码参数
     * @return
     */
    public StringBuilder createPageNameSql(String sql, String startPageParmName, String endPageParmName) {

        /**
         有ORDER BY排序的写法。(效率最高)(经过测试，此方法随着查询范围的扩大，速度也会越来越慢哦！)
         SELECT *
         FROM (SELECT tt.*, ROWNUM AS rowno
         FROM (  SELECT t.* FROM emp t
         WHERE hire_date BETWEEN TO_DATE ('20060501', 'yyyymmdd')
         AND TO_DATE ('20060731', 'yyyymmdd')
         ORDER BY create_time DESC, emp_no) tt
         WHERE ROWNUM <= 20) table_alias
         WHERE table_alias.rowno >= 10;
         */

        StringBuilder pageSql = new StringBuilder("SELECT * FROM (");
        pageSql.append("SELECT ROWNUM RW, MT.* FROM (")
                .append(sql)
                .append(") MT WHERE ROWNUM <= :" + endPageParmName + " ");
        pageSql.append(") MST WHERE MST.RW >= :" + startPageParmName + " ");

//		logger.debug(pageSql.toString());
        return pageSql;
    }

    /**
     * 构建等值查询语句
     *
     * @param po     条件vo
     * @param clause 条件从句
     * @param <T>    无
     * @return 语句
     */
    @Deprecated
    public <T> StringBuilder createSqlWhereByPo(T po, String clause) {

        StringBuilder createSql = new StringBuilder();
        BeanProperty beanProperty = compiler.getBeanProperty(po.getClass());

        createSql.append(" WHERE 1=1 ");
        String[] notNullCols = (UpdateSqlCreator.getInstance()).notNullCols(po);

        if (notNullCols != null && notNullCols.length > 0) {
            for (int i = 0; i < notNullCols.length; i++) {
                createSql.append(" AND ").append(beanProperty.getTableAlias()).append(".").append(notNullCols[i].toUpperCase()).append("=:").append(notNullCols[i]);
            }
        }

        if (clause != null && clause.length() > 0) {
            if (clause.trim().substring(0, 5).toUpperCase().startsWith("ORDER")) {
                createSql.append(" ").append(clause);
            } else {
                createSql.append(" AND ").append(clause);
            }
        }

        return createSql;
    }

    /**
     * PO等值查询(没什么用，不如直接写SQL)
     */
    @Deprecated
    public <T> StringBuilder createSqlByPo(T po, ColumnFilter filter, String clause) {
        return createSql(po.getClass(), filter).append(createSqlWhereByPo(po, clause));
    }

    /**
     * 构建等值查询
     *
     * @param clazz  实体
     * @param params 查询参数
     * @param filter 类别过滤器
     * @param clause 条件从句
     * @return 语句
     */
    public StringBuilder createSqlByMap(Class<?> clazz, Map<String, Object> params, ColumnFilter filter, String clause) {
        StringBuilder createSql = createSql(clazz, filter);

        BeanProperty beanProperty = compiler.getBeanProperty(clazz);
        String[] primaryKeys = beanProperty.getPrimaryKeys();

        if (primaryKeys.length > 1)
            throw new DaoException(ExEnum.MULTIPLE_KEY_FOUND.toString());

        createSql.append(" WHERE 1=1 ");
        for (String key : params.keySet()) {
            createSql.append(" AND ").append(beanProperty.getTableAlias()).append(".").append(key.toUpperCase()).append("=:").append(key);
        }

        if (clause != null && clause.length() > 0) {
            if (clause.trim().substring(0, 5).toUpperCase().startsWith("ORDER")) {
                createSql.append(" ").append(clause);
            } else {
                createSql.append(" AND ").append(clause);
            }
        }

        return createSql;
    }
}
