package org.zap.framework.orm.exception;

public enum ExEnum {

	PARAMS_IS_NULL("参数不能为空", "parameters can't be null"),
	
	ANNOTATION_NOT_FOUND("实体没有注解", "annotation JdbcTable not found"),
	
	FAIL_GET_VALUE("获取属性值失败", "failed to get value"),
	
	MULTIPLE_KEY_FOUND("不支持复合主键", "multiple key not supported"),
	
	CONSISTENCE_DELETED("该单据已经被他人删除，请刷新界面", "deleted"),
	
	CONSISTENCE_MODIFIED("该单据已经被他人修改，请刷新界面", "modified"),

	VIEW_VERSION_NOT_SUPPORT("视图不支持版本查询", "query version not support for view"),

	ENTITY_VERSION_NOT_SUPPORT("实体不支持版本查询", "query version not support for entity"),

	UNSERIALIZABLE("实体或视图未实现序列化接口", "entity or view is not assign from interface Serializable")

	;

	
	private String message;
	
	private String code;
	
	private ExEnum(String message, String code) {
		this.message = message;
		this.code = code;
	}
	
	public String toString() {
		return message;
	}
}
