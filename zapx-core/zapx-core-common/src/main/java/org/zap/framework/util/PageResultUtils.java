package org.zap.framework.util;

import org.zap.framework.common.entity.PageResult;

/**
 * Created by haojc on 2016/1/28.
 */
public class PageResultUtils {

    /**
     * 增加,更新,删除的业务操作返回值格式化
     *
     * @param num
     * @param message
     * @return
     */
    public static PageResult BusiFormat(int num, String message) {
        PageResult result;
        if (num > 0) {
            result = new PageResult(false, message + "成功!", "");
        } else {
            result = new PageResult(true, message + "失败!", "");
        }
        return result;
    }

    /**
     * 增,删,改的业务操作返回值
     *
     * @param message
     * @return
     */
    public static PageResult BusiSuccessFormat(String message) {

        PageResult result = new PageResult(false, message + "成功!", "");

        return result;
    }
}
