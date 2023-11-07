package com.niuma.binapi.config.mq;

import com.niuma.binapicommon.constant.RabbitMqConstant;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * 队列交换机绑定关系
 * @author niuma
 */
@Component
public class QueueExchangeBinding {

    /** 队列-交换机，绑定关系 */
    private Map<String, List<MqProperties.QueueBinding>> queueExchangeBindingMap;

    public QueueExchangeBinding () {
    }

    /**
     * 获取绑定交换机和队列
     * @return
     */
    public synchronized Map<String, List<MqProperties.QueueBinding>> getQueueExchangeBindingMap() {
        if (queueExchangeBindingMap != null) {
            return queueExchangeBindingMap;
        }

        // 声明binapi这个vhost的监听队列，并绑定到交换机
        List<MqProperties.QueueBinding> binApi = getBinApiQueueBinding();

        // 聚合：vhost -> queueBinding
        Map<String, List<MqProperties.QueueBinding>> bindingMap = new HashMap<>();
        bindingMap.put(MqProperties.VHOST.BINAPI, binApi);
        queueExchangeBindingMap = Collections.unmodifiableMap(bindingMap);

        return queueExchangeBindingMap;
    }

    private List<MqProperties.QueueBinding> getBinApiQueueBinding(){
        return Arrays.asList(
                new MqProperties.QueueBinding(RabbitMqConstant.SMS_EXCHANGE_TOPIC_NAME, RabbitMqConstant.SMS_QUEUE_NAME, RabbitMqConstant.SMS_EXCHANGE_ROUTING_KEY),
                new MqProperties.QueueBinding(RabbitMqConstant.SMS_EXCHANGE_TOPIC_NAME, RabbitMqConstant.SMS_DELAY_QUEUE_NAME, RabbitMqConstant.SMS_DELAY_EXCHANGE_ROUTING_KEY),

                new MqProperties.QueueBinding(RabbitMqConstant.ORDER_EXCHANGE_TOPIC_NAME, RabbitMqConstant.ORDER_SEND_QUEUE_NAME, RabbitMqConstant.ORDER_SEND_EXCHANGE_ROUTING_KEY),
                new MqProperties.QueueBinding(RabbitMqConstant.ORDER_EXCHANGE_TOPIC_NAME, RabbitMqConstant.ORDER_TIMEOUT_QUEUE_NAME, RabbitMqConstant.ORDER_TIMEOUT_EXCHANGE_ROUTING_KEY),
                new MqProperties.QueueBinding(RabbitMqConstant.ORDER_EXCHANGE_TOPIC_NAME, RabbitMqConstant.ORDER_SUCCESS_QUEUE_NAME, RabbitMqConstant.ORDER_SUCCESS_EXCHANGE_ROUTING_KEY)
        );
    }
}
