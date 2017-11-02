package org.zap.framework.orm.criteria.call;

import org.zap.framework.orm.sql.FunctionCall;
import org.zap.framework.orm.sql.literal.IntegerLiteral;

public class CountFunction extends FunctionCall {

	public CountFunction() {
		super("COUNT", new IntegerLiteral(1));
	}
}
