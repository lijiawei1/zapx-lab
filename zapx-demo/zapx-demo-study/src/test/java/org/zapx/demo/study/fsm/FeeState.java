package org.zapx.demo.study.fsm;

/**
 * 3.超时费计算公式：（以入厂和出厂时间计算）
 * 晚上20点进到第二天早上8点前免超时费
 * 半个小时内不收超时费，超过半个小时的按一个小时算
 * 从早上8点开始的时间段开始的是免4小时，超出4小时按每小时100元，封顶1200元一天
 * <p>
 * Created by Administrator on 2017/11/7 0007.
 */
public enum FeeState {

    /**
     * 初始状态
     */
    INIT {
        @Override
        void next(FeeFsm ffsm) {
            System.out.println("----------------------------------------------");
            exit(ffsm);
            ffsm.state = (ffsm.isArrivalInEvening() ? EVE_START : MOR_START);
            entry(ffsm);
            ffsm.state.next(ffsm);
        }
    }

    , END {
        @Override
        void next(FeeFsm ffsm) {
            exit(ffsm);
            //停止计算，输出状态
            System.out.println(ffsm);
        }
    }
    /**
     * 晚间免费时段
     */
    , EVE_START {
        @Override
        void next(FeeFsm ffsm) {
            exit(ffsm);
            //计算晚间时间是否免费
            ffsm.state = (ffsm.isEndInEveningStart() ? END : MOR_CHARGE);
            entry(ffsm);
            ffsm.state.next(ffsm);
        }
    }
    , EVE_FREE {
        @Override
        void next(FeeFsm ffsm) {
            exit(ffsm);
            //计算晚间时间是否免费
            ffsm.state = (ffsm.isEndInEvening() ? END : MOR_CHARGE);
            entry(ffsm);
            ffsm.state.next(ffsm);
        }
    }

    /**
     * 早间收费时段
     */
    , MOR_CHARGE {
        @Override
        void next(FeeFsm ffsm) {
            exit(ffsm);
            ffsm.state = ffsm.isEndInMorning() ? END : EVE_FREE;
            entry(ffsm);
            ffsm.state.next(ffsm);
        }
    }
    /**
     * 早间免费4小时时段
     */
    , MOR_START {
        @Override
        void next(FeeFsm ffsm) {
            exit(ffsm);
            ffsm.state = ffsm.isEndInMorningStart() ? END : EVE_FREE;
            entry(ffsm);
            ffsm.state.next(ffsm);
        }
    };

    void entry(FeeFsm ffsm) {
        System.out.println("  " + ffsm + "->" + ffsm.state.name());
    }

    void exit(FeeFsm ffsm) {
        System.out.print(ffsm.state.name() + "->");
    }

    abstract void next(FeeFsm ffsm);

}
