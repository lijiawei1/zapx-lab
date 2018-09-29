package org.zap.framework.test.dao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.zap.framework.common.entity.pagination.PaginationSupport;
import org.zap.framework.lang.LDouble;
import org.zap.framework.orm.base.BaseEntity;
import org.zap.framework.orm.criteria.Query;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.test.entity.NotVerEntity;
import org.zap.framework.test.entity.TestView;
import org.zap.framework.test.pojo.TestProcessor;
import org.zap.framework.test.pojo.TestVo;
import org.zap.framework.test.pojo.TestVoComparator;
import org.zap.framework.test.pojo.TestVoFactory;
import org.zap.framework.util.Utils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//import org.zap.framework.orm.enhance.temp.TempTableFactory;
//import org.zap.framework.orm.itf.IDataLang;
//import org.zap.framework.orm.itf.ITempTable;

/**
 * 数据层试用例
 *
 * @author Shin
 */
public class DaoTestCase {

    private static Logger logger = LoggerFactory.getLogger(DaoTestCase.class);

    public static int TOTAL_COUNT_20 = 20;

    /**
     * 测试前初始化数据
     */
    public static void initData(Resource resource, IBaseDao baseDao) throws IOException {

        //删除全部
        baseDao.deleteAll(TestVo.class);
        List<String> readLines = FileUtils.readLines(resource.getFile());
        //执行
        long start = System.currentTimeMillis();
        List<String> batch = new ArrayList<String>();
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


    /**
     * 测试结果抽取器器
     *
     * @param baseDao
     */
    public static void testExtractor(IBaseDao baseDao) {

        List<Map<String, Object>> maps = baseDao.queryForEnhanceMapList("SELECT * FROM ZAP_TEST");

        logger.debug("{}", maps.get(0));

        List<Map<String, Object>> mapList = baseDao.queryForEnhanceMapList("SELECT INT_FIELD, VARCHAR_FIELD FROM ZAP_TEST ORDER BY INT_FIELD");

        int pageSize = 3;
        for (int page = 0; page < 3; page++) {
            PaginationSupport ps = baseDao.queryPage("SELECT INT_FIELD, VARCHAR_FIELD FROM ZAP_TEST ORDER BY INT_FIELD ", page, pageSize);

            assertEquals(page, ps.getCurrentPage());
            assertEquals(page * pageSize + 1, ps.getStart());
            assertEquals((page + 1) * pageSize, ps.getEnd());

            List<Map<String, Object>> data = ps.getData();

            for (int i = 0; i < data.size(); i++) {

                int index = ps.getStart() + i - 1;
                assertEquals(data.get(i).get("int_field"), mapList.get(index).get("int_field"));
            }
        }
    }

    /**
     * 测试选择
     *
     * @param baseDao
     */
    public static void testSelect(IBaseDao baseDao) {

        StringBuffer buffer = new StringBuffer();

        List<TestVo> queryAll = baseDao.queryAll(TestVo.class);

        TestVo vo0 = queryAll.get(0);
        TestVo vo1 = queryAll.get(1);
        TestVo vo2 = queryAll.get(2);

        final Map<String, TestVo> testVoMap = queryAll.stream().collect(Collectors.toMap(t -> t.getId(), t -> t));

        logger.debug("--------------------------------阶段一");
        //assertEquals(TOTAL_COUNT_20, baseDao.queryCount(TestVo.class));
        assertEquals(TOTAL_COUNT_20, queryAll.size());
        assertEquals(1, baseDao.queryCount(TestVo.class, "ZT.id = ?", vo0.getId()));

        logger.debug("--------------------------------阶段二");
        buffer.append(" ZT.ID IN (")
                .append(StringUtils.repeat("?", ",", 3))
                .append(")");
        assertEquals(3, baseDao.queryCount(TestVo.class, buffer.toString(), vo0.getId(), vo1.getId(), vo2.getId()));

        logger.debug("--------------------------------阶段三");
        TestVo querypk1 = baseDao.queryByPrimaryKey(TestVo.class, vo0.getId());
        assertEquals(querypk1.getId(), vo0.getId());

        logger.debug("--------------------------------阶段四");
        List<TestVo> querypk3 = baseDao.queryByPrimaryKey(TestVo.class, new String[]{vo0.getId(), vo1.getId(), vo2.getId()});
        Set<TestVo> queryidSet = Utils.collection2FieldSet(querypk3, "id", TestVo.class);
        assertTrue(queryidSet.containsAll(Arrays.asList(vo0.getId(), vo1.getId(), vo2.getId())));

        logger.debug("--------------------------------阶段五-主键数组查询");
        List<String> idlist = Utils.collection2FieldList(queryAll, "id", String.class);
        List<TestVo> byPrimaryKey = baseDao.queryByPrimaryKey(TestVo.class, idlist.toArray(new String[idlist.size()]));
        assertEquals(queryAll.size(), byPrimaryKey.size());

        logger.debug("--------------------------------阶段六-主键数组查询带条件");
        List<TestVo> byPrimaryKeyClause = baseDao.queryByPrimaryKey(TestVo.class, new String[]{vo0.getId(), vo1.getId(), vo2.getId()}, " ZT.DR = 0 ORDER BY ZT.ID");
        List<String> collect = byPrimaryKeyClause.stream().map(t -> t.getId()).collect(Collectors.toList());
        assertTrue(collect.containsAll(Arrays.asList(vo0.getId(), vo1.getId(), vo2.getId())));

        logger.debug("--------------------------------阶段七-版本号");
        //List<BaseEntity> baseEntityList = baseDao.queryForVersion(TestVo.class, new String[]{vo0.getId(), vo1.getId(), vo2.getId()});
        //baseEntityList.forEach(b -> assertEquals(b.getVersion(), testVoMap.get(b.getId()).getVersion()));

        logger.debug("--------------------------------阶段八-查询单个");
        TestVo queryOneNull = baseDao.queryOneByClause(TestVo.class, null);
        assertTrue(queryOneNull != null && StringUtils.isNotBlank(queryOneNull.getId()));
        TestVo queryOneEmpty = baseDao.queryOneByClause(TestVo.class, "");
        assertTrue(queryOneNull != null && StringUtils.isNotBlank(queryOneEmpty.getId()));
        //TestVo queryOneNullId = baseDao.queryOneByClause(TestVo.class, " zt.id = ? ", null);
        //assertTrue(queryOneNullId.getId() == null);
        TestVo queryOneP1 = baseDao.queryOneByClause(TestVo.class, " ZT.ID = ? ", new String[]{""});
        assertTrue(queryOneP1 == null);
        TestVo queryOneP2 = baseDao.queryOneByClause(TestVo.class, " ZT.ID = ? ", "");
        assertTrue(queryOneP2 == null);
        try {
//			TestVo queryOneP3 = baseDao.queryOneByClause(TestVo.class, " ZT.ID = ? ", null);
//			assertTrue(queryOneP3 == null);
        } catch (DaoException de) {
        }
        TestVo queryOneOrder = baseDao.queryOneByClause(TestVo.class, "ORDER BY ZT.INT_FIELD");
        assertTrue(queryOneOrder != null && queryOneOrder.getInt_field() == 0);
        assertTrue(queryOneOrder != null && queryOneOrder.getInteger_field() == 0);

        logger.debug("--------------------------------查询一个");
//		baseDao.querySortByClause(clazz, sortCols, clause, params, asc)

        List<TestVo> queryByClause1 = baseDao.queryByClause(TestVo.class, " ZT.ID = ? ", "");
        assertTrue(queryByClause1.size() == 0);
        List<TestVo> queryByClause2 = baseDao.queryByClause(TestVo.class, " ZT.ID = ? ", vo0.getId());
        assertTrue(queryByClause2.size() == 1);
        List<TestVo> queryByClause3 = baseDao.queryByClause(TestVo.class, " ZT.ID = ? OR ZT.ID = ? ", vo0.getId(), vo1.getId());
        assertTrue(queryByClause3.size() == 2);
        ;

        List<TestVo> queryByClause4 = baseDao.queryByClause(TestVo.class, " ZT.INT_FIELD = ? ", 0);
        assertTrue(queryByClause4.size() == 1);
        assertTrue(queryByClause4.get(0) != null);
        assertTrue(queryByClause4.get(0).getInt_field() == 0);

        List<TestVo> queryByClause5 = baseDao.queryByClause(TestVo.class, null);
        assertTrue(queryByClause5.size() == TOTAL_COUNT_20);

        logger.debug("--------------------------------阶段九-包装类");
        List<TestProcessor> rows = baseDao.query("SELECT DATE_FIELD, VARCHAR_FIELD FROM ZAP_TEST ", new BeanListExtractor(TestProcessor.class));
        for (TestProcessor tp : rows) {
            logger.debug(ReflectionToStringBuilder.toString(tp));
            assertTrue(tp.getDate_field() != null);
            assertTrue(tp.getVarchar_field() != null);
        }

        logger.debug("--------------------------------阶段十-MAPLIST");
        List<Map<String, Object>> maplist = baseDao.queryForMapList("SELECT * FROM ZAP_TEST");

        //必须是基本类型才生效
//		List<TestProcessor> objectlist = baseDao.getJdbcTemplate().queryForList("SELECT * FROM ZAP_TEST", TestProcessor.class);

        List<Object[]> objectList = baseDao.queryForObjectList("SELECT DATE_FIELD, VARCHAR_FIELD FROM ZAP_TEST");
        for (Object[] m : objectList) {
            assertTrue(m.length == 2);
//			assertTrue(m[0] == "2015-07-21 19:52:29");
            assertTrue(m[1].toString().startsWith(TestVo.VARCHAR_FIELD));
            logger.debug(Arrays.toString(m));
        }

    }

    /**
     * 测试视图
     *
     * @param baseDao
     */
    public static void testView(IBaseDao baseDao) {
        TestView testView = baseDao.queryOneByClause(TestView.class, " TV.INT_FIELD = 1 ");
        logger.debug("[{}]", testView.getInt_field());
    }

    /**
     * 测试分页
     *
     * @param baseDao
     */
    public static void testPage(IBaseDao baseDao) {
        List<TestVo> queryAll = baseDao.queryAll(TestVo.class);
        PaginationSupport<TestVo> queryPage = baseDao.queryPage(TestVo.class, " ORDER BY INT_FIELD ", 0, 5);
        //参照数组排序
        Collections.sort(queryAll, (o1, o2) -> o1.getInt_field() - o2.getInt_field());

        //比较
        for (int i = 0; i < queryPage.getData().size(); i++) {
            List<TestVo> data = queryPage.getData();
            TestVoComparator.compare(data.get(i), queryAll.get(i));
        }

    }

    /**
     * 测试插入
     *
     * @param baseDao
     */
    public static void testInsert(IBaseDao baseDao) {

        TestVo[] testVos = TestVoFactory.getInstance().create(2);
        testVos[1].setId(null);
        try {
            baseDao.insertArray(testVos, true);
        } catch (Exception e) {
        }

    }


    /**
     * 初始化数据
     *
     * @param baseDao
     */
    private static TestVo[] initData4TestIntField(IBaseDao baseDao) {
        //构造测试数据
        TestVo[] vos = new TestVo[5];

        for (int i = 0; i < vos.length; i++) {
            vos[i] = new TestVo();
        }

        //初始化整形
        vos[0].setInt_field(Integer.MIN_VALUE);
        vos[0].setInteger_field(Integer.MIN_VALUE);

        vos[1].setInt_field(-1);
        vos[1].setInteger_field(-1);

        vos[2].setInt_field(0);
        vos[2].setInteger_field(0);

        vos[3].setInt_field(1);
        vos[3].setInteger_field(1);

        vos[4].setInt_field(Integer.MAX_VALUE);
        vos[4].setInteger_field(Integer.MAX_VALUE);

        baseDao.deleteAll(TestVo.class);
        baseDao.insertArray(vos, false);

        return vos;
    }

    private static TestVo[] initData4TestLDoubleField(IBaseDao baseDao) {

        //构造测试数据
        TestVo[] vos = new TestVo[9];

        for (int i = 0; i < vos.length; i++) {
            vos[i] = new TestVo();
        }
        //10,8
        vos[0].setLdouble_field(new LDouble("-9999999999.12345678"));
        vos[1].setLdouble_field(LDouble.ZERO_DBL.sub(LDouble.ONE_DBL));

        vos[2].setLdouble_field(new LDouble(-0.11111111));
        vos[3].setLdouble_field(LDouble.ZERO_DBL);
        vos[4].setLdouble_field(new LDouble("0.11111111"));
        vos[5].setLdouble_field(LDouble.ONE_DBL);

        vos[6].setLdouble_field(new LDouble(1000000000.00000001));
        vos[7].setLdouble_field(new LDouble("1000000000.00000001"));

        vos[8].setLdouble_field(new LDouble("9999999999.12345678"));

        baseDao.deleteAll(TestVo.class);
        baseDao.insertArray(vos, false);

        return vos;
    }

    private static TestVo initData4TestNULL(IBaseDao baseDao) {
        TestVo vo = new TestVo();

        vo.setInt_field(0);
        vo.setInteger_field(null);
        vo.setBool_field(true);
        vo.setBoolean_field(null);
        vo.setLdouble_field(null);
        vo.setLong_field(null);
        vo.setVarchar_field(null);
        vo.setChar_field("");

        baseDao.deleteAll(TestVo.class);
        baseDao.insert(vo);

        return vo;

    }

    /**
     * 测试Oracle数据库的Date类型
     *
     * @param baseDao
     */
    public static void testOldDate(IBaseDao baseDao) {

        baseDao.getJdbcTemplate().execute(
                "UPDATE ZAP_TEST SET OLD_DATETIME = SYSDATE + INTEGER_FIELD, OLD_DATE = SYSDATE + INTEGER_FIELD, OLD_TIME = SYSDATE + INTEGER_FIELD"
        );

        List<Object[]> objectList = baseDao.queryForObjectList("SELECT OLD_DATETIME,OLD_DATE,OLD_TIME FROM ZAP_TEST WHERE INT_FIELD = 1");
        if (objectList.size() > 0) {
            Object[] objects = objectList.get(0);

            //TODO JDBC 对数据库中date类型的包装类sql.Date默认只有年月日,暂时在结果集中用getTimestamp处理
            logger.debug("{}", objects[0].getClass());
        }
        TestVo testVo = baseDao.queryOneByClause(TestVo.class, " INT_FIELD = ? ", 1);

        logger.debug("[{}] [{}] [{}]", testVo.getOld_datetime(), testVo.getOld_date(), testVo.getOld_time());


        TestVo testVo1 = baseDao.queryOneByClause(TestVo.class, "");

        testVo1.setOld_datetime(null);
        testVo1.setOld_date(LocalDate.now());

        baseDao.update(testVo1, new String[] { "old_datetime", "old_date"}, true);

        TestVo testVo2 = baseDao.queryByPrimaryKey(TestVo.class, testVo1.getId());

        assertEquals(null, testVo2.getOld_datetime());
        System.out.println(ReflectionToStringBuilder.toString(testVo2, ToStringStyle.MULTI_LINE_STYLE));

    }

    /**
     * 测试整型
     *
     * @param baseDao
     */
    public static void testInt(IBaseDao baseDao) {

        //初始化整形
        TestVo[] vos = initData4TestIntField(baseDao);

        List<TestVo> queryVos = baseDao.queryByClause(TestVo.class, " ORDER BY INT_FIELD");
        for (int i = 0; i < vos.length; i++) {
            assertTrue(Integer.compare(queryVos.get(i).getInt_field(), vos[i].getInt_field()) == 0);
            assertTrue(Integer.compare(queryVos.get(i).getInteger_field(), vos[i].getInteger_field()) == 0);
        }
    }

    /**
     * 测试LDouble
     *
     * @param baseDao
     */
    public static void testLDouble(IBaseDao baseDao) {

        TestVo[] vos = initData4TestLDoubleField(baseDao);

        List<TestVo> queryVos = baseDao.queryByClause(TestVo.class, " ORDER BY LDOUBLE_FIELD");
        for (int i = 0; i < vos.length; i++) {
            logger.debug("Qry:[" + queryVos.get(i).getLdouble_field().toString() + "]  Gen:[" + vos[i].getLdouble_field().toString() + "]");
            assertTrue(vos[i].getLdouble_field().compareTo(queryVos.get(i).getLdouble_field()) == 0);
        }

    }

    /**
     * 测试字段为空
     *
     * @param baseDao
     */
    public static void testNULL(IBaseDao baseDao) {
        //初始化
        initData4TestNULL(baseDao);

        TestVo nullvo = baseDao.queryOneByClause(TestVo.class, "");
        assertEquals(null, nullvo.getBoolean_field());
        assertEquals(null, nullvo.getInteger_field());
        assertEquals(0, nullvo.getInt_field());
        assertEquals(null, nullvo.getLong_field());
        assertEquals(null, nullvo.getLdouble_field());
        //MYSQL的空字符串""
//		assertEquals("", nullvo.getVarchar_field());
//		assertEquals(null, nullvo.getChar_field());

//		vo.setInt_field(0);
//		vo.setInteger_field(null);
//		vo.setBool_field(true);
//		vo.setBoolean_field(null);
//		vo.setLdouble_field(null);
//		vo.setLong_field(null);
//		vo.setVarchar_field(null);
//		vo.setChar_field("");

    }

    /**
     * 测试所有更新方法的入口
     *
     * @param baseDao
     */
    public static void testUpdate(IBaseDao baseDao) {

        //查出首个元素
        //List<TestVo> testVos = baseDao.queryAll(TestVo.class);
        //TestVo testVo = testVos.get(0);
        //
        ////原值
        //String originValue = testVo.getVarchar_field();
        ////修改值
        //String updateValue = "111111";
        //
        //int originIntValue = testVo.getInt_field();
        //int updateIntValue = 9999;
        //
        ////设置为修改值
        //testVo.setVarchar_field(updateValue);
        //testVo.setInt_field(updateIntValue);
        //
        ////更新后判断是否修改了该字段
        //baseDao.update(testVo, new String[]{"varchar_field"}, false);
        //TestVo queryVo = baseDao.queryByPrimaryKey(TestVo.class, testVo.getId());
        //assertEquals(originValue, queryVo.getVarchar_field());
        //assertEquals(updateIntValue, queryVo.getInt_field());
        //
        ////更新后判断是否修改了改字段
        //queryVo.setVarchar_field(updateValue);
        //queryVo.setInt_field(originIntValue);
        //baseDao.update(queryVo, new String[]{"varchar_field"}, true);
        //TestVo queryVo2 = baseDao.queryByPrimaryKey(TestVo.class, testVo.getId());
        //assertEquals(updateValue, queryVo2.getVarchar_field());
        //assertEquals(updateIntValue, queryVo2.getInt_field());

//        TestVo vonull = new TestVo();
//        vonull.setId("sdf");
//        int updateCount = baseDao.update(vonull);
//        assertEquals(updateCount, 0);

        TestVo vo1 = new TestVo();
        vo1.setInt_field(99);
        int i = baseDao.updateByClause(vo1, new String[]{"int_field"}, true, " INT_FIELD = ? ", 1);
        assertEquals(i, 1);
    }

    /**
     * 测试更新方法适配接口
     *
     * @param baseDao
     */
    public static void testUpdate1(IBaseDao baseDao) {
        TestVo testVo = baseDao.queryOneByClause(TestVo.class, "");
        String id = testVo.getId();

        //更新版本号
        TestVo testVo1 = baseDao.queryByPrimaryKey(TestVo.class, id);
        int version1 = testVo1.getVersion();
        assertEquals(1, baseDao.update(testVo1));
        TestVo testVo11 = baseDao.queryByPrimaryKey(TestVo.class, id);
        assertEquals(version1 + 1, testVo11.getVersion());

        //更新版本号
        TestVo testVo2 = baseDao.queryByPrimaryKey(TestVo.class, id);
        int version2 = testVo2.getVersion();
        assertEquals(1, baseDao.updateList(Arrays.asList(testVo2)));
        TestVo testVo21 = baseDao.queryByPrimaryKey(TestVo.class, id);
        assertEquals(version2 + 1, testVo21.getVersion());

        //更新版本号
        TestVo testVo3 = baseDao.queryByPrimaryKey(TestVo.class, id);
        int version3 = testVo3.getVersion();
        assertEquals(1, baseDao.updateArray(new TestVo[]{testVo3}));
        TestVo testVo31 = baseDao.queryByPrimaryKey(TestVo.class, id);
        assertEquals(version3 + 1, testVo31.getVersion());

        //不更新版本号
        TestVo testVo4 = baseDao.queryByPrimaryKey(TestVo.class, id);
        int version4 = testVo4.getVersion();
        assertEquals(1, baseDao.updateNotVersion(testVo4));
        TestVo testVo41 = baseDao.queryByPrimaryKey(TestVo.class, id);
        assertEquals(version4, testVo41.getVersion());

    }

    /**
     * 测试特殊的个更新接口 updateNotNull
     *
     * @param baseDao
     */
    public static void testUpdate2(IBaseDao baseDao) {

        TestVo testVo = baseDao.queryOneByClause(TestVo.class, "");
        String id = testVo.getId();

        //更新VO中的非空字段
        int i = baseDao.updateNotNull(testVo);


    }

    public static void testNotVersion(IBaseDao baseDao) {

        List<NotVerEntity> ts = baseDao.queryAll(NotVerEntity.class);

        for (NotVerEntity ne : ts) {
            ne.setInt_field(9999);
        }

        baseDao.updateNotVersion(ts.toArray(new NotVerEntity[ts.size()]), new String[] { "int_field" }, true);

    }

    public static void testBatchUpdate(IBaseDao baseDao) {

        List<TestVo> testVos = baseDao.queryAll(TestVo.class);

        for (int i = 0; i < testVos.size(); i++) {
            testVos.get(i).setInt_field(i);
        }

        String var = "!@#$%^&*()&*___<<>>?><,./''''[like %,set t";

        var = StringEscapeUtils.escapeSql(var);
        testVos.get(0).setVarchar_field(var);

        assertEquals(testVos.size(), baseDao.update(testVos));

        testVos.get(0).setVersion(100);
        assertEquals(testVos.size() - 1, baseDao.update(testVos));
    }

    public static void testPreparedStatement(IBaseDao baseDao) throws SQLException {
        JdbcTemplate jdbcTemplate = baseDao.getJdbcTemplate();
        DataSource dataSource = jdbcTemplate.getDataSource();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE ZAP_TEST SET INT_FIELD = ?");

        preparedStatement.setInt(1, -1);
        preparedStatement.addBatch();

        int[] rows = preparedStatement.executeBatch();
        System.out.println(ReflectionToStringBuilder.toString(rows));

        DataSourceUtils.doCloseConnection(connection, dataSource);

    }

    public static void testDeleteAll(IBaseDao baseDao) throws SQLException {
        assertEquals(20, baseDao.deleteAll(TestVo.class));
    }


    /**
     * 测试删除
     *
     * @param baseDao
     */
    public static void testDelete(IBaseDao baseDao) {

        List<TestVo> vosfromdb = baseDao.queryAll(TestVo.class);
        TestVo[] testvoArrays = vosfromdb.toArray(new TestVo[vosfromdb.size()]);

        //删除值为1的行
        String retrieveId = retrieveId(testvoArrays, 1);
        baseDao.deleteByPrimaryKey(TestVo.class, retrieveId);
        assertEquals(null, baseDao.queryByPrimaryKey(TestVo.class, retrieveId));

        //删除值为0的行
        baseDao.deleteByClause(TestVo.class, "INT_FIELD = ?", 0);
        assertEquals(null, baseDao.queryOneByClause(TestVo.class, "ZT.INT_FIELD = ?", 0));

        assertEquals(2, baseDao.deleteByClause(TestVo.class, "INT_FIELD = ? OR INT_FIELD = ?", 2, 3));
        assertEquals(0, baseDao.queryCount(TestVo.class, "ZT.INT_FIELD = ? OR ZT.INT_FIELD = ?", 2, 3));

        assertEquals(16, baseDao.queryCount(TestVo.class));

        //批量删除
        //int expectedMinus = 3;
        //int nextCount = TOTAL_COUNT_20 - expectedMinus;
        //
        //TestVo[] toBeDeleted = (TestVo[]) ArrayUtils.subarray(testvoArrays, 0, expectedMinus);
        //baseDao.delete(toBeDeleted);
        //assertEquals(nextCount - expectedMinus, baseDao.queryCount(TestVo.class));

//		baseDao.delete((TestVo[])ArrayUtils.subarray(tests, 0, expectedMinus));
//		queryCount = baseDao.getJdbcTemplate().queryForObject("SELECT COUNT(1) FROM ZAP_TEST", Integer.class);
//		assertEquals(expected - expectedMinus, queryCount);

    }

    public static void testBatchDelete(IBaseDao baseDao) {

        List<TestVo> vosfromdb = baseDao.queryAll(TestVo.class);

        String[] keys = new String[]{
                vosfromdb.get(0).getId(),
                vosfromdb.get(1).getId(),
                vosfromdb.get(2).getId(),
        };

        //assertEquals(3, baseDao.deleteByPrimaryKey(TestVo.class, keys));

        //vosfromdb.get(0).setVersion(100);

        TestVo[] testVos = new TestVo[]{
                vosfromdb.get(0),
                vosfromdb.get(1),
                vosfromdb.get(2)
        };

        //删除3条记录
        assertEquals(3, baseDao.deleteArray(testVos));
        //删除单条记录
        assertEquals(1, baseDao.delete(vosfromdb.get(4)));

        assertEquals(1, baseDao.deleteNotVersion(vosfromdb.get(5)));

        vosfromdb.get(6).setVersion(100);
        assertEquals(3, baseDao.deleteArrayNotVersion(new TestVo[] {
                vosfromdb.get(6),
                vosfromdb.get(7),
                vosfromdb.get(8)
        }));

    }

    /**
     * 测试百万级数据量分页
     *
     * @param baseDao
     */
    public static void testMillion(IBaseDao baseDao) {

        baseDao.deleteAll(TestVo.class);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            baseDao.insertArray(TestVoFactory.getInstance().create(1000), true);
        }
        long end = System.currentTimeMillis();
        logger.debug("耗时：" + String.valueOf((end - start)));


    }

    /**
     * 测试查询分页
     *
     * @param b
     * @param init20
     */
    public static void testQuerySelect(IBaseDao b, TestVo[] init20) {

        List<TestVo> queryAll = q(b).list();
        TestVo testVo = queryAll.get(0);
        TestVo testVo1 = queryAll.get(1);
        TestVo testVo2 = queryAll.get(2);

        final Map<String, TestVo> testVoMap = queryAll.stream().collect(Collectors.toMap(t -> t.getId(), t -> t));


        //q(b).in("ID", )
    }

    /**
     * 测试Query
     *
     * @param b
     * @param init20
     */
    public static void testQuery(IBaseDao b, TestVo[] init20) {

        q(b).count1();
        if (true) return;

        //
        assertEquals(TOTAL_COUNT_20, q(b).count());

        assertEquals(1, q(b).eq(TestVo.INT_FIELD, 1).count());
        assertEquals(1, q(b).eq(TestVo.INTEGER_FIELD, 1).count());
        assertEquals(0, q(b).eq(TestVo.INT_FIELD, 1).eq(TestVo.INT_FIELD, 2).count());
        assertEquals(0, q(b).eq(TestVo.INT_FIELD, 1).eq(TestVo.INTEGER_FIELD, 2).count());
        try {
            assertEquals(1, q(b).eq(TestVo.LDOUBLE_FIELD, "a").eq(TestVo.INTEGER_FIELD, 1).count());

        } catch (Exception e) {
            // TODO: handle exception
        }

        assertEquals(1, q(b).eq(TestVo.LDOUBLE_FIELD, 1).eq(TestVo.INT_FIELD, 1).count());

        //注意值不相等对ORACLE还有要判断IS NULL
        assertEquals(TOTAL_COUNT_20 - 1, q(b).notEq(TestVo.INTEGER_FIELD, 1).count());

        //顺序排列
        TestVo[] array1 = q(b).asc(TestVo.INT_FIELD).list().toArray(new TestVo[TOTAL_COUNT_20]);
        Arrays.sort(init20, new Comparator<TestVo>() {

            @Override
            public int compare(TestVo o1, TestVo o2) {
                return o1.getInt_field() - o2.getInt_field();
            }
        });
        for (int i = 0; i < init20.length; i++) {
            assertEquals(init20[i].getInt_field(), array1[i].getInt_field());
        }
        //逆序排列
        TestVo[] array2 = q(b).desc(TestVo.INT_FIELD).list().toArray(new TestVo[TOTAL_COUNT_20]);
        Arrays.sort(init20, new Comparator<TestVo>() {

            @Override
            public int compare(TestVo o1, TestVo o2) {
                return o2.getInt_field() - o1.getInt_field();
            }
        });
        for (int i = 0; i < init20.length; i++) {
            assertEquals(init20[i].getInt_field(), array2[i].getInt_field());
        }

        //between
        assertEquals(13, q(b).between(TestVo.VARCHAR_FIELD, "varchar_field_1", "varchar_field_3").count());
        assertEquals(5, q(b).between(TestVo.INT_FIELD, 6, 10).count());
        assertEquals(5, q(b).between(TestVo.LDOUBLE_FIELD, 6, 10).count());

        assertEquals(1, q(b).between(TestVo.LDOUBLE_FIELD, 6, 10)
                .between(TestVo.INT_FIELD, 3, 7)
                .between(TestVo.VARCHAR_FIELD, "varchar_field_4", "varchar_field_6")
                .count());

        //大于
        assertEquals(10, q(b).gt(TestVo.LDOUBLE_FIELD, 9).count());
        assertEquals(5, q(b).gt(TestVo.LDOUBLE_FIELD, 14).count());
        assertEquals(10, q(b).gt(TestVo.INT_FIELD, 9).count());
        assertEquals(5, q(b).gt(TestVo.INT_FIELD, 14).count());
        assertEquals(5, q(b).gt(TestVo.VARCHAR_FIELD, "varchar_field_4").count());
        //TODO 存在问题的
        assertEquals(6, q(b).gt(TestVo.CHAR_FIELD, "char_field_4").count());

        //大于等于
        assertEquals(10, q(b).gtOrEq(TestVo.LDOUBLE_FIELD, 10).count());
        assertEquals(5, q(b).gtOrEq(TestVo.LDOUBLE_FIELD, 15).count());
        assertEquals(10, q(b).gtOrEq(TestVo.INT_FIELD, 10).count());
        assertEquals(5, q(b).gtOrEq(TestVo.INT_FIELD, 15).count());
        assertEquals(6, q(b).gtOrEq(TestVo.VARCHAR_FIELD, "varchar_field_4").count());
        //TODO 存在问题的
        assertEquals(6, q(b).gtOrEq(TestVo.CHAR_FIELD, "char_field_4").count());

        //小于
        assertEquals(10, q(b).ls(TestVo.LDOUBLE_FIELD, 10).count());
        assertEquals(5, q(b).ls(TestVo.LDOUBLE_FIELD, 5).count());
        assertEquals(10, q(b).ls(TestVo.INT_FIELD, 10).count());
        assertEquals(5, q(b).ls(TestVo.INT_FIELD, 5).count());
        assertEquals(1, q(b).ls(TestVo.VARCHAR_FIELD, "varchar_field_1").count());
        //TODO 存在问题的
        assertEquals(16, q(b).ls(TestVo.CHAR_FIELD, "char_field_6").count());

        //小于等于
        assertEquals(10, q(b).lsOrEq(TestVo.LDOUBLE_FIELD, 9).count());
        assertEquals(5, q(b).lsOrEq(TestVo.LDOUBLE_FIELD, 4).count());
        assertEquals(10, q(b).lsOrEq(TestVo.INT_FIELD, 9).count());
        assertEquals(5, q(b).lsOrEq(TestVo.INT_FIELD, 4).count());
        assertEquals(2, q(b).lsOrEq(TestVo.VARCHAR_FIELD, "varchar_field_1").count());
        //TODO 存在问题的
        assertEquals(1, q(b).lsOrEq(TestVo.CHAR_FIELD, "char_field_1").count());

        //IN在 mycode:off
        assertEquals(3, q(b).in(TestVo.INT_FIELD, new Object[]{1, 2, 3}).count());
        assertEquals(3, q(b).in(TestVo.VARCHAR_FIELD, new Object[]{
                TestVo.VARCHAR_FIELD + "_1",
                TestVo.VARCHAR_FIELD + "_2",
                TestVo.VARCHAR_FIELD + "_3",
        }).count());

        assertEquals(0, q(b).in(TestVo.CHAR_FIELD, new Object[]{
                TestVo.CHAR_FIELD + "_1",
                TestVo.CHAR_FIELD + "_2",
                TestVo.CHAR_FIELD + "_3",
        }).count());
        //mycode:on

        //字符类型比较
        //TODO 暂时不知道数字字符比较会存在的问题
        assertEquals(10, q(b).gt(TestVo.LDOUBLE_FIELD, "9").count());
        assertEquals(5, q(b).gt(TestVo.LDOUBLE_FIELD, "14").count());
        assertEquals(10, q(b).gt(TestVo.INT_FIELD, "9").count());
        assertEquals(5, q(b).gt(TestVo.INT_FIELD, "14").count());

        //相近
        assertEquals(TOTAL_COUNT_20, q(b).like(TestVo.VARCHAR_FIELD, TestVo.VARCHAR_FIELD).count());
        assertEquals(0, q(b).leftLike(TestVo.VARCHAR_FIELD, TestVo.VARCHAR_FIELD).count());
        assertEquals(TOTAL_COUNT_20, q(b).rightLike(TestVo.VARCHAR_FIELD, TestVo.VARCHAR_FIELD).count());

    }

    /**
     * 测试临时表,暂时无法做到多字段关联
     *
     * @param b
     * @param init20
     */
    //public static void testTempTable(IBaseDao b, TestVo[] init20) {
    //
    //    ITempTable instance = TempTableFactory.getInstance(b);
    //    IDataLang lang = instance.getLang();
    //
    //    //1.创建临时表
    //    String tempTableName = instance.createTempTable("ZAP_TEMP", new String[]{
    //            "A " + lang.typeText(), "B " + lang.typeInt(), "C " + lang.typeLocalDate()
    //    }, new String[]{});
    //
    //    //2.插入数据
    //    b.getJdbcTemplate().execute("INSERT INTO ZAP_TEMP(A,B,C) VALUES('TEXT', 10, '2016-03-04')");
    //    b.getJdbcTemplate().execute("INSERT INTO ZAP_TEMP(A,B,C) VALUES('TEXT', 12, '2016-03-04')");
    //    Integer count = b.getJdbcTemplate().queryForObject("SELECT COUNT(1) FROM ZAP_TEMP", Integer.class);
    //
    //    assertTrue(count == 2);
    //
    //    //3.进行业务数据关联操作
    //    List<TestVo> testVos = b.queryByClause(TestVo.class, "ZT.INT_FIELD IN (SELECT B FROM ZAP_TEMP)");
    //    assertEquals(2, testVos.size());
    //
    //    //4.删除临时表
    //    instance.dropTempTable(tempTableName);
    //}

    public static void t(IBaseDao b, TestVo[] init20) {

        PaginationSupport<TestVo> page = q(b).asc(TestVo.INT_FIELD).page(0, 5);
        //TODO 测试空值，摸清特性
        b.queryByClause(TestVo.class, " DR = 0");

    }

    private static Query<TestVo> q(IBaseDao baseDao) {
        return new Query<>(baseDao, TestVo.class);
    }


    /**
     * @param vos   获取ID
     * @param value 获取值
     * @return
     */
    private static String retrieveId(TestVo[] vos, int value) {
        for (int i = 0; i < vos.length; i++) {
            if (vos[i].getInt_field() == value) {
                return vos[i].getId();
            }
        }
        return "";
    }

    IBaseDao baseDao;

    public DaoTestCase(IBaseDao baseDao) {
        super();
        this.baseDao = baseDao;
    }

}
