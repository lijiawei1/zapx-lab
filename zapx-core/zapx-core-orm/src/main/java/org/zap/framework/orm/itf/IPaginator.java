package org.zap.framework.orm.itf;

import org.zap.framework.orm.page.PaginationSupport;

import java.util.Map;

/**
 * 分页查询接口
 * @author Shin
 *
 */
public interface IPaginator {

	<T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, Object[] params, int currentPage, int pageSize);

	PaginationSupport queryPage(String sql, int currentPage, int pageSize, Object... params);

	PaginationSupport queryPage(String sql, int currentPage, int pageSize, Map<String, Object> args);

}
