package com.iocoder.integral.operationlog.core.annotations.method.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({GlobalMethodOperationLogSelector.class})
@Configuration
public @interface EnableGlobalMethodOperationLog  {
    int order() default Ordered.LOWEST_PRECEDENCE;
}
