package org.zap.framework.orm.extractor;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.lob.LobHandler;
import org.zap.framework.lang.LDouble;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC结果集数据类型增强
 * 1.时间增强为LocalDateTime
 * 2.浮点增强为LDouble
 * Created by Shin on 2015/12/23.
 */
public class EnhanceMapListExtractor implements Extractor<List<Map<String, Object>>> {

    public EnhanceMapListExtractor(LobHandler lobHandler) {
        this.lobHandler = lobHandler;
    }

    protected LobHandler lobHandler;



    public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException,
            DataAccessException {

//		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        ResultSetMetaData rm = rs.getMetaData();
        int ccount = rm.getColumnCount();

        List<Map<String, Object>> mapls = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 1; i <= ccount; i++) {
                String label = rm.getColumnLabel(i);
                int type = rm.getColumnType(i);
                int scale = rm.getScale(i);
                map.put(label.toLowerCase(), getEnhanceValue(rs, i, type, scale));
            }
            mapls.add(map);
        }
        return mapls;

    }

    public void setLobHandler(LobHandler lobHandler) {
        this.lobHandler = lobHandler;
    }

    /**
     * 获取值 某些字段自动增强
     *
     * @param rs    结果集
     * @param index 字段索引
     * @param type  字段类型
     * @param scale
     * @return 值
     * @throws SQLException
     */
    public Object getEnhanceValue(ResultSet rs, int index, int type, int scale) throws SQLException {

        Object value = null;

        switch (type) {

            case Types.BLOB: {
                value = lobHandler.getBlobAsBytes(rs, index);
                break;
            }
            case Types.NCLOB:
            case Types.CLOB: {
                return lobHandler.getClobAsString(rs, index);
            }
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY: {
                value = lobHandler.getBlobAsBytes(rs, index);
                break;
            }
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.TIME: {
                //增强
                Timestamp timestamp = rs.getTimestamp(index);
                if (timestamp != null) {
                    value = timestamp.toLocalDateTime();
                }
                break;
            }
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            case Types.CHAR: {
                //ORACLE 空字符串''会转换成null
                //MYSQL 插入null会转换成''
                value = rs.getString(index);
                break;
            }
            case Types.LONGNVARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR: {
                value = rs.getNString(index);
                break;
            }
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER: {
                value = rs.getInt(index);
                break;
            }
            case Types.BIGINT: {
                value = rs.getLong(index);
                break;
            }
            case Types.DECIMAL:
            case Types.NUMERIC: {
                BigDecimal rsBigDecimal = rs.getBigDecimal(index);
                if (rsBigDecimal != null) {
                    if (scale == 0) {
                        //数据库字段精度为0，可以判断是整形
                        value = rsBigDecimal.intValue();
                    } else {
                        value = new LDouble(rsBigDecimal);
                    }
                }
                break;
            }
            case Types.DOUBLE:
            case Types.FLOAT: {
                value = new LDouble(rs.getDouble(index));
                break;
            }
            default:
                value = rs.getObject(index);
                break;
        }
        return value;
    }
}
