package org.zap.framework.util;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 *
 *
 * Created by Shin on 2016/5/11.
 */
public class MathUtils {


    /**
     * 获取最小空缺值
     *
     * @param strNos
     * @return
     */
    public static String getMinOpening(String[] strNos, int bits) {

        if (strNos == null || strNos.length == 0 || StringUtils.isBlank(strNos[0])) {
            return "";
        }

        Integer[] nos = Stream.of(strNos).map(m -> {
            String result = "1" + StringUtils.trimToEmpty(m);
            return Integer.parseInt(result);
        }).toArray(Integer[]::new);

        Arrays.sort(nos);

        int mod = (int) Math.pow(10, bits - 1);

        int max = nos[nos.length - 1];
        int minOpening = max + 1;
        int index = nos[0] - nos[0] % mod + 1;

        for (int k = 0; index <= max; index++, k++) {
            if (index != nos[k]) {
                minOpening = index;
                break;
            }
        }
        return String.valueOf(minOpening).substring(1);
    }
}
