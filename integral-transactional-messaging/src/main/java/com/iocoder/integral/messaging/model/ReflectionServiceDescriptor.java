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
package com.iocoder.integral.messaging.model;


import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;

public class ReflectionServiceDescriptor implements ServiceDescriptor {
    private final String interfaceName;
    private final Class<?> serviceInterfaceClass;
    // to accelerate search
    private final Map<String, List<MethodDescriptor>> methods = new HashMap<>();
    private final Map<String, Map<String, MethodDescriptor>> descToMethods = new HashMap<>();


    public void addMethod(MethodDescriptor methodDescriptor) {
        methods.put(methodDescriptor.getMethodName(), Collections.singletonList(methodDescriptor));
    }

    public ReflectionServiceDescriptor(Class<?> interfaceClass) {
        this.serviceInterfaceClass = interfaceClass;
        this.interfaceName = interfaceClass.getName();
        initMethods();
    }


    private void initMethods() {
        Method[] methodsToExport = this.serviceInterfaceClass.getMethods();
        for (Method method : methodsToExport) {
            method.setAccessible(true);

            MethodDescriptor methodDescriptor = new ReflectionMethodDescriptor(method);

            List<MethodDescriptor> methodModels = methods.computeIfAbsent(method.getName(), (k) -> new ArrayList<>(1));
            methodModels.add(methodDescriptor);
        }

    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public Class<?> getServiceInterfaceClass() {
        return serviceInterfaceClass;
    }

    public Set<MethodDescriptor> getAllMethods() {
        Set<MethodDescriptor> methodModels = new HashSet<>();
        methods.forEach((k, v) -> methodModels.addAll(v));
        return methodModels;
    }

    /**
     * Does not use Optional as return type to avoid potential performance decrease.
     *
     * @param methodName
     * @param params
     * @return
     */
    public MethodDescriptor getMethod(String methodName, String params) {
        Map<String, MethodDescriptor> methods = descToMethods.get(methodName);
        if (!CollectionUtils.isEmpty(methods)) {
            return methods.get(params);
        }
        return null;
    }

    /**
     * Does not use Optional as return type to avoid potential performance decrease.
     *
     * @param methodName
     * @param paramTypes
     * @return
     */
    public MethodDescriptor getMethod(String methodName, Class<?>[] paramTypes) {
        List<MethodDescriptor> methodModels = methods.get(methodName);
        if (!CollectionUtils.isEmpty(methodModels)) {
            for (MethodDescriptor descriptor : methodModels) {
                if (Arrays.equals(paramTypes, descriptor.getParameterClasses())) {
                    return descriptor;
                }
            }
        }
        return null;
    }

    public List<MethodDescriptor> getMethods(String methodName) {
        return methods.get(methodName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReflectionServiceDescriptor that = (ReflectionServiceDescriptor) o;
        return Objects.equals(interfaceName, that.interfaceName)
                && Objects.equals(serviceInterfaceClass, that.serviceInterfaceClass)
                && Objects.equals(methods, that.methods)
                && Objects.equals(descToMethods, that.descToMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interfaceName, serviceInterfaceClass, methods, descToMethods);
    }
}
