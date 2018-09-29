package org.zap.framework.test.pojo;

import com.eaio.uuid.UUID;
import org.apache.commons.lang.StringUtils;
import org.zap.framework.lang.LDouble;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;


public class TestVoFactory {

	private TestVoFactory() {}
	
	private static TestVoFactory instance;
	
	public static TestVoFactory getInstance() {
		if (instance == null) {
			instance = new TestVoFactory();
		}
		return instance;
	}
	
	public TestVo[] update(TestVo[] vos) {
		for (int i = 0; i < vos.length; i++) {
			vos[i].setChar_field(vos[i].getChar_field() + "_" + i);
			vos[i].setDbl_field(vos[i].getDbl_field() * 100);
			vos[i].setNumber_field(vos[i].getNumber_field().divide(new BigDecimal("4")));
		}
		return vos;
	}
	

	public TestVo[] createNormal(int count) {
		TestVo[] vos = new TestVo[count];
		for (int i = -5; i < vos.length - 5; i++) {
			vos[i] = new TestVo();
			
			vos[i].setCorp_id("10000000-0000-0000-0000-000000000000");
			vos[i].setCreator_id("30000000-0000-0000-0000-000000000000");
			
			vos[i].setId(new UUID().toString().toUpperCase());
			vos[i].setVarchar_field(new StringBuffer().append((char)('A' + i)).toString());
			vos[i].setInt_field(1 + i);
			vos[i].setInteger_field(1 + i);
			
			vos[i].setBool_field(i % 2 == 0);
			vos[i].setBoolean_field(i % 3 == 0);
			
			vos[i].setDate_field(LocalDate.now().plusDays(i));
			vos[i].setDatetime_field(LocalDateTime.now().plusHours(i * 10));
			
		}
		return vos;
	}
	
	
	/**
	 */
	public TestVo[] create(int count) {

		TestVo[] vos = new TestVo[count];
		for (int i = 0; i < vos.length; i++) {
			vos[i] = new TestVo();
			vos[i].setId(new UUID().toString().toUpperCase());
			vos[i].setChar_field("char_field" + "_" + i);
			vos[i].setVarchar_field("varchar_field" + "_" + i);
			vos[i].setLdouble_field(LDouble.ONE_DBL.multiply(i));
			vos[i].setDbl_field(13.0 / (i + 1));
			vos[i].setDouble_field(19.0 / (i + 1));
			vos[i].setInt_field(i);
			vos[i].setInteger_field(i);
			vos[i].setLong_field(13L * i);
			vos[i].setNumber_field(new BigDecimal(i * 17));
			vos[i].setClob_field("clob_field" + "_" + i);
//			vos[i].setBlob_field("blob_field".getBytes());
			
			vos[i].setDatetime_field(LocalDateTime.now(ZoneId.systemDefault()));
			vos[i].setTime_field(LocalTime.now(ZoneId.systemDefault()));
			vos[i].setDate_field(LocalDate.now(ZoneId.systemDefault()));

			vos[i].setOld_datetime(LocalDateTime.now().plusDays(i));
			vos[i].setOld_time(LocalTime.now().plusHours(i));
			vos[i].setOld_date(LocalDate.now().plusMonths(i));

			//
			vos[i].setVersion(10);
			vos[i].setCreate_time(LocalDateTime.now());
//			vos[i].setCreator_id();
			vos[i].setModify_time(LocalDateTime.now());
//			vos[i].setModifier_id();
//			vos[i].setCorp_id();
//			vos[i].setDept_id();
			vos[i].setBoolean_field(Boolean.TRUE);
			vos[i].setBool_field(false);


			
			vos[i].setRemark("REMARK_" + i);
			
		}
		
		return vos;
	}
	
	
}
