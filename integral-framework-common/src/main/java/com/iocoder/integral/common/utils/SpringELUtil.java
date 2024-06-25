package com.iocoder.integral.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SpringELUtil {
    public static final String START_TOKEN = "#{";
    public static final String END_TOKEN = "}";

    public static <T> T tryParseParameterValue(EvaluationContext ctx, ExpressionParser parser, Class<T> parameterType, String elValue) {
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
    public static <T> T parseParameterValue(EvaluationContext ctx, ExpressionParser parser, Class<T> parameterType, String elValue) {
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
    private static boolean isExpression(String elValue) {
        return elValue.startsWith(START_TOKEN) && elValue.endsWith(END_TOKEN);
    }

    /**
     * 判断表达式是否应该转换为JSON格式的字符串
     *
     * @param value 表达式的值
     * @return 如果表达式的类型不是字符串、数字、布尔型，则返回true，否则返回false
     */
    private static boolean shouldConvertToJson(Object value, Class<?> parameterType) {
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
    private static String evaluateNestedExpressions(EvaluationContext ctx, ExpressionParser parser, Class<?> parameterType, String elValue) {
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
    private static List<String> extractExpressions(String elValue) {
        List<String> expressions = new ArrayList<>();
        int startIndex = elValue.indexOf(SpringELUtil.START_TOKEN);
        while (startIndex >= 0) {
            int endIndex = elValue.indexOf(SpringELUtil.END_TOKEN, startIndex + SpringELUtil.START_TOKEN.length());
            if (endIndex < 0) {
                break;
            }
            String expression = elValue.substring(startIndex, endIndex + 1);
            expressions.add(expression);
            startIndex = elValue.indexOf(SpringELUtil.START_TOKEN, endIndex + 1);
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
    private static Object evaluateExpression(EvaluationContext ctx, ExpressionParser parser, String elValue) {
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
    public static String convertExpression(String elValue) {
        elValue = "#" + elValue.substring(SpringELUtil.START_TOKEN.length(), elValue.length() - 1);
        return elValue;
    }


}
