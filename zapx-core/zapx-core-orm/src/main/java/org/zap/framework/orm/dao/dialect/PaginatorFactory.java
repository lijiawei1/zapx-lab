package org.zap.framework.orm.dao.dialect;

import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.itf.IPaginator;
import org.zap.framework.orm.exception.DaoException;

/**
 * 获取分页器
 * @author Shin
 *
 */
public class PaginatorFactory {

	public static IPaginator getInstance(BaseDao baseDao) {
		switch (baseDao.getDbType()) {
		case MYSQL:
			return new MysqlPaginator(baseDao);
		case ORACLE:
			return new OraclePaginator(baseDao);
			case MICROSOFT:
			return new SqlServerPaginator(baseDao);
		}
		throw new DaoException("No implement of database type: " + baseDao.getDbType());
	}
	
}
