package org.zap.framework.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JdbcTable {

	/**
	 * 数据库表名
	 */
	String value();
	
	/**
	 * 表别名
	 */
	String alias() default "";
	/**
	 * 视图
	 */
	boolean view() default false;
}
