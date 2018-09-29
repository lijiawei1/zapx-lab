package org.zap.framework.orm.sql.output;

/**
 * Utility to quickly grab the complete String from an object that is Outputtable
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 */
public class ToStringer {
    public static String toSqlString(Outputable outputable) {
        Output out = new Output("    ");
        outputable.write(out);
        return out.toString();
    }
}
