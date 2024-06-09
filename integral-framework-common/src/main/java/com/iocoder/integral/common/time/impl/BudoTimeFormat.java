package com.iocoder.integral.common.time.impl;

import com.iocoder.integral.common.time.TimeFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * 时间格式化规则 SimpleDateFormat 的线程安全的代理
 *
 * @author lmw
 */
@Getter
@Setter
public class BudoTimeFormat implements Serializable, TimeFormat {
    private static final long serialVersionUID = -6504816189392736328L;

    private String pattern;

    public BudoTimeFormat(String pattern) {
        this.pattern = pattern;
    }

    public Date parse(String value) {
        if (null == value || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim()) || "undefined".equalsIgnoreCase(value.trim())) {
            return null;
        }

        try {
            DateFormatFactory dateFormatFactory = DateFormatFactory.getCurrent();
            DateFormat dateFormat = dateFormatFactory.getDateFormat(this.getPattern());
            return dateFormat.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String format(Date date) {
        DateFormatFactory dateFormatFactory = DateFormatFactory.getCurrent();
        DateFormat dateFormat = dateFormatFactory.getDateFormat(this.getPattern());
        return dateFormat.format(date);
    }
}