package org.zap.framework.exception;

import org.springframework.dao.DataAccessException;

public class BusinessException extends DataAccessException {

	/**
	 * 错误码
	 */
	int code;

	public BusinessException(String message, int code) {
		super(message);
		this.code = code;
	}

	public BusinessException(String message) {
		super(message);
	}
	
	public BusinessException(String message, Exception e) {
		super(message, e);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5178982418640599640L;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
