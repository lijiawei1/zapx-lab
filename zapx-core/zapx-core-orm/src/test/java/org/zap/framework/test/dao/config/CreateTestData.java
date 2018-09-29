package org.zap.framework.test.dao.config;

import org.junit.Test;
import org.zap.framework.util.ZipUtils;
import org.zap.framework.test.pojo.TestVo;
import org.zap.framework.test.pojo.TestVoFactory;

import java.io.IOException;

/**
 * 生成测试数据
 */
public class CreateTestData {
	
	private static String FILE_PATH = "D:/data";
	private static int RECORDS_20 = 20;
	private static int RECORDS_30 = 30;
	private static int RECORDS_1000 = 1000;
	private static int RECORDS_10000 = 10000;
	
	private static int OBJECT_QTY = 10000;
	
	protected String DATA_PATH = "/script/data";
	
	@Test
	public void readData() throws IOException {
//		TestVo[] readValues20 = ZipUtils.unzipJsonObjectFromFile(FILE_PATH + RECORDS_20, TestVo[].class);
//		TestVo[] readValues20 = ZipUtils.unzipJsonObjectFromFile("D:\\codespace\\zap-smp\\zap\\zap-core\\src\\test\\resources\\META-INF\\script\\data20", TestVo[].class);
//		TestVo[] readValues1000 = ZipUtils.unzipJsonObjectFromFile(FILE_PATH + RECORDS_1000, TestVo[].class);
//		TestVo[] unzipJsonObjectFromFile = ZipUtils.unzipJsonObjectFromFile(new PathResource(FILE_PATH + RECORDS_20), TestVo[].class);
//		System.out.println(readValues20.length);
//		System.out.println(readValues1000.length);
//		System.out.println(unzipJsonObjectFromFile);
		
		
//		ClassPathResource classPathResource = new ClassPathResource(DATA_PATH + RECORDS_20);
//		String file = classPathResource.getFile().getCanonicalPath();
		
		
		TestVo[] readValues20 = ZipUtils.unzipJsonObjectFromFile("D:\\codespace\\zap-smp\\zap\\zap-core\\src\\test\\resources\\META-INF\\script\\data20", TestVo[].class);
		TestVo[] readValues20plus = ZipUtils.unzipJsonObjectFromFile("D:\\codespace\\zap-smp\\zap\\zap-core\\target\\test-classes\\META-INF\\script\\data20", TestVo[].class);
//		ZipUtils.unzipJsonObjectFromFile(file, TestVo[].class);
	}
	
	@Test
	public void writeData() {
//		TestVo[] creates = TestVoFactory.getInstance().create(OBJECT_QTY);
		ZipUtils.zipJsonObjectToFile(TestVoFactory.getInstance().create(RECORDS_20), FILE_PATH + RECORDS_20 + ".dat");
		ZipUtils.zipJsonObjectToFile(TestVoFactory.getInstance().create(RECORDS_1000), FILE_PATH + RECORDS_1000 + ".dat");
		ZipUtils.zipJsonObjectToFile(TestVoFactory.getInstance().create(RECORDS_10000), FILE_PATH + RECORDS_10000 + ".dat");
//		ZipUtils.zipJsonObjectToFile(TestVoFactory.getInstance().create(RECORDS_30), new ClassPathResource(DATA_PATH + RECORDS_30));
//		ZipUtils.zipSerializeObjectToFile(creates, FILE_PATH + "2");
	}

}
