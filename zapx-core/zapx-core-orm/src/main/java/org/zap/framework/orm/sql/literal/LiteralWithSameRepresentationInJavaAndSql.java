package org.zap.framework.orm.sql.literal;

import org.zap.framework.orm.sql.Literal;
import org.zap.framework.orm.sql.output.Output;

/**
 * @author Nat Pryce
 */
public abstract class LiteralWithSameRepresentationInJavaAndSql extends Literal {
	private final Object literalValue;

	protected LiteralWithSameRepresentationInJavaAndSql(Object literalValue) {
		this.literalValue = literalValue;
	}
	
	public void write(Output out) {
		out.print(literalValue);
	}
}
