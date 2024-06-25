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


import com.iocoder.integral.common.reflection.model.ReflectionMethodDescriptor;
import com.iocoder.integral.common.utils.SpringELUtil;
import com.iocoder.integral.messaging.annotations.TransactionalMessageBegin;
import com.iocoder.integral.messaging.context.TransactionalMessageContext;
import com.iocoder.integral.messaging.meta.TransactionMessage;
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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Role(2)
@Slf4j
public class TransactionalMessageAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {
    private static final long serialVersionUID = 3973140764023872988L;

    private Pointcut pointcut = null;// new MethodOperationLogMetadataSourcePointcut();

    private final Advice advice = new MethodOperationLogInterceptor();

    private BeanFactory beanFactory;

    /**
     * 在 jdk 8 以前java源码编译后通过反射是无法获得形参名的，在Java 8及之后，编译的时候可以通过-parameters 为反射生成元信息，可以获取到方法的参数名，但这个行为默认是关闭的,
     * 我们平常用Spring的时候也没有开启, 那在没有开启和jdk 8 以前那在Springmvc项目中是如何获得方法的形参的呢，这个形参可关系到根据参数名称依赖注入和Controller中参数绑定。
     * 答案是 通过字节码技术，将该类的class文件读进来，通过class的存储信息来得到的 ，就是LocalVariableTableParameterNameDiscoverer的inspectClass方法得到形参名
     */
    private LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    /**
     * Get the Pointcut that drives this advisor.
     */
    @Override
    public Pointcut getPointcut() {

        if (pointcut == null) {
            AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();
            aspectJExpressionPointcut.setExpression(String.format("@annotation(%s", TransactionalMessageBegin.class.getName()));
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

    public TransactionMessage buildTransactionMessage(MethodInvocation invocation) {
        // 开始准备表达式运行环境
        EvaluationContext ctx = new StandardEvaluationContext();
        setCtxVariableByMethodParameter(invocation, ctx);
        // 使用spring 工具类 内部会有缓存
        TransactionalMessageBegin transactionalMessageAnnotation = AnnotationUtils.findAnnotation(invocation.getMethod(), TransactionalMessageBegin.class);
        if (transactionalMessageAnnotation == null) {
            return null;
        }
        TransactionMessage transactionMessage = new TransactionMessage();
        // 表达式解析器
        ExpressionParser parser = new SpelExpressionParser();
        transactionMessage.setBusinessType(SpringELUtil.tryParseParameterValue(ctx, parser, String.class, transactionMessage.getBusinessType()));
        transactionMessage.setMessageId(SpringELUtil.tryParseParameterValue(ctx, parser, String.class, transactionMessage.getMessageId()));
        transactionMessage.setMaxRetryCount(transactionMessage.getMaxRetryCount());
        transactionMessage.setTimeOutRetry(transactionMessage.getTimeOutRetry());
        ReflectionMethodDescriptor reflectionMethodDescriptor = new ReflectionMethodDescriptor(invocation.getMethod());
        return transactionMessage;
    }

    /**
     * 预置方法参数为el 内置变量
     *
     * @param invocation
     * @param ctx
     */
    private void setCtxVariableByMethodParameter(MethodInvocation invocation, EvaluationContext ctx) {
        Method method = invocation.getMethod();
        Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length <= 0) {
            return;
        }
        // 内部有缓存
        String[] names = localVariableTableParameterNameDiscoverer.getParameterNames(method);
        Object[] arguments = invocation.getArguments();
        for (int i = 0; i < names.length; i++) {
            if (log.isDebugEnabled()) {
                log.debug("#111  ctx.setVariable name={},value={}", names[i], arguments[i]);
            }
            ctx.setVariable(names[i], arguments[i]);
        }
    }
}
