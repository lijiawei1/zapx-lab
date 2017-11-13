package org.zap.framework.orm.dao.dialect;

import org.apache.commons.lang.ArrayUtils;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.extractor.EnhanceMapListExtractor;
import org.zap.framework.orm.itf.IPaginator;
import org.zap.framework.orm.creator.SelectSqlCreator;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.common.entity.pagination.PaginationSupport;

import java.util.ArrayList;
import java.util.Map;

public class MysqlPaginator implements IPaginator {

	BaseDao baseDao;
	
	public MysqlPaginator(BaseDao baseDao) {
		this.baseDao = baseDao;
	}
	
	@Override
	public <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, Object[] params, int currentPage, int pageSize) {
		
		PaginationSupport<T> ps = new PaginationSupport<T>();
		int totalCount = baseDao.queryCount(clazz, clause, params);
		ps.setTotalCount(totalCount);
		ps.setPageSize(pageSize == 0 ? 10 : pageSize);
		ps.setPageCount((int)(ps.getTotalCount() / ps.getPageSize()) + 
				(ps.getTotalCount() % ps.getPageSize() == 0 ? 0 : 1)	);
		
		ps.setCurrentPage(currentPage > (ps.getPageCount() - 1) ? 0 : currentPage);
		ps.count();
		if (ps.getStart() > totalCount) {
			//请求当前业没有数据
			ps.setData(new ArrayList<T>());
		} else {
			
			ps.setData(baseDao.getJdbcTemplate().query(
					SelectSqlCreator.getInstance().createByClauseSql(clazz, null, clause).append(" LIMIT ?, ? ").toString(),
				ArrayUtils.addAll(params, new Object[] { ps.getPageSize() * ps.getCurrentPage(), ps.getPageSize() }),
				new BeanListExtractor<T>(clazz, baseDao.getLobHandler())));
		}
		
		return ps;
	}

	@Override
	public PaginationSupport queryPage(String sql, int currentPage, int pageSize, Object... params) {
		PaginationSupport ps = new PaginationSupport();
		int totalCount = baseDao.queryCount(sql, params);
		ps.setTotalCount(totalCount);
		ps.setPageSize(pageSize == 0 ? 10 : pageSize);
		ps.setPageCount((int) (ps.getTotalCount() / ps.getPageSize()) +
				(ps.getTotalCount() % ps.getPageSize() == 0 ? 0 : 1));

		ps.setCurrentPage(currentPage > (ps.getPageCount() - 1) ? 0 : currentPage);
		ps.count();
		if (ps.getStart() > totalCount) {
			//请求当前业没有数据
			ps.setData(new ArrayList());
		} else {

			ps.setData(baseDao.getJdbcTemplate().query(new StringBuffer(sql).append(" LIMIT ?, ? ").toString(),
					ArrayUtils.addAll(params, new Object[]{ps.getPageSize() * ps.getCurrentPage(), ps.getPageSize()}),
					new EnhanceMapListExtractor(baseDao.getLobHandler())));
		}

		return ps;
	}

	@Override
	public PaginationSupport queryPage(String sql, int currentPage, int pageSize, Map<String, Object> args) {
		return null;
	}

}
