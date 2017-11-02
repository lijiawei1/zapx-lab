package org.zap.framework.common.entity;

import org.apache.commons.lang.StringUtils;
import org.zap.framework.util.SqlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 将检索规则 翻译成 where sql 语句,并生成相应的参数列表
 *
 * @author haojc
 */
public class FilterTranslator {

    /// 组条件括号
    protected final static String GROUP_LEFT_TOKEN = "(";

    /// 右条件括号
    protected final static String GROUP_RIGHT_TOKEN = ")";

    /// 模糊查询符号
    protected final static String LIKE_TOKEN = "%";

    protected int parmsAliaCounter = 0;

    //几个主要的属性
    protected FilterGroup group;

    //从句
    protected String commandText;

    //名值对
    protected String nameParmsCommandText;

    //字段别名
    protected String alias;

    //数据库类型
    protected String dbType;

    //参数列表
    protected List<Object> parmsList = new ArrayList<>();

    //参数名值对
    public Map<String, Object> parmsMap = new HashMap<>();

    public FilterTranslator(FilterGroup group, String alias) {
        this.group = group;
        this.alias = alias + ".";
        Translate();
    }

    public FilterTranslator(FilterGroup group, String alias, String dbType) {
        this.group = group;
        this.alias = alias + ".";
        this.dbType = dbType;
        Translate();
    }

    public FilterTranslator(FilterGroup group, String alias, String dbType, boolean nameParms) {
        this.group = group;
        this.alias = alias + ".";
        this.dbType = dbType;

        if (nameParms) {
            //解析成名值对
            this.nameParmsCommandText = translateGroupForNameParms(this.group);
        } else {
            Translate();
        }

    }

    public String translateGroupForNameParms(FilterGroup group) {
        StringBuilder bulider = new StringBuilder();

        if (group == null)
            return "AND 1=1 ";

        boolean appended = false;
        bulider.append(GROUP_LEFT_TOKEN);
        //如果规则不为空
        if (group.getRules() != null && group.getRules().size() > 0) {
            for (FilterRule rule : group.rules) {
                // 获取操作符的SQL Text
                String opText = GetOperatorQueryText(group.op);
                String ruleText = translateRuleForNameParms(rule);
                boolean blank = StringUtils.isBlank(ruleText);
                if (appended) {
                    bulider.append(blank ? "" : opText + ruleText);
                } else {
                    bulider.append(ruleText);
                    appended = !blank;
                }


            }
        }
        if (group.getGroups() != null && group.getGroups().size() > 0) {
            for (FilterGroup subgroup : group.getGroups()) {
                if (appended)
                    bulider.append(GetOperatorQueryText(group.op));
                bulider.append(translateGroupForNameParms(subgroup));
                appended = true;
            }
        }
        bulider.append(GROUP_RIGHT_TOKEN);
        if (appended == false)
            return " 1=1 ";
        return bulider.toString();
    }

    /**
     * 翻译
     *
     * @param rule
     * @return
     */
    private String translateRuleForNameParms(FilterRule rule) {

        StringBuilder bulider = new StringBuilder();
        if (rule == null) return "";

        //存放参数别名
        String parmsAlias = rule.field + (parmsAliaCounter++);
        parmsMap.put(parmsAlias, rule.value); //为参数取别名
        parmsMap.put(rule.field, rule.value);

        if (rule.isIgnore()) {
            return "";
        }

        bulider.append(getFieldText(rule));
        bulider.append(GetOperatorQueryText(rule.op));

        String op = rule.op.toLowerCase();
        String field = rule.field;

        /**
         * 注意下列情况都是互相独立，不能兼容
         */
        if ("like".equalsIgnoreCase(op)) {
            bulider.append(" '%' || ").append(":" + parmsAlias).append(" || '%'");
        } else if ("endwith".equalsIgnoreCase(op)) {
            bulider.append(" '%' || ").append(":" + parmsAlias);
        } else if ("startwith".equalsIgnoreCase(op)) {
            bulider.append(" :" + parmsAlias).append(" || '%'");
        } else if ("in".equalsIgnoreCase(op) || "notin".equals(op)) {
            String[] values = rule.value.toString().split(",");
            String[] fields = new String[values.length];

            for (int i = 0; i < values.length; i++) {
                parmsMap.put(rule.field + i, values[i]);
                fields[i] = (":" + rule.field + i);
            }

            bulider.append("(");
            bulider.append(StringUtils.join(fields, ","));
            bulider.append(")");
        } else if ("instr".equalsIgnoreCase(op)) {
            //this.parmsList.add(rule.value);
        } else if ("clause".equalsIgnoreCase(op)) {
            //不支持名值参数化
            //if (StringUtils.isNotBlank(rule.value == null ? "" : rule.value.toString())) {
            //    String[] values = rule.value.toString().split(",");
            //
            //    for (String value : values) {
            //        this.parmsList.add(value);
            //    }
            //}
        }
        //is null 和 is not null 不需要值
        else if (!"isnull".equalsIgnoreCase(op) && !"isnotnull".equalsIgnoreCase(op)) {
            //放入parms
            bulider.append(":").append(parmsAlias);
        }
        return bulider.toString();


    }

    public void Translate() {
        this.commandText = TranslateGroup(this.group);
    }

    /**
     * 对多组规则进行翻译解析
     *
     * @param group 规则数组
     * @return
     */
    public String TranslateGroup(FilterGroup group) {
        StringBuilder bulider = new StringBuilder();

        if (group == null)
            return "AND 1=1 ";

        boolean appended = false;
        bulider.append(GROUP_LEFT_TOKEN);
        //如果规则不为空
        if (group.getRules() != null && group.getRules().size() > 0) {
            for (FilterRule rule : group.rules) {
                // 获取操作符的SQL Text
                if (appended)
                    bulider.append(GetOperatorQueryText(group.op));

                bulider.append(TranslateRule(rule));
                appended = true;
            }
        }
        if (group.getGroups() != null && group.getGroups().size() > 0) {
            for (FilterGroup subgroup : group.getGroups()) {
                if (appended)
                    bulider.append(GetOperatorQueryText(group.op));
                bulider.append(TranslateGroup(subgroup));
                appended = true;
            }
        }
        bulider.append(GROUP_RIGHT_TOKEN);
        if (appended == false)
            return " 1=1 ";
        return bulider.toString();
    }

    /**
     * 获取字段文本
     *
     * @param rule
     * @return
     */
    private String getFieldText(FilterRule rule) {

        //默认格式：别名.字段名
        String fieldText = rule.field.indexOf(".") == -1 ? alias + rule.field : rule.field;

        //特殊格式
        if ("instr".equals(rule.getOp())) {
            return SqlUtils.instr(alias, rule.getField().split(","));
        } else {
            if (StringUtils.isNotBlank(rule.datatype)) {

                String type = rule.datatype.toLowerCase();

                if ("date".equals(type) || "datetime".equals(type)) {

                    String formatString = "yyyy-MM-dd";

                    String key = type + "-" + dbType;

                    //转换各种时间类型和格式
                    switch (key) {
                        //问题是不能使用ORACLE的date索引
                        case "date-oracle":
                            formatString = String.format("TO_CHAR(%s, 'yyyy-MM-dd')", fieldText);
                            break;
                        case "datetime-oracle":
                            formatString = String.format("TO_CHAR(%s,'yyyy-MM-dd hh24:mi:ss')", fieldText);
                            break;
                        case "date-mysql":
                            formatString = String.format("date_format(%s,'%Y-%m-%d')", fieldText);
                            break;
                        case "datetime-mysql":
                            formatString = String.format("date_format(%s, '%Y-%m-%d %H:%i:%s')", fieldText);
                            break;
                        case "date-sqlserver":
                            formatString = "";
                            break;
                        case "datetime-sqlserver":
                            formatString = "";
                            break;
                    }

                    return formatString;
                }
            }
        }

        return fieldText;
    }

    /**
     * 翻译规则
     *
     * @param rule 规则
     * @return
     */
    public String TranslateRule(FilterRule rule) {

        StringBuilder bulider = new StringBuilder();
        if (rule == null) return " 1=1 ";

        bulider.append(getFieldText(rule));

        bulider.append(GetOperatorQueryText(rule.op));

        String op = rule.op.toLowerCase();

        if ("like".equals(op) || "endwith".equals(op)) {
            String value = rule.value.toString();
            if (!value.startsWith(LIKE_TOKEN)) {
                rule.value = LIKE_TOKEN + value;
            }
        }
        if ("like".equals(op) || "startwith".equals(op)) {
            String value = rule.value.toString();
            if (!value.endsWith(LIKE_TOKEN.toString())) {
                rule.value = value + LIKE_TOKEN;
            }
        }

        /**
         * 注意下列情况都是互相独立，不能兼容
         */
        if ("in".equals(op) || "notin".equals(op)) {
            String values[] = rule.value.toString().split(",");
            boolean appended = false;
            bulider.append("(");
            for (String value : values) {
                if (appended)
                    bulider.append(",");
                //放入parms
                bulider.append("?");
                this.parmsList.add(value);
                appended = true;
            }
            bulider.append(")");
        } else if ("instr".equals(op)) {
            this.parmsList.add(rule.value);
        } else if ("clause".equals(op)) {
            //不用干活
            if (StringUtils.isNotBlank(rule.value == null ? "" : rule.value.toString())) {
                String[] values = rule.value.toString().split(",");

                for (String value : values) {
                    this.parmsList.add(value);
                }
            }
        }
        //is null 和 is not null 不需要值
        else if (!"isnull".equals(op) && !"isnotnull".equals(op)) {
            //放入parms
            bulider.append("?");
            this.parmsList.add(rule.getValue());
        }
        return bulider.toString();
    }

    public Object[] getParmsArray() {
        return parmsList.toArray(new Object[parmsList.size()]);
    }

    /**
     * 获取操作符的SQL Text
     * @param op
     * @return
     */
    public static String GetOperatorQueryText(String op) {
        switch (op.toLowerCase()) {
            case "clause":
                return "";
            case "add":
                return " + ";
            case "bitwiseand":
                return " & ";
            case "bitwisenot":
                return " ~ ";
            case "bitwiseor":
                return " | ";
            case "bitwisexor":
                return " ^ ";
            case "divide":
                return " / ";
            case "equal":
                return " = ";
            case "greater":
                return " > ";
            case "greaterorequal":
                return " >= ";
            case "isnull":
                return " is null ";
            case "isnotnull":
                return " is not null ";
            case "less":
                return " < ";
            case "lessorequal":
                return " <= ";
            case "like":
                return " like ";
            case "startwith":
                return " like ";
            case "endwith":
                return " like ";
            case "modulo":
                return " % ";
            case "multiply":
                return " * ";
            case "notequal":
                return " <> ";
            case "subtract":
                return " - ";
            case "and":
                return " and ";
            case "or":
                return " or ";
            case "in":
                return " in ";
            case "notin":
                return " not in ";
            case "instr":
                return " ";
            default:
                return " = ";
        }
    }

    public FilterGroup getGroup() {
        return group;
    }

    public void setGroup(FilterGroup group) {
        group = group;
    }

    public String getCommandText() {
        return commandText;
    }

    public void setCommandText(String commandText) {
        commandText = commandText;
    }

    public List<Object> getParmsList() {
        return parmsList;
    }

    public void setParmsList(ArrayList<Object> parmsList) {
        parmsList = parmsList;
    }

    public Map<String, Object> getParmsMap() {
        return parmsMap;
    }

    public void setParmsMap(Map<String, Object> parmsMap) {
        this.parmsMap = parmsMap;
    }

    public String getNameParmsCommandText() {
        return nameParmsCommandText;
    }

    public void setNameParmsCommandText(String nameParmsCommandText) {
        this.nameParmsCommandText = nameParmsCommandText;
    }
}
