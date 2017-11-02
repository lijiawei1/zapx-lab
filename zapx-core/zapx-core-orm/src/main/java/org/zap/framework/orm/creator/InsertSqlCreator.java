package org.zap.framework.orm.creator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.orm.exception.ExEnum;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.converter.DefaultConverter;
import org.zap.framework.orm.exception.DaoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插入语句生成器
 * 1.单实例
 * 2.支持类作为主键的插入语句缓存
 * 3.支持批量生成插入参数，保证顺序一致
 *
 * @author Shin
 */
public class InsertSqlCreator extends BaseCreator {

    private static Logger logger = LoggerFactory.getLogger(InsertSqlCreator.class);

    private static Map<Class<?>, String> sqlCache = new ConcurrentHashMap<Class<?>, String>();

    protected static InsertSqlCreator instance;

    public static InsertSqlCreator getInstance() {
        if (instance == null) {
            instance = new InsertSqlCreator();
        }
        return instance;
    }

    private InsertSqlCreator() {
    }

    /**
     * 返回插入sql
     */
    public String getSql(Class<?> clazz) {
        if (!sqlCache.containsKey(clazz)) {
            sqlCache.put(clazz, createSql(clazz));
        }
        return sqlCache.get(clazz);
    }

    /**
     * 生成sql
     */
    private String createSql(Class<?> clazz) {

        checkBase(clazz);

        //获取Class的orm信息
        BeanProperty beanProperty = compiler.getBeanProperty(clazz);

        StringBuilder ins = new StringBuilder("INSERT INTO ");
        StringBuilder pas = new StringBuilder("");
        ins.append(beanProperty.getTableName()).append(" (");

        String[] columns = beanProperty.getColumns();

        //主键字段
        String[] primaryKeys = beanProperty.getPrimaryKeys();
        for (int i = 0; i < primaryKeys.length; i++) {
            ins.append(primaryKeys[i]).append(",");
            pas.append("?,");
        }
        //版本字段
        if (beanProperty.isVersionControl()) {
            ins.append(beanProperty.getVersionColumn()).append(",");
            pas.append("?,");
        }
        //其他字段名称
        for (int i = 0; i < columns.length; i++) {
            ins.append(columns[i]).append(",");
            pas.append("?,");
        }
        ins.replace(ins.length() - 1, ins.length(), ")");
        ins.append(" VALUES (").append(pas.replace(pas.length() - 1, pas.length(), ")"));

        return ins.toString();
    }

    /**
     * 创建参数
     */
    public List<Object[]> createParamList(Class<?> clazz, Object[] pojos) {
        List<Object[]> paramList = new ArrayList<Object[]>();

        //从缓存中获取
        BeanProperty beanProperty = compiler.getBeanProperty(clazz);

        Field[] primaryFields = beanProperty.getPrimaryFields();
        Field[] fields = beanProperty.getFields();
        //获取字段类型
        Integer[] types = beanProperty.getTypes();

        //字段默认值
        Object[] defaultValues = beanProperty.getDefaultValues();

//		BaseConverter[] converters = beanProperty.getConverters();

        try {

            int paramLength = fields.length + primaryFields.length + (beanProperty.isVersionControl() ? 1 : 0);

            for (int i = 0; i < pojos.length; i++) {

                Object[] params = new Object[paramLength];

                int k = 0, j = 0;

                for (k = 0; k < primaryFields.length; k++) {
                    params[k] = primaryFields[k].get(pojos[i]);
                }

                /**
                 * 获取版本字段
                 */
                if (beanProperty.isVersionControl()) {
                    params[k++] = beanProperty.getVersionField().get(pojos[i]);
                }

                for (j = k, k = 0; k < fields.length; k++, j++) {
                    params[j] = DefaultConverter.getInstance().convertValue(fields[k], types[k], pojos[i]);
                }
                paramList.add(params);
            }
        } catch (IllegalAccessException ex) {
            throw new DaoException(ExEnum.FAIL_GET_VALUE.toString() + beanProperty.getClazz());
        }
        return paramList;
    }

    public static void main(String[] args) throws IllegalAccessException {
//		InsertSqlCreator creator = InsertSqlCreator.getInstance();
//		logger.debug(creator.createSql(TestVo.class));
//		logger.debug(creator.createSql(User.class));
//		logger.debug(creator.createSql(TestVo.class));
//		logger.debug(creator.createSql(TestVo.class));
//		
//		TestVo[] vos = TestVoFactory.getInstance().create(1);
//		List<Object[]> createParamList = creator.createParamList(TestVo.class, vos);
//		for (Object[] obj : createParamList) {
//			logger.debug(Arrays.toString(obj));
//		}


    }

}
