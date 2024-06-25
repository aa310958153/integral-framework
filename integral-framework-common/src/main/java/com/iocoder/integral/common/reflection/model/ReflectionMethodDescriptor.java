/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iocoder.integral.common.reflection.model;


import com.iocoder.integral.messaging.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

@Slf4j
public class ReflectionMethodDescriptor implements MethodDescriptor {

    private final ConcurrentMap<String, Object> attributeMap = new ConcurrentHashMap<>();
    public final String methodName;
    private final String[] compatibleParamSignatures;
    private final Class<?>[] parameterClasses;
    private final Type[] genericParameterTypes;
    private final Class<?> returnClass;
    private final Type[] returnTypes;
    private final String paramDesc;
    private final Method method;

    public ReflectionMethodDescriptor(Method method) {
        this.method = method;
        this.methodName = method.getName();
        this.parameterClasses = method.getParameterTypes();
        this.returnClass = method.getReturnType();
        this.genericParameterTypes = method.getGenericParameterTypes();
        Type[] returnTypesResult;
        try {
            returnTypesResult = ReflectUtils.getReturnTypes(method);
        } catch (Throwable throwable) {
            log.error(
                    "fail to get return types. Method name: " + methodName + " Declaring class:"
                            + method.getDeclaringClass().getName(),
                    throwable);
            returnTypesResult = new Type[]{returnClass, returnClass};
        }
        this.returnTypes = returnTypesResult;
        this.paramDesc = ReflectUtils.getDesc(parameterClasses);
        this.compatibleParamSignatures =
                Stream.of(parameterClasses).map(Class::getName).toArray(String[]::new);
    }


    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String[] getCompatibleParamSignatures() {
        return compatibleParamSignatures;
    }

    @Override
    public Class<?>[] getParameterClasses() {
        return parameterClasses;
    }

    @Override
    public String getParamDesc() {
        return paramDesc;
    }

    @Override
    public Class<?> getReturnClass() {
        return returnClass;
    }

    @Override
    public Type[] getReturnTypes() {
        return returnTypes;
    }


    public void addAttribute(String key, Object value) {
        this.attributeMap.put(key, value);
    }

    public Object getAttribute(String key) {
        return this.attributeMap.get(key);
    }


}
