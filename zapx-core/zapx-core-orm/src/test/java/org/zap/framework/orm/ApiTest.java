package org.zap.framework.orm;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.zap.framework.orm.base.BaseEntity;
import org.zap.framework.orm.creator.SelectSqlCreator;
import org.zap.framework.orm.criteria.Query;
import org.zap.framework.test.pojo.TestVo;

public class ApiTest {
    @Test
    public void etst() {
        //Query<TestVo> testVoQuery = new Query<>(null, TestVo.class)
        //        .eq("int_field", "B");
        Query<TestVo> testVoQuery = new Query<>(null, TestVo.class);
        testVoQuery.count1();
    }

    @Test
    public void test () {
        System.out.println(
                SelectSqlCreator.getInstance().createPageSql(TestVo.class, " corp_id = ? ").toString()
        );

        //ArrayUtils.addAll(params, new Object[]{1, 0}
    }
}
