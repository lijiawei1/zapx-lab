package org.zap.framework.orm.sql;

import org.zap.framework.orm.sql.output.Output;

import java.util.Set;

public class FunctionCall implements Matchable, Selectable {
    private final String functionName;
    private final Matchable[] arguments;

    public FunctionCall(String functionName, Matchable... arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public void write(Output out) {
        out.print(functionName).print("(");
        for (int i = 0; i < arguments.length; i++) {
			if (i > 0) out.print(", ");
			arguments[i].write(out);
		}
        out.print(")");
    }

    public void addReferencedTablesTo(Set<Table> tables) {
    	for (Matchable argument : arguments) {
    		argument.addReferencedTablesTo(tables);
		}
    }
}
