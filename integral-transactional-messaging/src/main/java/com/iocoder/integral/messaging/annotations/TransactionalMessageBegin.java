package com.iocoder.integral.messaging.annotations;

public @interface TransactionalMessageBegin {
    /**
     * 业务类型
     *
     * @return
     */
    String businessType() default "";

    /**
     * 最大重试次数
     *
     * @return
     */

    int maxRetryCount() default 10;

    /**
     * 超时重试时间 s
     *
     * @return
     */

    int timeOutRetry() default 10;

    /**
     * 消息id 控制幂等
     *
     * @return
     */

    String messageId() default "";


}
