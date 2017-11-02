package org.zap.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zap.framework.exception.BusinessException;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;


public class FileToolUtils {

	static Logger logger = LoggerFactory.getLogger(FileToolUtils.class);

	/**
	 * 确保文件在本地文件系统存在
	 *
	 * @param file
	 * @throws BusinessException
	 */
	public static void insureFileExists(File file) throws BusinessException {
		if (!file.exists()) {
			File parentFile = file.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			if (file.isDirectory()) {
				file.mkdir();
			} else {
				try {
					file.createNewFile();
				} catch (IOException e) {
					logger.error("", e);
					throw new BusinessException("创建文件" + file.getPath() + "错误！");
				}
			}
		}
	}

	/**
	 * 获取有效非目录文件路径
	 *
	 * @param filePath
	 * @return
	 */
	public static String getValidateUnDirFilePath(String filePath) {
		String valiFilePath = getValidateFilePath(filePath);
		valiFilePath = valiFilePath.substring(0, valiFilePath.lastIndexOf(File.separator));
		return valiFilePath;
	}

	/**
	 * 获取有效目录文件路径
	 *
	 * @param filePath
	 * @return
	 */
	public static String getValidateDirFilePath(String filePath) {
		return getValidateFilePath(filePath);
	}

	/**
	 * 获得有效文件名称
	 *
	 * @param filePath
	 * @return
	 */
	public static String getValidateFileName(String filePath) {
		String valiFilePath = getValidateUnDirFilePath(filePath);
		return new File(valiFilePath).getName();
	}


	/**
	 * 删除文件
	 *
	 * @param fullName
	 * @return
	 */
	public static boolean deleteFile(String fullName) {
		boolean flag = true;

		try {
			File f = new File(fullName);//定义文件路径
			if (f.exists() && f.isDirectory()) {//判断是文件还是目录
				if (f.listFiles().length == 0) {//若目录下没有文件则直接删除
					f.delete();
				} else {//若有则把文件放进数组，并判断是否有下级目录
					File delFile[] = f.listFiles();
					int i = f.listFiles().length;
					for (int j = 0; j < i; j++) {
						if (delFile[j].isDirectory()) {
							deleteFile(delFile[j].getAbsolutePath());//递归调用deleteFile方法并取得子目录路径
						}
						delFile[j].delete();//删除文件
					}

					f.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 获取跨平台的有效文件系统路径,以分隔符结束
	 *
	 * @param path
	 * @return
	 */
	public static String getValidateFilePath(String path) {
		path = path.replaceAll("\\+", Matcher.quoteReplacement(File.separator));
		path = path.replaceAll("/+", Matcher.quoteReplacement(File.separator));
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		return path;
	}
}
