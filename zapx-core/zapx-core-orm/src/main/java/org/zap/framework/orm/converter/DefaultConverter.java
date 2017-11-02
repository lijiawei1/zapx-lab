package org.zap.framework.orm.converter;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.lang.LDouble;
import org.zap.framework.util.DateUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 默认数据类型转换
 *
 * @author Shin
 */
public class DefaultConverter extends BaseConverter {

    Logger logger = LoggerFactory.getLogger(DefaultConverter.class);

    public static String NULL_VALUE = "null";

    /**
     * 转换为插库的值
     *
     * @param bpType 当前数据类型
     * @param value  要转换的数据类型
     * @return
     */
    public Object convertValue(Class<?> bpType, int type, Object value) {

        //默认类型
        if (Boolean.class.equals(bpType) || boolean.class.equals(bpType)) {

            if (value == null)
                return null;

            if (value != null && (Boolean) value) {
                return "Y";
            } else {
                return "N";
            }
        } else if (BigDecimal.class.equals(bpType)) {
            // if (value == null)
            // return BigDecimal.ZERO;

        } else if (String.class.equals(bpType)) {
            if (value == null) {
                return "";
            }
            // 高精度浮点数，默认保留8位小数
        } else if (LDouble.class.equals(bpType)) {
            if (value != null) {
                if (value instanceof LDouble) {
                    return ((LDouble) value).toBigDecimal();
                } else {
                    return (new LDouble(value.toString())).toBigDecimal();
                }
            }
        } else if (LocalDate.class.equals(bpType)) {
            if (value != null) {
                //return DateUtils.FORMATTER_DATE.format((LocalDate) value);
                return getValue((LocalDate) value, type);
            } else {
                //注意Date类型和Datetime类型
                return type == Types.DATE ? null : "";
            }
        } else if (LocalDateTime.class.equals(bpType)) {
            if (value != null) {
                return getValue((LocalDateTime) value, type);
                //return DateUtils.FORMATTER_DATETIME.format((LocalDateTime) value);
            } else {
                return type == Types.DATE ? null : "";
            }
        } else if (LocalTime.class.equals(bpType)) {
            if (value != null) {
                //return DateUtils.FORMATTER_TIME.format((LocalTime) value);
                return getValue((LocalTime) value, type);
            } else {
                return type == Types.DATE ? null : "";
            }
        }

        return value;

    }

    /**
     * 获取插库的值
     *
     * @param value 时间
     * @param type  数据库类型
     * @return
     */
    private Object getValue(LocalDateTime value, int type) {

        switch (type) {
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.TIME:
                return Timestamp.valueOf(value);
            default:
                return DateUtils.FORMATTER_DATETIME.format(value);
        }

    }

    /**
     * 日期转换，时分秒为0
     *
     * @param value
     * @param type
     * @return
     */
    private Object getValue(LocalDate value, int type) {

        switch (type) {
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.TIME:
                return Timestamp.valueOf(LocalDateTime.of(value.getYear(), value.getMonth(), value.getDayOfMonth(), 0, 0));
            default:
                return DateUtils.FORMATTER_DATE.format(value);
        }

    }

    /**
     * 获取时间，年月日为
     *
     * @param value
     * @param type
     * @return
     */
    private Object getValue(LocalTime value, int type) {

        switch (type) {
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.TIME:
                //return Timestamp.valueOf(LocalDateTime.from(value));
                return Timestamp.valueOf(LocalDateTime.of(0, 1, 1, value.getHour(), value.getMinute(), value.getSecond(), value.getNano()));
            default:
                return DateUtils.FORMATTER_TIME.format(value);
        }

    }

    /**
     * 日期转换，时分秒为0
     *
     * @param value
     * @param type
     * @return
     */
    private String getSqlValue(LocalDate value, int type) {
        switch (type) {
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.TIME:
                return String.format("to_date('%s', '%s')", DateUtils.FORMATTER_DATE.format(value), DateUtils.PATTERN_ORACLE_DATE);
            default:
                return String.format("'%s'", DateUtils.FORMATTER_DATE.format(value));
        }

    }

    /**
     * 获取时间，年月日为
     *
     * @param value
     * @param type
     * @return
     */
    private String getSqlValue(LocalTime value, int type) {
        switch (type) {
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.TIME:
                return String.format("to_date('%s', '%s')", DateUtils.FORMATTER_TIME.format(value), DateUtils.PATTERN_ORACLE_TIME);
            default:
                return String.format("'%s'", DateUtils.FORMATTER_TIME.format(value));
        }
    }

    /**
     * @param value
     * @param type
     * @return
     */
    private String getSqlValue(LocalDateTime value, int type) {
        switch (type) {
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.TIME:
                return String.format("to_date('%s', '%s')", DateUtils.FORMATTER_DATETIME.format(value), DateUtils.PATTERN_ORACLE_DATETIME);
            default:
                return String.format("'%s'", DateUtils.FORMATTER_DATETIME.format(value));
        }
    }

    /**
     * @param value
     * @return
     */
    private String getSqlValue(Object value) {
        return value == null ? NULL_VALUE : String.format((value instanceof String ? "'%s'" : "%s"), StringEscapeUtils.escapeSql(value.toString()));
    }


    /**
     * 转换值
     *
     * @param field  字段
     * @param type   字段数据库类型
     * @param object 存放字段的实体对象
     * @return
     */
    @Override
    public Object convertValue(Field field, int type, Object object) {
        Object value = null;
        try {
            value = field.get(object);
        } catch (Exception e) {
            logger.error("Convert value fail to get field value", e);
        }
        return convertValue(field.getType(), type, value);
    }

    /**
     * 返回字段
     *
     * @param field  字段
     * @param type   字段数据库类型
     * @param object 存放字段的实体对象
     * @return
     */
    public String convertSqlValue(Field field, int type, Object object) {

        Object value = null;
        try {
            value = field.get(object);
        } catch (Exception e) {
            logger.error("Convert value fail to get field value", e);
        }

        return convertSqlValue(field.getType(), type, value);
    }

    private String convertSqlValue(Class<?> bpType, int type, Object value) {

        //默认类型
        if (Boolean.class.equals(bpType) || boolean.class.equals(bpType)) {
            if (value == null)
                return NULL_VALUE;
            if (value != null && (Boolean) value) {
                return "'Y'";
            } else {
                return "'N'";
            }
        } else if (BigDecimal.class.equals(bpType)) {
            return getSqlValue(value);
        } else if (String.class.equals(bpType)) {
            return getSqlValue(value);
        } else if (LDouble.class.equals(bpType)) {
            // 高精度浮点数，默认保留8位小数
            if (value != null) {
                if (value instanceof LDouble) {
                    return value.toString();
                } else {
                    return (new LDouble(value.toString())).toString();
                }
            }
        } else if (LocalDate.class.equals(bpType)) {
            return value == null ? NULL_VALUE : getSqlValue((LocalDate) value, type);
        } else if (LocalDateTime.class.equals(bpType)) {
            return value == null ? NULL_VALUE : getSqlValue((LocalDateTime) value, type);
        } else if (LocalTime.class.equals(bpType)) {
            return value == null ? NULL_VALUE : getSqlValue((LocalTime) value, type);
        }

        return String.valueOf(value);
    }

    private static DefaultConverter instance;

    public static DefaultConverter getInstance() {
        if (instance == null) {
            instance = new DefaultConverter();
        }
        return instance;
    }


}
