package org.zap.framework.test.pojo;

import org.apache.commons.lang.StringUtils;
import org.zap.framework.util.DateUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestVoComparator {
	
	/**
	 * 批量比较
	 * @param expecteds
	 * @param querys
	 */
	public static void compare(TestVo[] expecteds, TestVo[] querys) {

		assertTrue(expecteds != null);
		assertTrue(querys != null);
		
		assertEquals(expecteds.length, querys.length);
		
		Map<String, TestVo> queryBuffer = new HashMap<String, TestVo>();
		for (int i =0 ; i < querys.length; i++) {
			queryBuffer.put(querys[i].getId(), querys[i]);
		}
		
		for (int i = 0; i < expecteds.length; i++) {
			TestVo expected = expecteds[i];
			TestVo query = queryBuffer.get(expecteds[i]);
			compare(expected, query);
		}
	}
	
	

	/**
	 * 比较TestVo
	 * @param expected
	 * @param query
	 */
	public static void compare(TestVo expected, TestVo query) {

		assertEquals(expected.getId(), query.getId());
		assertEquals(expected.getChar_field(), expected.getChar_field());
		assertEquals(expected.getVarchar_field(), expected.getVarchar_field());
		assertEquals(expected.getClob_field(), query.getClob_field());
		
		assertEquals(expected.getNumber_field(), query.getNumber_field());
		assertEquals(expected.getInt_field(), query.getInt_field());
		assertEquals(expected.getInteger_field(), query.getInteger_field());
		assertTrue(expected.getDbl_field() - query.getDbl_field() < 0.000000001);
		assertTrue(expected.getDouble_field() - query.getDouble_field() < 0.0000001);
		
		//浮点数比较/精度是否丢失
		assertEquals(expected.getLdouble_field(), query.getLdouble_field());
		
		assertEquals(expected.getLong_field(), query.getLong_field());
		assertEquals(expected.getDate_field(), query.getDate_field());
		assertEquals(expected.getDatetime_field().format(DateUtils.FORMATTER_DATE), query.getDatetime_field().format(DateUtils.FORMATTER_DATE));
		assertEquals(expected.getTime_field().format(DateUtils.FORMATTER_TIME), query.getTime_field().format(DateUtils.FORMATTER_TIME));
//		assertArrayEquals(expected.getBlob_field(), query.getBlob_field());

		assertEquals(expected.getBoolean_field(), query.getBoolean_field());
		assertEquals(expected.isBool_field(), query.isBool_field());
		
		assertEquals(DateUtils.FORMATTER_DATE.format(expected.getModify_time()), DateUtils.FORMATTER_DATE.format(query.getModify_time()));
		assertEquals(StringUtils.trim(expected.getModifier_id()), StringUtils.trim(query.getModifier_id()));
		assertEquals(DateUtils.FORMATTER_DATE.format(expected.getCreate_time()), DateUtils.FORMATTER_DATE.format(query.getCreate_time()));
		assertEquals(StringUtils.trim(expected.getCreator_id()), StringUtils.trim(query.getCreator_id()));
		assertEquals(expected.getRemark(), query.getRemark());
		assertEquals(expected.getVersion(), query.getVersion());
		assertEquals(StringUtils.trim(expected.getCorp_id()), StringUtils.trim(query.getCorp_id()));
		assertEquals(StringUtils.trim(expected.getDept_id()), StringUtils.trim(query.getDept_id()));
	}
	
}
