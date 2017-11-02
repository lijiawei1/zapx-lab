package org.zap.framework.orm.criteria.call;

import org.zap.framework.orm.sql.FunctionCall;
import org.zap.framework.orm.sql.Matchable;

public class MinFunction extends FunctionCall {

	public MinFunction(Matchable... arguments) {
		super("min", arguments);
	}
}
