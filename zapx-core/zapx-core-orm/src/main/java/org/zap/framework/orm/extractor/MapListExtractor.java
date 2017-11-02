package org.zap.framework.orm.extractor;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.lob.LobHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapListExtractor implements Extractor<List<Map<String, Object>>>{

	protected LobHandler lobHandler;
	
	public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		
//		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ResultSetMetaData rm = rs.getMetaData();
		int ccount = rm.getColumnCount();
		
		List<Map<String,Object>> mapls = new ArrayList<Map<String,Object>>();
		while (rs.next()) {
			Map<String,Object> map = new HashMap<String, Object>();
			for (int i = 1; i <= ccount; i++) {
				String label = rm.getColumnLabel(i);
				Object obj = rs.getObject(label);
				map.put(label, obj);
			}
			mapls.add(map);
		}
		return mapls;

	}

	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}
	
}
