package org.zap.framework.orm;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.zap.framework.orm.base.BaseEntity;
import org.zap.framework.orm.creator.SelectSqlCreator;
import org.zap.framework.orm.criteria.Query;
import org.zap.framework.test.pojo.TestVo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        System.out.println(mask(null));
        System.out.println(mask(""));
        System.out.println(mask("1"));
        System.out.println(mask("19"));
        System.out.println(mask("199"));
        System.out.println(mask("1998-1"));
        System.out.println(mask("1998-12-01"));
        System.out.println(mask("1998-12-31 "));
        System.out.println(mask("1998-12-31 0"));
        System.out.println(mask("1998-12-31 11"));
        System.out.println(mask("1998-12-31 22:"));
        System.out.println(mask("1998-12-31 22:33:44"));
        System.out.println(mask("1998-12-31 22:33:464"));
        System.out.println(mask("1998-12-31 22:33:4845"));

    }

    /**
     *
     * @param value
     * @return
     */
    private LocalDateTime mask(Object value) {

        if (value == null)
            return null;
        String valueTrim = StringUtils.trimToEmpty(value.toString());
        if (valueTrim.length() == 0) {
            return null;
        }
        if (valueTrim.length() > 0 && valueTrim.length() < 19) {
            String mask = "1900-01-01 00:00:00";
            valueTrim += mask.substring(valueTrim.length());
        }

        if (valueTrim.length() >= 19) {
            valueTrim = valueTrim.substring(0, 19);
        }


        return LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(valueTrim));

    }

}
