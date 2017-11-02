package org.zap.framework.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具类
 */
public class SqlUtils {

	/**
	 * 获取IN条件语句
	 *
	 * @param field  列名
	 * @param values 值
	 * @return
	 */
	public static String inClause(String field, String[] values) {

		if (values == null || values.length == 0)
			return "";

		StringBuilder inbuffer = new StringBuilder("(");

		List<String[]> stringsArray = Utils.splitArray(values, 800);

		if (stringsArray.size() > 0) {
			inbuffer.append(" 1 <> 1");
			for (String[] strings : stringsArray) {
				inbuffer.append(" OR ").append(field).append(" IN ('").append(StringUtils.join(strings, "','")).append("')");
			}
		}
		inbuffer.append(")");

		return inbuffer.toString();
	}

	/**
	 * @param field     列
	 * @param valueList 键值
	 * @return
	 */
	public static String inClause(String field, List<String> valueList) {

		if (valueList == null || valueList.size() == 0)
			return "";

		StringBuilder inbuffer = new StringBuilder("(");

		List<String[]> stringsArray = Utils.splitArray(valueList.stream().toArray(String[]::new), 800);

		if (stringsArray.size() > 0) {
			inbuffer.append(" 1 <> 1");
			for (String[] strings : stringsArray) {
				inbuffer.append(" OR ").append(field).append(" IN ('").append(StringUtils.join(strings, "','")).append("')");
			}
		}
		inbuffer.append(")");

		return inbuffer.toString();
	}


	public String inStr(String field, int size) {

		StringBuffer inStr = new StringBuffer(" IN (");

		if (size < 1000) {

		}

		inStr.append(")");

		return inStr.toString();
	}

	/**
	 * 获取排序条件
	 *
	 * @param sortnames
	 * @param sortorders
	 * @return
	 */
	public static String getSortPart(String sortnames, String sortorders) {

		String sortPart = "";

		if (StringUtils.isNotBlank(sortnames)) {

			List<String> sorts = new ArrayList<>();

			String[] names = StringUtils.trim(sortnames).split(",");
			String[] orders = StringUtils.trim(sortorders).split(",");

			int i = 0, k = 0;
			for (; i < names.length; i++, k++) {

				if (StringUtils.isNotBlank(names[i])) {
					String temp = names[i];

					if (k < orders.length) {
						temp += (" " + (StringUtils.isEmpty(orders[k]) ? "ASC" : orders[k]));
					}
					sorts.add(temp);
				}
			}
			sortPart += (" ORDER BY " + StringUtils.join(sorts, ","));
		}

		return sortPart;
	}

	/**
	 * 拼接 instr字段
	 *
	 * @param alias
	 * @param fields
     * @return
     */
	public static String instr(String alias, String[] fields) {

		String clause = "";

		if (fields != null && fields.length > 0) {
			for (int i = 0; i < fields.length; i++) {
				fields[i] = StringUtils.isBlank(alias) ? fields[i].toUpperCase() :
						(StringUtils.indexOf(alias, ".") != -1 ? alias : alias + ".") + fields[i].toUpperCase();
				fields[i] = " NVL(" + fields[i] + ",'') ";
			}
			clause = " INSTR(" + StringUtils.join(fields, "||") + ", ?) > 0";
		}
		return clause.toString();
	}
}
