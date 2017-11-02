package org.zap.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.zap.framework.common.json.CustomObjectMapper;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 文件工具类
 * @author Shin
 *
 */
public class ZipUtils {

	static Logger logger = LoggerFactory.getLogger(ZipUtils.class);
	
	/**
	 * 将内容从文件读出解压并反JSON化
	 * @param resource 资源路径
	 * @param valueType 内容类型
	 * @return 结果
	 */
	public static <T> T unzipJsonObjectFromFile(Resource resource, Class<T> valueType) {
		
		CustomObjectMapper om = new CustomObjectMapper();
		GZIPInputStream gis =null;
		T readValue = null;
		try {
			gis = new GZIPInputStream(resource.getInputStream());
			long start = System.currentTimeMillis();
			readValue = om.readValue(gis, valueType);

			long end = System.currentTimeMillis();
			logger.debug("Read:" + (end - start));
		} catch (Exception e) {
			e.printStackTrace();
			closeStream(gis);
		} finally {
			closeStream(gis);
		}
		return readValue;
	}
	
	/**
	 * 将内容从文件读出解压并反JSON化
	 * @param filePath 文件路径
	 * @param valueType 内容类型
	 * @return 结果
	 */
	public static <T> T unzipJsonObjectFromFile(String filePath, Class<T> valueType) {
		CustomObjectMapper om = new CustomObjectMapper();
		GZIPInputStream gis =null;
		T readValue = null;
		try {
			gis = new GZIPInputStream(new FileInputStream(filePath));
			long start = System.currentTimeMillis();
			readValue = om.readValue(gis, valueType);
			
			long end = System.currentTimeMillis();
			logger.debug("Read:" + (end - start));
		} catch (Exception e) {
			e.printStackTrace();
			closeStream(gis);
		} finally {
			closeStream(gis);
		}
		return readValue;
	}
	
	
	
	/**
	 * 将内容序列号后压缩到文件
	 * @param value
	 * @param filePath
	 */
	public static void zipSerializeObjectToFile(Object value, String filePath) {
		ObjectOutputStream oos = null;
		GZIPOutputStream gos = null;
		
		try {
			
			long start = System.currentTimeMillis();
			gos = new GZIPOutputStream(new FileOutputStream(filePath));
			oos = new ObjectOutputStream(gos);
			oos.writeObject(value);
			
			long end = System.currentTimeMillis();
			logger.debug("Write:" + (end - start));
			
		} catch (Exception e) {
			e.printStackTrace();
			closeStream(gos);
			closeStream(oos);
		} finally {
			closeStream(gos);
			closeStream(oos);
		}
	}
	
	/**
	 * 将内容以json格式压缩到文件
	 * @param value 内容
	 * @param filePath 文件路径
	 */
	public static void zipJsonObjectToFile(Object value, String filePath) {
		CustomObjectMapper om = new CustomObjectMapper();
		GZIPOutputStream gos = null;
		
		try {
			
			long start = System.currentTimeMillis();
			gos = new GZIPOutputStream(new FileOutputStream(new File(filePath)));
			om.writeValue(gos, value);
			
			long end = System.currentTimeMillis();
			logger.debug("Write:" + (end - start));
			
		} catch (IOException e) {
			closeStream(gos);
			e.printStackTrace();
		} finally {
			closeStream(gos);
		}
	}
	
	private static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
}
