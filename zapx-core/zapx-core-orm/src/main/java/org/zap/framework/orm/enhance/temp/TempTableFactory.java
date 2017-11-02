package org.zap.framework.orm.enhance.temp;

import org.zap.framework.orm.dao.dialect.DBType;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.itf.ITempTable;

/**
 * Created by Shin on 2016/4/22.
 */
public class TempTableFactory {

    public static ITempTable getInstance(BaseDao baseDao) {
        switch (baseDao.getDbType()) {
            case DBType.MYSQL:
                //return new MysqlPaginator(baseDao);
                return null;
            case DBType.ORACLE:
                return new OracleTempTable(baseDao);
            case DBType.MICROSOFT:
                //return new SqlServerPaginator(baseDao);
                return null;
        }
        throw new DaoException("No implement of database type: " + baseDao.getDbType());
    }
}
