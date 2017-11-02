package org.zap.framework.orm.dao.dialect.lang;

import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.itf.IDataLang;

/**
 * DML的定义
 * Created by Shin on 2016/4/21.
 */
public class DataLangFactory {

    public static IDataLang getInstance(BaseDao baseDao) {
        switch (baseDao.getDbType()) {
            case MYSQL:
                //return new MysqlPaginator(baseDao);
                return null;
            case ORACLE:
                return new OracleDataLang();
            case MICROSOFT:
                //return new SqlServerPaginator(baseDao);
                return null;
        }
        throw new DaoException("No implement of database type: " + baseDao.getDbType());
    }
}
