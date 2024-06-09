package com.iocoder.integral.common.time.impl;


import com.iocoder.integral.common.time.Time;
import com.iocoder.integral.common.time.TimeFormat;
import com.iocoder.integral.common.time.TimePoint;
import com.iocoder.integral.common.time.TimeSpan;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 表示一个时间点
 *
 * @author limingwei2
 */
public abstract class AbstractTimePoint implements Serializable, TimePoint {
    private static final long serialVersionUID = -4618733947086651402L;

    private static final int YEAR = Calendar.YEAR, // 年
            MONTH = Calendar.MONTH, // 月
            DAY_OF_MONTH = Calendar.DAY_OF_MONTH, // 日
            DAY_OF_WEEK = Calendar.DAY_OF_WEEK, // 星期几
            WEEK_OF_MONTH = Calendar.WEEK_OF_MONTH, // 当月第几周
            HOUR_OF_DAY = Calendar.HOUR_OF_DAY, // 时
            MINUTE = Calendar.MINUTE, // 分
            SECOND = Calendar.SECOND, // 秒
            MILLISECOND = Calendar.MILLISECOND; // 毫秒

    private Date date;

    protected abstract AbstractTimePoint timeFieldSet(Integer field, Number value);

    protected abstract AbstractTimePoint timeFieldPlus(Integer field, Number amount);

    /**
     * Getter Setter便于序列化
     */
    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * 默认构造器 序列化需要
     */
    public AbstractTimePoint() {
    }

    public AbstractTimePoint(Date date) {
        this.date = date;
    }

    public Date toDate() {
        if (null == this.getDate()) {
            return null;
        }

        return new Date(this.getDate().getTime());
    }

    public Calendar toCalendar() {
        if (null == this.getDate()) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.getDate());
        return calendar;
    }

    public Timestamp toTimestamp() {
        if (null == this.getDate()) {
            return null;
        }

        return new Timestamp(this.toMilliSeconds());
    }

    public java.sql.Time toSqlTime() {
        if (null == this.getDate()) {
            return null;
        }

        return new java.sql.Time(this.toMilliSeconds());
    }

    public java.sql.Date toSqlDate() {
        if (null == this.getDate()) {
            return null;
        }

        return new java.sql.Date(this.toMilliSeconds());
    }

    /**
     * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public Long toMilliSeconds() {
        if (null == this.getDate()) {
            return null;
        }

        return this.getDate().getTime();
    }

    /**
     * Returns the number of seconds since 1970-1-1, 00:00:00 GMT
     */
    public Long toSeconds() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toMilliSeconds() / 1000;
    }

    protected AbstractTimePoint calendarFieldMinus(Integer field, Number amount) {
        if (null == amount) {
            return this;
        }

        return this.timeFieldPlus(field, -amount.intValue());
    }

    public AbstractTimePoint setYear(Number year) {
        return this.timeFieldSet(YEAR, year);
    }

    /**
     * @param month 一月是0 二月是1
     */
    public AbstractTimePoint setMonth(Number month) {
        return this.timeFieldSet(MONTH, month);
    }

    /**
     * DAY_OF_MONTH
     */
    public AbstractTimePoint setDay(Number day) {
        return this.timeFieldSet(DAY_OF_MONTH, day);
    }

    public AbstractTimePoint setDayOfWeek(Number dayOfWeek) {
        return this.timeFieldSet(DAY_OF_WEEK, dayOfWeek);
    }

    public AbstractTimePoint setWeekOfMonth(Number weekOfMonth) {
        return this.timeFieldSet(WEEK_OF_MONTH, weekOfMonth);
    }

    public AbstractTimePoint setHour(Number hour) {
        return this.timeFieldSet(HOUR_OF_DAY, hour);
    }

    public AbstractTimePoint setMinute(Number minute) {
        return this.timeFieldSet(MINUTE, minute);
    }

    public AbstractTimePoint setSecond(Number second) {
        return this.timeFieldSet(SECOND, second);
    }

    public AbstractTimePoint setMilliSecond(Number milliSecond) {
        return this.timeFieldSet(MILLISECOND, milliSecond);
    }

    /**
     * 更新日期为本月最后一天
     */
    public AbstractTimePoint setDayMax() {
        return this.setDay(this.getDayMax());
    }

    /**
     * 更新时间为 23, 59, 59, 999
     */
    public AbstractTimePoint setTimeMax() {
        return this.setTime(23, 59, 59, 999);
    }

    /**
     * DAY_OF_MONTH 返回当月最大日期 30/31/29/28
     */
    public Integer getDayMax() {
        return this.toCalendar().getActualMaximum(DAY_OF_MONTH);
    }

    public Integer getYear() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toCalendar().get(YEAR);
    }

    public Integer getMonth() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toCalendar().get(MONTH);
    }

    /**
     * DAY_OF_MONTH
     */
    public Integer getDay() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toCalendar().get(DAY_OF_MONTH);
    }

    public Integer getHour() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toCalendar().get(HOUR_OF_DAY);
    }

    public Integer getMinute() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toCalendar().get(MINUTE);
    }

    public Integer getSecond() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toCalendar().get(SECOND);
    }

    public Integer getMilliSecond() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toCalendar().get(MILLISECOND);
    }

    public Integer getDayOfWeek() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toCalendar().get(DAY_OF_WEEK);
    }

    public Integer getWeekOfMonth() {
        if (null == this.getDate()) {
            return null;
        }

        return this.toCalendar().get(WEEK_OF_MONTH);
    }

    // 加法
    public AbstractTimePoint plusYear(Number year) {
        return this.timeFieldPlus(YEAR, year);
    }

    public AbstractTimePoint plusMonth(Number month) {
        return this.timeFieldPlus(MONTH, month);
    }

    /**
     * DAY_OF_MONTH
     */
    public AbstractTimePoint plusDay(Number day) {
        return this.timeFieldPlus(DAY_OF_MONTH, day);
    }

    public AbstractTimePoint plusHour(Number hour) {
        return this.timeFieldPlus(HOUR_OF_DAY, hour);
    }

    public AbstractTimePoint plusMinute(Number minute) {
        return this.timeFieldPlus(MINUTE, minute);
    }

    public AbstractTimePoint plusSecond(Number second) {
        return this.timeFieldPlus(SECOND, second);
    }

    public AbstractTimePoint plusMilliSecond(Number milliSecond) {
        return this.timeFieldPlus(MILLISECOND, milliSecond);
    }

    // 减法
    public AbstractTimePoint minusYear(Number year) {
        return this.calendarFieldMinus(YEAR, year);
    }

    public AbstractTimePoint minusMonth(Number month) {
        return this.calendarFieldMinus(MONTH, month);
    }

    /**
     * DAY_OF_MONTH
     */
    public AbstractTimePoint minusDay(Number day) {
        return this.calendarFieldMinus(DAY_OF_MONTH, day);
    }

    public AbstractTimePoint minusHour(Number hour) {
        return this.calendarFieldMinus(HOUR_OF_DAY, hour);
    }

    public AbstractTimePoint minusMinute(Number minute) {
        return this.calendarFieldMinus(MINUTE, minute);
    }

    public AbstractTimePoint minusSecond(Number second) {
        return this.calendarFieldMinus(SECOND, second);
    }

    public AbstractTimePoint minusMilliSecond(Number milliSecond) {
        return this.calendarFieldMinus(MILLISECOND, milliSecond);
    }

    /**
     * 设置时间部分
     */
    public AbstractTimePoint setTime(Number hour, Number minute, Number second, Number milliSecond) {
        return this.setHour(hour).setMinute(minute).setSecond(second).setMilliSecond(milliSecond);
    }

    public AbstractTimePoint setTime(Number hour, Number minute, Number second) {
        return this.setHour(hour).setMinute(minute).setSecond(second);
    }

    /**
     * 设置日期部分
     *
     * @param year
     * @param month 一月是0，二月是1
     * @param day
     */
    public AbstractTimePoint setDate(Number year, Number month, Number day) {
        return this.setYear(year).setMonth(month).setDay(day);
    }

    /**
     * 将日期部分设置为 1970-0-1，即 70年1月1号 后返回
     */
    public AbstractTimePoint timePart() {
        return this.setDate(1970, 0, 1);
    }

    /**
     * 是周六
     */
    public Boolean isSaturday() {
        if (null == this.getDate()) {
            return null;
        }

        return this.getDayOfWeek().intValue() == Calendar.SATURDAY;
    }

    /**
     * 是周日
     */
    public Boolean isSunday() {
        if (null == this.getDate()) {
            return null;
        }

        return this.getDayOfWeek().intValue() == Calendar.SUNDAY;
    }

    // 时间比较

    /**
     * 二者中有空值时返回false
     */
    public Boolean isBefore(TimePoint that) {
        if (null == this.getDate() || null == that || null == that.getDate()) {
            return false;
        }

        return this.toMilliSeconds() < that.toMilliSeconds();
    }

    @Override
    public Boolean isBeforeNow() {
        return this.isBefore(Time.now());
    }

    /**
     * 二者中有空值时返回false
     */
    public Boolean isNotBefore(TimePoint that) {
        if (null == this.getDate() || null == that || null == that.getDate()) {
            return false;
        }

        return !this.isBefore(that);
    }

    /**
     * 二者中有空值时返回false
     */
    public Boolean isAfter(TimePoint that) {
        if (null == this.getDate() || null == that || null == that.getDate()) {
            return false;
        }

        return this.toMilliSeconds() > that.toMilliSeconds();
    }

    @Override
    public Boolean isAfter(Date that) {
        return isAfter(Time.when(that));
    }

    /**
     * 二者中有空值时返回false
     */
    public Boolean isNotAfter(TimePoint that) {
        if (null == this.getDate() || null == that || null == that.getDate()) {
            return false;
        }

        return !this.isAfter(that);
    }

    /**
     * 二者中有空值时返回false
     */
    public Boolean isEqual(TimePoint that) {
        if (null == this.getDate() || null == that || null == that.getDate()) {
            return false;
        }

        return this.toMilliSeconds().longValue() == that.toMilliSeconds().longValue();
    }

    /**
     * 二者中有空值时返回false
     */
    public Boolean isNotEqual(AbstractTimePoint that) {
        if (null == this.getDate() || null == that || null == that.getDate()) {
            return false;
        }

        return !this.isEqual(that);
    }

    @Override
    public Boolean isBetween(Date min, Date max) {
        return isBetween(Time.when(min), Time.when(max));
    }

    /**
     * 三者中有空值时返回false 与两端相等返回true
     */
    public Boolean isBetween(TimePoint min, TimePoint max) {
        if (null == this.getDate() || null == min || null == min.getDate() || null == max || null == max.getDate()) {
            return false;
        }

        return this.isNotBefore(min) && this.isNotAfter(max);
    }

    /**
     * 三者中有空值时返回false 与两端相等返回false
     */
    public Boolean isNotBetween(AbstractTimePoint min, AbstractTimePoint max) {
        if (null == this.getDate() || null == min || null == min.getDate() || null == max || null == max.getDate()) {
            return false;
        }

        return !this.isBetween(min, max);
    }

    /**
     * 三者中有空值时返回false 与两端相等返回false
     */
    public Boolean isBetwixt(TimePoint min, TimePoint max) {
        if (null == this.getDate() || null == min || null == min.getDate() || null == max || null == max.getDate()) {
            return false;
        }

        return this.isAfter(min) && this.isBefore(max);
    }

    /**
     * 三者中有空值时返回false 与两端相等返回true
     */
    public Boolean isNotBetwixt(AbstractTimePoint min, AbstractTimePoint max) {
        if (null == this.getDate() || null == min || null == min.getDate() || null == max || null == max.getDate()) {
            return false;
        }

        return !this.isBetwixt(min, max);
    }

    // 时间范围工具

    /**
     * @param that 应该是偏后的时间,否则会会返回负数
     */
    public BudoTimeSpan until(TimePoint that) {
        return new BudoTimeSpan(this, that);
    }

    /**
     * @param that 应该是偏后的时间,否则会会返回负数
     */
    public BudoTimeSpan until(Date that) {
        return this.until(Time.when(that));
    }

    /**
     * @param that 一定会返回正数
     */
    public TimeSpan between(TimePoint that) {
        if (this.isAfter(that)) {
            return new BudoTimeSpan(that, this);
        } else {
            return new BudoTimeSpan(this, that);
        }
    }

    /**
     * @param that 一定会返回正数
     */
    public TimeSpan between(Date that) {
        return this.between(Time.when(that));
    }

    /**
     * 将时间部分设置为 0:0:0.0 后返回
     */
    public TimePoint datePart() {
        return this.setTimeMin();
    }

    /**
     * @see #datePart()
     */
    @Override
    public TimePoint setTimeMin() {
        return this.setTime(0, 0, 0, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toType(Class<T> type) {
        if (type.equals(Timestamp.class)) {
            return (T) toTimestamp();
        }

        if (type.equals(Date.class)) {
            return (T) toDate();
        }

        if (type.equals(java.sql.Date.class)) {
            return (T) toSqlDate();
        }

        if (type.equals(java.sql.Time.class)) {
            return (T) toSqlTime();
        }

        throw new RuntimeException("#558 toType, this=" + this + ", type=" + type);
    }

    // 格式化工具
    public String toString(DateFormat dateFormat) {
        if (null == this.getDate()) {
            return null;
        }

        synchronized (dateFormat) {
            return dateFormat.format(this.getDate());
        }
    }

    public String toString(TimeFormat timeFormat) {
        return timeFormat.format(this.getDate());
    }

    /**
     * yyyy-MM-dd
     */
    public String toString(String pattern) {
        DateFormat dateFormat = DateFormatFactory.getCurrent().getDateFormat(pattern);
        return this.toString(dateFormat);
    }

    public String toString(String pattern, Locale locale) {
        DateFormat dateFormat = DateFormatFactory.getCurrent().getDateFormat(pattern, locale);
        return this.toString(dateFormat);
    }

    public String toString() {
        return super.toString() + ", " + this.toString("yyyy-MM-dd HH:mm:ss.SSS");
    }
}