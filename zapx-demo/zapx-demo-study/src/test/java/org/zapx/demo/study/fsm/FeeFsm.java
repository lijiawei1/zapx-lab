package org.zapx.demo.study.fsm;

import java.math.BigDecimal;
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
    /**
     * 压车天数
     */
    long next_day_yc = 0L;
    long next_amount = 0L;

    /**
     * 工作时间长度
     */
    final long MORNING_TIME_LONG = LocalTime.of(8, 0).until(LocalTime.of(20, 0), ChronoUnit.MINUTES);


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
        return t_arrival.isAfter(LocalTime.of(19, 59, 59))
                || t_arrival.isBefore(LocalTime.of(8, 0, 1));
    }

    /**
     *
     * @return
     */
    public boolean isEndInEveningStart() {

        long min_until = const_dt_arrival.toLocalTime()
                .until(LocalTime.of(23, 59, 59), ChronoUnit.MINUTES) + 1 +
                8 * 60;

        if (next_min_left <= min_until) {
            return true;
        } else {
            next_min_left -= min_until;
        }
        return false;
    }

    public boolean isEndInEvening() {
        boolean end = next_min_left <= MORNING_TIME_LONG;
        long min = Math.min(next_min_left, MORNING_TIME_LONG);
        next_min_left -= min;
        return end;
    }

    /**
     * 早间结束，免4小时
     *
     * @return
     */
    public boolean isEndInMorningStart() {

        long min_until = const_dt_arrival.toLocalTime()
                .until(LocalTime.of(20, 0, 0), ChronoUnit.MINUTES);

        boolean end = next_min_left <= min_until;
        long min = Math.min(next_min_left, min_until);

        //在早间结束
        if (min > 60 * 4) {
            //小于4小时，免费
            //开始计费
            next_min_left -= min;
            min -= 60 * 4;

            long amount = roundHour(min) * PRICE_PER_HOUR;
            if (amount < MAX_CHARGE_PER_DAY) {
                next_min_yc += min;
            } else {
                amount = MAX_CHARGE_PER_DAY;
                //压车天数
                next_day_yc++;
            }

            next_amount += amount;
        }

        return end;
    }

    public boolean isEndInMorning() {

        boolean end = next_min_left <= MORNING_TIME_LONG;
        long min = Math.min(next_min_left, MORNING_TIME_LONG);
        long amount = Math.min(roundHour(min) * PRICE_PER_HOUR, MAX_CHARGE_PER_DAY);

        if (amount < MAX_CHARGE_PER_DAY) {
            next_min_yc += min;
        } else {
            next_day_yc++;
        }
        next_min_left -= min;
        next_amount += amount;
        return end;
    }

    private long roundHour(long min) {
        return new BigDecimal(((double)min) / 60.0).setScale(0, BigDecimal.ROUND_HALF_DOWN).longValue();
    }

    public FeeFsm start() {
        this.state.next(this);
        return this;
    }

    public String toString() {
        return String.format("[压车: %d天 %d分, 费用: %d, 剩余分钟: %d]", next_day_yc, next_min_yc, next_amount, next_min_left);
    }

    public long getNext_amount() {
        return next_amount;
    }

}
