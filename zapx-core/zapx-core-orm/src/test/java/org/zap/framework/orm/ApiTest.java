package org.zap.framework.orm;

import org.junit.Test;
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
}
