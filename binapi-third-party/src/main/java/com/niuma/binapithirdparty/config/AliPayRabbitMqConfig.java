package com.niuma.binapithirdparty.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.niuma.binapicommon.constant.RabbitMqConstant.*;

/**
 * RabbitMQ配置 - 统一在backend模块创建
 * @author niumazlb
 */
@Slf4j
//@Configuration
@Deprecated
public class AliPayRabbitMqConfig {


    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue alipayQueue(){
        return QueueBuilder.durable(ORDER_SUCCESS_QUEUE_NAME).build();
    }


    /**
     * 交换机和普通队列绑定
     * @return
     */
    @Bean
    public Binding alipayBinding(){
        return new Binding(ORDER_SUCCESS_QUEUE_NAME, Binding.DestinationType.QUEUE, ORDER_EXCHANGE_TOPIC_NAME,ORDER_SUCCESS_EXCHANGE_ROUTING_KEY,null);
    }

}
