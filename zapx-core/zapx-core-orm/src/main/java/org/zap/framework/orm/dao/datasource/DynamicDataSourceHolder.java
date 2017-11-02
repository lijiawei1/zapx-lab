package org.zap.framework.orm.dao.datasource;

public class DynamicDataSourceHolder {

	public static final ThreadLocal<String> holder = new ThreadLocal<String>();
	
	public static String getDataSource() {
		return holder.get();
	}
	
	public static void putDataSource(String dataSourceName) {
		holder.set(dataSourceName);
	}
	
}
