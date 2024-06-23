package com.iocoder.integral.messaging.annotations.configuration;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

public class GlobalMethodTransactionalMessageSelector implements ImportSelector {
    GlobalMethodTransactionalMessageSelector() {
    }

    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> classNames = new ArrayList<>(1);
        classNames.add(MethodTransactionalMessageSelectorAdvisorRegistrar.class.getName());
        return (String[]) classNames.toArray(new String[0]);
    }
}
