package org.zap.framework.common.excel.parser;

/**
 * Created by Shin on 2017/5/4.
 */
public class Assert {

    public static void notBlank(CharSequence text, String message) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
}
