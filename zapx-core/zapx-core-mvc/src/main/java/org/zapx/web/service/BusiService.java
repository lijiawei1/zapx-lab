package org.zapx.web.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zap.framework.common.entity.FilterGroup;
import org.zap.framework.common.entity.FilterRule;
import org.zap.framework.common.entity.FilterTranslator;
import org.zap.framework.common.entity.LigerGridPager;
import org.zap.framework.common.json.CustomObjectMapper;
import org.zap.framework.exception.BusinessException;
import org.zap.framework.orm.annotation.JdbcTable;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.page.PaginationSupport;
import org.zap.framework.util.SqlUtils;

import java.io.IOException;
import java.util.List;

/**
 * 业务相关接口
 *
 * @author Shin
 */
@Service("busiService")
@Transactional
public class BusiService extends BaseService {

	@Autowired
	protected IBaseDao baseDao;

	@Autowired
	CustomObjectMapper customObjectMapper;

	/**
	 * @param clazz
	 * @param where
	 * @param sortname
	 * @param sortorder
	 * @param dr
	 * @param <T>
	 * @return
	 */
	public <T> List<T> list(Class<T> clazz, String where, String sortname, String sortorder, boolean dr) {

		String orderPart = StringUtils.isNotBlank(sortname) ? String.format(" ORDER BY %s %s ", sortname, sortorder) : "";
		JdbcTable annotation = clazz.getAnnotation(JdbcTable.class);
		if (annotation == null)
			throw new BusinessException("实体没有JdbcTable注解");

		String alias = annotation.alias();

		if (StringUtils.isNotBlank(where)) {
			try {
				FilterGroup group = customObjectMapper.readValue(where, FilterGroup.class);

				//翻译where条件
				FilterTranslator whereTranslator = new FilterTranslator(group, alias, baseDao.getDbTypeString());
				//翻译的sql和参数
				String commandText = whereTranslator.getCommandText();
				Object[] parmsArray = whereTranslator.getParmsArray();

				return baseDao.queryByClause(clazz, commandText + orderPart, parmsArray);
			} catch (IOException e) {
				throw new BusinessException("WHERE条件解析出错", e);
			}
		}
		return baseDao.queryByClause(clazz, (dr ? alias + ".DR = 0 " : "") + orderPart);
	}

	/**
	 * 分页查询接口
	 *
	 * @param clazz
	 * @param req
	 * @param where       外部JSON过滤条件
	 * @param dr
	 * @param filterRules 服务端过滤条件
	 * @param <T>
	 * @return
	 */
	public <T> PaginationSupport<T> page(Class<T> clazz, LigerGridPager<?> req, String where, boolean dr, FilterRule[] filterRules) {

		//增强多字段排序
		String orderPart = SqlUtils.getSortPart(req.getSortname(), req.getSortorder());

		//分页参数
		int currentPage = req.getPage() - 1;
		int pageSize = req.getPagesize();

		//获取表别名
		JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
		String alias = annTable.alias();

		FilterGroup group = new FilterGroup();

		if (StringUtils.isNotBlank(where)) {
			try {
				group = customObjectMapper.readValue(where, FilterGroup.class);
			} catch (IOException e) {
			}
		}

		//DR条件
		if (dr) {
			group.getRules().add(new FilterRule("DR", 0));
		}

		//添加额外的条件
		if (filterRules != null && filterRules.length > 0) {
			for (int i = 0; i < filterRules.length; i++) {
				group.getRules().add(filterRules[i]);
			}
		}

		if (group.getRules() != null && group.getRules().size() > 0) {

			//翻译where条件
			FilterTranslator whereTranslator = new FilterTranslator(group, alias, baseDao.getDbTypeString());
			//翻译的sql和参数
			String commandText = whereTranslator.getCommandText();
			Object[] parmsArray = whereTranslator.getParmsArray();
			//数据库分页查询
			return baseDao.queryPage(clazz, commandText + orderPart, parmsArray, currentPage, pageSize);
		}

		return baseDao.queryPage(clazz, orderPart, currentPage, pageSize);
	}


	/**
	 * 分页查询
	 *
	 * @param clazz
	 * @param req
	 * @param where
	 * @param dr
	 * @param <T>
	 * @return
	 */
	public <T> PaginationSupport<T> page(Class<T> clazz, LigerGridPager<?> req, String where, boolean dr) {

		//排序部分
		String orderPart = SqlUtils.getSortPart(req.getSortname(), req.getSortorder());

		//分页参数
		int currentPage = req.getPage() - 1;
		int pageSize = req.getPagesize();
		//获取表别名
		JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
		String alias = annTable.alias();
		//如果where条件不为空
		if (where != null & !"".equals(where)) {
			//反序列化where条件
			FilterGroup group = null;
			try {
				group = customObjectMapper.readValue(where, FilterGroup.class);
			} catch (IOException e) {
				group = new FilterGroup();
			}
			//翻译where条件
			FilterTranslator whereTranslator = new FilterTranslator(group, alias, baseDao.getDbTypeString());
			//翻译的sql和参数
			String commandText = whereTranslator.getCommandText();
			Object[] parmsArray = whereTranslator.getParmsArray();
			//数据库分页查询
			return baseDao.queryPage(clazz, commandText + orderPart, parmsArray, currentPage, pageSize);
		}

		return baseDao.queryPage(clazz, (dr ? alias + ".DR = 0 " : "") + orderPart, currentPage, pageSize);
	}

	public <T> PaginationSupport<T> page(Class<T> clazz, LigerGridPager<?> req, String where) {
		return page(clazz, req, where, true);
	}


	/**
	 *
	 */
	public <T> PaginationSupport<T> page(T entity, int currentPage, int pageSize, String clause) {
		return (PaginationSupport<T>)queryPage(entity.getClass(), clause, currentPage, pageSize);
	}

	public <T> PaginationSupport<T> page(Class<T> clazz, int currentPage, int pageSize, String clause) {
		return baseDao.queryPage(clazz, clause, currentPage, pageSize);
	}

	public <T> PaginationSupport<T> page(Class<T> clazz, int currentPage, int pageSize, String clause, Object[] params) {
		return baseDao.queryPage(clazz, clause, params, currentPage, pageSize);
	}

	public <T> List<T> tree(Class<T> clazz, String clause, Object... params) {
		return baseDao.queryTreeByClause(clazz, clause, params);
	}

	public <T> List<T> tree(Class<T> clazz, String clause) {
		return baseDao.queryTreeByClause(clazz, clause);
	}

	/**
	 */
	public <T> List<T> list(Class<T> clazz, String clause) {
		return baseDao.queryByClause(clazz, clause);
	}

	/**
	 */
	public <T> List<T> list(Class<T> clazz, String clause, Object[] params) {
		return baseDao.queryByClause(clazz, clause, params);
	}

	public <T> T get(Class<T> clazz, String id) {
		return (T) baseDao.queryByPrimaryKey(clazz, id);
	}


	/**
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(T entity, String id) {
		return (T) baseDao.queryByPrimaryKey(entity.getClass(), id);
	}

}
