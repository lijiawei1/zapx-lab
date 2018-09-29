package org.zap.framework.test.dao.dbcase;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.test.BaseTestOracle;
import org.zap.framework.test.dao.DaoTestCase;
import org.zap.framework.test.pojo.TestVo;
import org.zap.framework.test.rest.AbstractClientTest;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单个oracle数据源
 *
 * @author Shin
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestOracle extends BaseTestOracle {

    @Configuration
    @SpringBootApplication
    public static class Config {

        @Bean
        public DataSource dataSource(DataSourceProperties dataSourceProperties) {
            return dataSourceProperties.initializeDataSourceBuilder().type(DruidDataSource.class).build();
        }

        @Bean
        public IBaseDao baseDao(DataSource dataSource) {
            return new BaseDao(dataSource);
        }

    }



    @Test
    public void testEntity() {
        init20();
        entityCase.testMulti();

    }


    @Test
    public void rawTest() {
        Object query = baseDao.getJdbcTemplate().query("SELECT * FROM ZAP_TEST WHERE INT_FIELD = 1", new Object[]{},
                new ResultSetExtractor() {

                    @Override
                    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {

                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        for (int i = 1; i <= columnCount; i++) {
                            String columnLabel = metaData.getColumnLabel(i);

                            System.out.println(columnLabel + "\t\t" +
                                    metaData.getColumnType(i) + "\t\t" +
                                    metaData.getPrecision(i) + "\t\t" +
                                    metaData.getColumnClassName(i) + "\t\t" +
                                    metaData.getScale(i)
                            );
                        }

                        List<Map<String, Object>> mapls = new ArrayList<Map<String, Object>>();
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            for (int i = 1; i <= columnCount; i++) {
                                String label = metaData.getColumnLabel(i);
                                Object obj = rs.getObject(label);
                                map.put(label, obj);
                            }
                            mapls.add(map);
                        }
                        return mapls;
                    }

                });

    }

    @Test
    public void testSelect() {

        init20();

        entityCase.testCheck();

        entityCase.testQuery();

        DaoTestCase.testSelect(baseDao);

        DaoTestCase.testPage(baseDao);

        DaoTestCase.testView(baseDao);

        DaoTestCase.testExtractor(baseDao);

    }

    @Test
    public void testField() {
        //测试int/Integer
        DaoTestCase.testInt(baseDao);
        //测试LDouble
        DaoTestCase.testLDouble(baseDao);
        //测试空值
        DaoTestCase.testNULL(baseDao);
        //测试旧时间格式
        init20();
        DaoTestCase.testOldDate(baseDao);
    }

    @Test
    public void testQuery() {
        //初始化20条数据
        TestVo[] init20 = init20();

        //DaoTestCase.t(baseDao, init20);

        DaoTestCase.testQuery(baseDao, init20);


    }

    @Test
    public void testInsert() {

        int all = baseDao.deleteAll(TestVo.class);

        System.out.println(all);

        DaoTestCase.testInsert(baseDao);
    }

    @Test
    public void testUpdate() {

        init20();

        DaoTestCase.testUpdate(baseDao);

    }

    @Test
    public void testUpdate1() {
        init20();

        DaoTestCase.testUpdate1(baseDao);
    }

    @Test(expected = DaoException.class)
    public void testNotVersion() {
        init20();

        DaoTestCase.testNotVersion(baseDao);

    }

    @Test
    public void testDelete() {
        init20();

        DaoTestCase.testDelete(baseDao);
    }


    @Test
    public void testView() {
        init20();
        DaoTestCase.testView(baseDao);
    }

    /**
     * 临时表示事务相关的表
     */
    @Test
    @Transactional
    public void testTempTable() {

        //DaoTestCase.testTempTable(baseDao, null);
    }

    //@Test
    //public void testRelation() {
    //
    //    String corp_id = SysConstants.CORPORATION_ROOT_ID;
    //
    //    String user_id = "802c2c40-0860-11e6-a7a1-74d435cbb17a";
    //    //user_id = "";
    //
    //    ListBuilder<Object> listBuilder = BuildUtils.LIST_BUILDER();
    //    StringBuilder sqlBuilder = new StringBuilder("");
    //
    //    if (StringUtils.isNotEmpty(user_id)) {
    //        sqlBuilder.append("SELECT R.*,NVL2(UR.ROLE_ID,'Y','N') AS CHECKED FROM ZAP_AUTH_ROLE R LEFT JOIN (SELECT USER_ID,ROLE_ID FROM ZAP_AUTH_RE_USER_ROLE WHERE USER_ID = ? ) UR ON R.ID = UR.ROLE_ID");
    //        listBuilder.add(user_id);
    //    } else {
    //        sqlBuilder.append("SELECT R.*,'N' AS CHECKED FROM ZAP_AUTH_ROLE R ");
    //    }
    //
    //    sqlBuilder.append(" WHERE R.DR = 0 AND R.CORP_ID = ?");
    //    listBuilder.add(corp_id);
    //
    //    List<RoleCheck> query = baseDao.query(sqlBuilder.toString(), listBuilder.toObjectArray(), new BeanListExtractor(RoleCheck.class));
    //
    //    query.forEach(q -> System.out.println(ReflectionToStringBuilder.toString(q)));
    //
    //
    //}

    @Test
    public void testBatchDelete() {
        init20();
        DaoTestCase.testBatchDelete(baseDao);
    }

    @Test
    public void testBatchUpdate() throws SQLException {
        init20();
        //OK
        //int[] ints = baseDao.getJdbcTemplate().batchUpdate4Oracle("UPDATE ZAP_TEST SET INTEGER_FIELD = 12 WHERE ID = '5FA4A520-A97D-11E5-91DB-B888E391EB30'");
        //System.out.println(ReflectionToStringBuilder.toString(ints));
        //

        //baseDao.getJdbcTemplate().batchUpdate4Oracle("");
        //
        //final List<Object[]> params = new ArrayList<>();
        //params.add(new Object[]{1, "5FA4A520-A97D-11E5-91DB-B888E391EB30"});
        //
        //int[][] ints = baseDao.getJdbcTemplate().batchUpdate4Oracle("UPDATE ZAP_TEST SET INTEGER_FIELD = ? WHERE ID = ?", params, params.size(), new ParameterizedPreparedStatementSetter<Object[]>() {
        //
        //    @Override
        //    public void setValues(PreparedStatement ps, Object[] argument) throws SQLException {
        //        for (int k = 0; k < argument.length; k++) {
        //            ps.setObject(k + 1, argument[k]);
        //        }
        //    }
        //});
        //
        //System.out.println(ReflectionToStringBuilder.toString(ints));


        //int[] ints1 = baseDao.getJdbcTemplate().batchUpdate4Oracle("UPDATE ZAP_TEST SET INTEGER_FIELD = ? WHERE ID = ?", params);
        //System.out.println(ReflectionToStringBuilder.toString(ints1));

        //int[] ints2 = baseDao.getJdbcTemplate().batchUpdate4Oracle("UPDATE ZAP_TEST SET INTEGER_FIELD = ? WHERE ID = ?", new BatchPreparedStatementSetter() {
        //    @Override
        //    public void setValues(PreparedStatement ps, int i) throws SQLException {
        //
        //        Object[] obj = params.get(i);
        //        for (int k = 0; k < obj.length; k++) {
        //            ps.setObject(k + 1, obj[k]);
        //        }
        //    }
        //
        //    @Override
        //    public int getBatchSize() {
        //        return params.size();
        //    }
        //});
        //System.out.println(ReflectionToStringBuilder.toString(ints2));

        //DaoTestCase.testBatchUpdate(baseDao);
        //DaoTestCase.testPreparedStatement(baseDao);
        DaoTestCase.testDeleteAll(baseDao);

    }

    /**
     * 测试高级特性
     */
    @Test
    public void testEnhance() {

        class C{

        }

        C c = new C();

        //List<TestVo> query = baseDao.getJdbcTemplate().query("SELECT * FROM ZAP_TEST", BeanPropertyRowMapper.newInstance(TestVo.class));

        System.out.println("");


    }


    //@Test
    public void testMillion() {
        DaoTestCase.testMillion(baseDao);
    }

}
