package org.zap.framework.util;

import org.apache.commons.lang.StringUtils;
import org.zap.framework.lang.LDouble;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Created by Shin on 2017/5/11.
 */
public class ConvertUtils {

    /**
     * 数据库值转换到实体
     *
     * @param type
     * @param value
     * @return
     */
    public static Object convertValueToEntity(Class<?> type, String value) {
        Object returnValue = null;

        if (value == null) {
            //空值返回null || 0

            if (type != null) {

                //转换基础类型
                if (type.equals(int.class) || type.equals(double.class) || type.equals(float.class)) {
                    return 0;
                } else if (type.equals(long.class)) {
                    return 0L;
                } else if (type.equals(boolean.class)) {
                    return false;
                }
            }
            return null;
        } else if (value.getClass().equals(type)) {
            return value;
        }

        if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            if ("0".equalsIgnoreCase(value)
                    || "N".equalsIgnoreCase(value)
                    || "No".equalsIgnoreCase(value)
                    || "否".equalsIgnoreCase(value)
                    || "false".equalsIgnoreCase(value)
                    ) {
                // 0, N, No表示false
                return false;
            } else if ("1".equalsIgnoreCase(value)
                    || "Y".equalsIgnoreCase(value)
                    || "Yes".equalsIgnoreCase(value)
                    || "是".equalsIgnoreCase(value)
                    || "true".equalsIgnoreCase(value)
                    ) {
                // 非0...表示true
                return true;
            } else {
                return false;
            }
        }
        if (Long.class.equals(type) || long.class.equals(type)) {
            try {
                Double d = Double.valueOf(value);
                return d.longValue();
            } catch (NumberFormatException e) {
            }
        }
        if (Integer.class.equals(type) || int.class.equals(type)) {
            try {
                Integer d = Integer.valueOf(String.valueOf(value));
                return d.intValue();
            } catch (NumberFormatException e) {
            }
        }
        if (Double.class.equals(type) || double.class.equals(type)) {
            try {
                Double d = Double.valueOf(value);
                return d.doubleValue();
            } catch (NumberFormatException e) {
            }
        }
        if (LDouble.class.equals(type)) {
            try {
                return new LDouble(value);
            } catch (NumberFormatException e) {
            }
        }
        if (LocalDateTime.class.equals(type)) {
            return (value == null || StringUtils.isBlank(value.toString())) ?
                    null : DateUtils.parseDateTime(value.toString());
        }
        if (LocalDate.class.equals(type)) {
            return (value == null || StringUtils.isBlank(value.toString())) ?
                    null : DateUtils.parseDate(value.toString());
        }
        if (LocalTime.class.equals(type)) {
            return (value == null || StringUtils.isBlank(value.toString())) ?
                    null : DateUtils.parseTime(value.toString());
        }

        return returnValue;
    }
}
