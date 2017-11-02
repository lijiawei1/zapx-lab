package org.zap.framework.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class RateUtils {

    /**
     * 根据部分汇率转换全部汇率的工具
     * 1.要求传入的MAP的格式为<String,Double>
     * 2.要求key的值格式为 源币别#目标币别 例如:USD#RMB
     * 3.要求目标币别一致.
     *
     * @param currMap
     * @return
     */
    public static HashMap<String, Double> convertAllTax(HashMap<String, Double> currMap) {
        HashMap<String, Double> result = new HashMap<>();

        HashMap<String, Double> otherCurrMap = new HashMap<>();

        //遍历币别MAP.并重构
        for (Map.Entry<String, Double> set : currMap.entrySet()) {
            //获取源币别
            otherCurrMap.put(set.getKey().split("#")[0], set.getValue());
            //保留原有值
            result.put(set.getKey(), set.getValue());
        }
        //遍历其他源币别
        for (Map.Entry<String, Double> source_curr : otherCurrMap.entrySet()) {
            String source_key = source_curr.getKey();
            Double source_value = source_curr.getValue();
            //源币别和其他源币别之间转换
            for (Map.Entry<String, Double> target_curr : otherCurrMap.entrySet()) {
                String target_key = target_curr.getKey();
                Double target_value = target_curr.getValue();
                if (!target_key.equals(source_key)) {
                    //最终结果.四舍五入保留6为小数
                    BigDecimal b = new BigDecimal(source_value / target_value);
                    double value = b.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                    result.put(source_key + "#" + target_key, value);
                }
            }

        }
        return result;
    }

    //输出报表用
    public static HashMap<String, Object>  convertTaxToOutput(HashMap<String, Double> currMap) {
        HashMap<String, Object> result = new HashMap<>();
        //遍历币别MAP.并重构
        for (Map.Entry<String, Double> set : currMap.entrySet()) {
            //保留原有值
            result.put(set.getKey().replace("#", "_"), set.getValue()); //主要为了输出报表用
            String[] rates = set.getKey().split("#");
            //格式化汇率
            if (rates != null && rates.length == 2) {
                String rateFormat = rates[0] + " 1.00 = " + rates[1] + " " + set.getValue();
                result.put(set.getKey().replace("#", "_") + "_FORMAT", rateFormat);
            }
        }
        return result;
    }
}
