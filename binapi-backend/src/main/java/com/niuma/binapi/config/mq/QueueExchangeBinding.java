package com.niuma.binapi.config.mq;

import com.niuma.binapicommon.constant.RabbitMqConstant;
import org.springframework.stereotype.Component;

import java.util.*;


/**
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
        Map<String, Object> arguments = new HashMap<>();
        //声明死信队列和交换机消息，过期时间：1分钟
        arguments.put("x-dead-letter-exchange", RabbitMqConstant.SMS_EXCHANGE_TOPIC_NAME);
        arguments.put("x-dead-letter-routing-key", RabbitMqConstant.SMS_DELAY_EXCHANGE_ROUTING_KEY);
        arguments.put("x-message-ttl", 60000);
        List<MqProperties.QueueBinding> list = Arrays.asList(
                new MqProperties.QueueBinding(RabbitMqConstant.SMS_EXCHANGE_TOPIC_NAME, RabbitMqConstant.SMS_QUEUE_NAME, RabbitMqConstant.SMS_EXCHANGE_ROUTING_KEY),
                new MqProperties.QueueBinding(RabbitMqConstant.SMS_EXCHANGE_TOPIC_NAME, RabbitMqConstant.SMS_DELAY_QUEUE_NAME, RabbitMqConstant.SMS_DELAY_EXCHANGE_ROUTING_KEY, new MqExchangeConfigPar(null, true, false, arguments))
        );
        return list;
    }
}
