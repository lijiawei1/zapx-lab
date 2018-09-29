package org.zap.framework.orm.creator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.orm.annotation.JdbcTable;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.compiler.PreCompiler;
import org.zap.framework.orm.converter.DefaultConverter;
import org.zap.framework.orm.exception.DaoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Shin
 */
public class UpdateSqlCreator {

    private static Logger logger = LoggerFactory.getLogger(UpdateSqlCreator.class);

//	private static Map<Class<?>, String> sqlCache = new ConcurrentHashMap<Class<?>, String>();

    protected PreCompiler compiler = PreCompiler.getInstance();

    protected static UpdateSqlCreator instance;

    public static UpdateSqlCreator getInstance() {
        if (instance == null) {
            instance = new UpdateSqlCreator();
        }
        return instance;
    }

    private UpdateSqlCreator() {
    }

    /**
     * 创建条件更新的SQL，简化批量操作
     * 不支持带版本号
     *
     * @param clazz
     * @param filter
     * @return
     */
    public StringBuilder createSqlByClause(Class<?> clazz, ColumnFilter filter, String clause) {
        //检查是否标识注解
        JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
        if (annTable == null) {
            throw new DaoException("类型没有注解");
        }

        //获取Class的orm信息
        BeanProperty beanProperty = compiler.getBeanProperty(clazz);
        //主键
        String[] primaryKeys = beanProperty.getPrimaryKeys();
        String[] columns = beanProperty.getColumns();

        StringBuilder upd = new StringBuilder("UPDATE ");
        upd.append(beanProperty.getTableName()).append(" SET ");

        //字段名称
        for (int i = 0; i < columns.length; i++) {
            //判断是否包含属性
            if (filter != null && filter.contain(columns[i])) {
                continue;
            }

            upd.append(columns[i]).append("=?,");
        }

        if (StringUtils.isBlank(clause)) {
            throw new DaoException("条件语句不能为空");
        }

        return upd.append(" WHERE ").append(clause);
    }


    /**
     * 版本更新SQL
     *
     * @param clazz
     * @param filter
     * @param withVersion
     * @return
     */
    public StringBuilder createSql(Class<?> clazz, ColumnFilter filter, boolean withVersion) {


        //检查是否标识注解
        JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
        if (annTable == null) {
            throw new DaoException("实体没有表信息注解@JdbcTable");
        }

        //获取Class的orm信息
        BeanProperty beanProperty = compiler.getBeanProperty(clazz);
        //主键
        String[] primaryKeys = beanProperty.getPrimaryKeys();
        String[] columns = beanProperty.getColumns();

        StringBuilder upd = new StringBuilder("UPDATE ");
        upd.append(beanProperty.getTableName()).append(" SET ");

        //字段名称
        for (int i = 0; i < columns.length; i++) {
            //判断是否包含属性
            if (filter != null && filter.contain(columns[i])) {
                continue;
            }

            upd.append(columns[i]).append("=?,");
        }

        //版本字段更新
        if (beanProperty.isVersionControl()) {
            upd.append(beanProperty.getVersionColumn()).append("=?");
        } else {
            upd.replace(upd.length() - 1, upd.length(), "");
        }

//		upd.replace(upd.length() - 1, upd.length(), " ");
        upd.append(" WHERE ");
        for (int i = 0; i < primaryKeys.length; i++) {
            upd.append(primaryKeys[i]).append("=? AND ");
        }

        //版本更新字段
        if (beanProperty.isVersionControl() && withVersion) {
            upd.append(beanProperty.getVersionColumn()).append(" =?");
        } else {
            upd.replace(upd.length() - 4, upd.length(), " ");
        }

//		logger.debug(upd.toString());

        return upd;
    }

    /**
     * update a set a.a = ? where a.id = ?
     *
     * @param clazz
     * @param pojo
     * @param clause
     * @param filter
     * @return
     */
    public String createSqlByClauseWithParam(Class<? extends Object> clazz, Object pojo, String clause, ColumnFilter filter) {
        //检查是否标识注解
        JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
        if (annTable == null) {
            throw new DaoException("类型没有注解");
        }

        //从缓存中获取
        BeanProperty beanProperty = compiler.getBeanProperty(clazz);

        //主键
        String[] columns = beanProperty.getColumns();

        Field[] fields = beanProperty.getFields();
        Integer[] types = beanProperty.getTypes();

        StringBuilder upd = new StringBuilder("UPDATE ");
        upd.append(beanProperty.getTableName()).append(" SET ");

        //字段名称
        for (int k = 0; k < columns.length; k++) {
            //判断是否包含属性
            if (filter != null && filter.contain(columns[k])) {
                continue;
            }
            //
            String s = DefaultConverter.getInstance().convertSqlValue(fields[k], types[k], pojo);
            upd.append(columns[k]).append("=").append(s).append(",");
        }
        upd.replace(upd.length() - 1, upd.length(), " ");

        upd.append(" WHERE ").append(clause);

        //主键更新条件
        logger.debug("{}", upd.toString());

        return upd.toString();

    }

    /**
     * 暂时只提供单引号的转义
     *
     * @param clazz
     * @param pojos
     * @param filter
     * @param withVersion
     * @return
     */
    public String[] createSqlWithParam(Class<? extends Object> clazz, Object[] pojos, ColumnFilter filter, boolean withVersion) {

        //检查是否标识注解
        JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
        if (annTable == null) {
            throw new DaoException("类型没有注解");
        }

        String[] sqls = new String[pojos.length];

        //从缓存中获取
        BeanProperty beanProperty = compiler.getBeanProperty(clazz);

        //主键
        String[] columns = beanProperty.getColumns();

        Field[] primaryFields = beanProperty.getPrimaryFields();
        String[] primaryKeys = beanProperty.getPrimaryKeys();
        Field[] fields = beanProperty.getFields();
        Integer[] types = beanProperty.getTypes();

        try {
            for (int i = 0; i < pojos.length; i++) {

                StringBuilder upd = new StringBuilder("UPDATE ");
                upd.append(beanProperty.getTableName()).append(" SET ");

                //字段名称
                for (int k = 0; k < columns.length; k++) {
                    //判断是否包含属性
                    if (filter != null && filter.contain(columns[k])) {
                        continue;
                    }
                    //
                    String s = DefaultConverter.getInstance().convertSqlValue(fields[k], types[k], pojos[i]);
                    upd.append(columns[k]).append("=").append(s).append(",");
                }

                //版本字段更新
                if (beanProperty.isVersionControl()) {
                    int versionPlus = (withVersion ? 1 : 0) + (Integer) beanProperty.getVersionField().get(pojos[i]);
                    upd.append(beanProperty.getVersionColumn()).append("=").append(versionPlus);
                } else {
                    upd.replace(upd.length() - 1, upd.length(), " ");
                }

                upd.append(" WHERE ");

                //主键更新条件
                for (int k = 0; k < primaryKeys.length; k++) {
                    Object o = primaryFields[k].get(pojos[i]);
                    upd.append(primaryKeys[k]).append("= '").append(StringEscapeUtils.escapeSql(o == null ? "null" : o.toString())).append("' AND ");
                }

                //版本更新条件
                if (beanProperty.isVersionControl() && withVersion) {
                    Object version = beanProperty.getVersionField().get(pojos[i]);
                    upd.append(beanProperty.getVersionColumn()).append(" = ").append(version);
                } else {
                    upd.replace(upd.length() - 4, upd.length(), " ");
                }

                sqls[i] = upd.toString();

                logger.debug("{}", sqls[i]);
            }
        } catch (IllegalAccessException e) {
            logger.error("", e);
        }

        return sqls;
    }


    /**
     * 更新非空
     */
    public String[] notNullCols(Object pojo) {
        //从缓存中获取
        BeanProperty beanProperty = compiler.getBeanProperty(pojo.getClass());
        Field[] fields = beanProperty.getFields();

        List<String> columnlist = new ArrayList<String>();

        try {
            for (int i = 0; i < fields.length; i++) {
                Object object = fields[i].get(pojo);
                if (object != null) {
                    columnlist.add(fields[i].getName());
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return columnlist.toArray(new String[columnlist.size()]);
    }

    /**
     * 创建参数
     */
    public List<Object[]> createParamList(Class<? extends Object> clazz, Object[] pojos, ColumnFilter filter, boolean withVersion) {
        List<Object[]> paramList = new ArrayList<Object[]>();

        //从缓存中获取
        BeanProperty beanProperty = compiler.getBeanProperty(clazz);
        //主键
        String[] columns = beanProperty.getColumns();

        Field[] primaryFields = beanProperty.getPrimaryFields();
        Field[] fields = beanProperty.getFields();
        Integer[] types = beanProperty.getTypes();

        //字段默认值
        Object[] defaultValues = beanProperty.getDefaultValues();

        if (filter != null) {
            filter.validate(columns);
        }

        //计算参数长度
        //int length = filter == null ? (fields.length + primaryFields.length) : (filter.length() + primaryFields.length);
        //length += withVersion ? 2 : 1;

        int length = countParamLength(filter, fields.length, primaryFields.length, beanProperty.isVersionControl(), withVersion);
        int versionLength = beanProperty.isVersionControl() ? 0 : 1;

        try {

            for (int i = 0; i < pojos.length; i++) {
                Object[] params = new Object[length];

                int k = 0, index = 0;
                for (k = 0; k < fields.length; k++) {
                    if (filter != null && filter.contain(columns[k]))
                        continue;
                    params[index] = DefaultConverter.getInstance().convertValue(fields[k], types[k], pojos[i]);
                    if (params[index] == null) {

                        Object object = defaultValues[k + primaryFields.length + versionLength];
                        params[index] = "NULL".equals(object) || null == object || "".equals(object) ? null : object;        //保证为空
                    }
                    index++;
                }

                //是否更新版本字段
                if (beanProperty.isVersionControl()) {
                    //版本更新字段，是否加1
                    params[index++] = (withVersion ? 1 : 0) + (Integer) beanProperty.getVersionField().get(pojos[i]);
                }

                //where id = ? and version = ?
                //主键更新条件
                for (k = 0; k < primaryFields.length; k++, index++) {
                    params[index] = primaryFields[k].get(pojos[i]);
                }
                //版本更新条件
                if (beanProperty.isVersionControl() && withVersion) {
                    params[index] = beanProperty.getVersionField().get(pojos[i]);
                }

                paramList.add(params);
            }
        } catch (Exception ex) {
            logger.error("", ex);
        }
        return paramList;
    }

    /**
     * 计算参数长度
     * <p>
     * 基础长度 字段长度、主键长度
     * 无版本控制 0 （无）
     * 版本控制 不计版本 1（更新位）
     * 版本控制 版本更新 2（更新位与条件位）
     *
     * @param fieldLength
     * @param primaryLength
     * @param versionControl
     * @param withVersion    @return
     */
    public int countParamLength(ColumnFilter filter, int fieldLength, int primaryLength, boolean versionControl, boolean withVersion) {

        int length = 0;
        if (filter == null) {
            length = fieldLength + primaryLength;
        } else {
            length = (filter.isInclude() ? (filter.length() + primaryLength) : (filter.length() + primaryLength + fieldLength));
        }

        return length + (versionControl ? (withVersion ? 2 : 1) : 0);
    }

}
