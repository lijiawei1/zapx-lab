package org.zap.framework.orm.sql.literal;

import org.zap.framework.orm.sql.Literal;
import org.zap.framework.orm.sql.output.Output;

public class ParamLiteral extends Literal {

	private ParamLiteral() {}
	public static ParamLiteral instance = new ParamLiteral();
	
	@Override
	public void write(Output out) {
		out.print('?');
	}

}
