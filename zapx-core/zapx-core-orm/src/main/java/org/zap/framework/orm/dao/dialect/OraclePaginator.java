package org.zap.framework.orm.dao.dialect;

import com.alibaba.druid.sql.SQLUtils;
import org.apache.commons.lang.ArrayUtils;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.orm.extractor.EnhanceMapListExtractor;
import org.zap.framework.orm.itf.IPaginator;
import org.zap.framework.common.entity.pagination.PaginationSupport;
import org.zap.framework.orm.creator.SelectSqlCreator;

import java.util.ArrayList;
import java.util.Map;

/**
 * 分页等工具类
 * @author Shin
 *
 */
public class OraclePaginator implements IPaginator {
	BaseDao baseDao;
	
	public OraclePaginator(BaseDao baseDao) {
		this.baseDao = baseDao;
	}

	@Override
	public <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, Object[] params, int currentPage, int pageSize) {
		
		PaginationSupport<T> ps = new PaginationSupport<T>();
		int totalCount = baseDao.queryCount(clazz, clause, params);
		ps.setCurrentPage(currentPage);
		ps.setTotalCount(totalCount);
		ps.setPageSize(pageSize == 0 ? 10 : pageSize);
		ps.setPageCount((ps.getTotalCount() / ps.getPageSize()) +
				(ps.getTotalCount() % ps.getPageSize() == 0 ? 0 : 1)	);

		if (currentPage > (ps.getPageCount() - 1)) {
			//页码大于可用页码
			ps.setData(new ArrayList<>());
		} else {
			ps.count();
			//debug分页信息
	//		logger.debug(ReflectionToStringBuilder.toString(ps));

			if (ps.getStart() > totalCount) {
				//请求当前业没有数据
				ps.setData(new ArrayList<>());
			} else {
				ps.setData(baseDao.getJdbcTemplate().query(
						SQLUtils.formatOracle(SelectSqlCreator.getInstance().createPageSql(clazz, clause).toString()),
					ArrayUtils.addAll(params, new Object[] { ps.getEnd(), ps.getStart() }),
					new BeanListExtractor<T>(clazz, baseDao.getLobHandler())));
			}
		}
		return ps;
		
	}

	@Override
	public PaginationSupport queryPage(String sql, int currentPage, int pageSize, Object... params) {

		PaginationSupport<Map<String, Object>> ps = new PaginationSupport<>();

		int totalCount = baseDao.queryCount(sql, params);
		ps.setCurrentPage(currentPage);
		ps.setTotalCount(totalCount);
		ps.setPageSize(pageSize == 0 ? 10 : pageSize);
		ps.setPageCount((ps.getTotalCount() / ps.getPageSize()) +
				(ps.getTotalCount() % ps.getPageSize() == 0 ? 0 : 1));

		if (currentPage > (ps.getPageCount() - 1)) {
			ps.setData(new ArrayList<>());
		} else {
			ps.count();

			if (ps.getStart() > totalCount) {
				ps.setData(new ArrayList<>());
			} else {
				ps.setData(baseDao.getJdbcTemplate().query(SQLUtils.formatOracle(SelectSqlCreator.getInstance().createPageSql(sql).toString()),
						ArrayUtils.addAll(params, new Object[]{ps.getEnd(), ps.getStart()}),
						new EnhanceMapListExtractor(baseDao.getLobHandler())));
			}
		}

		return ps;
	}

	@Override
	public PaginationSupport queryPage(String sql, int currentPage, int pageSize, Map<String, Object> params) {

		PaginationSupport<Map<String, Object>> ps = new PaginationSupport<>();

		int totalCount = baseDao.queryCount(sql, params);
		ps.setCurrentPage(currentPage);
		ps.setTotalCount(totalCount);
		ps.setPageSize(pageSize == 0 ? 10 : pageSize);
		ps.setPageCount((ps.getTotalCount() / ps.getPageSize()) +
				(ps.getTotalCount() % ps.getPageSize() == 0 ? 0 : 1));

		if (currentPage > (ps.getPageCount() - 1)) {
			ps.setData(new ArrayList<>());
		} else {
			ps.count();

			if (ps.getStart() > totalCount) {
				ps.setData(new ArrayList<>());
			} else {

				//保证分页参数名称唯一
				String startPageParmName = "start_page";
				String endPageParmName = "end_page";
				int i = 0;
				while (params.containsKey(startPageParmName + i)) {
					i++;
				}
				startPageParmName += i;
				while (params.containsKey(endPageParmName + i)) {
					i++;
				}
				endPageParmName += i;

				//参数名称唯一
				params.put(startPageParmName, ps.getStart()); //开始页码
				params.put(endPageParmName, ps.getEnd()); //结束页码

				ps.setData(baseDao.getNameTemplate().query(
						SQLUtils.formatOracle(
								SelectSqlCreator.getInstance().createPageNameSql(
										//插入分页参数，指定开始结束参数名称
										sql, startPageParmName, endPageParmName
								).toString()),
						params,
						new EnhanceMapListExtractor(baseDao.getLobHandler())));
			}
		}

		return ps;
	}
}
