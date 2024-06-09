package com.iocoder.integral.common.time;

/**
 * @author lmw
 */
public interface TimeSpan {
    Long toYears();

    Long toMonths();

    /**
     * 减去整年的部分 余下的月数
     */
    Long remainderMonths();

    /**
     * 时间转换为天数
     */
    Long toDays();

    Long remainderDays();

    /**
     * 转为分钟数 ，可用 intValue() 舍弃余量
     */
    Long toMinutes();

    Long remainderMinutes();

    /**
     * 转为秒数 ，可用 intValue() 舍弃余量
     */
    Long toSeconds();

    Long remainderSeconds();

    /**
     * 时间段转换为毫秒数
     */
    Long toMilliSeconds();

    Long remainderMilliSeconds();

    /**
     * 转为小时数 ，可用 intValue() 舍弃余量
     */
    Long toHours();

    Long remainderHours();
}