/*
 *   Copyright (c) 2022 Oray Inc. All rights reserverd.
 *
 *   No Part of this file may be reproduced, stored
 *   in a retrieval system, or transmitted, in any form, or by any means,
 *   electronic, mechanical, photocopying, recording, or otherwise,
 *   without the prior consent of Oray Inc.
 *
 *
 *   @author qiang.li
 *
 */

package com.iocoder.integral.messaging.aop;


import com.iocoder.integral.messaging.context.TransactionalMessageContext;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;

@Role(2)
@Order(Integer.MAX_VALUE)
@Slf4j
public class TransactionalMessageAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {
    private static final long serialVersionUID = 3973140764023872988L;

    private Pointcut pointcut = null;// new MethodOperationLogMetadataSourcePointcut();

    private final Advice advice = new MethodOperationLogInterceptor();

    private BeanFactory beanFactory;

    /**
     * Get the Pointcut that drives this advisor.
     */
    @Override
    public Pointcut getPointcut() {
        if (pointcut == null) {
            AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();
            aspectJExpressionPointcut.setExpression("@annotation(com.iocoder.integral.messaging.annotations.TransactionalMessage");
            pointcut = aspectJExpressionPointcut;
        }
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


    class MethodOperationLogInterceptor implements MethodInterceptor {

        private String startToken = "#{";
        private String endToken = "}";
        /**
         * 在 jdk 8 以前java源码编译后通过反射是无法获得形参名的，在Java 8及之后，编译的时候可以通过-parameters 为反射生成元信息，可以获取到方法的参数名，但这个行为默认是关闭的,
         * 我们平常用Spring的时候也没有开启, 那在没有开启和jdk 8 以前那在Springmvc项目中是如何获得方法的形参的呢，这个形参可关系到根据参数名称依赖注入和Controller中参数绑定。
         * 答案是 通过字节码技术，将该类的class文件读进来，通过class的存储信息来得到的 ，就是LocalVariableTableParameterNameDiscoverer的inspectClass方法得到形参名
         */
        private LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {

            if (log.isTraceEnabled()) {
                log.trace("堆栈信息查看", new Exception());
            }
            try {

                boolean inited = TransactionalMessageContext.init();
                if (log.isDebugEnabled()) {
                    log.debug("#128 invoke TransactionalMessageC inited={},  method={},this={}", inited, invocation.getMethod(), this);
                }
                return invocation.proceed();
            } finally {
                TransactionalMessageContext.clear();
            }


        }
    }


}