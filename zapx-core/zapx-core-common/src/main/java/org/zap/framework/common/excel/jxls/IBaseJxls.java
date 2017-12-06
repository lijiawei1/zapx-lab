package org.zap.framework.common.excel.jxls;

import org.jxls.common.Context;

/**
 * 输出报表接口
 * @author Sunqx
 *
 */
public interface IBaseJxls {
	
	/**
	 * 输出报表前对数据源的二次处理
	 * @param context
	 * @return
	 */
	public Context processContext(Context context) throws Exception ;
	
}
