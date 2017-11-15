package org.zap.framework.orm.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.zap.framework.orm.creator.ColumnFilter;
import org.zap.framework.orm.criteria.Query;
import org.zap.framework.orm.extractor.Extractor;
import org.zap.framework.orm.itf.IUpdateCallBack;
import org.zap.framework.common.entity.pagination.PaginationSupport;

import java.util.List;
import java.util.Map;

/**
 * Created by Shin on 2017/11/2.
 */
public interface IBaseDao {

    int queryCount(String sql, Object... params);

    int queryCount(String sql, Map<String, Object> params);

    int queryCount(Class<?> clazz);

    int queryCount(Class<?> clazz, String clause, Object... params);

    int queryCount(Class<?> clazz, String clause, Map<String, Object> params);

    <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, Object[] params, int currentPage, int pageSize);

    <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, int currentPage, int pageSize);

    <T> List<T> query(String sql, final Object[] params, Extractor<List<T>> rsExtractor);

    <T> List<T> query(String sql, Extractor<List<T>> rsExtractor);

    <T> List<T> queryAll(Class<T> clazz);

    <T> List<T> queryByClause(Class<T> clazz, String clause, Object... params);

    <T> T queryOneByClause(Class<T> clazz, String clause, Object... params);

    <T> List<T> queryTreeByClause(Class<T> clazz, String clause, Object... params);

    PaginationSupport queryPage(String sql, int currentPage, int pageSize, Object... params);

    PaginationSupport queryPage(String sql, int currentPage, int pageSize, Map<String, Object> params);

    <T> T queryByPrimaryKey(Class<T> clazz, String id);

    <T> List<T> queryByPrimaryKey(Class<T> clazz, String[] ids);

    <T> List<T> queryByPrimaryKey(Class<T> clazz, String[] pks, String clause);

    <T> List<T> queryByMap(Class<T> clazz, Map<String, Object> params, String clause);

    <T> int insert(T entity);

    <T> int insertArray(T[] entities, boolean withId);

    <T> int insertList(List<T> entityList, boolean withId);

    <T> int updateNotNull(T[] entities);

    <T> int updateNotNull(T entity);

    /**
     * 带版本号更新
     * @param entity
     * @param <T>
     * @return
     */
    <T> int update(T entity);

    <T> int update(T entity, String[] cols, boolean include);

    <T> int updateArray(T[] entities);

    <T> int updateArray(T[] entities, String[] cols, boolean include);

    <T> int updateList(List<T> entityList);

    <T> int updateList(List<T> pojoList, String[] cols, boolean include);

    <T> int updateNotVersion(T entity, String[] cols, boolean include);

    <T> int updateArrayNotVersion(T[] entities, String[] cols, boolean include);

    /**
     * 不带版本号更新
     * @param entity
     * @param <T>
     * @return
     */
    <T> int updateNotVersion(T entity);

    <T> int updateArrayNotVersion(T[] entities);

    <T> int updateListNotVersion(List<T> entityList);

    <T> int updateNotVersion(T entity, ColumnFilter filter, IUpdateCallBack callBack);

    <T> int updateByClause(T entity, String[] cols, boolean include, String clause, Object... params);

    <T> int updateArrayByClause(T[] entities, String[] cols, boolean include, String clause, Object... params);

    int deleteAll(Class<?> clazz);

    <T> int delete(T entity);

    <T> int deleteList(List<T> entityList);

    <T> int deleteArray(T[] entities);

    <T> int deleteNotVersion(T entity);

    <T> int deleteArrayNotVersion(T[] entities);

    int deleteByClause(Class<?> clazz, String clause, Object... params);

    int deleteByPrimaryKey(Class<?> clazz, Object... keys);

    <T> int queryByVersion(T entities[]);

    List<Object[]> queryForObjectList(String sql, Object... args);

    List<Map<String, Object>> queryForEnhanceMapList(String sql, Object... args);

    List<Map<String, Object>> queryForEnhanceMapList(String sql, Map<String, Object> args);

    List<Map<String, Object>> queryForMapList(String sql, Object... args);

    List<Map<String, Object>> queryForMapList(String sql, Map<String, Object> args);

    <T> List<T> query(String sql, RowMapper<T> rowMapper);

    <T> Query<T> getQuery(Class<T> clazz);

    JdbcTemplate getJdbcTemplate();

    String getDbTypeString();

}
