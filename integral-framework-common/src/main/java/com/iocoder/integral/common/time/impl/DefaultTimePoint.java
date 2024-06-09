package com.iocoder.integral.common.time.impl;

import com.iocoder.integral.common.time.TimeFormat;

import java.util.Calendar;
import java.util.Date;

/**
 * @author lmw
 */
public class DefaultTimePoint extends AbstractTimePoint {
    private static final long serialVersionUID = 8227187159610051289L;

    public DefaultTimePoint() {
    }

    public DefaultTimePoint(Date date) {
        super(date);
    }

    /**
     * 时间计算，采用Calendar实现
     */
    protected AbstractTimePoint timeFieldPlus(Integer field, Number amount) {
        if (null == this.getDate() || null == amount) {
            return new DefaultTimePoint(null);
        }

        Calendar calendar = this.toCalendar();
        calendar.add(field, amount.intValue());

        return new DefaultTimePoint(calendar.getTime());
    }

    protected AbstractTimePoint timeFieldSet(Integer field, Number value) {
        if (null == this.getDate() || null == value) {
            return new DefaultTimePoint(null);
        }

        Calendar calendar = this.toCalendar();
        calendar.set(field, value.intValue());

        return new DefaultTimePoint(calendar.getTime());
    }

    @Override
    public String toString(TimeFormat timeFormat) {
        return null;
    }
}