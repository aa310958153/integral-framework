package com.iocoder.integral.operationlog.core.annotations.method.configuration;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

public class GlobalMethodOperationLogSelector implements ImportSelector {
    GlobalMethodOperationLogSelector() {
    }

    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> classNames = new ArrayList<>(1);
        classNames.add(MethodOperationLogAdvisorRegistrar.class.getName());
        return (String[]) classNames.toArray(new String[0]);
    }
}
