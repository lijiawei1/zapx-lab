package org.zap.framework.util;

import org.apache.commons.lang.StringUtils;
import org.zap.framework.lang.LDouble;

/**
 * 数据类型转换
 * @author Shin
 *
 */
public class DataTypeUtils {

	public static String parseString(String text) {
		String trim = StringUtils.trimToEmpty(text);
		return "null".equalsIgnoreCase(trim) ? "" : trim;
	}

	public static Boolean parseBoolean(String text) {
		Boolean result = null;
		if ("true".equalsIgnoreCase(text) || "y".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text)) {
			result = Boolean.TRUE;
		} else if ("false".equalsIgnoreCase(text) || "n".equalsIgnoreCase(text) || "no".equalsIgnoreCase(text)) {
			result = Boolean.FALSE;
		}
		return result;
	}
	
	public static boolean parseBool(String text) {
		return "true".equalsIgnoreCase(text) || "y".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text) ? true : false;
	}
	
	public static LDouble parseLDouble(String text) {
		return (StringUtils.isBlank(text) || "null".equalsIgnoreCase(text.trim())) ? null : new LDouble(text);
	}
	
	public static int parseInt(String text) {
		return StringUtils.isBlank(text)  || "null".equalsIgnoreCase(text.trim()) ? 0 : Integer.parseInt(text);
	}
	
}
