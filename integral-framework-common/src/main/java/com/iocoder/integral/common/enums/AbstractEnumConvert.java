package com.iocoder.integral.common.enums;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName EnumConvert
 * @Author qiang.li
 * @Date 2021/11/29 6:35 下午
 * @Description 枚举转换父类 使用方法看main测试类
 */
@Slf4j
public abstract class AbstractEnumConvert<T> {
    private Class tClass;

    public AbstractEnumConvert() {
        this.tClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 因为是通过反射获取values遍历获取的。避免每次都反射获取对应方法的method元素数据缓存起来
     */
    private static Map<String, Method> reflectCache = new ConcurrentHashMap<>();


    public T tryConvert(Object convertValue) {
        try {
            return convert(convertValue);
        } catch (Exception e) {
            log.error("enum convert error str:{},convert:{}", convertValue, tClass);
            return null;
        }
    }

    public T convert(Object convertValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (convertValue == null) {
            return null;
        }
        //从缓存获取Method 如果缓存没有就初始化到缓存
        Method method = getMethod("values");
        T[] values = (T[]) method.invoke(null);
        if (values == null) {
            return null;
        }
        for (T item :
                values) {
            Object value = null;
            //针对部分枚举可能不是根据name映射 提供EnumValue接口自定义映射value
            if (item instanceof EnumValue) {
                value = ((EnumValue) item).getValue();
            } else {
                //默认根据枚举name
                value = item.toString();
            }
            if (value == null) {
                return null;
            }
            //根据value匹配convertValue映射枚举
            if (value.equals(convertValue)) {
                return item;
                //针对convertValue是字符串 value是int情况尝试做一次比较
            } else if (value.toString().equals(convertValue)) {
                return item;
            }
        }
        return null;
    }


    private Method getMethod(String methodName) throws NoSuchMethodException {
        //通过枚举class+method名字作为缓存key
        String key = tClass.getName() + "#" + methodName;
        //尝试从缓存获取，如果缓存没有则反射获取元数据 并存入缓存
        Method method = reflectCache.get(key);
        if (method == null) {
            method = tClass.getMethod(methodName);
            reflectCache.put(key, method);
        }
        return method;
    }


}
