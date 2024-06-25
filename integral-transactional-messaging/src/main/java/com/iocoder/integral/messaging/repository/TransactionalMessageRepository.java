package com.iocoder.integral.messaging.repository;

import com.iocoder.integral.messaging.meta.TransactionMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TransactionalMessageRepository {

    @Resource
    private JdbcTemplate jdbcTemplate;

    public void save(TransactionMessage transactionMessage) {
        String sql = "INSERT INTO transactional_messages (" +
                "business_type, message_id, process_remark, status, payload, retry_count, max_retry_count, " +
                "time_out_retry, callback_bean, callback_method, process_time, created_at, updated_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                transactionMessage.getBusinessType(),
                transactionMessage.getMessageId(),
                transactionMessage.getProcessRemark(),
                transactionMessage.getStatus(),
                transactionMessage.getPayload(),
                transactionMessage.getRetryCount(),
                transactionMessage.getMaxRetryCount(),
                transactionMessage.getTimeOutRetry(),
                transactionMessage.getCallbackBean(),
                transactionMessage.getCallbackMethod(),
                transactionMessage.getProcessTime(),
                transactionMessage.getCreatedAt(),
                transactionMessage.getUpdatedAt());
    }
}
