/**
 *
 */
package org.zap.framework.orm.extractor;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.support.lob.LobHandler;
import org.zap.framework.lang.LDouble;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.compiler.JoinProperty;
import org.zap.framework.orm.compiler.TableProperty;
import org.zap.framework.orm.exception.DaoException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 */
public class BeanRowProcessor {

    private BeanRowProcessor() {
    }

    private static BeanRowProcessor instance;

    public static BeanRowProcessor getInstance() {
        if (instance == null) {
            instance = new BeanRowProcessor();
        }
        return instance;
    }

    /**
     * 面向普通对象
     */
    public <T> T toBean(ResultSet rs, BeanProperty bp, TableProperty tp, Class<T> clz, LobHandler lobHandler) throws SQLException {

        T pojo = newInstance(clz);
        //包括所有的域
        Field[] fields = bp.getFields();

        try {
            for (int i = 0; i < fields.length; i++) {

                String column = StringUtils.upperCase(fields[i].getName());

                if (tp.containLabel(column)) {
                    fields[i].set(pojo,
                            convertType(getValue(rs, column, tp.getType(column), fields[i].getType(),
                                            lobHandler),
                                    tp.getType(column), fields[i].getType()));
                }
            }
        } catch (Exception e) {
            throw new DaoException("Result set to pojo error:" + e.toString());
        }
        return pojo;
    }

    public <T> T toBean(ResultSet rs, Class<T> clz,
                        BeanProperty bp, TableProperty tp, JoinProperty jp,
                        LobHandler lobHandler) throws SQLException {

        String[] primaryKeys = bp.getPrimaryKeys();
        String[] columns = bp.getColumns();

        Field[] primaryFields = bp.getPrimaryFields();
        Field[] fields = bp.getFields();

        T pojo = newInstance(clz);
        try {
            //1主键
            if (!bp.isView()) {
                for (int i = 0; i < primaryFields.length; i++) {
                    if (tp.getIndex(primaryKeys[i]) != -1) {
                        primaryFields[i].set(pojo,
                                getValue(rs, primaryKeys[i], tp.getType(primaryKeys[i]), primaryFields[i].getType(),
                                        lobHandler)
                        );
                    }
                }
            }

            //2业务字段
            for (int i = 0; i < fields.length; i++) {
                if (tp.getIndex(columns[i]) != -1) {

                    fields[i].set(pojo,
                            convertType(getValue(rs, columns[i], tp.getType(columns[i]), fields[i].getType(),
                                            lobHandler),
                                    tp.getType(columns[i]), fields[i].getType())
                    );
                }

            }

            //3版本字段
            if (tp.getIndex(bp.getVersionColumn()) != -1 && bp.isVersionControl()) {
                bp.getVersionField().set(pojo, rs.getInt(bp.getVersionColumn()));
            }

            //4关联字段
            if (jp != null) {
                Field[] joinFields = jp.getJoinFields();
                for (int i = 0; i < joinFields.length; i++) {

                    String column = StringUtils.upperCase(joinFields[i].getName());

                    if (tp.getIndex(column) != -1) {
                        joinFields[i].set(pojo,
                                convertType(getValue(rs, column, tp.getType(column), joinFields[i].getType(),
                                                lobHandler),
                                        tp.getType(column), joinFields[i].getType()));
                    }
                }
            }

        } catch (Exception e) {
            throw new DaoException("Result set to pojo error:", e);
        }

        return pojo;
    }

    /**
     * 转换类型
     *
     * @param value  通用类型的值
     * @param tpType 数据库字段类型
     * @param bpType bean字段类型
     * @return
     */
    private Object convertType(Object value, int tpType, Class<?> bpType) {

        if (value == null) {
            //空值返回null || 0

            if (bpType != null) {

                //转换基础类型
                if (bpType.equals(int.class) || bpType.equals(double.class) || bpType.equals(float.class)) {
                    return 0;
                } else if (bpType.equals(long.class)) {
                    return 0L;
                } else if (bpType.equals(boolean.class)) {
                    return false;
                }
//				} else if (bpType.equals(Integer.class) || bpType.equals(Boolean.class) || bpType.equals(LDouble.class)) {
            }
            return null;
        } else if (value.getClass().equals(bpType)) {
            return value;
        }

        //// 目标类型：boolean
        if (Boolean.class.equals(bpType) || boolean.class.equals(bpType)) {
            switch (tpType) {
                case Types.CHAR:
                case Types.VARCHAR:
                    String str = String.valueOf(value);
                    if ("0".equalsIgnoreCase(str)
                            || "N".equalsIgnoreCase(str)
                            || "No".equalsIgnoreCase(str)
                            ) {
                        // 0, N, No表示false
                        return false;
                    } else {
                        // 非0...表示true
                        return true;
                    }
                default:
                    return value;
            }
        }

        //// 目标类型：long
        if (Long.class.equals(bpType) || long.class.equals(bpType)) {
            switch (tpType) {
                case Types.DECIMAL:
                    if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).longValue();
                    }
                case Types.NUMERIC:
                case Types.BIGINT:
                case Types.INTEGER:
                    if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).longValue();
                    } else {
                        try {
                            Double d = Double.valueOf(String.valueOf(value));
                            return d.longValue();
                        } catch (NumberFormatException e) {
                        }
                    }
                default:
                    return value;
            }
        }

        //// 目标类型：int
        if (Integer.class.equals(bpType) || int.class.equals(bpType)) {
            switch (tpType) {
                case Types.DECIMAL:
                    if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).intValue();
                    }
                case Types.NUMERIC:
                case Types.BIGINT:
                case Types.INTEGER:
                    if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).intValue();
                    } else {
                        try {
                            Integer d = Integer.valueOf(String.valueOf(value));
                            return d.intValue();
                        } catch (NumberFormatException e) {
                        }
                    }
                default:
                    return value;
            }
        }

        // 目标类型：double
        if (Double.class.equals(bpType) || double.class.equals(bpType)) {
            switch (tpType) {
                case Types.DECIMAL:
                    if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).doubleValue();
                    }
                case Types.NUMERIC:
                case Types.BIGINT:
                case Types.INTEGER:
                    if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).doubleValue();
                    } else {
                        try {
                            Double d = Double.valueOf(String.valueOf(value));
                            return d.doubleValue();
                        } catch (NumberFormatException e) {
                        }
                    }
                default:
                    return value;
            }
        }

        // 目标类型：ldouble
        if (LDouble.class.equals(bpType)) {
            switch (tpType) {
                case Types.DECIMAL:
                    if (value instanceof BigDecimal) {
                        return new LDouble((BigDecimal) value);
                    }
                case Types.NUMERIC:
                case Types.BIGINT:
                case Types.INTEGER:
                    if (value instanceof BigDecimal) {
                        return new LDouble((BigDecimal) value);
                    } else {
                        try {
                            return new LDouble(String.valueOf(value));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return LDouble.ZERO_DBL;
                        }
                    }
                default:
                    return value;
            }
        }

        if (LocalDateTime.class.equals(bpType)) {
            switch (tpType) {
                case Types.VARCHAR:
                case Types.CHAR: {

                    if (value == null)
                        return null;
                    String valueTrim = StringUtils.trimToEmpty(value.toString());
                    if (valueTrim.length() == 0) {
                        return null;
                    }
                    if (valueTrim.length() > 0 && valueTrim.length() < 19) {
                        String mask = "1900-01-01 00:00:00";
                        valueTrim += mask.substring(valueTrim.length());
                    }
                    if (valueTrim.length() >= 19) {
                        valueTrim = valueTrim.substring(0, 19);
                    }
                    return LocalDateTime.from(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(valueTrim));
                }
                case Types.TIMESTAMP:
                case Types.DATE: {
                    return (value == null) ? null : value;
                }
                default:
                    return null;
            }
        }
        if (LocalTime.class.equals(bpType)) {
            switch (tpType) {
                case Types.VARCHAR:
                case Types.CHAR: {
                    return (value == null || StringUtils.isBlank(value.toString())) ?
                            null : LocalTime.parse((String) value, DateTimeFormatter.ofPattern("HH:mm:ss"));
                }
                case Types.TIMESTAMP:
                case Types.DATE: {
                    return (value == null) ? null : ((LocalDateTime) value).toLocalTime();
                }
                default:
                    return null;
            }
        }
        if (LocalDate.class.equals(bpType)) {
            switch (tpType) {
                case Types.VARCHAR:
                case Types.CHAR: {
                    return (value == null || StringUtils.isBlank(value.toString())) ?
                            null : LocalDate.parse((String) value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                case Types.TIMESTAMP:
                case Types.DATE: {
                    return (value == null) ? null : ((LocalDateTime) value).toLocalDate();
                }
                default:
                    return null;
            }
        }
        return value;
    }


    /**
     * 获取数据库结果集中字段值，转换为系统常用类型
     *
     * @param rs         查询数据库返回的结果集
     * @param label      字段标签
     * @param tbType     字段的数据库类型
     * @param bpType     字段的系统实体类型
     * @param lobHandler 对象处理
     * @return 系统常用类型表示的值
     * @throws SQLException
     */
    private Object getValue(ResultSet rs, String label,
                            int tbType, Class<?> bpType, LobHandler lobHandler) throws SQLException {

        Object value = null;

        switch (tbType) {
            case Types.BLOB: {

                if (bpType.equals(Blob.class)) {
                    value = rs.getBlob(label);
                } else if (bpType.equals(byte[].class) || bpType.equals(Byte[].class)) {
                    value = lobHandler.getBlobAsBytes(rs, label);
                } else if (bpType.equals(String.class)) {
                    byte[] blobAsBytes = lobHandler.getBlobAsBytes(rs, label);
                    value = new String(blobAsBytes);
                } else if (bpType.equals(Object.class)) {
                    value = getObject(rs.getBlob(label).getBinaryStream());
                }
                break;
            }
            case Types.NCLOB:
            case Types.CLOB: {
                if (bpType.equals(Clob.class)) {
                    value = rs.getClob(label);
                } else if (bpType.equals(String.class)) {
                    value = lobHandler.getClobAsString(rs, label);
                }
                break;
            }
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY: {
//			value = getObject(rs.getBinaryStream(label));

                if (bpType.equals(Blob.class)) {
                    value = rs.getBlob(label);
                } else if (bpType.equals(byte[].class) || bpType.equals(Byte[].class)) {
                    value = lobHandler.getBlobAsBytes(rs, label);
                } else if (bpType.equals(String.class)) {
                    byte[] blobAsBytes = lobHandler.getBlobAsBytes(rs, label);
                    value = new String(blobAsBytes);
                } else if (bpType.equals(Object.class)) {
                    value = getObject(rs.getBlob(label).getBinaryStream());
                }
                break;
            }
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.TIME: {
                //通用
                Timestamp timestamp = rs.getTimestamp(label);
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
                value = rs.getString(label);
                break;
            }
            case Types.LONGNVARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR: {
                value = rs.getNString(label);
                break;
            }
            default:
                value = rs.getObject(label);
                break;
        }
        return value;
    }

    private Object getObject(InputStream in) throws SQLException {
        if (in == null) {
            return null;
        }
        ObjectInputStream objIn = null;
        try {
            objIn = new ObjectInputStream(new BufferedInputStream(in));
            return objIn.readObject();
        } catch (IOException e) {
//		    throw new SQLException("将Blob类型字段里的对象转换为Java POJO出错", e);
            throw new DaoException(e);
        } catch (Exception e) {
            throw new SQLException("将Blob类型字段里的对象转换为Java POJO出错");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new SQLException("将Blob类型字段里的对象转换为Java POJO出错", e);
                }
            }
            if (objIn != null) {
                try {
                    objIn.close();
                } catch (IOException e) {
                    throw new SQLException("将Blob类型字段里的对象转换为Java POJO出错", e);
                }
            }
        }
    }


    protected <T> T newInstance(Class<T> c) throws SQLException {
        try {
            return c.newInstance();

        } catch (InstantiationException e) {
            throw new SQLException("Cannot create " + c.getName(), e);

        } catch (IllegalAccessException e) {
            throw new SQLException("Cannot create " + c.getName(), e);
        }
    }
}
