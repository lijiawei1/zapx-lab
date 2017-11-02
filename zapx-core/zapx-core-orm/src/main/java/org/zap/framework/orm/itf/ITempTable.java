package org.zap.framework.orm.itf;

public interface ITempTable {

	public	String createTempTable(String tableName, String[] columns, String[] indexs);

	public void dropTempTable(String tableName);

	public IDataLang getLang();

}
