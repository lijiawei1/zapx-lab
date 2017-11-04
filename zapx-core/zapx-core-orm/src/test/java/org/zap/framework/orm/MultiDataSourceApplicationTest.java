package org.zap.framework.orm;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.dao.impl.BaseDao;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by Administrator on 2017/11/2 0002.
 */
@ActiveProfiles("multi-datasource")
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "application-multi-datasource.yaml", classes = MultiDataSourceApplicationTest.Config.class)
public class MultiDataSourceApplicationTest {

    @Autowired
    @Qualifier("baseDao")
    IBaseDao iBaseDao;

    @Autowired
    @Qualifier("secondBaseDao")
    IBaseDao secondBaseDao;

    //@Autowired
    //@Qualifier("secondDatasource")
    //DataSource secondDatasource;

    @Configuration
    @SpringBootApplication
    public static class Config {

        @Bean()
        @Primary
        @ConfigurationProperties("spring.datasource")
        public DataSourceProperties dataSourceProperties() {
            return new DataSourceProperties();
        }

        @Bean
        @Primary
        @ConfigurationProperties(prefix = "spring.datasource")
        public DataSource dataSource() {
            return dataSourceProperties().initializeDataSourceBuilder().type(DruidDataSource.class).build();
        }

        @Bean
        @ConfigurationProperties("spring.second")
        public DataSourceProperties secondProperties() {
            return new DataSourceProperties();
        }

        @Bean(name = "secondDatasource")
        @ConfigurationProperties(prefix = "spring.second")
        public DataSource secondDataSource() {
            return secondProperties().initializeDataSourceBuilder().type(DruidDataSource.class).build();
        }

        @Bean(name = "secondBaseDao")
        public IBaseDao secondBaseDao(@Qualifier("secondDatasource") DataSource dataSource) {
            return new BaseDao(dataSource);
        }
    }

    @Test
    public void contextLoads() throws SQLException {

        //Connection connection = secondDatasource.getConnection();
        Assert.assertEquals(secondBaseDao.queryForMapList("select id from USER").size(), 3);

        System.out.println(iBaseDao.queryCount("SELECT PK_ID FROM TMS_BAS_CLIENT"));
    }

}
