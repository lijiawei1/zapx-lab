package org.zap.framework.test;

import com.eaio.uuid.UUID;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.test.dao.config.BaseTest;
import org.zap.framework.test.dao.testcase.EntityCase;
import org.zap.framework.test.pojo.TestVo;
import org.zap.framework.util.ZipUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class BaseTestOracle extends BaseTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    protected IBaseDao baseDao;

    protected EntityCase entityCase;

    //@Test
    public void contextLoads() {
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM TMS_BAS_CLIENT");
        List<Map<String, Object>> maps1 = baseDao.queryForEnhanceMapList("SELECT * FROM TMS_BAS_CLIENT");
        Assert.assertEquals(maps.size(), maps1.size());
        System.out.println();
    }

    /**
     * 初始化数据
     */
    //@Override
    protected TestVo[] init(Resource resource) {

        //清空表
        baseDao.getJdbcTemplate().execute("TRUNCATE TABLE ZAP_TEST");
        //读取内容
        TestVo[] readValues;
        readValues = ZipUtils.unzipJsonObjectFromFile(resource, TestVo[].class);
        //插表
        baseDao.insertArray(readValues, true);

        entityCase = new EntityCase(baseDao);

        return readValues;
    }

    //@Test
    public void testJdbcInsert() {

        String id = new UUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Timestamp timestamp = Timestamp.valueOf(now);
        LocalTime localTime = now.toLocalTime();
        LocalDate localDate = now.toLocalDate();
        Timestamp timestamp_date = Timestamp.valueOf(LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), 0, 0));

        System.out.println(LocalDateTime.of(0, 1, 1, localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano()));
        //baseDao.getJdbcTemplate().update("INSERT INTO ZAP_TEST(ID, OLD_DATETIME, OLD_DATE) VALUES(?, ?, ?)", id, timestamp, timestamp_date);

    }
}
