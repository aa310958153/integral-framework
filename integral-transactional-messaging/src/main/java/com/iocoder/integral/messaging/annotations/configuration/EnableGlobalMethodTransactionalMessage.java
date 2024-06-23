package com.iocoder.integral.messaging.annotations.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({GlobalMethodTransactionalMessageSelector.class})
@Configuration
public @interface EnableGlobalMethodTransactionalMessage {
    int order() default Ordered.LOWEST_PRECEDENCE;
}
