package org.zap.framework.orm.extractor;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by Shin on 2015/12/23.
 */
public class EnhanceRowMapper implements RowMapper {

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        //忽略大小写的哈希表
        Map<String, Object> mapOfColValues = new LinkedCaseInsensitiveMap<>(columnCount);

        for (int i = 1; i <= columnCount; i++) {
            //字段名称
            String columnName = JdbcUtils.lookupColumnName(rsmd, i);
            //字段值
            Object columnValue = JdbcUtils.getResultSetValue(rs, i);
            //字段类型
            int columnType = rsmd.getColumnType(i);


            mapOfColValues.put(columnName, columnValue);
        }
        return mapOfColValues;
    }
}
