package com.iocoder.integral.operationlog.core.annotations.method.configuration;

import com.iocoder.integral.operationlog.core.aop.OperationLogAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

public class MethodOperationLogAdvisorRegistrar implements
        ImportBeanDefinitionRegistrar {
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {

        BeanDefinitionBuilder advisor = BeanDefinitionBuilder
                .rootBeanDefinition(OperationLogAdvisor.class);
        advisor.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        MultiValueMap<String, Object> attributes = importingClassMetadata.getAllAnnotationAttributes(EnableGlobalMethodOperationLog.class.getName());
        assert attributes != null;
        Integer order = (Integer) attributes.getFirst("order");
        if (order != null) {
            advisor.addPropertyValue("order", order);
        }

        registry.registerBeanDefinition("operationLogAdvisor",
                advisor.getBeanDefinition());
    }
}
