package com.iocoder.integral.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 *
 */
@AllArgsConstructor
@Getter
public enum DeletedType implements EnumValue<Integer> {
    //正常
    NORMAL(0, "正常"),
    //删除
    DELETED(1, "删除");
    private Integer code;
    private String name;

    public final static Convert CONVERT = new Convert();

    @Override
    public Integer getValue() {
        return code;
    }

    public static class Convert extends AbstractEnumConvert<DeletedType> {

    }
}
