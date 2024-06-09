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
import org.springframework.expression.Expression;
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

        private String startToken = "#{";
        private String endToken = "}";
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
                OperationLogMeta operationLogMeta = null;
                if (operationLogProcessHandle == null) {
                    log.error("#139 handleOperationLog 没有日志埋点处理器[OperationLogProcessHandle] 不执行埋点");
                    return;
                }
                // 使用spring 工具类 内部会有缓存
                OperationLog operationLog = AnnotationUtils.findAnnotation(invocation.getMethod(), OperationLog.class);
                operationLogMeta = new OperationLogMeta();
                String bathTarget = operationLog.batchTarget();
                List<OperationLogMeta> operationLogMetas = new ArrayList<>();
                List<?> batchItems = null;
                // 批量埋点记录日志
                if (!"".equals(bathTarget)) {
                    batchItems = tryParseParameterValue(ctx, parser, List.class, operationLog.batchTarget());
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
                    log.debug("#124 日志元数据operationLogProcessHandle={},operationLogMeta={}", operationLogProcessHandle, JSON.toJSONString(operationLogMeta));
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
            Boolean notes = parseParameterValue(ctx, parser, Boolean.class, operationLog.notesExpression());
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
            return String.format("%s%s%s%s", startToken, INNER_VARIABLE_PREFIX, innerVariableName, endToken);
        }

        private OperationLogMeta buildOperationLogMeta(EvaluationContext ctx, ExpressionParser parser, OperationLog operationLog) {
            OperationLogMeta operationLogMeta = new OperationLogMeta();
            operationLogMeta.setAfterValue(tryParseParameterValue(ctx, parser, String.class, operationLog.afterValue()));
            operationLogMeta.setBeforeValue(tryParseParameterValue(ctx, parser, String.class, operationLog.beforeValue()));
            operationLogMeta.setDescription(tryParseParameterValue(ctx, parser, String.class, operationLog.description()));
            operationLogMeta.setIp(tryParseParameterValue(ctx, parser, String.class, adaptInnerVariableExpression(operationLog.ip(), INNER_VARIABLE_IP)));
            operationLogMeta.setOperationType(tryParseParameterValue(ctx, parser, String.class, operationLog.operationType()));
            operationLogMeta.setTargetType(tryParseParameterValue(ctx, parser, String.class, operationLog.targetType()));
            operationLogMeta.setTargetId(tryParseParameterValue(ctx, parser, String.class, operationLog.targetId()));
            operationLogMeta.setUserId(tryParseParameterValue(ctx, parser, Integer.class, adaptInnerVariableExpression(operationLog.userId(), INNER_VARIABLE_USERID)));
            return operationLogMeta;
        }

        public <T> T tryParseParameterValue(EvaluationContext ctx, ExpressionParser parser, Class<T> parameterType, String elValue) {
            try {
                return parseParameterValue(ctx, parser, parameterType, elValue);
            } catch (Exception e) {
                log.error("#216 解析value出错 elValue={}", elValue, e);
                return null;
            }
        }

        /**
         * 从参数值中解析出相应的值
         *
         * @param ctx           解析表达式的上下文
         * @param parser        表达式解析器
         * @param parameterType 参数类型
         * @param elValue       参数值
         * @return 参数解析后的值
         */
        public <T> T parseParameterValue(EvaluationContext ctx, ExpressionParser parser, Class<T> parameterType, String elValue) {
            if (log.isDebugEnabled()) {
                log.debug("#257 parseParameterValue elValue={},parameterType={}", elValue, parameterType);
            }
            if (elValue == null || elValue.isEmpty()) {
                return null;
            }
            if (isExpression(elValue)) {
                Object value = evaluateExpression(ctx, parser, elValue);
                if (value == null) {
                    return null;
                }
                if (shouldConvertToJson(value, parameterType)) {
                    return (T) JSON.toJSONString(value);
                }
                if (parameterType.isAssignableFrom(String.class)) {
                    return (T) value.toString();
                }
                return (T) value;
            }
            String value = evaluateNestedExpressions(ctx, parser, parameterType, elValue);
            return (T) value;
        }

        /**
         * 判断参数值是否为表达式
         *
         * @param elValue 参数值
         * @return 如果参数值是表达式则返回true，否则返回false
         */
        private boolean isExpression(String elValue) {
            return elValue.startsWith(startToken) && elValue.endsWith(endToken);
        }

        /**
         * 判断表达式是否应该转换为JSON格式的字符串
         *
         * @param value 表达式的值
         * @return 如果表达式的类型不是字符串、数字、布尔型，则返回true，否则返回false
         */
        private boolean shouldConvertToJson(Object value, Class<?> parameterType) {
            return !(value instanceof String) && !(value instanceof Number) && !(value instanceof Boolean) && parameterType.isAssignableFrom(String.class);
        }

        /**
         * 对带有嵌套表达式的参数值进行解析
         * 修改了用户#{userName}的性别为:#{sex}
         * ->
         * 修改了用户张三的性别为:男
         *
         * @param ctx           解析表达式的上下文
         * @param parser        表达式解析器
         * @param parameterType 参数类型
         * @param elValue       参数值
         * @return 参数解析后的值
         */
        private String evaluateNestedExpressions(EvaluationContext ctx, ExpressionParser parser, Class<?> parameterType, String elValue) {
            List<String> expressions = extractExpressions(elValue);
            for (String expression : expressions) {
                Object value = evaluateExpression(ctx, parser, expression);
                String strValue = (value == null ? "" : value.toString());
                elValue = elValue.replace(expression, strValue);
            }

            if (parameterType.isAssignableFrom(String.class)) {
                return elValue;
            }
            return null;
        }

        /**
         * 从带有嵌套表达式的参数值中提取出所有表达式
         * 如:修改了用户#{userName}的性别为:#{sex} ->["#{userName}","#{sex}"]
         *
         * @param elValue 带有嵌套表达式的参数值
         * @return 所有表达式的列表
         */
        private List<String> extractExpressions(String elValue) {
            List<String> expressions = new ArrayList<>();
            int startIndex = elValue.indexOf(startToken);
            while (startIndex >= 0) {
                int endIndex = elValue.indexOf(endToken, startIndex + startToken.length());
                if (endIndex < 0) {
                    break;
                }
                String expression = elValue.substring(startIndex, endIndex + 1);
                expressions.add(expression);
                startIndex = elValue.indexOf(startToken, endIndex + 1);
            }
            return expressions;
        }

        /**
         * 对表达式进行求值
         *
         * @param ctx     解析表达式的上下文
         * @param parser  表达式解析器
         * @param elValue 表达式
         * @return 表达式的值
         */
        private Object evaluateExpression(EvaluationContext ctx, ExpressionParser parser, String elValue) {
            elValue = convertExpression(elValue);
            Expression expression = parser.parseExpression(elValue);
            return expression.getValue(ctx);
        }

        /**
         * 转换为标准el #{userId} to  #userId
         *
         * @param elValue
         * @return
         */
        public String convertExpression(String elValue) {
            elValue = "#" + elValue.substring(startToken.length(), elValue.length() - 1);
            return elValue;
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