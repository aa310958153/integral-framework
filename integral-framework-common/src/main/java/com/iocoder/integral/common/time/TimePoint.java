package com.iocoder.integral.common.time;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author lmw
 */
public interface TimePoint {
    /**
     * @param that 一定会返回正数
     */
    TimeSpan between(TimePoint that);

    /**
     * 将时间部分设置为 0:0:0.0 后返回
     */
    TimePoint datePart();

    /**
     * Getter Setter便于序列化
     */
    Date getDate();

    /**
     * DAY_OF_MONTH
     */
    Integer getDay();

    Integer getDayOfWeek();

    Integer getHour();

    Integer getMilliSecond();

    Integer getMinute();

    Integer getMonth();

    Integer getSecond();

    Integer getYear();

    /**
     * 二者中有空值时返回false
     */
    Boolean isAfter(TimePoint that);

    /**
     * 二者中有空值时返回false
     */
    Boolean isBefore(TimePoint that);

    /**
     * 三者中有空值时返回false 与两端相等返回true
     */
    Boolean isBetween(TimePoint min, TimePoint max);

    /**
     * 三者中有空值时返回false 与两端相等返回true
     */
    Boolean isBetween(Date min, Date max);

    /**
     * 三者中有空值时返回false 与两端相等返回false
     */
    Boolean isBetwixt(TimePoint min, TimePoint max);

    /**
     * 二者中有空值时返回false
     */
    Boolean isEqual(TimePoint that);

    /**
     * 是周六
     */
    Boolean isSaturday();

    /**
     * 是周日
     */
    Boolean isSunday();

    /**
     * DAY_OF_MONTH
     */
    TimePoint minusDay(Number day);

    TimePoint minusHour(Number hour);

    TimePoint minusMinute(Number minute);

    TimePoint minusMonth(Number month);

    TimePoint minusSecond(Number second);

    /**
     * DAY_OF_MONTH
     */
    TimePoint plusDay(Number day);

    TimePoint plusHour(Number hour);

    TimePoint plusMilliSecond(Number milliSecond);

    TimePoint plusMinute(Number minute);

    TimePoint plusMonth(Number month);

    TimePoint plusSecond(Number second);

    TimePoint plusYear(Number year);

    /**
     * 设置日期部分
     *
     * @param year
     * @param month 一月是0，二月是1
     * @param day
     */
    TimePoint setDate(Number year, Number month, Number day);

    /**
     * DAY_OF_MONTH
     */
    TimePoint setDay(Number day);

    TimePoint setHour(Number hour);

    TimePoint setMilliSecond(Number milliSecond);

    TimePoint setMinute(Number minute);

    TimePoint setSecond(Number second);

    TimePoint setTime(Number hour, Number minute, Number second);

    /**
     * 设置时间部分
     */
    TimePoint setTime(Number hour, Number minute, Number second, Number milliSecond);

    /**
     * 更新时间部分为 23, 59, 59, 999
     */
    TimePoint setTimeMax();

    /**
     * 更新时间部分为 0, 0, 0, 0
     */
    TimePoint setTimeMin();

    /**
     * 将日期部分设置为 1970-0-1，即 70年1月1号 后返回
     */
    TimePoint timePart();

    /**
     * 更新日期为本月最后一天
     */
    TimePoint setDayMax();

    Calendar toCalendar();

    Date toDate();

    /**
     * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
     */
    Long toMilliSeconds();

    /**
     * Returns the number of seconds since 1970-1-1, 00:00:00 GMT
     */
    Long toSeconds();

    java.sql.Date toSqlDate();

    java.sql.Time toSqlTime();

    String toString(TimeFormat timeFormat);

    /**
     * yyyy-MM-dd yyyy-MM-dd HH:mm:ss.SSS
     */
    String toString(String pattern);

    String toString(String pattern, Locale locale);

    Timestamp toTimestamp();

    Boolean isBeforeNow();

    Boolean isAfter(Date date);

    <T> T toType(Class<T> type);
}