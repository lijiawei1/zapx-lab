package org.zap.framework.orm.itf;

import org.zap.framework.orm.creator.Criteria;

public interface ICriteria<T> {
	public Criteria<T> execute(Criteria<T> criteria, T entity);
}
