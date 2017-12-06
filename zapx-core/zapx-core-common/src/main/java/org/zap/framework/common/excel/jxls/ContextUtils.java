package org.zap.framework.common.excel.jxls;

import org.apache.commons.beanutils.PropertyUtils;
import org.jxls.common.GroupData;
import org.zap.framework.exception.BusinessException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Administrator
 *
 */
public class ContextUtils {

	public static Collection getGridDataByHeaderCollection(Collection collection, Collection gridHeader, String headProperty,Map defaultValueMap){
		List headItems = new ArrayList();
		try {
			if( collection != null){
				//每个分组转为表格样式
				for (Object header : gridHeader) {
					Object headerValue = ((Map)header).get("value");
					Object cells = null;	
					for (Object bean : collection) {
						if( headerValue.equals(getObjectProperty(bean, headProperty, true)) ){
							cells = bean;
							break;
						}						
					}	
					if(cells==null){
						if(defaultValueMap!=null){
							cells = defaultValueMap;
						}else{
							cells = header;
						}
					}
					headItems.add(cells);
				}
			}		
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new BusinessException("excel模板Header设置有误");
		}
		return headItems;
	}
	
	
	
	public static Collection getGridHeaderCollection(Collection collection, String headProperty, String headOrder){
		List<Map<String,Object>> listMap = new ArrayList<Map<String,Object>> ();
		Set headByValues = null;
		if( collection != null ){
			if (headOrder != null) {
				if ("desc".equalsIgnoreCase(headOrder)) {
					headByValues = new TreeSet(Collections.reverseOrder());
				} else {
					headByValues = new TreeSet();
				}
			} else {
				headByValues = new LinkedHashSet();
			}
			for (Object bean : collection) {
				headByValues.add(getObjectProperty(bean, headProperty, true));
			}
			
			int index1 =  0 ;
			for (Object value : headByValues) {
				index1 ++ ;
				Map<String,Object> map = new HashMap<String,Object> ();
				map.put("index", index1);
				map.put("value", value);
				listMap.add(map);
			}
		}
		return listMap;
	}
	
	
	
	
	public static Collection<GroupData> groupCollection(Collection collection, String groupProperty, String groupOrder, List<String> groupTotalPropertys){
		Collection<GroupData> result = new ArrayList<GroupData>();
		try {
			if( collection != null ){
				Set groupByValues;
				if (groupOrder != null) {
					if ("desc".equalsIgnoreCase(groupOrder)) {
						groupByValues = new TreeSet(Collections.reverseOrder());
					} else {
						groupByValues = new TreeSet();
					}
				} else {
					groupByValues = new LinkedHashSet();
				}
				for (Object bean : collection) {
					groupByValues.add(getObjectProperty(bean, groupProperty, true));
				}
				int index1 =  0 ;
				for (Iterator iterator = groupByValues.iterator(); iterator.hasNext();) {
					Object groupValue = iterator.next();
					List items = new ArrayList();
					for (Object bean : collection) {
						Object groupPropertyValue= getObjectProperty(bean, groupProperty, true);	
						if(groupValue.equals(groupPropertyValue)){
							items.add(bean);
						}
					}
					Map groupItem = PropertyUtils.describe(items.get(0)) ; //全部转为MAP来处理
					if(groupItem.isEmpty()||(groupItem.containsKey("empty")&&groupItem.get("empty")==null)){
						groupItem = BeanMapUtils.cloneMap((Map) items.get(0));
					}
					index1++;
					groupItem.put("index", index1);
					
					//分组小计 
					int index2 = 0 ;
					List groupItems = new ArrayList();
					for(int i = 0 ; i< items.size(); i++ ){	
						Map item = PropertyUtils.describe(items.get(i)) ;
						if(item.isEmpty()||(item.containsKey("empty")&&item.get("empty")==null)){
							item = BeanMapUtils.cloneMap((Map) items.get(i));
						}
						index2++;
						item.put("index", index2);					
						if(groupTotalPropertys!=null&&groupTotalPropertys.size()>0){
							for (String groupTotalProperty : groupTotalPropertys) {	
								double total =0.0;
								if(i >=1){
									Object s_total =getObjectProperty(groupItem, groupTotalProperty, true);		//获取分组的字段				
									total = (s_total==null?0.0:Double.valueOf(s_total.toString()));
								}
								Object s_value = getObjectProperty(item, groupTotalProperty, true);//每个分组字段的值				
								double value = (s_value==null?0.0:Double.valueOf(s_value.toString()));

								PropertyUtils.setProperty(groupItem, groupTotalProperty, getPropertyValueOf(PropertyUtils.getPropertyType(groupItem, groupTotalProperty),total+value));	
							}
						}
						groupItems.add(item);
					}
					GroupData groupData = new GroupData(groupItem, groupItems);	
					result.add(groupData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		    throw new BusinessException("excel模板Group设置有误");
		}
		return result;
	}
	
	public static Object getPropertyValueOf(Class type, double value){
		
		//return new LDouble(value);
		return value;
	}
	
	
	
	public static Object getObjectProperty(Object obj, String propertyName, boolean failSilently){
		try {
			return getObjectProperty(obj, propertyName);
		} catch (Exception e) {
			String msg = "failed to get property '" + propertyName + "' of object " + obj;
			System.out.println(msg);
			if( failSilently ){
				return null;
			}else{
				throw new IllegalArgumentException(e);
			}
		}
	}

	public static Object getObjectProperty(Object obj, String propertyName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		
		Object value = PropertyUtils.getProperty(obj, propertyName);
		
		return value;
		
	}

}
