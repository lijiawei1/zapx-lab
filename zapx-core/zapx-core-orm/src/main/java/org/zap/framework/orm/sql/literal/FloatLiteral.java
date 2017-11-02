package org.zap.framework.orm.sql.literal;

/**
 * @author Nat Pryce
 */
public class FloatLiteral extends LiteralWithSameRepresentationInJavaAndSql {
	public FloatLiteral(double literalValue) {
		super(new Double(literalValue));
	}
}
