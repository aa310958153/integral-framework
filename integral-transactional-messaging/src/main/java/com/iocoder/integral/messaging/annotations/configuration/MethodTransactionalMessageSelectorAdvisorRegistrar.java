package com.iocoder.integral.messaging.annotations.configuration;

import com.iocoder.integral.messaging.aop.TransactionalMessageAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

public class MethodTransactionalMessageSelectorAdvisorRegistrar implements
        ImportBeanDefinitionRegistrar {
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {

        BeanDefinitionBuilder advisor = BeanDefinitionBuilder
                .rootBeanDefinition(TransactionalMessageAdvisor.class);
        advisor.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        MultiValueMap<String, Object> attributes = importingClassMetadata.getAllAnnotationAttributes(EnableGlobalMethodTransactionalMessage.class.getName());
        assert attributes != null;
        Integer order = (Integer) attributes.getFirst("order");
        if (order != null) {
            advisor.addPropertyValue("order", order);
        }

        registry.registerBeanDefinition("operationLogAdvisor",
                advisor.getBeanDefinition());
    }
}
