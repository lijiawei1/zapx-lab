package org.zap.framework.orm;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.dao.impl.BaseDao;

import javax.sql.DataSource;
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

    @Configuration
    @SpringBootApplication
    public static class Config {
    }

    @Test
    public void contextLoads() {

        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM TMS_BAS_CLIENT");

        List<Map<String, Object>> maps1 = iBaseDao.queryForEnhanceMapList("SELECT * FROM TMS_BAS_CLIENT");

        Assert.assertEquals(maps.size(), maps1.size());
    }

}
