package org.zap.framework.orm.base;

import org.apache.commons.beanutils.BeanUtils;
import org.zap.framework.orm.annotation.JdbcColumn;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Shin
 *
 */
public class BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8869195575120413720L;

	@JdbcColumn(id=true)
	private String id;
	
	@JdbcColumn(version=true)
	private int version;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	public Object clone() {
		try {
			return BeanUtils.cloneBean(this);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
