package org.zap.framework.bigdata.init;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.test.BaseTestOracle;

import javax.sql.DataSource;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestBigdataOracle extends BaseTestOracle {

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

    static int SIZE = 100000;

    @Test
    public void init10w() {

        for (int i = 0; i < SIZE; i++) {

        }
    }
}
