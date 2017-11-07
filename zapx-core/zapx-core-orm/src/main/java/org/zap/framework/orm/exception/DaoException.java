package org.zap.framework.orm.exception;

/**
 * @author Shin
 *
 */
public class DaoException extends RuntimeException {
	
	public DaoException() {
	}

	/**
	 */
	public DaoException(String message) {
		super(message);
	}

	/**
	 */
	public DaoException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}
	
}