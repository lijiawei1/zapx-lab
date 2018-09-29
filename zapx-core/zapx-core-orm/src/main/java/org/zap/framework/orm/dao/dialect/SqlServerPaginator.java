package org.zap.framework.orm.dao.dialect;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.orm.dao.IBaseDao;
import org.zap.framework.orm.dao.impl.BaseDao;
import org.zap.framework.orm.itf.IPaginator;
import org.zap.framework.orm.creator.SelectSqlCreator;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.extractor.BeanListExtractor;
import org.zap.framework.orm.helper.SqlHelpler;
import org.zap.framework.common.entity.pagination.PaginationSupport;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Shin on 2015/11/20.
 */
public class SqlServerPaginator implements IPaginator {

    IBaseDao baseDao;

    public SqlServerPaginator(IBaseDao baseDao) {
        this.baseDao = baseDao;
    }

    @Override
    public <T> PaginationSupport<T> queryPage(Class<T> clazz, String clause, Object[] params, int currentPage, int pageSize) {

        PaginationSupport<T> ps = new PaginationSupport<T>();
        int totalCount = baseDao.queryCount(clazz, clause, params);
        ps.setTotalCount(totalCount);
        ps.setPageSize(pageSize == 0 ? 10 : pageSize);
        ps.setPageCount((ps.getTotalCount() / ps.getPageSize()) +
                (ps.getTotalCount() % ps.getPageSize() == 0 ? 0 : 1));

        ps.setCurrentPage(currentPage > (ps.getPageCount() - 1) ? 0 : currentPage);
        ps.count();
        if (ps.getStart() > totalCount) {
            //请求当前业没有数据
            ps.setData(new ArrayList<>());
        } else {

            //原始查询的SQL
            String querySql = SelectSqlCreator.getInstance().createByClauseSql(clazz, null, clause).toString();

            //生成分页SQL
            StringBuffer strBuf = new StringBuffer(querySql.length() + 100);
            strBuf.append("select * from(");
            strBuf.append(getRowSql(querySql));
            strBuf.append(") a where row >= ? and row <= ?");

            //if (ps.getStart() > 1) {
            //    strBuf.append("where row >").append(ps.getStart() - 1).append(" and row<=").append(ps.getStart() + ps.getTotalCount() - 1);
            //} else {
            //    strBuf.append("where row<=").append(ps.getStart() + ps.getTotalCount() - 1);
            //}

            logger.debug(strBuf.toString());

            ps.setData(baseDao.getJdbcTemplate().query(strBuf.toString(),
                    ArrayUtils.addAll(params, new Object[]{ ps.getStart(), ps.getEnd() }),
                    new BeanListExtractor<T>(clazz, baseDao.getLobHandler())));
        }

        return ps;
    }

    @Override
    public PaginationSupport queryPage(String sql, int currentPage, int pageSize, Object... params) {
        return null;
    }

    @Override
    public PaginationSupport queryPage(String sql, int currentPage, int pageSize, Map<String, Object> args) {
        return null;
    }

    /**
     * 获取行SQL
     * @param sql
     * @return
     */
    private String getRowSql(String sql) {
        sql = sql.toLowerCase();
        int orderbyindex = getOrderByIndex(sql);
        int fromindex = sql.indexOf(" from ");

        String orderby = null;
        if (orderbyindex >= 0) {
            // 原始查询有order by 的情景
            orderby = sql.substring(orderbyindex, sql.length());
            sql = sql.substring(0, orderbyindex);
        } else {
            // 原始查询没有order by的情景，默认按照第一个字段排序
            String fldStr = StringUtils.substringBetween(sql, "select", "from");
            String[] fldary = StringUtils.split(fldStr, ",");
            int iLen = fldary == null ? 0 : fldary.length;
            if (fldary != null && fldary.length > 0 && fldary[0] == "*") {
                throw new DaoException("SQL解析出错，分页时找不到默认排序字段");
            }
            String result = null;
            for (int i = 0; i < iLen; i++) {
                if (SqlHelpler.isConst(fldary[i]) || SqlHelpler.withAggFunc(fldary[i]) || fldary[i].contains("distinct")
                        || fldary[i].contains("top"))
                    continue;
                else {
                    result = StringUtils.substringBeforeLast(fldary[i], "as").trim();
                    break;
                }
            }
            //V55+ yza 以上条件都不能处理普通的字段列表
            if (result == null || result.length() == 0) {
                if (fldary == null || fldary.length == 0) {
                    throw new DaoException("SQL解析出错，分页时找不到字段");
                }
                result = StringUtils.substringBeforeLast(fldary[0], "as").trim();
            }
            orderby = "order by " + result.trim();
        }
        String frontSql = sql.substring(0, fromindex);
        String endSql = sql.substring(fromindex, sql.length());
        StringBuffer buffer = new StringBuffer();
        String row = "row=row_number() over(" + orderby + ")";
        buffer.append(frontSql);
        buffer.append(",").append(row).append(" ");
        buffer.append(endSql);
        return buffer.toString();
    }

    private int getOrderByIndex(String querySql) {
        int index = querySql.toLowerCase().indexOf("order by");
        return index;
    }

    private static Logger logger = LoggerFactory.getLogger(SqlServerPaginator.class);
}
