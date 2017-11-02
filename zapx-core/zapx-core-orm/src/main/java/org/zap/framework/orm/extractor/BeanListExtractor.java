package org.zap.framework.orm.extractor;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.lob.LobHandler;
import org.zap.framework.orm.compiler.JoinProperty;
import org.zap.framework.orm.compiler.TableProperty;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.compiler.PreCompiler;

import java.lang.annotation.Annotation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 持久化对象结果集转换
 */
public class BeanListExtractor<T> implements Extractor<List<T>> {

	private Class<T> pojoType;
	
	protected LobHandler lobHandler;

	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	public BeanListExtractor(Class<T> cls) {
		this.pojoType = cls;
	}
	
	/**
	 * @param cls	class 
	 */
	public BeanListExtractor(Class<T> cls, LobHandler lobHandler) {
		this.pojoType = cls;
		this.lobHandler = lobHandler;
	}

	public List<T> extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		
		List<T> ls = new ArrayList<T>();
		
		BeanRowProcessor instance = BeanRowProcessor.getInstance();
		
		PreCompiler compiler = PreCompiler.getInstance();
		BeanProperty beanProperty = compiler.getBeanProperty(pojoType);
		TableProperty tableProperty = compiler.getTableProperty(rs);
		List<Annotation> classAnnotations = beanProperty.getClassAnnotations();
		if (classAnnotations == null || classAnnotations.size() == 0) {
			//普通无注解VO
			while (rs.next()) {
				ls.add(instance.toBean(rs, beanProperty, tableProperty, pojoType, lobHandler));
			}
		} else {
			JoinProperty joinProperty = compiler.getJoinProperty(pojoType);
			//持久化注解vo
			while (rs.next()) {
				ls.add(instance.toBean(rs, pojoType, beanProperty, tableProperty, joinProperty, lobHandler));
			}
		}
		return ls;
	}
}
