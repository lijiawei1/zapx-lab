package org.zap.framework.test.dao;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.zap.framework.dao.service.BaseService;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.dao.impl.BaseDao;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Created by Shin on 2017/11/8.
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class ZapCoreMvcApplicationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    IBaseDao iBaseDao;

    @Autowired
    BaseService baseService;

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

        @Bean
        public BaseService baseService(IBaseDao baseDao) {
            BaseService baseService = new BaseService();
            baseService.setBaseDao(baseDao);
            return baseService;
        }

    }


    @Test
    public void contextLoads() {

        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM TMS_BAS_CLIENT");

        List<Map<String, Object>> maps1 = baseService.queryForEnhanceMapList("SELECT * FROM TMS_BAS_CLIENT");

        Assert.assertEquals(maps.size(), maps1.size());
    }

}
