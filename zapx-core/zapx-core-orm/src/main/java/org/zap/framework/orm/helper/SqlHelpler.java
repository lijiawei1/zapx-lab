package org.zap.framework.orm.helper;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by Shin on 2015/11/21.
 */
public class SqlHelpler {
    /**
     * 获得order by在SQL中出现的位置
     *
     * @param strSql
     * @return
     */
    public static int containsOrderby(String strSql) {

        String patternStr = "\\s+order\\s+by\\s+";
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE
                + Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(strSql);
        if (matcher.find())
            return matcher.start();
        else
            return -1;
    }

    /**
     * 判断某表达式是否为常量表达式 有可能是这样的无字段函数：convert(char,getdate())
     *
     * @param exp String
     * @return boolean
     */
    public static boolean isConst(String exp) {
        if (exp.startsWith("\'") && exp.endsWith("\'")) {
            return true;
        }
        try {
            Double.valueOf(exp);
            return true;
        } catch (Exception e) {
        }
        // 处理无字段函数
        if (exp.indexOf(".") == -1) {
            return true;
        }
        return false;
    }

    /**
     * 是否带有聚集函数 创建日期：(02-4-28 12:18:22)
     *
     * @param strExp String
     * @return boolean
     */
    public static boolean withAggFunc(String strExp) {
        if (strExp == null) {
            return false;
        }
        // 去掉字符串中的空格
        strExp = StringUtils.replace(strExp, " ", "", -1).toLowerCase();
        // 判断
        if (strExp.startsWith("(select")) {
            return false;
        } else if (strExp.indexOf("sum(") != -1 || strExp.indexOf("avg(") != -1
                || strExp.indexOf("max(") != -1 || strExp.indexOf("min(") != -1
                || strExp.indexOf("count(") != -1) {
            return true;
        } else {
            return false;
        }
    }
}
