package org.zapx.demo.study.fsm;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Created by Administrator on 2017/11/7 0007.
 */
public class FeeFsm {

    FeeState state = FeeState.INIT;

    /**
     * 初始状态
     */
    final LocalDateTime const_dt_arrival;
    final LocalDateTime const_dt_leave;
    final long const_min_diff;

    final long MAX_CHARGE_PER_DAY = 1200;
    final long PRICE_PER_HOUR = 120;

    /**
     * 进入下状态的变量
     */
    long next_min_left = 0L;
    long next_min_yc = 0L;
    long next_amount = 0L;

    /**
     *
     * @param const_text_arrival
     * @param const_text_leave
     */
    public FeeFsm(String const_text_arrival, String const_text_leave) {
        this.const_dt_arrival = LocalDateTime.parse(const_text_arrival, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.const_dt_leave = LocalDateTime.parse(const_text_leave, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.const_min_diff = this.const_dt_arrival.until(this.const_dt_leave, ChronoUnit.MINUTES);

        this.next_min_left = const_min_diff;
    }

    /**
     * 晚20点后入场
     * @return
     */
    public boolean isArrivalInEvening() {
        LocalTime t_arrival = const_dt_arrival.toLocalTime();
        return t_arrival.isAfter(LocalTime.of(20, 0))
                && t_arrival.isBefore(LocalTime.of(8, 0));
    }

    /**
     * 工作时间长度
     */
    final long MORNING_TIME_LONG = LocalTime.of(8, 0).until(LocalTime.of(20, 0), ChronoUnit.MINUTES);

    public boolean isEndInEvening() {

        long min_until = const_dt_arrival.toLocalTime()
                .until(LocalTime.of(24, 0), ChronoUnit.MINUTES) +
                8 * 60;

        if (next_min_left <= min_until) {
            return true;
        } else {
            next_min_left -= min_until;
        }
        return false;
    }

    public boolean isEndInMorning() {

        if (next_min_left <= MORNING_TIME_LONG) {

            //早上8点开始的时段
            if (next_min_yc == 0) {
                //早上8点开始的时间段开始是免4小时
                if (next_min_left > 60 * 4) {
                    next_min_yc += (next_min_left - 4 * 60);
                    //大于4小时
                    long round = Math.round(((double) next_min_yc) / 60.0);
                    next_amount += round * PRICE_PER_HOUR;
                }
            } else {
                next_min_yc += next_min_left;
                long round = Math.round(((double) next_min_left) / 60.0);
                //一天最大收费
                next_amount += Math.min(round * PRICE_PER_HOUR, MAX_CHARGE_PER_DAY);
            }
            return true;
        } else {
            //早间计费
            next_min_left -= MORNING_TIME_LONG;
            //整天时长
            next_amount += MAX_CHARGE_PER_DAY;
        }
        return false;
    }

    public void start() {
        this.state.next(this);
    }
}