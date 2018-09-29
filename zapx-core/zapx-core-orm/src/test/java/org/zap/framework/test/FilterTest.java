package org.zap.framework.test;

import org.junit.Test;
import org.zap.framework.common.entity.FilterGroup;
import org.zap.framework.common.entity.FilterRule;
import org.zap.framework.common.entity.FilterTranslator;

import java.util.Arrays;

/**
 * Created by Shin on 2016/11/1.
 */
public class FilterTest {

    @Test
    public void testInstrRules() {

        FilterGroup group = new FilterGroup();

        group.getRules().add(new FilterRule("a,b,c", "v", "instr", "", ""));

        FilterTranslator whereTranslator = new FilterTranslator(group, "A", "oracle");
        System.out.println(whereTranslator.getCommandText());
        System.out.println(Arrays.toString(whereTranslator.getParmsArray()));

    }

    @Test
    public void testRules() {

        FilterGroup group = new FilterGroup();

        group.getRules().add(new FilterRule("dr", 0));

        FilterRule sadf = new FilterRule("A.MST_ID IN (SELECT * FROM DUAL)", null);
        sadf.setOp("clause");

        group.getRules().add(sadf);

        FilterTranslator whereTranslator = new FilterTranslator(group, "A", "oracle");

        System.out.println(whereTranslator.getCommandText());
        System.out.println(Arrays.toString(whereTranslator.getParmsArray()));


    }
}
