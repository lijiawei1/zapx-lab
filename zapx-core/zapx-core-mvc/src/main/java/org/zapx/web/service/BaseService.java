package org.zapx.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zap.framework.exception.BusinessException;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.compiler.PreCompiler;
import org.zap.framework.orm.criteria.Query;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.enhance.temp.TempTable;
import org.zap.framework.orm.page.PaginationSupport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基础接口，事务增强，乐观锁(版本)检查
 *
 * @author Shin
 */
@Service
@Transactional
public class BaseService {

    /****查询方法开始****/
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int queryCount(String sql, Object... params) {
        return baseDao.queryCount(sql, params);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int queryCount(Class<?> clazz, String clause) {
        return baseDao.queryCount(clazz, clause);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int queryCount(Class<?> clazz, String clause, Object... params) {
        return baseDao.queryCount(clazz, clause, params);
    }

    /**
     * 基础查询
     *
     * @param sql  查询语句
     * @param args 查询参数
     * @return 哈希表数组
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String, Object>> queryForMapList(String sql, Object... args) {
        return baseDao.queryForMapList(sql, args);
    }

    /**
     * 基础查询 字段类型查询
     *
     * @param sql  查询语句
     * @param args 查询参数
     * @return 哈希表数组
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Map<String, Object>> queryForEnhanceMapList(String sql, Object... args) {
        return baseDao.queryForEnhanceMapList(sql, args);
    }

    /**
     * 基础查询
     *
     * @param sql  查询语句
     * @param args 查询参数
     * @return 对象数组
     */
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Object[]> queryForObjectList(String sql, Object... args) {
        return baseDao.queryForObjectList(sql, args);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, int currentPage, int pageSize) {
        return baseDao.queryPage(clazz, clause, currentPage, pageSize);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public PaginationSupport queryPage(String sql, int currentPage, int pageSize, Object... params) {
        return baseDao.queryPage(sql, currentPage, pageSize, params);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, Object[] params, int currentPage, int pageSize) {
        return baseDao.queryPage(clazz, clause, params, currentPage, pageSize);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public <T> List<T> queryAll(Class<T> clazz) {
        return baseDao.queryAll(clazz);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public <T> List<T> queryByClause(Class<T> clazz, String clause, Object... params) {
        return baseDao.queryByClause(clazz, clause, params);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public <T> T queryOneByClause(Class<T> clazz, String clause, Object... params) {
        return baseDao.queryOneByClause(clazz, clause, params);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public <T> T queryByPrimaryKey(Class<T> clazz, String pk) {
        return baseDao.queryByPrimaryKey(clazz, pk);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public <T> List<T> queryByPrimaryKey(Class<T> clazz, String[] pks) {
        return baseDao.queryByPrimaryKey(clazz, pks);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public <T> List<T> queryByPrimaryKey(Class<T> clazz, String[] pks,
                                         String clause) {
        return baseDao.queryByPrimaryKey(clazz, pks, clause);
    }
    /****查询方法结束****/

    /****操作方法开始****/
    /**
     * 新增
     *
     * @param entity
     */
    public <T> int insert(T entity) {
        return baseDao.insert(entity);
    }

    /**
     * 新增
     *
     * @param entities
     * @param withId
     */
    public <T> int insert(T[] entities, boolean withId) {
        return baseDao.insertArray(entities, withId);
    }

    /**
     * 插入
     *
     * @param entityList 实体列表
     * @param withId     是否有主键
     * @return
     */
    public <T> int insert(List<T> entityList, boolean withId) {
        return baseDao.insertList(entityList, withId);
    }

    /**
     * 更新
     *
     * @param entity
     */
    public <T> int update(T entity) {
        return checkOptimisticLock(baseDao.update(entity), 1);
    }

    /**
     * 悲观锁
     *
     * @param entities
     */
    public <T> int update(T[] entities) {
        return checkOptimisticLock(baseDao.update(entities), entities.length);
    }

    /**
     * 更新
     *
     * @param entity  待更新VO
     * @param cols    字段
     * @param include 如果true：只更新传入的cols字段；如果false：更新除了传入的cols的其它业务字段
     */
    public <T> int update(T entity, String[] cols, boolean include) {
        return checkOptimisticLock(baseDao.update(entity, cols, include), 1);
    }

    /**
     * 更新
     *
     * @param entities 待更新VO
     * @param cols     字段
     * @param include  如果true：只更新传入的cols字段；如果false：更新除了传入的cols的其它业务字段
     */
    public <T> int update(T[] entities, String[] cols, boolean include) {
        //检查数据一致性
        return checkOptimisticLock(baseDao.update(entities, cols, include), entities.length);
    }

    /**
     * 更新
     *
     * @param entities 待更新VO
     * @param cols     只更新指定字段
     */
    public <T> int updatePartFields(T[] entities, String[] cols) {
        return checkOptimisticLock(baseDao.update(entities, cols, true), entities.length);
    }

    /**
     * 更新部分字段
     *
     * @param entity 持久化类
     * @param cols   只更新指定字段
     * @return
     */
    public <T> int updatePartField(T entity, String[] cols) {
        return checkOptimisticLock(baseDao.update(entity, cols, true), 1);
    }

    /**
     * 根据条件字段更新
     * 不支持版本号
     *
     * @param entity
     * @param cols
     * @param include
     * @param clause
     * @param params
     * @param <T>
     * @return
     */
    public <T> int updateByClause(T entity, String[] cols, boolean include, String clause, Object... params) {
        return baseDao.updateByClause(entity, cols, include, clause, params);
    }

    /**
     * 检查乐观锁
     *
     * @param affectedRow
     * @param perRow
     * @return
     */
    public int checkOptimisticLock(int affectedRow, int perRow) {
        if (affectedRow != perRow) {
            throw new BusinessException("该数据已经被修改，请刷新");
        }
        return affectedRow;
    }

    /**
     * TODO 乐观锁检查
     * <p>
     * 带版本校验
     *
     * @param entity 实体
     * @param <T>    继承基础实体
     * @return
     */
    public <T> int delete(T entity) {
        return checkOptimisticLock(baseDao.delete(entity), 1);
    }

    /**
     * TODO 乐观锁检查
     * <p>
     * 带版本校验
     *
     * @param entities 实体
     * @param <T>      继承基础实体
     * @return
     */
    public <T> int delete(T[] entities) {
        return checkOptimisticLock(baseDao.delete(entities), entities.length);
    }

    /**
     * TODO 乐观锁检查
     * <p>
     * 带版本校验
     *
     * @param entityList 实体
     * @param <T>        继承基础实体
     * @return
     */
    public <T> int delete(List<T> entityList) {
        return checkOptimisticLock(baseDao.delete(entityList), entityList.size());
    }

    public <T> int deleteNotVersion(T entity) {
        return baseDao.delete(entity);
    }

    public <T> int deleteNotVersion(T[] entities) {
        return baseDao.delete(entities);
    }

    public <T> int deleteNotVersion(List<T> entityList) {
        return baseDao.delete(entityList);
    }


    /**
     *
     */
    public int deleteByClause(Class<?> clazz, String clause, Object... params) {
        return baseDao.deleteByClause(clazz, clause, params);
    }

    /**
     * 数据库删除
     *
     * @param clazz
     * @param keys
     */
    public int deleteByPrimaryKey(Class<?> clazz, Object... keys) {
        return baseDao.deleteByPrimaryKey(clazz, keys);
    }

    public <T> int updateNotVersion(T entities) {
        return baseDao.updateNotVersion(entities);
    }

    public <T> int updateNotVersion(T[] entities) {
        return baseDao.updateNotVersion(entities);
    }

    public <T> int updateNotVersion(T entity, String[] cols, boolean include) {
        return baseDao.updateNotVersion(entity, cols, include);
    }

    public <T> int updateNotVersion(T[] entities, String[] cols, boolean include) {
        return baseDao.updateNotVersion(entities, cols, include);
    }

    /****
     * 操作方法结束
     ****/


    public <T> Query<T> getQuery(Class<T> clazz) {
        return baseDao.getQuery(clazz);
    }

    private IBaseDao baseDao;

    private static Logger logger = LoggerFactory.getLogger(BaseService.class);

    public IBaseDao getBaseDao() {
        return baseDao;
    }

    @Autowired
    @Qualifier(value = "baseDao")
    public void setBaseDao(IBaseDao baseDao) {
        this.baseDao = baseDao;
    }


    /******************************************临时表相关*****************************************/
    /**
     * @param clazz
     * @param clause
     * @param attrs  字段名称
     * @param values 属性值
     * @param <T>
     * @return
     */
    @Transactional(readOnly = true)
    public <T> List<T> queryByTemp(Class<T> clazz, String clause, String[] attrs, Object[] values) {
        //return baseDao.queryByClause(clazz, clause, params);


        return null;
    }

    /**
     * 临时表查询
     *
     * @param clazz
     */
    public <T> List<T> queryByTemp(Class<?> clazz) {

        List<T> arrlist = new ArrayList<T>();

        BeanProperty beanProperty = PreCompiler.getInstance().getBeanProperty(clazz);
        String tableName = beanProperty.getTableName();

        //时间后缀
        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));

        String tempTableName = tableName + suffix;

        TempTable tempTable = new TempTable();
        tempTableName = tempTable.createTempTable(tempTableName, new String[]{}, null);

        //临时表插入数值
        String sql = "INSERT INTO " + tempTableName + "() VALUES(?)";
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        baseDao.getJdbcTemplate().batchUpdate(sql, batchArgs);

        tempTable.dropTempTable(tempTableName);


        return arrlist;
    }

    public void tempTable() {

        //同一事务里是成功的
        TempTable tempTable = new TempTable();
        try {
            Integer wtfbefore = baseDao.getJdbcTemplate().queryForObject("SELECT COUNT(1) FROM DDD", Integer.class);

            int update = baseDao.getJdbcTemplate().update("INSERT INTO DDD(DD, DR, TS) VALUES('SS', 89, 'DDDD')");

            Integer wtf = baseDao.getJdbcTemplate().queryForObject("SELECT COUNT(1) FROM DDD", Integer.class);

            //System.out.println(String.valueOf(wtf));
        } catch (DataAccessException e) {
            //e.printStackTrace();
            logger.error("", e);
        }
        tempTable.setJdbcTemplate(baseDao.getJdbcTemplate());
        tempTable.create("ddd", new String[]{
                "dd varchar(100)", "ts char(19)", "dr integer"
        }, null);


    }
}
