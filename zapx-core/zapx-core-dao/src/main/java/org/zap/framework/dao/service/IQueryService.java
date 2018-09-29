package org.zap.framework.dao.service;

import org.zap.framework.common.entity.ListSupport;
import org.zap.framework.common.entity.pagination.PaginationRequest;
import org.zap.framework.common.entity.pagination.PaginationSupport;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 查询接口
 * 注意点：
 * 1.分页起始页为0
 * 2.
 *
 */
public interface IQueryService {
    /**
     *
     * @param request
     * @param where
     * @param alias
     * @param resource
     * @return
     */
    ListSupport queryList(PaginationRequest request, String where, String alias, String resource);

    /**
     *
     * @param request
     * @param where
     * @param alias
     * @param resource
     * @param consumer
     * @return
     */
    ListSupport queryList(PaginationRequest request, String where, String alias, String resource,
                              Consumer<Map<String, Object>> consumer);

    /**
     *
     * @param request
     * @param where
     * @param alias
     * @param resource
     * @return
     */
    PaginationSupport queryPage(PaginationRequest request, String where, String alias, String resource);
}
