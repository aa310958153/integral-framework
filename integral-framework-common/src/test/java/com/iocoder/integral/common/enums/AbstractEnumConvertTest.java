package com.iocoder.integral.common.enums;


import org.junit.Test;


public class AbstractEnumConvertTest {


    @Test
    public void testTryConvert() {
        final DeletedType deletedType = DeletedType.CONVERT.tryConvert(0);
        assert deletedType.getCode() == 0;

    }

}
