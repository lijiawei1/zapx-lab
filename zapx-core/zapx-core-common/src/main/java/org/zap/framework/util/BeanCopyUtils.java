package org.zap.framework.util;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ContextClassLoaderLocal;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * Created by Shin on 2017/4/24.
 */
public class BeanCopyUtils extends BeanUtilsBean {

    /**
     * 复制orig中非空值到dest
     *
     * 如果dest中某字段有值（不为null），则该字段不复制；也就是dest中该字段没值时，才进行复制，适合于对dest进行补充值的情况
     *
     * @param dest
     * @param orig
     */
    public static void copyWhenDestNotNull(Object dest, Object orig) {
        try {
            CopyWhenNullBeanUtilsBean.getInstance().copyProperties(dest, orig);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制orig中的非空值到dest的非空值
     *
     * 如果orig中某字段没值（为null），则该字段不复制，也就是不要把null复制到dest当中
     *
     * @param dest
     * @param orig
     */
    public static void copyWhenOrigNotNull(Object dest, Object orig) {
        try {
            CopyFromNotNullBeanUtilsBean.getInstance().copyProperties(dest, orig);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void copyProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        if (value == null) {
            return;
        }
        super.copyProperty(bean, name, value);
    }

    static private class CopyWhenNullBeanUtilsBean extends BeanUtilsBean {
        private static final ContextClassLoaderLocal
                BEANS_BY_CLASSLOADER = new ContextClassLoaderLocal() {
            // Creates the default instance used when the context classloader is unavailable
            protected Object initialValue() {
                return new CopyWhenNullBeanUtilsBean();
            }
        };

        public static CopyWhenNullBeanUtilsBean getInstance() {
            return (CopyWhenNullBeanUtilsBean) BEANS_BY_CLASSLOADER.get();
        }
        @Override
        public void copyProperty(Object bean, String name, Object value)
                throws IllegalAccessException, InvocationTargetException {
            try {
                Object destValue = PropertyUtils.getSimpleProperty(bean, name);
                if (destValue == null) {
                    super.copyProperty(bean, name, value);
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

    }

    static private class CopyFromNotNullBeanUtilsBean extends BeanUtilsBean {

        private static final ContextClassLoaderLocal
                BEANS_BY_CLASSLOADER = new ContextClassLoaderLocal() {
            // Creates the default instance used when the context classloader is unavailable
            protected Object initialValue() {
                return new CopyFromNotNullBeanUtilsBean();
            }
        };
        public static CopyFromNotNullBeanUtilsBean getInstance() {
            return (CopyFromNotNullBeanUtilsBean)BEANS_BY_CLASSLOADER.get();
        }

        @Override
        public void copyProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
            if (value == null) {
                return;
            }
            super.copyProperty(bean, name, value);
        }
    }

}
