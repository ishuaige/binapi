package com.niuma.binapicommon.constant;

/**
 * @author niuma
 * @create 2023-04-28 22:05
 */
public interface RedisConstant {
    String SMS_PREFIX = "sms:";
    String SMS_BUCKET_PREFIX = SMS_PREFIX + "bucket:";
    String SMS_CODE_PREFIX = SMS_PREFIX + "code:";
    String SMS_MESSAGE_PREFIX = SMS_PREFIX + "mq:messageId:";
    String MQ_PRODUCER = SMS_PREFIX + "mq:producer:fail";

    String SEND_ORDER_PREFIX = "order:sendOrderNumInfo:";
    String ORDER_PAY_SUCCESS_INFO = "order:paySuccess:";
    String ORDER_PAY_RABBITMQ = "order:pay:rabbitMq:";
    String ALIPAY_TRADE_INFO = "alipay:tradeInfo:";
}
