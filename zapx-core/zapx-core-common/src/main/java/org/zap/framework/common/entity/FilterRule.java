package org.zap.framework.common.entity;

public class FilterRule {

    public static final String OP_ADD = "add";
    public static final String OP_CLAUSE = "clause";
    public static final String OP_OR = "or";
    public static final String OP_AND = "and";
    public static final String OP_IN = "in";
    public static final String OP_NOTIN = "notin";
    public static final String OP_NOTEQUAL = "notequal";
    public static final String OP_EQUAL = "equal";

    /**
     * 字段名称
     */
    public String field;
    /**
     * 文本值
     */
    public Object value;
    /**
     * 操作符
     */
    public String op;
    /**
     * 输入控件类型，一般input框的text
     */
    public String type;
    /**
     * 数据类型，特殊控件，日期
     */
    public String datatype;
    /**
     * 忽略条件拼接，用于一些自定义SQL的固定条件
     */
    public boolean ignore = false;

    public FilterRule() {}

    public FilterRule(String field, Object value, String op, String type, String datatype) {
        this.field = field;
        this.value = value;
        this.op = op;
        this.type = type;
        this.datatype = datatype;
    }

    public FilterRule(String field, Object value) {
        this.field = field;
        this.value = value;
        this.op = "equal";
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }
}
