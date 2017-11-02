package org.zap.framework.orm.enhance.temp;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.orm.dao.dialect.lang.DataLangFactory;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.itf.IDataLang;
import org.zap.framework.orm.itf.ITempTable;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建临时表
 */
public class OracleTempTable implements ITempTable {

    static Logger logger = LoggerFactory.getLogger(OracleTempTable.class);

    static List<String> existsTable = new ArrayList<>();

    public OracleTempTable(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    private BaseDao baseDao;

    /**
     * 创建临时表
     * @param tableName
     * @param columns
     * @param indexs
     * @return
     */
    public String createTempTable(String tableName, String[] columns, String[] indexs) {

        String sql;
        try {
            //缓存，能减少一次数据库请求
            if (existsTable.contains(tableName)) {
                try {
                    baseDao.getJdbcTemplate().execute("DELETE FROM " + tableName);
                } catch (Exception e) {
                    try {

                        //实物级临时表，提交时删除行数据
                        sql = new StringBuffer("CREATE GLOBAL TEMPORARY TABLE ")
                                .append(tableName)
                                .append("(").append(StringUtils.join(columns, ","))
                                .append(") ON COMMIT DELETE ROWS ")
                                .toString();

                        logger.debug(sql);
                        baseDao.getJdbcTemplate().execute(sql);

                        if (indexs != null && indexs.length > 0) {
                            sql = new StringBuffer("CREATE INDEX IDX_").append(tableName)
                                    .append(" ON ").append(tableName).append("(")
                                    .append(StringUtils.join(indexs, ",")).append(")")
                                    .toString();

                            baseDao.getJdbcTemplate().execute(sql);
                        }
                    } catch (Exception e1) {
                        logger.debug("FAIL CREATING TEMPORARY TABLE", e1);
                    }

                    return tableName;
                }
            }

            //删除存在的临时表
            Integer c = baseDao.getJdbcTemplate().queryForObject("SELECT COUNT(1) FROM USER_TABLES WHERE TEMPORARY = 'Y' AND TABLE_NAME = '" +
                    tableName.trim().toUpperCase() + "'", Integer.class);
            if (c > 0) {
                baseDao.getJdbcTemplate().execute("DELETE FROM " + tableName);
            }

            sql = new StringBuffer("CREATE GLOBAL TEMPORARY TABLE ")
                    .append(tableName)
                    .append("(").append(StringUtils.join(columns, ","))
                    .append(") ON COMMIT DELETE ROWS ")
                    .toString();

            logger.debug(sql);
            baseDao.getJdbcTemplate().execute(sql);

            if (indexs != null && indexs.length > 0) {
                sql = new StringBuffer("CREATE INDEX IDX_").append(tableName)
                        .append(" ON ").append(tableName).append("(")
                        .append(StringUtils.join(indexs, ",")).append(")")
                        .toString();

                baseDao.getJdbcTemplate().execute(sql);
            }
            existsTable.add(tableName);
        } catch (Exception e) {
            logger.debug("FAIL CREATING TEMPORARY TABLE", e);
        }

        return tableName;
    }

    /**
     * 删除临时表
     * @param tableName
     */
    public void dropTempTable(String tableName) {
        String sql = "SELECT TABLE_NAME FROM USER_TABLES WHERE TEMPORARY = 'Y'";
        if (tableName != null && tableName.length() > 0) {
            sql = "SELECT TABLE_NAME FROM USER_TABLES WHERE TEMPORARY = 'Y' AND TABLE_NAME = '" + tableName.trim().toUpperCase() + "'";
        }

        List<String> tableNameList = baseDao.getJdbcTemplate().queryForList(sql, String.class);
        logger.debug(tableNameList.toString());

        for (String name : tableNameList) {
            baseDao.getJdbcTemplate().execute("DROP TABLE " + name);
        }
    }

    /**
     * 构建columns
     *
     * @param columns
     * @param types
     */
    public String[] buildColumns(String[] columns, String[] types) {

        String[] result = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            result[i] = columns[i] + " " + types[i];
        }
        return result;
    }

    /**
     * 获取数据定义
     * @return
     */
    public IDataLang getLang() {
        return DataLangFactory.getInstance(baseDao);
    }


}
