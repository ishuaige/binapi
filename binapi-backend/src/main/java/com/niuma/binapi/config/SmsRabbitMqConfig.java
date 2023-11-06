package com.niuma.binapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.HashMap;

import static com.niuma.binapicommon.constant.RabbitMqConstant.*;

/**
 * RabbitMQ配置-已废弃，该方法在队列和交换机数量增加时，不便于管理和维护，并且采用的为延迟加载方式，在启动时不会自动创建队列，导致其他项目因为找不到队列而报错
 * @author niumazlb
 */
@Slf4j
//@Configuration
@Deprecated
public class SmsRabbitMqConfig {

    @Resource
    @Lazy
    RabbitAdmin rabbitAdmin;

    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue smsQueue(){
        //声明死信队列和交换机消息，过期时间：1分钟
        return QueueBuilder.durable(SMS_QUEUE_NAME)
                .deadLetterExchange(SMS_EXCHANGE_TOPIC_NAME)
                .deadLetterRoutingKey(SMS_DELAY_EXCHANGE_ROUTING_KEY)
                .ttl(60000).build();
    }

    /**
     * 死信队列：消息重试三次后放入死信队列
     * @return
     */
    @Bean
    public Queue smsDeadLetter(){
        return QueueBuilder.durable(SMS_DELAY_QUEUE_NAME).build();
    }

    /**
     * 主题交换机
     * @return
     */
    @Bean
    public Exchange smsExchange() {
        return ExchangeBuilder.topicExchange(SMS_EXCHANGE_TOPIC_NAME).durable(true).build();
    }


    /**
     * 交换机和普通队列绑定
     * @return
     */
    @Bean
    public Binding smsBinding(){
        return BindingBuilder.bind(smsQueue()).to(smsExchange()).with(SMS_EXCHANGE_ROUTING_KEY).and(new HashMap<>());
    }

    /**
     * 交换机和死信队列绑定
     * @return
     */
    @Bean
    public Binding smsDelayBinding(){
        return BindingBuilder.bind(smsDeadLetter()).to(smsExchange()).with(SMS_DELAY_EXCHANGE_ROUTING_KEY).and(new HashMap<>());
    }


    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }
    @Bean
    public void init(){
        // 使用这个方法它才会在项目启动时自动创建队列
        rabbitAdmin.declareExchange(smsExchange());
        rabbitAdmin.declareQueue(smsQueue());
        rabbitAdmin.declareQueue(smsDeadLetter());
    }

}
