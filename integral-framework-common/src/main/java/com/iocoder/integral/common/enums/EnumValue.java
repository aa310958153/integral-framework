package com.iocoder.integral.common.enums;

/**
 * @Author qiang.li
 * @Date 2021/11/30 10:58 上午
 * @Description 枚举映射可能不一定是根据枚举name 也可能是根据具体属性 增加此接口
 */
public interface EnumValue<T> {
    T getValue();
}
