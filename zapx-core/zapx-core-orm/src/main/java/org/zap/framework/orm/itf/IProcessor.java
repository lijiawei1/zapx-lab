package org.zap.framework.orm.itf;

public interface IProcessor<T> {
	public void process(T entity);
}
