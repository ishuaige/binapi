package com.niuma.binapi.config.mq;


import com.niuma.binapicommon.constant.RabbitMqConstant;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Mq相关的参数配置
 *
 * @author niuma
 */
public class MqProperties {

    /**
     * 虚拟目录
     */
    public interface VHOST {

        // TODO 这里应该做环境隔离，但因为项目为个人开发，也方便其他开发者能够更方便的启动项目，这里虚拟目录就用默认的 “/”
        String BINAPI = "";


        /**
         * 其他系统所使用的虚拟目录
         */
        List<String> OTHER_SYSTEM_VHOSTS = Arrays.asList(
                BINAPI
        );
    }

    /**
     * 连接池
     */
    public interface ConnectionFactory {

        String CONNECTION_FACTORY_SUFFIX = "MqConnectionFactory";

        String BINAPI = VHOST.BINAPI + CONNECTION_FACTORY_SUFFIX;

        static String getConnectionFactory(String vhost) {
            return vhost + CONNECTION_FACTORY_SUFFIX;
        }
    }


    /**
     * 监听器
     */
    public interface Listener {

        String LISTENER_SUFFIX = "MqListenerFactory";

        // 该参数用在注解上@RabbitListener的containerFactory属性
        String VHOST_BINAPI = VHOST.BINAPI + LISTENER_SUFFIX;

        static String getListener(String vhost) {
            return vhost + LISTENER_SUFFIX;
        }
    }

    /**
     * 交换机
     */
    public interface ExchangeInfo {

        List<Exchange> EXCHANGE_LIST = Arrays.asList(
                /** 主题交换器 */
                ExchangeBuilder.topicExchange(RabbitMqConstant.SMS_EXCHANGE_TOPIC_NAME)
                        .durable(true).build(),
                ExchangeBuilder.topicExchange(RabbitMqConstant.ORDER_EXCHANGE_TOPIC_NAME)
                        .durable(true).build()
                /** 广播交换机  */
        );

    }

    /**
     * 队列
     */
    public interface QueueInfo {

        List<Queue> QUEUE_LIST = Arrays.asList(
                /** 短信 */
                QueueBuilder.durable(RabbitMqConstant.SMS_QUEUE_NAME)
                        .deadLetterExchange(RabbitMqConstant.SMS_EXCHANGE_TOPIC_NAME)
                        .deadLetterRoutingKey(RabbitMqConstant.SMS_DELAY_EXCHANGE_ROUTING_KEY)
                        .ttl(60000).build(),
                QueueBuilder.durable(RabbitMqConstant.SMS_DELAY_QUEUE_NAME).build(),
                /** 订单 */
                QueueBuilder.durable(RabbitMqConstant.ORDER_SEND_QUEUE_NAME)
                        .deadLetterExchange(RabbitMqConstant.ORDER_EXCHANGE_TOPIC_NAME)
                        .deadLetterRoutingKey(RabbitMqConstant.ORDER_TIMEOUT_EXCHANGE_ROUTING_KEY)
                        .ttl(30 * 60000).build(),
                QueueBuilder.durable(RabbitMqConstant.ORDER_TIMEOUT_QUEUE_NAME).build(),
                QueueBuilder.durable(RabbitMqConstant.ORDER_SUCCESS_QUEUE_NAME).build()
        );
    }

    /**
     * 消息监听时，队列绑定关系
     */
    @lombok.Value
    public static class QueueBinding {

        public String exchange;
        public String queue;
        public String bindingKey;

        public QueueBinding(String exchange, String queue) {
            this.exchange = exchange;
            this.queue = queue;
            this.bindingKey = "#";
        }

        public QueueBinding(String exchange, String queue, String bindingKey) {
            this.exchange = exchange;
            this.queue = queue;
            this.bindingKey = bindingKey;
        }
    }

}
