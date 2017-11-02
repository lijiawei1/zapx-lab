package org.zap.framework.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Shin
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JdbcOne {
	
	/**
	 */
	Class joinObject();
	
	String foreignKey();
	
	String column();
	
	String alias();
	
}
