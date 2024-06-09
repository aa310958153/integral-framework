package com.iocoder.integral.common.time;

import java.util.Date;

/**
 * @author lmw
 */
public interface TimeFormat {
    Date parse(String value);

    String format(Date date);
}