package com.iocoder.integral.common.time;

import org.junit.Test;

public class TimeTest {
    @Test
    public void testTryConvert() {
        System.out.println(Time.now().toString(Time.DEFAULT_TIME_FORMATS[0]));
        System.out.println(Time.now().plusDay(-1).toString(Time.DEFAULT_TIME_FORMATS[0]));
        System.out.println(Time.when(Time.now()).setDayMax().toString(Time.DEFAULT_TIME_FORMATS[0]));
        System.out.println(Time.when(Time.now()).setTimeMax().toString(Time.DEFAULT_TIME_FORMATS[0]));
        System.out.println(Time.when(Time.now()).setTimeMin().toString(Time.DEFAULT_TIME_FORMATS[0]));
    }
}
