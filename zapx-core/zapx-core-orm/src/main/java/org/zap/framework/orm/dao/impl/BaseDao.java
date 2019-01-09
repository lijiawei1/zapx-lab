package org.zap.framework.orm.dao.impl;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.JdbcUtils;
import com.eaio.uuid.UUID;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Repository;
import org.zap.framework.exception.BusinessException;
import org.zap.framework.orm.creator.*;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.exception.ExEnum;
import org.zap.framework.orm.extractor.EnhanceMapListExtractor;
import org.zap.framework.orm.extractor.Extractor;
import org.zap.framework.orm.base.BaseEntity;
import org.zap.framework.orm.criteria.Query;
import org.zap.framework.orm.dao.dialect.DBType;
import org.zap.framework.orm.dao.dialect.PaginatorFactory;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.orm.helper.BeanHelper;
import org.zap.framework.orm.itf.ITree;
import org.zap.framework.orm.itf.IUpdateCallBack;
import org.zap.framework.common.entity.pagination.PaginationSupport;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 提供统一的数据访问层异常处理
 *
 * @author Shin
 */
@Repository("baseDao")
public class BaseDao implements IBaseDao {

    /**
     * 查询总数
     *
     * @param sql    查询语句
     * @param params 查询参数
     * @return 记录数量
     */
    public int queryCount(String sql, Object... params) {
        return jdbcTemplate.queryForObject(SQLUtils.format(new StringBuilder("SELECT COUNT(1) FROM (")
                .append(sql).append(")").toString(), getDbTypeString()), params, Integer.class);
    }

    /**
     * 查询总数
     *
     * @param sql    查询语句
     * @param params 查询参数
     * @return 记录数量
     */
    public int queryCount(String sql, Map<String, Object> params) {
        return getNameTemplate().queryForObject(SQLUtils.format(new StringBuilder("SELECT COUNT(1) FROM (")
                .append(sql).append(")").toString(), getDbTypeString()), params, Integer.class);
    }

    /**
     * 查询总数
     *
     * @param clazz 实体类型
     * @return 记录数量
     */
    public int queryCount(Class<?> clazz) {
        return jdbcTemplate.queryForObject(SQLUtils.format(selecteSqlCreator.createCountSql(clazz).toString(), getDbTypeString()), Integer.class);
    }

    /**
     * 查询总数
     *
     * @param clazz  实体类型
     * @param clause 条件/排序语句
     * @param params 查询参数
     * @return 记录数量
     */
    public int queryCount(Class<?> clazz, String clause, Object... params) {

        //过滤 ORDER子句
        if (DBType.MICROSOFT == this.dbType) {
            return jdbcTemplate.queryForObject(SQLUtils.format(selecteSqlCreator.createCountByClauseSqlForMssql(clazz, clause).toString(), getDbTypeString()), params,
                    Integer.class);
        }

        return jdbcTemplate.queryForObject(SQLUtils.format(selecteSqlCreator.createCountByClauseSql(clazz, clause).toString(), getDbTypeString()), params,
                Integer.class);
    }

    /**
     * 查询总数
     *
     * @param clazz  实体类型
     * @param clause 条件/排序语句
     * @param params 查询参数
     * @return 记录数量
     */
    public int queryCount(Class<?> clazz, String clause, Map<String, Object> params) {

        //过滤 ORDER子句
        if (DBType.MICROSOFT == this.dbType) {
            return getNameTemplate().queryForObject(SQLUtils.format(selecteSqlCreator.createCountByClauseSqlForMssql(clazz, clause).toString(), getDbTypeString()), params,
                    Integer.class);
        }

        return getNameTemplate().queryForObject(SQLUtils.format(selecteSqlCreator.createCountByClauseSql(clazz, clause).toString(), getDbTypeString()), params,
                Integer.class);
    }

    /**
     * 分页查询
     *
     * @param clazz       实体类型
     * @param clause      条件/排序语句
     * @param params      查询参数
     * @param currentPage 当前页，从0开始
     * @param pageSize    每页记录数
     * @param <T>         实体类型
     * @return 分页结果集
     */
    public <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, Object[] params, int currentPage, int pageSize) {
        return PaginatorFactory.getInstance(this).queryPage(clazz, clause, params, currentPage, pageSize);
    }

//    public <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, Map<String, Object> params, int currentPage, int pageSize) {
//        return PaginatorFactory.getInstance(this).queryPage(clazz, clause, params, currentPage, pageSize);
//    }

    /**
     * 分页查询
     *
     * @param sql         自定义语句
     * @param currentPage 当前页从0开始
     * @param pageSize    煤业记录数
     * @param params      查询参数
     * @return 分页结果集
     */
    public PaginationSupport queryPage(String sql, int currentPage, int pageSize, Object... params) {
        return PaginatorFactory.getInstance(this).queryPage(sql, currentPage, pageSize, params);
    }

    /**
     * 分页查询
     *
     * @param sql         自定义语句
     * @param currentPage 当前页从0开始
     * @param pageSize    煤业记录数
     * @param params      查询参数
     * @return 分页结果集
     */
    public PaginationSupport queryPage(String sql, int currentPage, int pageSize, Map<String, Object> params) {
        return PaginatorFactory.getInstance(this).queryPage(sql, currentPage, pageSize, params);
    }

    /**
     * 分页查询
     *
     * @param clazz       实体类型
     * @param clause      条件/排序语句
     * @param currentPage 当前页，从0开始
     * @param pageSize    每页记录数
     * @param <T>         实体类
     * @return 分页结果集
     */
    public <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, int currentPage, int pageSize) {
        return queryPage(clazz, clause, new Object[0], currentPage, pageSize);
    }

    /**
     * 普通查询，通过包装类解析查询的数据后返回
     *
     * @param sql         自定义SQL语句
     * @param params      查询参数
     * @param rsExtractor 包装处理类
     * @param <T>         实体类
     * @return 实体列表
     */
    public <T> List<T> query(String sql, final Object[] params, Extractor<List<T>> rsExtractor) {
        rsExtractor.setLobHandler(this.lobHandler);
        return jdbcTemplate.query(SQLUtils.format(sql, getDbTypeString()), new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
        }, rsExtractor);
    }

    /**
     * 执行自定义SQL，通过包装类解析查询的数据后返回
     *
     * @param sql         自定义SQL语句
     * @param rsExtractor 包装处理类
     * @param <T>         实体类
     * @return 实体列表
     */
    public <T> List<T> query(String sql, Extractor<List<T>> rsExtractor) {
        rsExtractor.setLobHandler(this.lobHandler);
        return jdbcTemplate.query(SQLUtils.format(sql, getDbTypeString()), rsExtractor);
    }

    /**
     * 查询表中所有数据
     *
     * @param clazz 实体类型型
     * @param <T>   实体类
     * @return 实体列表
     */
    public <T> List<T> queryAll(Class<T> clazz) {
        return jdbcTemplate.query(SQLUtils.format(selecteSqlCreator.createSql(clazz, null).toString(), getDbTypeString()),
                new BeanListExtractor<T>(clazz, lobHandler));
    }

    /**
     * 查询业务实体列表
     *
     * @param clazz  实体类型
     * @param clause 条件/排序语句
     * @param params 查询参数
     * @param <T>    实体类
     * @return 实体列表
     */
    public <T> List<T> queryByClause(Class<T> clazz, String clause, Object... params) {
        return jdbcTemplate.query(SQLUtils.format(selecteSqlCreator.createByClauseSql(clazz, null, clause).toString(), getDbTypeString()), params,
                new BeanListExtractor<>(clazz, lobHandler));
    }

    /**
     * 查询业务实体（一个）
     *
     * @param clazz  实体类型
     * @param clause 条件/排序语句
     * @param params 查询参数
     * @param <T>    实体类
     * @return 结果集中首个实体，没有结果返回空
     */
    public <T> T queryOneByClause(Class<T> clazz, String clause, Object... params) {
        //通过分页限制
        return PaginatorFactory.getInstance(this).queryOneByPage(clazz, clause, params);
    }

    /**
     * 查询业务实体树形列表，映射类必须实现树形接口ITree
     *
     * @param clazz  实体类型
     * @param clause 条件/排序语句
     * @param params 查询参数
     * @param <T>    实体类
     * @return 所有根节点的列表
     */
    public <T> List<T> queryTreeByClause(Class<T> clazz, String clause, Object... params) {

        notTree(clazz);

        List<T> query = null;

        if (params != null && params.length > 0) {
            query = jdbcTemplate.query(SQLUtils.format(selecteSqlCreator.createByClauseSql(clazz, null, clause).toString(), getDbTypeString()),
                    new BeanListExtractor<T>(clazz, lobHandler));
        } else {
            query = jdbcTemplate.query(SQLUtils.format(selecteSqlCreator.createByClauseSql(clazz, null, clause).toString(), getDbTypeString()), params,
                    new BeanListExtractor<T>(clazz, lobHandler));
        }

        return BeanHelper.buildTree(query);
    }

    /**
     * 使用主键查询实体, 只支持单个主键查询
     *
     * @param clazz 实体类型
     * @param id    实体主键
     * @param <T>   实体类
     * @return 实体结果
     */
    public <T> T queryByPrimaryKey(Class<T> clazz, String id) {
        List<T> query = jdbcTemplate.query(SQLUtils.format(selecteSqlCreator.createByPrimaryKeySql(clazz, null, null).toString(), getDbTypeString()),
                new Object[]{id}, new BeanListExtractor<T>(clazz, lobHandler));
        return query == null || query.size() == 0 ? null : query.get(0);
    }

    /**
     * 使用主键数组查询实体, 只支持单个主键查询
     *
     * @param clazz 实体类型
     * @param ids   实体主键数组
     * @param <T>   实体类
     * @return 实体列表
     */
    public <T> List<T> queryByPrimaryKey(Class<T> clazz, String[] ids) {

        return ids == null || ids.length == 0 ? new ArrayList<T>()
                : jdbcTemplate.query(SQLUtils.format(selecteSqlCreator.createByPrimaryKeySql(clazz, null, ids.length, null).toString(), getDbTypeString()),
                ids, new BeanListExtractor<T>(clazz, lobHandler));
    }

    /**
     * 使用主键数组查询实体, 只支持单个主键查询
     *
     * @param clazz  实体类型
     * @param pks    实体主键数组
     * @param clause 条件/排序语句
     * @param <T>    实体类
     * @return 实体列表
     */
    public <T> List<T> queryByPrimaryKey(Class<T> clazz, String[] pks, String clause) {

        return jdbcTemplate.query(SQLUtils.format(selecteSqlCreator.createByPrimaryKeySql(clazz, null, pks.length, clause).toString(), getDbTypeString()),
                pks, new BeanListExtractor<T>(clazz, lobHandler));
    }

    /**
     * 使用Map传值查询
     *
     * @param clazz  实体类型
     * @param params 条件键值对
     * @param clause 条件/排序语句
     * @param <T>    实体类
     * @return 实体列表
     */
    public <T> List<T> queryByMap(Class<T> clazz, Map<String, Object> params, String clause) {
        return getNameTemplate().query(SQLUtils.format(selecteSqlCreator.createSqlByMap(clazz, params, null, clause).toString(), getDbTypeString()), params,
                new BeanListExtractor<T>(clazz, lobHandler));
    }

    /**
     * 插表
     *
     * @param entity 实体
     * @param <T>    实体类
     * @return 影响的行数
     */
    public <T> int insert(T entity) {

        Object id = BeanHelper.getId(entity);
        return insertArray(new Object[]{entity}, StringUtils.isNotBlank((String) id));
    }

    /**
     * 批量插表
     *
     * @param entities 实体列表
     * @param withId   是否带ID插表
     * @param <T>      实体类
     * @return 影响的行数
     */
    public <T> int insertArray(T[] entities, boolean withId) {
        if (!withId) {
            for (int i = 0; i < entities.length; i++) {
                BeanHelper.setId(entities[i], new UUID().toString());
            }
        }
        return insertWithId(entities);
    }

    /**
     * 批量插表
     *
     * @param entityList 实体列表
     * @param withId     是否带ID插表
     * @param <T>        实体类
     * @return 影响的行数
     */
    public <T> int insertList(List<T> entityList, boolean withId) {

        if (!withId) {
            for (T entity : entityList) {
                //设置ID的值
                BeanHelper.setId(entity, new UUID().toString());
            }
        }
        return insertWithId(entityList.toArray(new Object[entityList.size()]));
    }

    /**
     * 批量插表
     *
     * @param entities 实体列表
     * @param <T>      实体类
     * @return 影响的行数
     */
    protected <T> int insertWithId(T[] entities) {
        notNull(entities);

        int r = 0;
        String sql = insertSqlCreator.getSql(entities[0].getClass());
        // logger.debug(sql);

        int count = entities.length / BATCH_SIZE;

        for (int i = 0; i < count; i++) {
            List<Object[]> batchArgs = insertSqlCreator.createParamList(entities[0].getClass(),
                    ArrayUtils.subarray(entities, i * BATCH_SIZE, (i + 1) * BATCH_SIZE));

            r += sum(jdbcTemplate.batchUpdate(sql, batchArgs));
        }

        int last = entities.length % BATCH_SIZE;
        if (last > 0) {
            List<Object[]> batchArgs = insertSqlCreator.createParamList(entities[0].getClass(),
                    ArrayUtils.subarray(entities, count * BATCH_SIZE, (count + 1) * BATCH_SIZE));
            r += sum(jdbcTemplate.batchUpdate(SQLUtils.format(sql, getDbTypeString()), batchArgs));
        }
        return r;
    }

    /**
     * 更新不为空的字段
     * <pre>
     * 注意：
     * 1.取数组第一个实体得到非空字段
     * 2.批量更新时以第一个实体为准
     * 3.int等原始类型数据类型有默认值，一律更新
     * 4.其余类型==null判断是否为空,String==""不为空 5.当所有业务字段为空时，也会更新版本号
     * </pre>
     *
     * @param entities 实体
     * @param <T>      实体类
     * @return 影响行数
     */
    public <T> int updateNotNull(T[] entities) {
        notNull(entities);

        // 非空列
        String[] notNullCols = updateSqlCreator.notNullCols(entities[0]);
        ColumnFilter filter = new ColumnFilter(notNullCols, true);
        String sql = updateSqlCreator.createSql(entities[0].getClass(), filter, true).toString();

        // logger.debug(sql);
        List<Object[]> batchArgs = updateSqlCreator.createParamList(entities[0].getClass(), entities, filter, true);
        // for (Object[] obj : batchArgs) {
        // logger.debug(Arrays.toString(obj));
        // }
        int[] rows = jdbcTemplate.batchUpdate(SQLUtils.format(sql, getDbTypeString()), batchArgs);

        // 更新成功，同步VO
        for (T entity : entities) {
            //entity.setVersion(entity.getVersion() + 1);
            BeanHelper.increaseVersion(entity, 1);

        }

        return sum(rows);
    }

    /**
     * 更新不为空
     *
     * @param entity 实体
     * @param <T>    实体类
     * @return 影响行数
     */
    public <T> int updateNotNull(T entity) {
        notNull(new Object[]{entity});

        // 非空列
        String[] notNullCols = updateSqlCreator.notNullCols(entity);
        ColumnFilter filter = new ColumnFilter(notNullCols, true);
        String sql = updateSqlCreator.createSql(entity.getClass(), filter, true).toString();

        // logger.debug(sql);
        List<Object[]> batchArgs = updateSqlCreator.createParamList(entity.getClass(), new Object[]{entity}, filter, true);
        // for (Object[] obj : batchArgs) {
        // logger.debug(Arrays.toString(obj));
        // }
        int[] rows = jdbcTemplate.batchUpdate(SQLUtils.format(sql, getDbTypeString()), batchArgs);

        // 更新成功，同步VO
        // for (BaseEntity entity : entities) { entity.setVersion(entity.getVersion() + 1);
        // }
        BeanHelper.increaseVersion(entity, 1);

        return sum(rows);

    }

    /**
     * 批量更新ORACLE专用，退化成普通SQL批量提交
     *
     * @param entities
     * @param cols
     * @param include
     * @param <T>
     * @return
     */
    protected <T> int batchUpdate4Oracle(T[] entities, String[] cols, boolean include, boolean withVersion) {

        notNull(entities);

        ColumnFilter filter = cols == null ? null : new ColumnFilter(cols, include);
        String[] sqls = updateSqlCreator.createSqlWithParam(entities[0].getClass(), entities, filter, withVersion);

        return sum(jdbcTemplate.batchUpdate(sqls));
    }

    /**
     * 批量更新ORACLE专用，退货成普通SQL批量提交
     *
     * @param <T>
     * @param entity
     * @param cols
     * @param include
     * @param clause
     * @return
     */
    protected <T> int updateByClause4Oracle(T entity, String[] cols, boolean include, String clause, Object... params) {
        ColumnFilter filter = cols == null ? null : new ColumnFilter(cols, include);
        String sql = updateSqlCreator.createSqlByClauseWithParam(entity.getClass(), entity, clause, filter);
        return jdbcTemplate.update(sql, params);
    }

    /**
     *
     * @param entities
     * @param withVersion
     * @param <T>
     * @return
     */
    protected <T> int batchDelete4Oracle(T[] entities, boolean withVersion) {
        notNull(entities);
        String[] sqls = deleteSqlCreator.createSqlWithParam(entities[0].getClass(), entities, withVersion);
        return sum(jdbcTemplate.batchUpdate(sqls));
    }

    /**
     * 更新
     *
     * @param entities 实体数组
     * @param cols     列名数组
     * @param include  包含或排除列
     * @param <T>      实体类
     * @return 行数
     */
    public <T> int updateArray(T[] entities, String[] cols, boolean include) {
        notNull(entities);

        int effectedRows = 0;

        if (DBType.ORACLE == getDbType()) {
            //ORACLE比较懒，不支持批处理的特性，不能返回影响行数，只有-2
            effectedRows =  batchUpdate4Oracle(entities, cols, include, true);
        } else {

            //其它类型的数据库支持
            ColumnFilter filter = cols == null ? null : new ColumnFilter(cols, include);
            String sql = updateSqlCreator.createSql(entities[0].getClass(), filter, true).toString();
            logger.debug(sql);
            List<Object[]> batchArgs = updateSqlCreator.createParamList(entities[0].getClass(), entities, filter, true);
            effectedRows = sum(jdbcTemplate.batchUpdate(sql, batchArgs));
        }

        // 更新成功，同步VO
        for (T entity : entities) {
            BeanHelper.increaseVersion(entity, 1);
        }

        return effectedRows;
    }

    /**
     * 更新
     *
     * @param pojoList
     * @param cols
     * @param include
     * @param <T>
     * @return
     */
    public <T> int updateList(List<T> pojoList, String[] cols, boolean include) {
        return update(pojoList.toArray(new BaseEntity[pojoList.size()]), cols, include);
    }

    /**
     * 更新
     *
     * @param entity
     * @param cols
     * @param include
     * @param <T>
     * @return
     */
    public <T> int update(T entity, String[] cols, boolean include) {

        if (entity == null)
            throw new DaoException(ExEnum.PARAMS_IS_NULL.toString());

        ColumnFilter filter = cols == null ? null : new ColumnFilter(cols, include);
        String sql = updateSqlCreator.createSql(entity.getClass(), filter, true).toString();
        // logger.debug(sql);
        List<Object[]> batchArgs = updateSqlCreator.createParamList(entity.getClass(), new Object[]{entity}, filter, true);

        int row = jdbcTemplate.update(SQLUtils.format(sql, getDbTypeString()), batchArgs.get(0));
        // 更新成功，同步VO
        BeanHelper.increaseVersion(entity, 1);

        return row;
    }

    /**
     * 更新
     * 单个更新，返回影响行数，一般情况下是1
     *
     * @param entity 待更新实体
     * @param <T>
     * @return 影响行数
     */
    public <T> int update(T entity) {
        return update(entity, null, true);
    }

    public <T> int updateArray(T[] entities) {
        return updateArray(entities, null, true);
    }

    /**
     * 批量更新
     * <p>
     * 因为ORACLE批量更新问题无法抛出乐观锁，导致并发会有问题
     *
     * @param entityList
     * @param <T>
     * @return
     */
    public <T> int updateList(List<T> entityList) {
        return updateArray(entityList.toArray(new BaseEntity[entityList.size()]));
    }

    /**
     * 忽略版本号更新 慎用
     *
     * @param entity  实体
     * @param cols    列名数组
     * @param include 包含或排除
     * @param <T>     实体类
     * @return 行数
     */
    public <T> int updateNotVersion(T entity, String[] cols, boolean include) {

        if (entity == null)
            throw new DaoException(ExEnum.PARAMS_IS_NULL.toString());

        ColumnFilter filter = cols == null ? null : new ColumnFilter(cols, include);
        String sql = updateSqlCreator.createSql(entity.getClass(), filter, false).toString();
        // logger.debug(sql);
        List<Object[]> batchArgs = updateSqlCreator.createParamList(entity.getClass(), new Object[]{entity}, filter, false);

        return jdbcTemplate.update(sql, batchArgs.get(0));

    }

    /**
     * 不区分版本号 慎用
     *
     * @param entities 实体数组
     * @param cols     列名数组
     * @param include  包含或排除列
     * @param <T>      实体类
     * @return 行数
     */
    public <T> int updateArrayNotVersion(T[] entities, String[] cols, boolean include) {
        notNull(entities);

        int effectedRows = 0;

        if (DBType.ORACLE == getDbType()) {
            //ORACLE比较懒，不支持批处理的特性，不能返回影响行数，只有-2
            effectedRows = batchUpdate4Oracle(entities, cols, include, false);
        } else {
            ColumnFilter filter = cols == null ? null : new ColumnFilter(cols, include);
            // 忽略版本号的更新
            String sql = updateSqlCreator.createSql(entities[0].getClass(), filter, false).toString();
            List<Object[]> batchArgs = updateSqlCreator.createParamList(entities[0].getClass(), entities, filter, false);
            int[] rows = jdbcTemplate.batchUpdate(sql, batchArgs);
            effectedRows = sum(rows);
        }

        return effectedRows;
    }

    public <T> int updateNotVersion(T entity) {
        return updateArrayNotVersion(new Object[]{entity}, null, true);
    }

    public <T> int updateArrayNotVersion(T[] entities) {
        return updateArrayNotVersion(entities, null, true);
    }

    public <T> int updateListNotVersion(List<T> entityList) {
        return updateArrayNotVersion(entityList.toArray(new BaseEntity[entityList.size()]), null, true);
    }

    /**
     * 朴素实现
     * TODO 非线程安全
     *
     * @param entity
     * @param callBack
     * @param <T>
     * @return
     */
    public <T> int updateNotVersion(T entity, ColumnFilter filter, IUpdateCallBack callBack) {

        T o = (T)queryByPrimaryKey(entity.getClass(), (String) BeanHelper.getId(entity));

        //比较修改后的字段
        if (callBack != null) {
            callBack.doWork(BeanHelper.compareFieldUpdated(entity, o, filter));
        }

        //更新数据
        return updateNotVersion(entity);

    }

    /**
     * 条件更新
     * 解决批量更新的烦锁写法
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> int updateByClause(T entity, String[] cols, boolean include, String clause, Object... params) {
        if (entity == null)
            throw new DaoException(ExEnum.PARAMS_IS_NULL.toString());

        ColumnFilter filter = cols == null ? null : new ColumnFilter(cols, include);
        String sql = updateSqlCreator.createSqlByClauseWithParam(entity.getClass(), entity, clause, filter);
        return jdbcTemplate.update(sql, params);
    }

    /**
     * 条件更新
     * 默认不更新主键和版本号字段
     *
     * @param entities
     * @param cols 待更新的字段
     * @param include 是否
     * @param clause 外部条件
     * @param params 外部参数
     * @param <T>
     * @return
     */
    public <T> int updateArrayByClause(T[] entities, String[] cols, boolean include, String clause, Object... params) {
        notNull(entities);
        throw new BusinessException("TO BE DONE");
        //return 0;

    }

    /**
     * 删除全部
     *
     * @param clazz 实体类型
     * @return 记录行数
     */
    public int deleteAll(Class<?> clazz) {
        return jdbcTemplate.update(SQLUtils.format(deleteSqlCreator.createByClauseSql(clazz, null).toString(), getDbTypeString()));
    }

    /**
     * 删除
     *
     * @param entity 实体
     * @param <T>    实体类
     * @return 影响行数
     */
    public <T> int delete(T entity) {
        return delete(entity, true);
    }

    /**
     * 删除
     *
     * @param entityList 实体列表
     * @param <T>        实体类
     * @return 影响行数
     */
    public <T> int deleteList(List<T> entityList) {
        return deleteArray(entityList.toArray(new Object[entityList.size()]));
    }

    /**
     * 数据库删除 必须带版本号，解决并发问题
     *
     * @param entities 实体数组
     * @param <T>      实体类
     * @return 记录行数
     */
    public <T> int deleteArray(T[] entities) {
        return deleteArray(entities, true);
    }

    /**
     * 删除 慎用
     *
     * @param entity 实体
     * @param <T>    实体类
     * @return 影响行数
     */
    public <T> int deleteNotVersion(T entity) {
        return delete(entity, false);
    }

    /**
     * 数据库删除 不带版本号 慎用
     *
     * @param entities 实体数组
     * @param <T>      实体类
     * @return 记录行数
     */
    public <T> int deleteArrayNotVersion(T[] entities) {
        return deleteArray(entities, false);
    }

    protected <T> int delete(T entity, boolean withVersion) {
        if (entity == null)
            throw new DaoException(ExEnum.PARAMS_IS_NULL.toString());

        String sql = deleteSqlCreator.createByPrimaryKeySql(entity.getClass(), withVersion).toString();
        //批量更新参数
        List<Object[]> batchArgs = deleteSqlCreator.createParamList(entity.getClass(), new Object[]{entity}, withVersion);
        //返回正确的行数
        return jdbcTemplate.update(sql, batchArgs.get(0));
    }

    protected <T> int deleteArray(T[] entities, boolean withVersion) {
        notNull(entities);

        if (DBType.ORACLE == getDbType()) {
            return batchDelete4Oracle(entities, withVersion);
        }

        String sql = deleteSqlCreator.createByPrimaryKeySql(entities[0].getClass(), withVersion).toString();

        List<Object[]> batchArgs = deleteSqlCreator.createParamList(entities[0].getClass(), entities, withVersion);
        //返回错误的行数
        return sum(jdbcTemplate.batchUpdate(SQLUtils.format(sql, getDbTypeString()), batchArgs));
    }

    /**
     * 根据条件/排序语句删除
     *
     * @param clazz  持久化类
     * @param clause 条件/排序语句
     * @return 成功条数
     */
    public int deleteByClause(Class<?> clazz, String clause, Object... params) {
        String sql = deleteSqlCreator.createByClauseSql(clazz, clause).toString();
        // logger.debug(sql);
        return sum(new int[]{jdbcTemplate.update(SQLUtils.format(sql, getDbTypeString()), params)});

    }

    /**
     * 根据主键列表删除
     *
     * @param clazz 持久化类
     * @param keys  持久化类主键
     * @return 行数
     */
    public int deleteByPrimaryKey(Class<?> clazz, Object... keys) {

        notNull(keys);

        String sql = deleteSqlCreator.createByPrimaryKeySql(clazz, false).toString();
        List<Object[]> batchArgs = new ArrayList<>();
        for (Object k : keys) {
            batchArgs.add(new Object[]{k});
        }
        return sum(jdbcTemplate.batchUpdate(SQLUtils.format(sql, getDbTypeString()), batchArgs));
    }

    /**
     * 查询版本号
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T> int queryByVersion(T entities[] ) {

        Object[] params = selecteSqlCreator.createParams(entities[0].getClass(), entities);
        return jdbcTemplate.queryForObject(
                SQLUtils.format(selecteSqlCreator.createForVersionSql(entities[0].getClass(), entities).toString(), getDbTypeString()),
                params, Integer.class);
    }

    /**
     * 查询主键和版本号对应
     *
     * @param clazz 持久化类
     * @param pk    主键
     * @return 主键版本对应
     */
    public BaseEntity queryForVersion(Class<?> clazz, String pk) {
        List<BaseEntity> baseEntities = queryForVersion(clazz, new String[]{pk});
        return baseEntities == null || baseEntities.size() == 0 ? null : baseEntities.get(0);
    }

    /**
     * 查询主键和版本号对应
     *
     * @param clazz 持久化类
     * @param pks   主键列表
     * @return 主键版本对应
     */
    public List<BaseEntity> queryForVersion(Class<?> clazz, String[] pks) {

        return jdbcTemplate.query(selecteSqlCreator.createForVersionSql(clazz, new ColumnFilter(new String[]{"ID", "VERSION"}, true),
                (pks == null ? 0 : pks.length), null).toString(),
                pks, new BeanListExtractor<>(BaseEntity.class, lobHandler));
    }

    /**
     * 悲观锁校验版本号
     *
     * @param clazz 持久化类
     * @param pk    主键
     * @return 主键版本对应
     */
    @Deprecated
    public BaseEntity queryVersionForUpdate(Class<?> clazz, String pk) {
        List<BaseEntity> baseEntities = queryVersionForUpdate(clazz, new String[]{pk});
        return baseEntities == null || baseEntities.size() == 0 ? null : baseEntities.get(0);
    }

    /**
     * 悲观锁校验版本号
     *
     * @param clazz 持久化类
     * @param pks   主键列表
     * @return 主键版本对应
     */
    @Deprecated
    public List<BaseEntity> queryVersionForUpdate(Class<?> clazz, String[] pks) {
        return jdbcTemplate.query(selecteSqlCreator.createVersionForUpdate(clazz, new ColumnFilter(new String[]{"ID", "VERSION"}, true),
                (pks == null ? 0 : pks.length), null).toString(),
                pks, new BeanListExtractor<>(BaseEntity.class, lobHandler));
    }


    /*******************************************************************************************/
    /*********** 提供返回特殊列表的写法参考，实际应用中用getJdbcTemplate实现个性化查询 ********/
    /*******************************************************************************************/

    /**
     * 对象以Object[]形式组织返回
     *
     * @param sql  查询语句
     * @param args 查询参数
     * @return 对象数组列表
     */
    public List<Object[]> queryForObjectList(String sql, Object... args) {
        return jdbcTemplate.query(SQLUtils.format(sql, getDbTypeString()), args, new RowMapper<Object[]>() {

            public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                ResultSetMetaData metaData = rs.getMetaData();

                Object[] objs = new Object[metaData.getColumnCount()];
                // 注意结果集的索引从1开始
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    objs[i - 1] = rs.getObject(i);
                }
                return objs;
            }

        });

    }


    /**
     * 增强查询 行数据以Map组织返回
     * <p>
     * date timestamp 增强为 LocalDateTime
     * numeric bigdecimal 增强为 LDouble
     *
     * @param sql  查询语句
     * @param args 查询参数
     * @return 哈希数组
     */
    public List<Map<String, Object>> queryForEnhanceMapList(String sql, Object... args) {
        return jdbcTemplate.query(SQLUtils.format(sql, getDbTypeString()), args, new EnhanceMapListExtractor(lobHandler));
    }

    /**
     * 增强查询 行数据以Map组织返回
     *
     * @param sql  查询语句
     * @param args 查询参数
     * @return 哈希数组
     */
    public List<Map<String, Object>> queryForEnhanceMapList(String sql, Map<String, Object> args) {
        return getNameTemplate().query(SQLUtils.format(sql, getDbTypeString()), args, new EnhanceMapListExtractor(lobHandler));
    }

    /**
     * 查询 对象以Map组织返回
     *
     * @param sql  查询语句
     * @param args 查询参数
     * @return 哈希数组
     */
    public List<Map<String, Object>> queryForMapList(String sql, Object... args) {
        return jdbcTemplate.queryForList(SQLUtils.format(sql, getDbTypeString()), args);
    }

    /**
     * 查询 对象以Map组织返回
     *
     * @param sql  查询语句
     * @param args 查询参数
     * @return 哈希数组
     */
    public List<Map<String, Object>> queryForMapList(String sql, Map<String, Object> args) {
        return getNameTemplate().queryForList(sql, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 获取查询工具
     *
     * @param clazz 实体
     * @param <T>   实体类
     * @return 查询工具
     */
    public <T> Query<T> getQuery(Class<T> clazz) {
        return new Query<>(this, clazz);
    }


    /****************************************************************************/

    /************************* helper method and others ***********************/

    /**
     * @param pojo 实体
     */
    private void notNull(Object[] pojo) {
        if (pojo == null || pojo.length == 0) {
            throw new DaoException(ExEnum.PARAMS_IS_NULL.toString());
        }
    }

    /**
     * 检查是否实现ITree接口
     *
     * @param clazz 实体
     * @param <T>   实体类
     */
    private <T> void notTree(Class<T> clazz) {

        Class<?>[] interfaces = clazz.getInterfaces();

        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i].equals(ITree.class)) {
                    return;
                }
            }
        }
        throw new DaoException("not implement interface ITree");
    }

    /**
     * 修正JDBC的OROACLE驱动影响行数返回-2
     *
     * @param rows 影响行数数组
     * @return 影响行数总和
     */
    private int sum(int[] rows) {
        int r = 0;
        if (rows != null) {
            for (int i = 0; i < rows.length; i++) {

                if (DBType.ORACLE.equals(getDbType())) {
                    r += rows[i] == -2 ? 0 : rows[i];
                } else {
                    ++r;
                }
            }
        }
        return r;
    }

    @Autowired
    @Qualifier("dataSource")
    public void setDataSource(DataSource ds) {
        dataSource = ds;
        jdbcTemplate = new JdbcTemplate(ds);
        namedJdbcTemplate = new NamedParameterJdbcTemplate(ds);
        lobHandler = new DefaultLobHandler();
        //设置数据库类型
        setDbType(retrieveType(ds));
    }

    /**
     * 获取数据源类型
     *
     * @param ds 数据源
     * @return 数据源类型
     */
    private DBType retrieveType(DataSource ds) {

        Connection connection = DataSourceUtils.getConnection(ds);
        try {
            DatabaseMetaData metaData = connection.getMetaData();

            //查看数据源信息
            logger.info("Current Database : [{}], URL : [{}], Driver Version : [{}], Read Only : [{}], Batch Update Supported : [{}], Instance : [{}]",
                    new Object[]{
                            metaData.getDatabaseProductName(),
                            metaData.getURL(),
                            metaData.getDatabaseProductVersion(),
                            metaData.isReadOnly(),
                            metaData.supportsBatchUpdates(),
                            this.getClass().getName()
                    });

            String databaseProductName = StringUtils.trimToEmpty(metaData.getDatabaseProductName()).toUpperCase();
            if (databaseProductName.contains(DBType.ORACLE.toString())) {
                return DBType.ORACLE;
            } else if (databaseProductName.contains(DBType.MYSQL.toString())) {
                return DBType.MYSQL;
            } else if (databaseProductName.contains(DBType.MICROSOFT.toString())) {
                return DBType.MICROSOFT;
            } else if (databaseProductName.contains(DBType.HSQL.toString())) {
                //H2兼容模式
                return DBType.MYSQL;
            } else if (databaseProductName.contains(DBType.H2.toString())) {
                return DBType.MYSQL;
            } else {
                throw new DaoException("Unsupported Database Type:" + databaseProductName);
            }

        } catch (SQLException e) {
            logger.error("数据源连接出错", e);
            DataSourceUtils.releaseConnection(connection, ds);
        } finally {
            DataSourceUtils.releaseConnection(connection, ds);
        }

        return DBType.ORACLE;

    }

    /**
     * 大数据字段处理类
     */
    protected LobHandler lobHandler;
    /**
     * 数据源
     */
    private DataSource dataSource;
    /**
     * 数据库类型，默认为ORACLE
     */
    protected DBType dbType = DBType.ORACLE;

    private NamedParameterJdbcTemplate namedJdbcTemplate;

    protected JdbcTemplate jdbcTemplate;

    public NamedParameterJdbcTemplate getNameTemplate() {
        if (namedJdbcTemplate == null) {
            namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        }
        return namedJdbcTemplate;
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }

    @Override
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedJdbcTemplate;
    }

    public LobHandler getLobHandler() {
        return lobHandler;
    }

    public BaseDao() {
    }

    public BaseDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    /**
     * 获取数据类型
     * @return
     */
    public String getDbTypeString() {
        switch (getDbType()) {
            case ORACLE:
                return JdbcUtils.ORACLE;
            case MYSQL:
                return JdbcUtils.MYSQL;
            case MICROSOFT:
                return JdbcUtils.SQL_SERVER;
        }
        return "";
    }

    public DBType getDbType() {
        return dbType;
    }

    public void setDbType(DBType dbType) {
        this.dbType = dbType;
    }

    private static Logger logger = LoggerFactory.getLogger(BaseDao.class);

    /**
     * 批量提交的大小
     */
    private static int BATCH_SIZE = 1000;

    protected InsertSqlCreator insertSqlCreator = InsertSqlCreator.getInstance();
    protected UpdateSqlCreator updateSqlCreator = UpdateSqlCreator.getInstance();
    protected DeleteSqlCreator deleteSqlCreator = DeleteSqlCreator.getInstance();
    protected SelectSqlCreator selecteSqlCreator = SelectSqlCreator.getInstance();

}

