package org.zap.framework.orm.sql.literal;

import org.zap.framework.orm.sql.Literal;
import org.zap.framework.orm.sql.output.Output;

public class StringLiteral extends Literal {
	private final String literalValue;
	
	public StringLiteral(String literalValue) {
		this.literalValue = literalValue;
	}
	
	public void write(Output out) {
		out.print(quote(literalValue));
	}
	
	protected String quote(String s) {
        if (s == null) return "null";
        
        StringBuffer str = new StringBuffer();
        str.append('\'');
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\\'
                    || s.charAt(i) == '\"'
                    || s.charAt(i) == '\'') {
                str.append('\\');
            }
            str.append(s.charAt(i));
        }
        str.append('\'');
        return str.toString();
    }
}
