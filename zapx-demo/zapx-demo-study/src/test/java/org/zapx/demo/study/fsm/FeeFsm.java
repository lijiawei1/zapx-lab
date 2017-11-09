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
    /**
     * 到达时间
     */
    final LocalDateTime const_dt_arrival;
    /**
     * 离开时间
     */
    final LocalDateTime const_dt_leave;
    /**
     * 总停留时长（分钟）
     */
    final long const_min_diff;

    /**
     * 每天最大费用
     */
    final long MAX_CHARGE_PER_DAY = 1200;
    /**
     * 超时费用单价
     */
    final long PRICE_PER_HOUR = 120;

    /**
     * 剩余停留时长
     */
    long next_min_left = 0L;
    /**
     * 计费超时时长（分钟）
     */
    long next_min_yc = 0L;
    /**
     * 计费超时时长（天）
     */
    long next_day_yc = 0L;
    /**
     * 累计费用
     */
    long next_amount = 0L;

    /**
     * 日间计费时间长度
     */
    final long MORNING_TIME_LONG = 16 * 60;
    /**
     * 夜间免费时长
     */
    final long EVENING_TIME_LONG = 8 * 60;


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

        //晚20时后进场的免费时长
        long min_until = const_dt_arrival.toLocalTime()
                .until(LocalTime.of(23, 59, 59), ChronoUnit.MINUTES) + 1 +
                8 * 60;

        if (next_min_left <= min_until) {
            //剩余停留时间在免费时长内，进入END状态
            return true;
        } else {
            //剩余更新停留时间，进入MOR_CHARGE状态
            next_min_left -= min_until;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public boolean isEndInEvening() {
        //剩余停留时间在时长内
        boolean end = next_min_left <= EVENING_TIME_LONG;
        //更新停留时间
        long min = Math.min(next_min_left, EVENING_TIME_LONG);
        next_min_left -= min;
        return end;
    }

    /**
     * 早间结束，免4小时
     *
     * @return
     */
    public boolean isEndInMorningStart() {

        //早8时后入场的计费时长
        long min_until = const_dt_arrival.toLocalTime()
                .until(LocalTime.of(23, 59, 0), ChronoUnit.MINUTES) + 1;

        //是否24时前结束
        boolean end = next_min_left <= min_until;
        long min = Math.min(next_min_left, min_until);

        //在早间结束，大于4小时计费
        if (min > 60 * 4) {
            //开始计费，更新剩余停留时间
            next_min_left -= min;
            //纳入计算的超时时长
            min -= 60 * 4;

            long amount = roundHour(min) * PRICE_PER_HOUR;
            if (amount < MAX_CHARGE_PER_DAY) {
                next_min_yc += min;
            } else {
                amount = MAX_CHARGE_PER_DAY;
                //若大于当天最大费用，将压车时长记录到天数
                next_day_yc++;
            }
            //累加费用
            next_amount += amount;
        }

        return end;
    }

    /**
     * 是否早间结束，8时开始计费
     *
     * @return
     */
    public boolean isEndInMorning() {

        boolean end = next_min_left <= MORNING_TIME_LONG;
        long min = Math.min(next_min_left, MORNING_TIME_LONG);
        long amount = Math.min(roundHour(min) * PRICE_PER_HOUR, MAX_CHARGE_PER_DAY);

        if (amount < MAX_CHARGE_PER_DAY) {
            next_min_yc += min;
        } else {
            next_day_yc++;
        }
        //更新剩余停留时长
        next_min_left -= min;
        //累加费用
        next_amount += amount;
        return end;
    }

    /**
     * 分钟舍入小时，30分钟以上当1小时
     * @param min
     * @return
     */
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
