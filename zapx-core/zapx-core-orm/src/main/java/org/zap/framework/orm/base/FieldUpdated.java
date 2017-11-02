package org.zap.framework.orm.base;

/**
 * 更新的字段
 *
 * Created by Shin on 2017/9/4.
 */
public class FieldUpdated {

    String name;

    /**
     * 新值
     */
    Object newValue;

    /**
     * 旧值
     */
    Object oldValue;

    /**
     * 类型
     */
    Class<?> type;

    public FieldUpdated() {
    }

    public FieldUpdated(String name, Object newValue, Object oldValue, Class<?> type) {
        this.name = name;
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.type = type;
    }

    public FieldUpdated(Object newValue, Object oldValue, Class<?> type) {
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.type = type;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
