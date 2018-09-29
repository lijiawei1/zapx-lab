package org.zap.framework.test;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.lang.LDouble;
import org.zap.framework.orm.annotation.JdbcColumn;
import org.zap.framework.orm.annotation.JdbcTable;
import org.zap.framework.orm.base.FieldUpdated;
import org.zap.framework.orm.creator.ColumnFilter;
import org.zap.framework.orm.creator.InsertSqlCreator;
import org.zap.framework.orm.creator.SelectSqlCreator;
import org.zap.framework.orm.creator.UpdateSqlCreator;
import org.zap.framework.orm.helper.BeanHelper;
import org.zap.framework.test.entity.MultiIdEntity;
import org.zap.framework.test.pojo.TestVo;
import org.zap.framework.test.pojo.TestVoFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * 工具类测试
 *
 * @author Shin
 */
public class UtilsTest {

    public static Logger logger = LoggerFactory.getLogger(UtilsTest.class);

    @Test
    public void testComparedVersion() {
        TestVo oldVo = new TestVo();
        TestVo newVo = new TestVo();

        newVo.setInt_field(1);
        newVo.setDouble_field(2.0);
        newVo.setLong_field(3L);
        newVo.setLdouble_field(new LDouble(4));

        List<FieldUpdated> fieldUpdateds = BeanHelper.compareFieldUpdated(newVo, oldVo, null);


        System.out.println();



    }


    @Test
    public void testCreateSqlNotVersion() {

        MultiIdEntity entity = new MultiIdEntity();
        entity.setMst_id("mst");
        entity.setCar_id("car");

        SelectSqlCreator creator = SelectSqlCreator.getInstance();
        StringBuilder sql = creator.createSql(MultiIdEntity.class, null);
        System.out.println(sql.toString());

        InsertSqlCreator insert = InsertSqlCreator.getInstance();
        System.out.println(insert.getSql(MultiIdEntity.class));
        List<Object[]> paramList = insert.createParamList(MultiIdEntity.class, new Object[]{entity});
        System.out.println();
    }

    @Test
    public void testUpdateSqlCreator() {

        UpdateSqlCreator creator = UpdateSqlCreator.getInstance();
        //logger.debug(creator.createSql(TestVo.class, new ColumnFilter(null, false), true).toString());

        logger.debug("==常规字段，两个有效字段");
        ColumnFilter twoFields = new ColumnFilter(new String[]{"char_field", "number_field"}, true);
        logger.debug(creator.createSql(TestVo.class, twoFields, true).toString());

        TestVo[] vos = TestVoFactory.getInstance().create(2);
        List<Object[]> createParamList1 = creator.createParamList(TestVo.class, vos, twoFields, true);
        for (Object[] obj : createParamList1) {
            logger.debug(Arrays.toString(obj));
        }


        logger.debug("==常规带无效字段，两个有效字段+两个无效字段");
        ColumnFilter fourFields = new ColumnFilter(new String[]{"char_field", "number_field", "a", "b"}, true);
        logger.debug(creator.createSql(TestVo.class, fourFields, true).toString());

        List<Object[]> createParamList2 = creator.createParamList(TestVo.class, vos, twoFields, true);
        for (Object[] obj : createParamList2) {
            logger.debug(Arrays.toString(obj));
        }

        logger.debug("==两个无效字段");
        ColumnFilter unvalidFields = new ColumnFilter(new String[]{"a", "b"}, true);
        logger.debug(creator.createSql(TestVo.class, unvalidFields, true).toString());

        List<Object[]> createParamList3 = creator.createParamList(TestVo.class, vos, unvalidFields, true);
        for (Object[] obj : createParamList3) {
            logger.debug(Arrays.toString(obj));
        }

        logger.debug("==no version");
        ColumnFilter noversion = new ColumnFilter(new String[]{"a", "b"}, true);
        logger.debug(creator.createSql(TestVo.class, noversion, false).toString());

        List<Object[]> createParamList4 = creator.createParamList(TestVo.class, vos, noversion, false);
        for (Object[] obj : createParamList4) {
            logger.debug(Arrays.toString(obj));
        }

        logger.debug("==by clause");
        logger.debug(creator.createSqlByClauseWithParam(TestVo.class, vos[1], " a = ?", noversion));


//			logger.debug("==");
//			List<Object[]> createParamList0 = creator.createParamList(TestVo.class, vos, new ColumnFilter(new String[] {
//					"char_field", "number_field"
//				}, true), true);
//			for (Object[] obj : createParamList2) {
//				logger.debug(Arrays.toString(obj));
//			}


//			logger.debug("=======================exclude========================");
//			
//			logger.debug(creator.createSql(TestVo.class, new ColumnFilter(new String[] {
//					"char_field", "number_field"
//				}, false) , true).toString());
//			List<Object[]> createParamList5 = creator.createParamList(TestVo.class, vos, new ColumnFilter(new String[] {
//					"char_field", "number_field"
//				}, false) , true);
//			for (Object[] obj : createParamList5) {
//				logger.debug(Arrays.toString(obj));
//			}
//
//			TestVo nullvo = new TestVo();
//			String[] notNullCols = creator.notNullCols(nullvo);
//			logger.debug(Arrays.asList(notNullCols).toString());
//			logger.debug(creator.createSql(TestVo.class, new ColumnFilter(notNullCols, true), true).toString());

    }

    @JdbcTable(value = "A", alias = "A")
    class A {
        @JdbcColumn(id = true)
        String pk_id = "pk_id";

        @JdbcColumn
        String fuck = "fuck";

        @JdbcColumn
        String you = "you";

        public String getPk_id() {
            return pk_id;
        }

        public void setPk_id(String pk_id) {
            this.pk_id = pk_id;
        }

        public String getFuck() {
            return fuck;
        }

        public void setFuck(String fuck) {
            this.fuck = fuck;
        }

        public String getYou() {
            return you;
        }

        public void setYou(String you) {
            this.you = you;
        }
    }

    @JdbcTable(value = "B", alias = "B")
    class B {
        @JdbcColumn(id = true)
        String id = "id";

        @JdbcColumn(version = true)
        int version = 0;

        @JdbcColumn
        String c = "c";

        @JdbcColumn
        String d = "d";

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public String getD() {
            return d;
        }

        public void setD(String d) {
            this.d = d;
        }
    }

    @Test
    public void testUpdateSqlNotVersion() {


        A a = new A();
        B b = new B();
        UpdateSqlCreator creator = UpdateSqlCreator.getInstance();

        List<Object[]> aParamList = creator.createParamList(A.class, new Object[]{a}, null, false);
        logger.debug(creator.createSql(A.class, null, false).toString());
        logger.debug(ReflectionToStringBuilder.toString(aParamList.get(0), ToStringStyle.MULTI_LINE_STYLE));
        Assert.assertEquals(3, aParamList.get(0).length);

        List<Object[]> bParamList = creator.createParamList(B.class, new Object[]{b}, null, true);
        logger.debug(creator.createSql(B.class, null, true).toString());
        logger.debug(ReflectionToStringBuilder.toString(bParamList.get(0), ToStringStyle.MULTI_LINE_STYLE));
        Assert.assertEquals(5, bParamList.get(0).length);

        List<Object[]> bbParamList = creator.createParamList(B.class, new Object[]{b}, null, false);
        logger.debug(creator.createSql(B.class, null, false).toString());
        logger.debug(ReflectionToStringBuilder.toString(bbParamList.get(0), ToStringStyle.MULTI_LINE_STYLE));
        Assert.assertEquals(4, bbParamList.get(0).length);

    }

    @Test
    public void testColumnFilter() {
        ColumnFilter filterInclude = new ColumnFilter(new String[]{"field"}, true);
        ColumnFilter filterExclude = new ColumnFilter(new String[]{"field"}, false);

        assertTrue(!filterInclude.contain("field"));
        assertTrue(filterInclude.contain("fffff"));

        assertTrue(filterExclude.contain("field"));
        assertTrue(!filterExclude.contain("fffff"));

    }



}
