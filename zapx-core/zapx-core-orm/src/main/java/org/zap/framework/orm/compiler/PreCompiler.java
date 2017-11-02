package org.zap.framework.orm.compiler;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.orm.annotation.JdbcColumn;
import org.zap.framework.orm.annotation.JdbcOne;
import org.zap.framework.orm.annotation.JdbcTable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预编译器
 * @author Shin
 *
 */
public class PreCompiler {


	private static Logger logger = LoggerFactory.getLogger(PreCompiler.class);
	/**
	 * 缓存对象
	 */
	private static Map<Class<?>, BeanProperty> annotationCache = new HashMap<Class<?>, BeanProperty>();
	/**
	 * 关联属性对象
	 */
	private static Map<Class<?>, JoinProperty> joinCache = new HashMap<Class<?>, JoinProperty>();
	
	/**
	 * 单例
	 */
	private static PreCompiler instance;
	
	private PreCompiler() {}
	
	/**
	 * 实例
	 */
	public static PreCompiler getInstance() {
		if (instance == null) {
			instance = new PreCompiler();
		}
		return instance;
	}

	/**
	 * 获取结果集属性
	 */
	public TableProperty getTableProperty(ResultSet rs) {
		return new TableProperty(rs);
	}
	
	/**
	 * 获取关联属性，只在查询使用
	 */
	public JoinProperty getJoinProperty(Class<?> clz) {
		JoinProperty jp = joinCache.get(clz);
		if (jp == null) {
			
			BeanProperty bp = getBeanProperty(clz);
			// 字段
			List<Field> declaredList = new ArrayList<Field>();
			Class<?> currClazz = clz;
			
			while (!currClazz.equals(Object.class)) {
				Field[] declaredFields = currClazz.getDeclaredFields();
				currClazz = currClazz.getSuperclass();
				if (declaredFields != null && declaredFields.length > 0) {
					for (int i = 0; i < declaredFields.length; i++) {
						declaredList.add(declaredFields[i]);
					}
				}
			}
			Field[] classFields = declaredList.toArray(new Field[declaredList.size()]);
			
			Map<String, List<JdbcOne>> checkMap = new HashMap<String, List<JdbcOne>>();
			Map<String, List<JdbcOne>> combineMap = new HashMap<String, List<JdbcOne>>();
			Map<JdbcOne, Field> one2FieldMap = new HashMap<JdbcOne, Field>();
			
			Map<String, String> fieldAlias = new HashMap<String, String>();
			
			for (int i = 0; i < classFields.length; i++) {
				
				//字段名称
				String colName = StringUtils.upperCase(classFields[i].getName());			
				
				JdbcOne annColumn = classFields[i].getAnnotation(JdbcOne.class);
				if (annColumn != null) {
					
					one2FieldMap.put(annColumn, classFields[i]);
					//外键+表别名判断
					String checkKey = annColumn.foreignKey() + annColumn.alias();
					if (checkMap.containsKey(checkKey)) {
						checkMap.get(checkKey).add(annColumn);
					} else {
						List<JdbcOne> list = new ArrayList<JdbcOne>();
						list.add(annColumn);
						checkMap.put(checkKey, list);
					}
					
					//用外键字段+表名作为key
					String key = annColumn.foreignKey() + annColumn.joinObject().getName() + annColumn.alias();

					if (combineMap.containsKey(key)) {
						combineMap.get(key).add(annColumn);
					} else {
						List<JdbcOne> list = new ArrayList<JdbcOne>();
						list.add(annColumn);
						combineMap.put(key, list);
					}
				}
			}
			
			if (combineMap.size() > 0) {
				
				//校验
				jp = new JoinProperty(clz);
				
				List<Field> fieldList = new ArrayList<Field>();
				List<String> columnList = new ArrayList<String>();
				List<String> sqlList = new ArrayList<String>();
				
				List<JoinProperty.MainProp> mainPropList = new ArrayList<JoinProperty.MainProp>();
				
				String[] keys = combineMap.keySet().toArray(new String[combineMap.size()]);
				for (int i = 0; i < keys.length; i++) {
					
					List<JdbcOne> list = combineMap.get(keys[i]);

					String fk = list.get(0).foreignKey();
					String alias = list.get(0).alias();
					
					BeanProperty fp = getBeanProperty(list.get(0).joinObject());
					
					//left join lit_corp ac on ac.id = au.corp_id
					StringBuffer leftJoinSql = new StringBuffer(" LEFT JOIN ");
					leftJoinSql.append(fp.getTableName()).append(" ").append(alias)
						.append(" ON ").append(alias).append(".").append(fp.getPrimaryKeys()[0]).append("=");
					
					if (fk != null && fk.indexOf(".") != -1) {
						leftJoinSql.append(fk);
					} else {
						leftJoinSql.append(bp.getTableAlias()).append(".").append(fk);
					}

					sqlList.add(leftJoinSql.toString());
					
					JoinProperty.MainProp mainProp = new JoinProperty.MainProp(fp.getPrimaryKeys()[0], fk, fp.getTableName(), alias);
					mainPropList.add(mainProp);
					
					for (JdbcOne one : list) {
						//AC.NAME AS CORP_NAME
						Field field = one2FieldMap.get(one);
						field.setAccessible(true);
						fieldList.add(field);
						
						StringBuffer col = new StringBuffer();
						col.append(alias).append(".").append(one.column()).append(" AS ").append(StringUtils.upperCase(field.getName()));
						columnList.add(col.toString());
						fieldAlias.put(StringUtils.upperCase(field.getName()), alias);
						
						mainProp.getColList().add(new JoinProperty.ColProp(field, one.column(), field.getName(), alias));
							
					}
				}
				
				jp.setMainPropList(mainPropList);
				//CORP_NAME = OC1.NAME  参照别名  参照字段  外键字段
				jp.setFieldAlias(fieldAlias);
				//field 外键字段
				jp.setJoinFields(fieldList.toArray(new Field[fieldList.size()]));
				//OC1.NAME AS CORP_NAME 查询字段，
				jp.setJoinColumns(columnList.toArray(new String[columnList.size()]));
				//LEFT JOIN LIT_CORP AC ON AC.ID = AU.CORP_ID 拼接的语句， LEFT JOIN ON + 从表名/从表别名 + 从表别名/从表主键 + 主表别名/从表外键字段
				jp.setJoinSqls(sqlList.toArray(new String[sqlList.size()]));
				
			}
			
			joinCache.put(clz, jp);
		}
		return jp;
	}
	
	/**
	 * 获取bean属性
	 */
	public BeanProperty getBeanProperty(Class<?> clz) {
		
		BeanProperty bp = annotationCache.get(clz);
		if (bp == null) {
			// 没有缓存，去获取
			bp = new BeanProperty(clz);
			// 字段
			List<Field> fieldList = new ArrayList<>();
			Class<?> currClazz = clz;
			
			while (!currClazz.equals(Object.class)) {
			
				Field[] declaredFields = currClazz.getDeclaredFields();
				currClazz = currClazz.getSuperclass();
				for (int i = 0; i < declaredFields.length; i++) {
					if (Modifier.isStatic(declaredFields[i].getModifiers())) {
						continue;
					}
					declaredFields[i].setAccessible(true);
					
					if (declaredFields[i].getAnnotations() != null) {
						bp.getFieldAnnotations().put(declaredFields[i], declaredFields[i].getAnnotations());
					}
					fieldList.add(declaredFields[i]);
				}
			}
			
			bp.setFields(fieldList.toArray(new Field[fieldList.size()]));
			
			// 读取Annotation
			Annotation[] ans = clz.getAnnotations();
			
			if (ans != null && ans.length > 0) {
				for (Annotation an : ans) {
					bp.getClassAnnotations().add(an);
				}
				parse(bp);
			}
			
			// 编译Annotation，放到缓存中
			annotationCache.put(clz, bp);
		} 
		return bp;
	}

	/**
	 * 解析缓存属性
	 */
	private void parse(BeanProperty bp) {
		
		Class<?> clazz = bp.getClazz();
		String tableName = "";
		String tableAlias = "";
		boolean view = false;
		//解析表
		JdbcTable annTable = clazz.getAnnotation(JdbcTable.class);
		if (annTable != null && StringUtils.isNotBlank(annTable.value())) {
			//TODO 将表名前缀写到配置文件
			tableName = StringUtils.upperCase(annTable.value());
		} else {
			tableName = StringUtils.upperCase(clazz.getSimpleName());
		}
		
		if (annTable != null && StringUtils.isNotBlank(annTable.alias())) {
			tableAlias = StringUtils.upperCase(annTable.alias());
		} else {
			tableAlias = tableName.substring(tableName.indexOf("_"));
		}

		if (annTable != null) {
			view = annTable.view();
		}
		
		//解析字段
		List<Field> keyFieldList = new ArrayList<Field>();
		List<Field> colFieldList = new ArrayList<Field>();
		
		List<String> keyList = new ArrayList<String>();
		List<String> colList = new ArrayList<String>();
		List<Integer> typeList = new ArrayList<Integer>();
		
		Map<String, Field> fieldMap = new HashMap<String, Field>();
		
		Field[] classFields = bp.getFields();
		Object[] defaultValues = new Object[classFields.length];
		
		for (int i = 0; i < classFields.length; i++) {
			
			//字段名称
			String colName = StringUtils.upperCase(classFields[i].getName());			
			
			JdbcColumn annColumn = classFields[i].getAnnotation(JdbcColumn.class);
			if (annColumn != null) {
				if (StringUtils.isNotBlank(annColumn.value())) {
					colName = StringUtils.upperCase(annColumn.value());
				}
				//默认值
				defaultValues[i] = annColumn.defaultValue();
				//转换器
				
				if (annColumn.id()) {
					//主键
					keyList.add(colName);
					keyFieldList.add(classFields[i]);
				} else if (annColumn.version()) {
					//版本
					bp.setVersionColumn(colName);
					bp.setVersionField(classFields[i]);
					bp.setVersionControl(true);
				} else {
					//普通字段
					colList.add(colName);
					colFieldList.add(classFields[i]);
					typeList.add(annColumn.type());
				}
				
				fieldMap.put(colName, classFields[i]);
			}
		}
		
		//数据库字段
		bp.setFieldMap(fieldMap);
		
		//默认值
		bp.setDefaultValues(defaultValues);
		
		bp.setTableName(tableName);
		bp.setTableAlias(tableAlias);
		bp.setView(view);
		
		bp.setColumns(colList.toArray(new String[colList.size()]));
		bp.setFields(colFieldList.toArray(new Field[colFieldList.size()]));
		bp.setTypes(typeList.toArray(new Integer[typeList.size()]));

		bp.setPrimaryKeys(keyList.toArray(new String[keyList.size()]));
		bp.setPrimaryFields(keyFieldList.toArray(new Field[keyFieldList.size()]));
		
	}

}
