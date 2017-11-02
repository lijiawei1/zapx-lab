package org.zap.framework.orm.sql.literal;

import java.math.BigDecimal;

/**
 * @author Nat Pryce
 */
public class BigDecimalLiteral extends LiteralWithSameRepresentationInJavaAndSql {
	public BigDecimalLiteral(BigDecimal literalValue) {
		super(literalValue);
	}
}
