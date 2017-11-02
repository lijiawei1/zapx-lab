package org.zap.framework.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * JavaBean 和 Map 转换
 * 
 * @author haojc
 */
public final class BeanMapUtils {

	/**
	 *  Converts a List<JavaBean> to List<Map<String,Object>>.
	 *  
	 * @param list
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 */
	public static final List<Map<String, Object>> toListMap(List<?> list) throws IntrospectionException,
			IllegalAccessException, InstantiationException, InvocationTargetException {
		List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
		for (Object bean : list) {
			Map<String, Object> map = toMap(bean);
			listMap.add(map);
		}
		return listMap;
	}

	/**
	 * Converts a map to a JavaBean.
	 * 
	 * @param type
	 *            type to convert
	 * @param map
	 *            map to convert
	 * @return JavaBean converted
	 * @throws IntrospectionException
	 *             failed to get class fields
	 * @throws IllegalAccessException
	 *             failed to instant JavaBean
	 * @throws InstantiationException
	 *             failed to instant JavaBean
	 * @throws InvocationTargetException
	 *             failed to call setters
	 */
	public static final Object toBean(Class<?> type, Map<String, ? extends Object> map) throws IntrospectionException,
			IllegalAccessException, InstantiationException, InvocationTargetException {
		BeanInfo beanInfo = Introspector.getBeanInfo(type);
		Object obj = type.newInstance();
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor descriptor = propertyDescriptors[i];
			String propertyName = descriptor.getName();
			if (map.containsKey(propertyName)) {
				Object value = map.get(propertyName);
				Object[] args = new Object[1];
				args[0] = value;
				descriptor.getWriteMethod().invoke(obj, args);
			}
		}
		return obj;
	}

	/**
	 * Converts a JavaBean to a map.
	 * 
	 * @param bean
	 *            JavaBean to convert
	 * @return map converted
	 * @throws IntrospectionException
	 *             failed to get class fields
	 * @throws IllegalAccessException
	 *             failed to instant JavaBean
	 * @throws InvocationTargetException
	 *             failed to call setters
	 */
	public static final Map<String, Object> toMap(Object bean) throws IntrospectionException, IllegalAccessException,
			InvocationTargetException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor descriptor = propertyDescriptors[i];
			String propertyName = descriptor.getName();
			if (!propertyName.equals("class")) {
				Method readMethod = descriptor.getReadMethod();
				Object result = readMethod.invoke(bean, new Object[0]);
				if (result != null) {
					returnMap.put(propertyName, result);
				} else {
					returnMap.put(propertyName, "");
				}
			}
		}
		return returnMap;
	}
	
	public static final Map<String, Object> cloneMap(Map<String, Object> srcMap) throws IntrospectionException, IllegalAccessException,
	InvocationTargetException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
        for(Iterator keyIt = srcMap.keySet().iterator();keyIt.hasNext();){
            String key = (String) keyIt.next();
            returnMap.put(key,srcMap.get(key));
       }
		return returnMap;
}	
	
}
