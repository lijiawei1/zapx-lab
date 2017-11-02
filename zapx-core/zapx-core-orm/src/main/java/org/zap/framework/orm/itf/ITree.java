package org.zap.framework.orm.itf;

import java.util.List;

/**
 * 实现查询树形结构的接口
 * @author Shin
 *
 * @param <T>
 */
public interface ITree<T> {
	
	String getId();
	
	String getPid();
	
	List<T> getChildren();

}
