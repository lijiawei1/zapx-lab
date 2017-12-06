package org.zap.framework.common.excel.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Shin on 2017/7/7.
 */
public class ExcelProp {

    /**
     * bean class
     */
    private Class<?> clazz;
    /**
     * bean fields
     */
    private Field[] fields;

    private Map<Field, Annotation[]> fieldAnnotations = new LinkedHashMap<>();

    /**
     * 类级注解
     */
    private Annotation[] classAnns;

    public <T> ExcelProp(Class<T> clazz) {

    }

    public Map<Field, Annotation[]> getFieldAnnotations() {
        return fieldAnnotations;
    }

    public void setFields(Field[] fields) {
        this.fields = fields;
    }

    public void setClassAnns(Annotation[] classAnns) {
        this.classAnns = classAnns;
    }
}
