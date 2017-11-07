package org.zapx.demo.study.fsm;

import org.junit.Test;

/**
 * Created by Administrator on 2017/11/7 0007.
 */
public class FsmTest {

    @Test
    public void test() {
        new FeeFsm("2017-10-01 20:00:00", "2017-10-01 22:00:00").start();
    }
}
