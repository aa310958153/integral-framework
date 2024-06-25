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

package com.iocoder.integral.operationlog.core.aop
        ;


import com.alibaba.fastjson.JSON;
import com.iocoder.integral.common.utils.SpringELUtil;
import com.iocoder.integral.operationlog.core.OperationLogMeta;
import com.iocoder.integral.operationlog.core.OperationLogProcessHandle;
import com.iocoder.integral.operationlog.core.annotations.OperationLog;
import com.iocoder.integral.operationlog.core.context.OperationLogContext;
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
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Role(2)
@Order(Integer.MAX_VALUE)
@Slf4j
public class OperationLogAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {
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
            // 不自己造轮子了
            /**
             * 介个更强大可以配置各种表达式
             *  <aop:config>
             *         <aop:pointcut id="capacityAccumulationMethodPointcut"
             *                       expression="(execution(public * com.ewei..service..TicketServiceImpl.save(..)))
             *             || (execution(* com.ewei..service..QuestionServiceImpl.save*(..)))
             *             || (execution(* com.ewei..service..AttachmentServiceImpl.save(..)))
             *             || (execution(* com.ewei..service..AttachmentServiceImpl.updateFileSizeByContentUrl(..)))
             *             || (execution(* com.ewei.module..service..ChatServiceImpl.save(..)))"/>
             *         <aop:advisor advice-ref="capacityAccumulationMethodInterceptor"
             *                      pointcut-ref="capacityAccumulationMethodPointcut"/>
             *     </aop:config>
             */
            AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();
            aspectJExpressionPointcut.setExpression("@annotation(com.iocoder.integral.operationlog.core.annotations.OperationLog)");
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


        /**
         * 在 jdk 8 以前java源码编译后通过反射是无法获得形参名的，在Java 8及之后，编译的时候可以通过-parameters 为反射生成元信息，可以获取到方法的参数名，但这个行为默认是关闭的,
         * 我们平常用Spring的时候也没有开启, 那在没有开启和jdk 8 以前那在Springmvc项目中是如何获得方法的形参的呢，这个形参可关系到根据参数名称依赖注入和Controller中参数绑定。
         * 答案是 通过字节码技术，将该类的class文件读进来，通过class的存储信息来得到的 ，就是LocalVariableTableParameterNameDiscoverer的inspectClass方法得到形参名
         */
        private LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
        private static final String INNER_VARIABLE_PREFIX = "innerVariable_";

        private static final String INNER_VARIABLE_USERID = "userId";
        private static final String INNER_VARIABLE_IP = "ip";
        private static final String INNER_VARIABLE_PROVIDER_ID = "providerId";


        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {

            if (log.isTraceEnabled()) {
                log.trace("堆栈信息查看", new Exception());
            }
            try {

                boolean inited = OperationLogContext.init();
                if (log.isDebugEnabled()) {
                    log.debug("#128 invoke OperationLogAdvisor inited={},  method={},this={}", inited, invocation.getMethod(), this);
                }
                Object result = invocation.proceed();
                // 防止重复配置切面
                if (inited) {
                    handleOperationLog(invocation, result);
                }
                return result;
            } finally {
                OperationLogContext.clear();
            }

        }

        private void handleOperationLog(MethodInvocation invocation, Object result) {
            try {
                // 开始准备表达式运行环境
                EvaluationContext ctx = new StandardEvaluationContext();
                // 设置内置变量
                setInnerVariable(ctx, invocation);
                // 预知参数变量到表表达式运行环境
                setCtxVariableByMethodParameter(invocation, ctx);
                // 设置上线变量
                setContextParameter(ctx, result);
                // 设置result变量
                ctx.setVariable("result", result);
                // 表达式解析器
                ExpressionParser parser = new SpelExpressionParser();
                OperationLogProcessHandle operationLogProcessHandle = beanFactory.getBean(OperationLogProcessHandle.class);
                if (operationLogProcessHandle == null) {
                    log.error("#139 handleOperationLog 没有日志埋点处理器[OperationLogProcessHandle] 不执行埋点");
                    return;
                }
                // 使用spring 工具类 内部会有缓存
                OperationLog operationLog = AnnotationUtils.findAnnotation(invocation.getMethod(), OperationLog.class);
                String bathTarget = operationLog.batchTarget();
                List<OperationLogMeta> operationLogMetas = new ArrayList<>();
                List<?> batchItems = null;
                // 批量埋点记录日志
                if (!"".equals(bathTarget)) {
                    batchItems = SpringELUtil.tryParseParameterValue(ctx, parser, List.class, operationLog.batchTarget());
                    if (batchItems != null && batchItems.size() > 0) {
                        for (Object batchItem : batchItems) {
                            ctx.setVariable("item", batchItem);
                            operationLogMetas.add(buildOperationLogMeta(ctx, parser, operationLog));
                        }
                    }
                } else {
                    // 单个埋点记录日志
                    operationLogMetas.add(buildOperationLogMeta(ctx, parser, operationLog));
                }
                if (operationLogMetas.size() <= 0) {
                    log.info("#157 没有数据跳过埋点 invocation={}", invocation);
                    return;
                }
                if (!isNotesNeedToBeProcessed(invocation, ctx, parser, operationLog)) {
                    log.info("#179 条件表达式校验不通过跳过method={}，Expression={}", invocation.getMethod(), operationLog.notesExpression());
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug("#124 日志元数据operationLogProcessHandle={},operationLogMeta={}", operationLogProcessHandle, JSON.toJSONString(operationLogMetas));
                }
                operationLogProcessHandle.process(operationLogMetas);
            } catch (Exception e) {
                log.error("#131 日志切面记录日志失败method,targetClass={}", invocation.getMethod().getName(), e);
            }
        }

        /**
         * 判断是否需要处理埋点日志
         *
         * @param invocation   方法调用信息
         * @param ctx          表达式运行上下文
         * @param parser       表达式解析器
         * @param operationLog 操作日志信息
         * @return true-需要处理，false-不需要处理
         */
        private boolean isNotesNeedToBeProcessed(MethodInvocation invocation, EvaluationContext ctx, ExpressionParser parser, OperationLog operationLog) {
            /**
             * 前置判断表达式
             * 如:方法返回true才记录埋点
             */
            if (StringUtils.isEmpty(operationLog.notesExpression())) {
                return true;
            }
            Boolean notes = SpringELUtil.parseParameterValue(ctx, parser, Boolean.class, operationLog.notesExpression());
            if (notes == null || !notes) {
                log.info("#149 条件表达式校验不通过跳过notes={},method={}，Expression={}", notes, invocation.getMethod(), operationLog.notesExpression());
                return false;
            }
            return true;
        }

        private String adaptInnerVariableExpression(String el, String innerVariableName) {
            if (!StringUtils.isEmpty(el)) {
                return el;
            }
            return String.format("%s%s%s%s", SpringELUtil.START_TOKEN, INNER_VARIABLE_PREFIX, innerVariableName, SpringELUtil.END_TOKEN);
        }

        private OperationLogMeta buildOperationLogMeta(EvaluationContext ctx, ExpressionParser parser, OperationLog operationLog) {
            OperationLogMeta operationLogMeta = new OperationLogMeta();
            operationLogMeta.setAfterValue(SpringELUtil.tryParseParameterValue(ctx, parser, String.class, operationLog.afterValue()));
            operationLogMeta.setBeforeValue(SpringELUtil.tryParseParameterValue(ctx, parser, String.class, operationLog.beforeValue()));
            operationLogMeta.setDescription(SpringELUtil.tryParseParameterValue(ctx, parser, String.class, operationLog.description()));
            operationLogMeta.setIp(SpringELUtil.tryParseParameterValue(ctx, parser, String.class, adaptInnerVariableExpression(operationLog.ip(), INNER_VARIABLE_IP)));
            operationLogMeta.setOperationType(SpringELUtil.tryParseParameterValue(ctx, parser, String.class, operationLog.operationType()));
            operationLogMeta.setTargetType(SpringELUtil.tryParseParameterValue(ctx, parser, String.class, operationLog.targetType()));
            operationLogMeta.setTargetId(SpringELUtil.tryParseParameterValue(ctx, parser, String.class, operationLog.targetId()));
            operationLogMeta.setUserId(SpringELUtil.tryParseParameterValue(ctx, parser, Integer.class, adaptInnerVariableExpression(operationLog.userId(), INNER_VARIABLE_USERID)));
            return operationLogMeta;
        }


        /**
         * 预置默认内置变量
         *
         * @param ctx
         */
        public void setInnerVariable(EvaluationContext ctx, MethodInvocation invocation) {
            Map<String, Object> innerVariables = new HashMap<>();
//            innerVariables.put(INNER_VARIABLE_IP, PrincipalContexts.getClientIp());
//            innerVariables.put(INNER_VARIABLE_USERID, PrincipalContexts.getUserId());
//            innerVariables.put(INNER_VARIABLE_PROVIDER_ID, PrincipalContexts.getProviderId());
            // ... 添加更多的内部变量
            //liqiangtodo 待放端点
            for (Map.Entry<String, Object> entry : innerVariables.entrySet()) {
                String variableName = INNER_VARIABLE_PREFIX + entry.getKey();
                if (log.isDebugEnabled()) {
                    log.debug("#308 setInnerVariable variableName={},value={}", variableName, entry.getValue());
                }
                ctx.setVariable(variableName, entry.getValue());
            }
        }

        /**
         * 预置OperationLogContext 为el变量
         *
         * @param ctx
         * @param result
         */
        private void setContextParameter(EvaluationContext ctx, Object result) {
            ctx.setVariable("context", OperationLogContext.get());
            Map<String, Object> contextMap = OperationLogContext.get();
            if (contextMap != null && contextMap.size() > 0) {
                contextMap.keySet().forEach(key -> {
                    if (log.isDebugEnabled()) {
                        log.debug("#402  ctx.setVariable name={},value={}", key, contextMap.get(key));
                    }
                    ctx.setVariable(key, contextMap.get(key));
                });
            }
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


}