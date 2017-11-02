package org.zap.framework.orm.creator;

import org.zap.framework.orm.exception.ExEnum;
import org.zap.framework.orm.annotation.JdbcTable;
import org.zap.framework.orm.compiler.PreCompiler;
import org.zap.framework.orm.exception.DaoException;

import java.io.Serializable;

/**
 *
 */
public abstract class BaseCreator {

	protected PreCompiler compiler = PreCompiler.getInstance();

	/**
	 * 检查实体基本情况
	 * @param clazz 实体
	 */
	protected void checkBase(Class<?> clazz) {

		//检查表注解
		JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
		if (annTable == null) {
			throw new DaoException(ExEnum.ANNOTATION_NOT_FOUND.toString());
		}

		//检查序列化
		if (!Serializable.class.isAssignableFrom(clazz)) {
			throw new DaoException(ExEnum.UNSERIALIZABLE.toString());
		}
		
	}
	
}
