package com.iocoder.integral.common.time;


import com.iocoder.integral.common.time.impl.BudoTimeFormat;
import com.iocoder.integral.common.time.impl.BudoTimeSpan;
import com.iocoder.integral.common.time.impl.DateFormatFactory;
import com.iocoder.integral.common.time.impl.DefaultTimePoint;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * @author lmw
 */
public class Time {
    private static final String NUMBER_REGEX = "^[0-9]*$";

    /**
     * 默认支持的几种时间格式
     */
    public static final String[] DEFAULT_TIME_FORMATS = { //
            "yyyy-MM-dd HH:mm:ss", //
            "yyyy-MM-dd", //
            "HH:mm:ss", //
            "HH:mm:ss SSS", //
            "HH:mm:ss.SSS", //
            "MM-dd HH:mm", //
            "yyyyMMddHHmmss", //
            "yyyy/MM/dd HH:mm:ss", //
            "yyyy-MM-dd HH:mm:ss SSS", //
            "yyyy-MM-dd HH:mm:ss.SSS", //
            "yyyy-MM-dd HH:mm:ss.SS", //
            "yyyy-MM-dd HH:mm:ss.S", //
            "yyyyMMdd.HHmmss.SSS", // 20210623.200122.784
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" //
    };

    public static TimePoint when(Object value) {
        return when(value, DEFAULT_TIME_FORMATS);
    }

    public static TimePoint when(Object value, String[] timeFormats) {
        if (null == value) {
            return new DefaultTimePoint(null);
        }

        String str = (value + "").trim();
        if ("".equalsIgnoreCase(str) || "null".equalsIgnoreCase(str) || "undefined".equalsIgnoreCase(str)) {
            return new DefaultTimePoint(null);
        }

        if (value instanceof TimePoint) {
            return new DefaultTimePoint(((TimePoint) value).toDate());
        }

        if (value instanceof Date) {
            return new DefaultTimePoint((Date) value);
        }

        if (value instanceof Instant) {
            return when((Instant) value);
        }

        if (value instanceof LocalDate) {
            return when((LocalDate) value);
        }

        if (value instanceof LocalTime) {
            return when((LocalTime) value);
        }

        if (value instanceof LocalDateTime) {
            return when((LocalDateTime) value);
        }

        // 数字
        if (value instanceof Number) {
            return new DefaultTimePoint(new Date(((Number) value).longValue()));
        }

        // 字符串形式的数字
        if (value instanceof String && ((String) value).matches(NUMBER_REGEX)) {
            long parseLong = Long.parseLong((String) value);
            return new DefaultTimePoint(new Date(parseLong));
        }

        // 根据时间格式，解析字符串为时间
        for (String timeFormat : timeFormats) {
            String val = ((String) value).trim();

            if (timeFormat.replace("\'", "").length() != val.length()) {
                continue; // 长度相同才会 parse
            }

            try {
                TimePoint timePoint = Time.when(val, timeFormat);
                return timePoint;
            } catch (Throwable e) {
                // 解析失败，try掉，尝试下一个格式，性能问题
            }
        }

        throw new IllegalArgumentException("#227 Unsupported TIME_FORMAT, value = " + value + ", type = " + value.getClass());
    }

    /**
     * @param time   时间
     * @param format 格式
     */
    public static TimePoint when(String time, String format) {
        TimeFormat timeFormat = timeFormat(format);
        return when(time, timeFormat);
    }

    public static TimeSpan span(TimePoint from, TimePoint to) {
        return new BudoTimeSpan(from, to);
    }

    public static TimeSpan span(Long milliSeconds) {
        return new BudoTimeSpan(milliSeconds);
    }

    public static TimeFormat timeFormat(String pattern) {
        return new BudoTimeFormat(pattern);
    }

    public static TimeSpan span(Date from, Date to) {
        return new BudoTimeSpan(from, to);
    }

    /**
     * 返回服务器当前时间, 不同服务器的机器时间可能不同
     */
    public static TimePoint now() {
        return new DefaultTimePoint(new Date());
    }

    /**
     * 明天 的当前时间
     */
    public static TimePoint tomorrow() {
        return now().plusDay(1);
    }

    /**
     * 昨天 的当前时间
     */
    public static TimePoint yesterday() {
        return now().plusDay(-1);
    }

    /**
     * 将Date转为TimePoint
     *
     * @param date 传入空值时不会报错
     */
    public static TimePoint when(Date date) {
        return new DefaultTimePoint(date);
    }

    /**
     * milliSeconds转TimePoint
     */
    public static TimePoint when(Number milliSeconds) {
        if (null == milliSeconds) {
            return new DefaultTimePoint(null);
        }

        return new DefaultTimePoint(new Date(milliSeconds.longValue()));
    }

    /**
     * 指定时间字符串和解析格式，返回对应时间
     */
    public static TimePoint when(String value, DateFormat dateFormat) {
        if (null == value) {
            return new DefaultTimePoint(null);
        }

        try {
            synchronized (dateFormat) {
                Date date = dateFormat.parse(value);
                return new DefaultTimePoint(date);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 指定时间字符串和解析格式，返回对应时间
     */
    public static TimePoint when(String value, TimeFormat timeFormat) {
        Date date = timeFormat.parse(value);
        return new DefaultTimePoint(date);
    }

    /**
     * 返回时间解析格式
     */
    public static DateFormat format(String format) {
        return DateFormatFactory.getCurrent().getDateFormat(format);
    }
}