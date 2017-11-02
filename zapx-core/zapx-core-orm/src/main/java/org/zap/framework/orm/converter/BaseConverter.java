package org.zap.framework.orm.converter;

import java.lang.reflect.Field;


public abstract class BaseConverter {
	
	public abstract Object convertValue(Field field, int type, Object object);

}
