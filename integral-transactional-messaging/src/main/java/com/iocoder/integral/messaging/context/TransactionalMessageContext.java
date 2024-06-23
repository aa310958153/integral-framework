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

package com.iocoder.integral.messaging.context;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liqiang
 * @date 2023/2/27
 */
@Slf4j
public class TransactionalMessageContext {
    private static final ThreadLocal<Map<String, Object>> PARAMS = new ThreadLocal<>();
    private static final long serialVersionUID = 6197002188998908797L;

    public static boolean init() {
        if (PARAMS.get() != null) {
            log.warn("#33 init ThreadLocal 已经初始化");
            return false;
        }
        PARAMS.set(new HashMap<>());
        return true;
    }

    public static void add(String key, Object value) {
        // 防止内存泄露 必须由切面初始化 因为释放也在切面 OperationLogAdvisor
        if (PARAMS.get() == null) {
            log.warn("#34 add ThreadLocal 未初始化key={}", key);
            return;
        }
        PARAMS.get().put(key, value);
    }

    public static Map<String, Object> get() {
        if (PARAMS.get() == null) {
            return null;
        }
        // 避免外部修改clone一份
        return new HashMap<>(PARAMS.get());
    }

    public static void remove(String key) {
        if (PARAMS.get() == null) {
            log.warn("#50  remove ThreadLocal 未初始化 key={}", key);
            return;
        }
        PARAMS.get().remove(key);
    }

    public static void clear() {
        PARAMS.remove();
    }
}