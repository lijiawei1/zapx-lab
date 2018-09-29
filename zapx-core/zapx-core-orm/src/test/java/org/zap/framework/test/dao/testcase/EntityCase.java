package org.zap.framework.test.dao.testcase;

import com.eaio.uuid.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.zap.framework.lang.LDouble;
import org.zap.framework.orm.base.BaseEntity;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.exception.ExEnum;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.test.entity.*;
import org.zap.framework.test.pojo.TestProcessor;
import org.zap.framework.test.pojo.TestVo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 实体测试用例
 * 多数据库
 * Created by Shin on 2015/12/24.
 */
public class EntityCase {

    public void testMulti() {
        List<MultiIdEntity> multiIdEntities = baseDao.queryAll(MultiIdEntity.class);

        MultiIdEntity entity = new MultiIdEntity();

        entity.setCar_id(new com.eaio.uuid.UUID().toString());
        entity.setMst_id(new UUID().toString());

        baseDao.insert(entity);

    }

    public void testCheck() {

        try {
            //未实现序列化接口
            baseDao.queryAll(UnserializableEntity.class);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            assertEquals(e.getClass(), DaoException.class);
            assertEquals(ExEnum.UNSERIALIZABLE.toString(), e.getMessage());
        }

        try {
            //没有注解
            baseDao.queryAll(NotAnnotationEntity.class);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            assertEquals(e.getClass(), DaoException.class);
            assertEquals(ExEnum.ANNOTATION_NOT_FOUND.toString(), e.getMessage());
        }
    }

    public void testQuery() {

        List<TestVo> testVos = baseDao.queryAll(TestVo.class);
        Map<String, TestVo> collect = testVos.stream().filter(t -> t.getInt_field() <= 2).collect(Collectors.toMap(t -> t.getId(), t -> t));
        String[] ids = collect.keySet().stream().toArray(String[]::new);

        testQueryCount();
        //
        testQueryByPrimaryKey(ids);

        //testQueryForVersion(ids, collect);

        testQueryOneByClause();

        testQueryByClause(ids);

        testQueryProcessor();
    }

    protected void testQueryProcessor() {
        List<TestProcessor> rows = baseDao.query("SELECT DATE_FIELD, VARCHAR_FIELD FROM ZAP_TEST ", new BeanListExtractor(TestProcessor.class));
        for (TestProcessor tp : rows) {
            logger.debug(ReflectionToStringBuilder.toString(tp));
            assertTrue(tp.getDate_field() != null);
            assertTrue(tp.getVarchar_field() != null);
        }

        List<Map<String, Object>> maplist = baseDao.queryForEnhanceMapList("SELECT * FROM ZAP_TEST ORDER BY INT_FIELD");
        Map<String, Object> m1 = maplist.get(0);
        assertEquals(m1.get("old_datetime").getClass(), LocalDateTime.class);
        assertEquals(m1.get("number_field").getClass(), LDouble.class);
        assertEquals(m1.get("ldouble_field").getClass(), LDouble.class);
        assertEquals(m1.get("int_field").getClass(), Integer.class);


    }

    protected void testQueryByClause(String[] ids) {
        List<TestVo> queryByClause1 = baseDao.queryByClause(TestVo.class, " ZT.ID = ? ", "");
        assertTrue(queryByClause1.size() == 0);
        List<TestVo> queryByClause2 = baseDao.queryByClause(TestVo.class, " ZT.ID = ? ", ids[0]);
        assertTrue(queryByClause2.size() == 1);
        List<TestVo> queryByClause3 = baseDao.queryByClause(TestVo.class, " ZT.ID = ? OR ZT.ID = ? ", ids[0], ids[1]);
        assertTrue(queryByClause3.size() == 2);
        List<TestVo> queryByClause4 = baseDao.queryByClause(TestVo.class, " ZT.INT_FIELD = ? ", 0);
        assertTrue(queryByClause4.size() == 1);
        assertTrue(queryByClause4.get(0) != null);
        assertTrue(queryByClause4.get(0).getInt_field() == 0);

        List<TestVo> queryByClause5 = baseDao.queryByClause(TestVo.class, null);
        assertTrue(queryByClause5.size() == TOTAL_COUNT_20);
    }

    protected void testQueryOneByClause() {

        //查询条件为空
        TestVo queryOneNull = baseDao.queryOneByClause(TestVo.class, null);
        assertTrue(queryOneNull != null && StringUtils.isNotBlank(queryOneNull.getId()));
        TestVo queryOneEmpty = baseDao.queryOneByClause(TestVo.class, "");
        assertTrue(queryOneEmpty != null && StringUtils.isNotBlank(queryOneEmpty.getId()));

        //测试条件
        TestVo queryOneP1 = baseDao.queryOneByClause(TestVo.class, " ZT.ID = ? ", new String[]{""});
        assertTrue(queryOneP1 == null);
        TestVo queryOneP2 = baseDao.queryOneByClause(TestVo.class, " ZT.ID = ? ", "");
        assertTrue(queryOneP2 == null);

        //测试排序
        TestVo queryOneOrder = baseDao.queryOneByClause(TestVo.class, "ORDER BY ZT.INT_FIELD");
        assertTrue(queryOneOrder != null && queryOneOrder.getInt_field() == 0);
        assertTrue(queryOneOrder != null && queryOneOrder.getInteger_field() == 0);

    }


    /**
     * 测试版本号
     */
    //protected void testQueryForVersion(String[] ids, Map<String, TestVo> collect) {
    //
    //    logger.debug("--------------------------------queryForVersion");
    //    List<BaseEntity> baseEntityList = baseDao.queryForVersion(TestVo.class, ids);
    //    baseEntityList.forEach(b -> assertEquals(b.getVersion(), collect.get(b.getId()).getVersion()));
    //
    //    try {
    //        //视图不支持版本查询
    //        baseDao.queryForVersion(TestView.class, "");
    //    } catch (RuntimeException e) {
    //        logger.error(e.getMessage(), e);
    //        assertEquals(e.getClass(), DaoException.class);
    //        assertEquals(ExEnum.VIEW_VERSION_NOT_SUPPORT.toString(), e.getMessage());
    //    }
    //
    //    try {
    //        //实体不支持版本查询
    //        baseDao.queryForVersion(NotVerEntity.class, "");
    //    } catch (RuntimeException e) {
    //        logger.error(e.getMessage(), e);
    //        assertEquals(e.getClass(), DaoException.class);
    //        assertEquals(ExEnum.ENTITY_VERSION_NOT_SUPPORT.toString(), e.getMessage());
    //    }
    //
    //}

    /**
     * 测试条件
     */
    protected void testQueryByPrimaryKey(String[] ids) {
        logger.debug("--------------------------------queryByPrimaryKey");

        //查询单个主键
        assertEquals(baseDao.queryByPrimaryKey(TestVo.class, ids[0]).getId(), ids[0]);
        //查询主键数组
        assertTrue(baseDao.queryByPrimaryKey(TestVo.class, ids).stream().map(TestVo::getId).collect(Collectors.toSet())
                .containsAll(Arrays.asList(ids)));

        //查询数组带条件并排序
        int[] ints = baseDao.queryByPrimaryKey(TestVo.class, ids, " ZT.DR = 0 ORDER BY ZT.INT_FIELD DESC").stream()
                .mapToInt(TestVo::getInt_field).toArray();
        for (int i = 0; i < 3; i++) {
            assertEquals(2 - i, ints[i]);
        }

    }

    /**
     * 测试计数
     */
    protected void testQueryCount() {

        logger.debug("--------------------------------queryCount");
        assertEquals(TOTAL_COUNT_20, baseDao.queryCount("SELECT * FROM ZAP_TEST"));
        assertEquals(1, baseDao.queryCount("SELECT * FROM ZAP_TEST WHERE INT_FIELD = ?", 1));
        assertEquals(0, baseDao.queryCount("SELECT * FROM ZAP_TEST WHERE 1 <> 1"));

        assertEquals(TOTAL_COUNT_20, baseDao.queryCount(TestVo.class));

        assertEquals(TOTAL_COUNT_20, baseDao.queryCount(TestVo.class, null));
        assertEquals(TOTAL_COUNT_20, baseDao.queryCount(TestVo.class, ""));
        assertEquals(1, baseDao.queryCount(TestVo.class, " ZT.INT_FIELD = ?", 1));
        assertEquals(TOTAL_COUNT_20 - 1, baseDao.queryCount(TestVo.class, " ZT.INT_FIELD <> 1"));
        assertEquals(TOTAL_COUNT_20 - 1, baseDao.queryCount(TestVo.class, " ZT.INT_FIELD <> ?", 2));

        //多查询参数
        assertEquals(TOTAL_COUNT_20 - 2, baseDao.queryCount(TestVo.class,
                " ZT.INT_FIELD <> ? AND ZT.INT_FIELD <> ?",
                1, 2));
        assertEquals(TOTAL_COUNT_20 - 2, baseDao.queryCount(TestVo.class,
                " ZT.INT_FIELD <> ? AND ZT.INT_FIELD <> ?",
                new Object[]{1, 2}));

    }

    /**
     * 测试没有版本号
     */
    protected void testForNowVersion() {

    }


    /**
     * 测试前初始化数据
     */
    public void initData(Resource resource, BaseDao baseDao) throws IOException {

        //删除全部
        baseDao.deleteAll(TestVo.class);
        List<String> readLines = FileUtils.readLines(resource.getFile());
        //执行
        long start = System.currentTimeMillis();
        List<String> batch = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            String line = readLines.get(i);

            batch.add(line);
            if (batch.size() == 200) {
                baseDao.getJdbcTemplate().batchUpdate(batch.toArray(new String[batch.size()]));
                batch.clear();
            }
        }
        if (batch.size() > 0) {
            baseDao.getJdbcTemplate().batchUpdate(batch.toArray(new String[batch.size()]));
            batch.clear();
        }
        long end = System.currentTimeMillis();
        logger.debug("耗时：" + String.valueOf(end - start));

    }

    private IBaseDao baseDao;

    public EntityCase(IBaseDao baseDao) {
        this.baseDao = baseDao;
    }

    private static Logger logger = LoggerFactory.getLogger(EntityCase.class);

    public static int TOTAL_COUNT_20 = 20;
}
