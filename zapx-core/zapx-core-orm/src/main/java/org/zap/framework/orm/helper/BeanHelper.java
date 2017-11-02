package org.zap.framework.orm.helper;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.orm.base.FieldUpdated;
import org.zap.framework.orm.compiler.BeanProperty;
import org.zap.framework.orm.compiler.PreCompiler;
import org.zap.framework.orm.creator.ColumnFilter;
import org.zap.framework.orm.exception.DaoException;
import org.zap.framework.orm.itf.ITree;
import org.zap.framework.util.Utils;

import java.lang.reflect.Field;
import java.util.*;

public class BeanHelper {

    static Logger logger = LoggerFactory.getLogger(BeanHelper.class);
    static PreCompiler compiler = PreCompiler.getInstance();

    /**
     * 设置主键的值
     *
     * @param entity
     * @param value
     */
    public static void setId(Object entity, Object value) {

        Field[] primaryFields = compiler.getBeanProperty(entity.getClass()).getPrimaryFields();

        if (primaryFields != null && primaryFields.length > 0) {
            try {
                for (int i = 0; i < primaryFields.length; i++) {
                    primaryFields[i].set(entity, value);
                }
            } catch (IllegalAccessException e) {
                throw new DaoException("Fail to set id for Class: [" + entity.getClass().getName() + "]", e);
            }
        }

    }

    /**
     * 获取主键值
     *
     * @param entity
     * @return
     */
    public static Object getId(Object entity) {
        Field[] primaryFields = compiler.getBeanProperty(entity.getClass()).getPrimaryFields();
        try {
            return primaryFields[0].get(entity);
        } catch (IllegalAccessException e) {
            throw new DaoException("Fail to get id from Class: [" + entity.getClass().getName() + "]", e);
        }
    }

    /**
     * 增加版本号
     *
     * @param entity
     * @param value
     */
    public static void increaseVersion(Object entity, int value) {

        BeanProperty beanProperty = compiler.getBeanProperty(entity.getClass());
        if (beanProperty.isVersionControl()) {

            try {
                Integer o = (Integer) beanProperty.getVersionField().get(entity);
                beanProperty.getVersionField().set(entity, o + value);
            } catch (IllegalAccessException e) {
                throw new DaoException("Fail to increate version for Class: [" + entity.getClass().getName() + "]");
            }
        }
    }


    public static Map<String, Object> toMap(Object pojo, String[] notNullCol) {

        Map<String, Object> result = new HashMap<String, Object>();
        BeanProperty beanProperty = PreCompiler.getInstance().getBeanProperty(pojo.getClass());

        Field[] fields = beanProperty.getFields();
        Set<String> array2Set = Utils.array2Set(notNullCol);

        try {
            for (int i = 0; i < fields.length; i++) {
                if (array2Set.contains(fields[i].getName())) {
                    result.put(fields[i].getName(), fields[i].get(pojo));
                }
            }
        } catch (Exception ex) {
            logger.error("", ex);
        }

        return result;
    }

    /**
     * 实现ITree接口后构建树形结构
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> buildTree(List<T> query) {

        List<T> rootlist = new ArrayList<T>();

        Map<String, ITree<T>> parents = new HashMap<String, ITree<T>>();

        for (T t : query) {
            ITree<T> entity = (ITree<T>) t;
            //缓存节点
            parents.put(entity.getId(), entity);
        }

        for (T t : query) {
            ITree<T> entity = (ITree<T>) t;
            //检查父节点
            if (StringUtils.isNotBlank(entity.getPid()) && parents.containsKey(entity.getPid())) {
                parents.get(entity.getPid()).getChildren().add(t);
            } else {
                //检查父节点
                rootlist.add(t);
            }
        }

        return rootlist;
    }

    /**
     * @return
     */
    public static List<FieldUpdated> compareFieldUpdated(Object newEntity, Object oldEntity, ColumnFilter filter) {

        List<FieldUpdated> result = new ArrayList<>();

        if (newEntity.getClass() != oldEntity.getClass()) {
            throw new DaoException("Fail to compare difference Class type : " + newEntity.getClass().getName() + " , " + oldEntity.getClass().getName());
        }

        BeanProperty beanProperty = compiler.getBeanProperty(oldEntity.getClass());

        String[] columns = beanProperty.getColumns();

        Map<String, Field> fieldMap = beanProperty.getFieldMap();

        List<String> toCompareField = new ArrayList<>();

        //字段名称
        for (int i = 0; i < columns.length; i++) {
            //判断是否包含属性
            if (filter != null && filter.contain(columns[i])) {
                continue;
            }
            toCompareField.add(columns[i]);
        }

        //比较
        if (toCompareField.size() > 0) {
            for (String fieldName : toCompareField) {
                Field field = fieldMap.get(fieldName);

                FieldUpdated fieldUpdated = null;
                if ((fieldUpdated = compareField(field, newEntity, oldEntity)) != null) {
                    result.add(fieldUpdated);
                }

            }
        }


        return result;
    }

    /**
     * @param field
     * @param newEntity
     * @param oldEntity
     * @return
     */
    private static FieldUpdated compareField(Field field, Object newEntity, Object oldEntity) {


        try {
            Object newValue = field.get(newEntity);

            Object oldValue = field.get(oldEntity);

            Class<?> bpType = field.getType();

            return !ObjectUtils.equals(newValue, oldValue) ? new FieldUpdated(field.getName(), newValue, oldValue, bpType) : null;

        } catch (IllegalAccessException e) {

        }

        return null;

    }
}
