package org.zap.framework.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Types;

/**
 * 
 * @author Shin
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JdbcColumn {
	
	/**
	 * 字段名
	 */
	String value() default "";

	/**
	 * 字段可读名
	 * @return
     */
	String name() default "";
	
	/**
	 * 是否主键
	 */
	boolean id() default false;
	
	/**
	 * 是否版本号
	 */
	boolean version() default false;

	/**
	 * <p>
	 *     指定数据库字段类型，不指定，使用系统默认值
	 *     如LocalDateTime LocalDate LocalTime 系列数据库默认使用字符串保存
	 * </p>
	 * <pre>
	 *     数据库      表达式          常量值   系统类型
	 *     INTEGER    Types.INTEGER   4       Integer int Long long
	 *     VARCHAR2   Types.VARCHAR   12      String LocalDateTime 系列
	 *     DATE    	  Types.DATE   	  91      Date 不推荐，只为兼容其它系统的数据库而使用
	 *     NUMBER     Types.NUMERIC   2       LDouble Double double
	 *     具体类型值请查看jdbc中的类型常量
	 * </pre>
	 * @see Types
	 */
	int type() default -9999;
	
	/**
	 * 逻辑主键
	 */
	String defaultValue() default "NULL";
	
	
}
