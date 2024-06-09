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

package com.iocoder.integral.operationlog.core.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liqiang
 * @date 2023/2/27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface OperationLog {
    /**
     *  是否执行前置判断
     * #{result>0}
     */
    String notesExpression() default "";

    /**
     * #{sensitiveWord.id==null?'新增':'更新'}了敏感词【#{sensitiveWordEntity.text}】
     * @return
     */
    String description() default "";


    /**
     * 操作 如:删除、修改、禁用、解绑等
     * @return
     */
    String operationType() default "";

    /**
     * 操作对象 如:敏感词、用户、角色、商品、活动
     * @return
     */
    String targetType() default "";

    /**
     * 操作对象id
     * 如果设置了batchTarget 则#{item.id}" 可获取到具体对象
     * @return
     */
    String targetId() default "";

    /**
     * 针对批量写入的集合
     * "#{context[items]}",
     * @return
     */
    String batchTarget() default "";


    /**
     * 修改前值
     * @return
     */
    String afterValue() default "";

    /**
     * 修改后值
     * @return
     */

    String beforeValue() default "";

    /**
     * 操作人ip
     * @return
     */
    String ip() default "";

    /**
     * 操作人用户id
     * @return
     */
    String userId() default "";

}