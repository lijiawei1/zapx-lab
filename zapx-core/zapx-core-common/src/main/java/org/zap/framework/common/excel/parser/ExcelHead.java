package org.zap.framework.common.excel.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Shin on 2017/7/7.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelHead {

    int headRow() default 1;

    int dataRow() default 2;

    int checkMode() default 1;

    /**
     * 检查所有的字段
     */
    static int CHECK_MODE_ALL = 1;
    /**
     * 有则录入
     */
    static int CHECK_MODE_ANY = 2;



}
