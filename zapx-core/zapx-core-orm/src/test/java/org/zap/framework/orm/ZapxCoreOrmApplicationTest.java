package org.zap.framework.orm;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.zap.framework.orm.dao.IBaseDao;

import java.util.List;
import java.util.Map;

/**
 *
 * 测试用例demo
 *
 * Created by Shin on 2017/11/2.
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class ZapxCoreOrmApplicationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    IBaseDao iBaseDao;

    @Test
    public void contextLoads() {

        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM TMS_BAS_CLIENT");

        List<Map<String, Object>> maps1 = iBaseDao.queryForEnhanceMapList("SELECT * FROM TMS_BAS_CLIENT");

        Assert.assertEquals(maps.size(), maps1.size());
    }

}
