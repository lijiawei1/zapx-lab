package org.zap.framework.orm.extractor;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.lob.LobHandler;

public interface Extractor<T> extends ResultSetExtractor<T> {
	public void setLobHandler(LobHandler lobHandler);
}
