package com.iocoder.integral.common.time.impl;

import com.iocoder.integral.common.time.Time;
import com.iocoder.integral.common.time.TimePoint;
import com.iocoder.integral.common.time.TimeSpan;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Date;

/**
 * 表示一个时间段
 *
 * @author limingwei
 */
@Slf4j
public class BudoTimeSpan implements Serializable, TimeSpan {
    private static final long serialVersionUID = 4355045459032295931L;

    /**
     * 开始
     */
    private TimePoint from;

    /**
     * 结束
     */
    private TimePoint to;

    /**
     * 长度
     */
    private Long milliSeconds;

    public BudoTimeSpan() {
    }

    /**
     * 制定时间段长度
     */
    public BudoTimeSpan(Long milliSeconds) {
        this.milliSeconds = milliSeconds;
    }

    /**
     * 指定时间段起止时间
     */
    public BudoTimeSpan(TimePoint from, TimePoint to) {
        this.from = from;
        this.to = to;
    }

    /**
     * 指定时间段起止时间
     */
    public BudoTimeSpan(Date from, Date to) {
        this(Time.when(from), Time.when(to));
    }

    /**
     * 时间段转换为毫秒数
     */
    @Override
    public Long toMilliSeconds() {
        if (null != this.milliSeconds) {
            return this.milliSeconds;
        }

        if (null == this.from || null == this.to) {
            log.debug("#73 toMilliSeconds, this={}", this);
            return null;
        }

        Long fromMilliSeconds = from.toMilliSeconds();
        Long toMilliSeconds = to.toMilliSeconds();
        if (null == fromMilliSeconds || null == toMilliSeconds) {
            log.debug("#80 toMilliSeconds, this={}", this);
            return null;
        }

        this.milliSeconds = toMilliSeconds - fromMilliSeconds;

        return this.milliSeconds;
    }

    /**
     * 转为秒数
     */
    @Override
    public Long toSeconds() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toMilliSeconds() / 1000;
    }

    /**
     * 转为分钟数
     */
    @Override
    public Long toMinutes() {
        if (null == this.toMilliSeconds()) {
            log.debug("#94 toMinutes, this={}", this);
            return null;
        }

        return this.toSeconds() / 60;
    }

    /**
     * 转为小时数
     */
    @Override
    public Long toHours() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toMinutes() / 60;
    }

    /**
     * 转为天数
     */
    @Override
    public Long toDays() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toHours() / 24;
    }

    /**
     * 到底是否应按30天来，二月份怎么办
     */
    @Override
    public Long toMonths() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toDays() / 30;
    }

    @Override
    public Long toYears() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toMonths() / 12;
    }

    @Override
    public Long remainderMonths() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toMonths() - this.toYears() * 12;
    }

    @Override
    public Long remainderDays() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toDays() - this.toMonths() * 30;
    }

    @Override
    public Long remainderHours() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toHours() - this.toDays() * 24;
    }

    @Override
    public Long remainderMinutes() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toMinutes() - this.toHours() * 60;
    }

    @Override
    public Long remainderSeconds() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toSeconds() - this.toMinutes() * 60;
    }

    @Override
    public Long remainderMilliSeconds() {
        if (null == this.toMilliSeconds()) {
            return null;
        }

        return this.toMilliSeconds() - this.toSeconds() * 1000;
    }

    public String toString() {
        String string = super.toString();

        if (null == this.milliSeconds) {
            string += ", from=" + this.from;
            string += ", to=" + this.to;
        } else {
            string += ", " + this.milliSeconds + " milliSeconds";
        }

        if (null != this.toMilliSeconds()) {
            string += ", " + this.toDays() + "天 ";
            string += this.remainderHours() + "小时 ";
            string += this.remainderMinutes() + " 分钟";
        }

        return string;
    }
}
