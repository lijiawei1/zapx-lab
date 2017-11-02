package org.zap.framework.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;

public class Utils {
	
	public static <E, T> Map<E, T> bufferArray(T[] array, String keyName, Class<E> clazz) {
		return bufferCollection(Arrays.asList(array), keyName, clazz);
	}
	
	/**
	 * 缓存
	 * 主表主键——子表列表
	 */
	@SuppressWarnings("unchecked")
	public static <E, T>Map<E, List<T>> bufferListMap(Collection<T> list, String keyName, Class<E> clazz) {
		Map<E, List<T>> result = new LinkedHashMap<E, List<T>>();
		
		if (list != null && list.size() > 0) {
			try {
				for (T obj : list) {
					E key = (E)PropertyUtils.getProperty(obj, keyName);
					
					if (!result.containsKey(key)) {
						result.put(key, new LinkedList<T>());
					}
					result.get(key).add(obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <E, T> Map<E, T> bufferCollection(Collection<T> list, String keyName, Class<E> clazz) {
		
		Map<E, T> result = new LinkedHashMap<E, T>();

		if (list != null && list.size() > 0) {
			try {
				for (T obj : list) {
					E key = (E)PropertyUtils.getProperty(obj, keyName);
					result.put(key, obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param array
	 * @param spiltSize
	 * @return
	 */
	@Deprecated
	public static <T> List<T[]> spiltArray(T[] array, int spiltSize) {
		return splitArray(array, spiltSize);
	}
	
	/**
	 * 拆分数组
	 * @param array 待拆分的数组
	 * @param spiltSize 拆分大小
	 * @return
	 */
	public static <T> List<T[]> splitArray(T[] array, int spiltSize) {
		
		List<T[]> resultList = new ArrayList<T[]>();
		
		int size =array.length;
		if (size <= spiltSize || spiltSize <= 0) {
			resultList.add(array);
			return resultList;
		}
		
		int times = 1;
		while (times * spiltSize <= size) {
			
			int startIndex = (times - 1) * spiltSize;
			int endIndex = times * spiltSize;
			T[] toSpilt = Arrays.copyOfRange(array, startIndex, endIndex);
			resultList.add(toSpilt);
			times++;
		}
		
		if ((times - 1) * spiltSize < size) {
			int startIndex = (times - 1) * spiltSize;
			int endIndex = size;
			T[] toSpilt = Arrays.copyOfRange(array, startIndex, endIndex);
			resultList.add(toSpilt);
		}
		return resultList;
	}
	
	public static <T> Set<T> array2Set(T[] arrays) {
		Set<T> result = new HashSet<T>();
		if (arrays != null && arrays.length > 0) {
			for (int i = 0; i < arrays.length; i++) {
				result.add(arrays[i]);
			}
		}
		return result;
	}

	/**
	 */
	public static <T, E> Set<E> array2FieldSet(T[] list, Field field, Class<E> clazz) {
		return collection2FieldSet(Arrays.asList(list), field, clazz);
	}
	
	/**
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> Set<E> collection2FieldSet(Collection<T> list, Field field, Class<E> clazz) {
		Set<E> result = new HashSet<E>();
		
		if (list != null && list.size() > 0) {
			try {
				for (T obj : list) {
					result.add((E)field.get(obj));
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 */
	public static <T, E> Set<E> array2FieldSet(T[] list, String fieldName, Class<E> clazz) {
		return collection2FieldSet(Arrays.asList(list), fieldName, clazz);
	}
	
	/**
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> Set<E> collection2FieldSet(Collection<T> list, String fieldName, Class<E> clazz) {
		Set<E> result = new HashSet<E>();
		if (list != null && list.size() > 0) {
			try {
				for (T obj : list) {
					E key = (E)PropertyUtils.getProperty(obj, fieldName);
					result.add(key);
				}			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	} 
	
	/**
	 */
	public static <T, E> List<E> array2FieldList(T[] list, String fieldName, Class<E> clazz) {
		return collection2FieldList(Arrays.asList(list), fieldName, clazz);
	}
	
	/**
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> Map<E, T> collection2FieldMap(Collection<T> list, String fieldName, Class<E> clazz) {
		Map<E, T> result = new HashMap<E, T>();
		
		if (list != null && list.size() > 0) {
			try {
				for (T obj : list) {
					E key = (E)PropertyUtils.getProperty(obj, fieldName);
					result.put(key, obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> List<E> collection2FieldList(Collection<T> list, String fieldName, Class<E> clazz) {
		List<E> result = new ArrayList<E>();
		
		if (list != null && list.size() > 0) {
			try {
				for (T obj : list) {
					E key = (E)PropertyUtils.getProperty(obj, fieldName);
					result.add(key);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 */
	public static <T, E> List<E> array2FieldList(T[] list, Field field, Class<E> clazz) {
		return collection2FieldList(Arrays.asList(list), field, clazz);
	}
	
	public static <T, E> String collection2String(Collection<T> list, String fieldName, Class<E> clazz) {
		
		StringBuffer buffer = new StringBuffer();
		if (!CollectionUtils.isEmpty(list)) {
			try {
				for (T obj : list) {
					String key = (String)PropertyUtils.getProperty(obj, fieldName);
					buffer.append(",").append(key);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return buffer.length() > 0 ?  buffer.substring(1) : buffer.toString();
	}
	
	/**
	 * 
	 */
	public static <T, E>  String array2String(T[] list, String fieldName, Class<E> clazz) {
		return collection2String(Arrays.asList(list), fieldName, clazz);
	}
	
	/**
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> List<E> collection2FieldList(Collection<T> list, Field field, Class<E> clazz) {
		List<E> result = new ArrayList<E>();
		
		if (list != null && list.size() > 0) {
			try {
				for (T obj : list) {
					field.setAccessible(true);
					result.add((E)field.get(obj));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	
	public static String getString(Object obj) {
		return obj == null ? "" : obj.toString();
	}

}

