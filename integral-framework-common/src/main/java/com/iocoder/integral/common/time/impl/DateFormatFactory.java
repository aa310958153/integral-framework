package com.iocoder.integral.common.time.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author limingwei2
 */
public class DateFormatFactory {
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT+8");

    private static final ThreadLocal<DateFormatFactory> THREAD_LOCAL = new ThreadLocal<DateFormatFactory>();

    private Map<String, DateFormat> dateFormatMap = new ConcurrentHashMap<String, DateFormat>();

    private DateFormatFactory() {
    }

    /**
     * SimpleDateFormat不是线程安全的，这里会为每个线程保持一个DateFormatPoll，保证同一个DateFormat对象不会被多个线程使用
     */
    public static DateFormatFactory getCurrent() {
        DateFormatFactory copy = THREAD_LOCAL.get();
        if (null != copy) {
            return copy;
        }

        DateFormatFactory instance = new DateFormatFactory();
        THREAD_LOCAL.set(instance);
        return instance;
    }

    public DateFormat getDateFormat(String pattern) {
        return this.getDateFormat(pattern, Locale.getDefault());
    }

    public DateFormat getDateFormat(String pattern, Locale locale) {
        String cacheKey = pattern + locale;
        DateFormat dateFormat = dateFormatMap.get(cacheKey);
        if (null != dateFormat) {
            return dateFormat;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
        simpleDateFormat.setTimeZone(TIME_ZONE);
        dateFormatMap.put(cacheKey, simpleDateFormat);
        return simpleDateFormat;
    }
}
