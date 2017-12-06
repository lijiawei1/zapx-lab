package org.zap.framework.common.excel.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Shin on 2017/7/7.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    /**
     * 列名
     *
     * 对应excel中首行的名字
     *
     * @return
     */
    String name() default "";
    /**
     * 是否虚拟列，接收数据后分发
     */
    boolean virtual() default false;
    /**
     * 值处理类
     * @return
     */
    Class<?> processor();

}
