package com.niuma.binapiorder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.niuma.binapicommon.constant.RabbitMqConstant.*;

/**
 * RabbitMQ配置
 * @author niumazlb
 */
@Slf4j
@Configuration
public class OrderRabbitMqConfig {


    /**
     * 普通队列
     * @return
     */
    @Bean("ORDER_QUEUE")
    public Queue orderQueue(){
        Map<String, Object> arguments = new HashMap<>();
        //声明死信队列和交换机消息，过期时间：30分钟
        arguments.put("x-dead-letter-exchange", ORDER_EXCHANGE_NAME);
        arguments.put("x-dead-letter-routing-key", ORDER_TIMEOUT_EXCHANGE_ROUTING_KEY);
        arguments.put("x-message-ttl", 30*60000);
       // arguments.put("x-message-ttl", 60000);// 这里测试1分钟
        return new Queue(ORDER_SEND_EXCHANGE_ROUTING_KEY,true,false,false ,arguments);
    }

    /**
     * 死信队列：消息重试三次后放入死信队列
     * @return
     */
    @Bean("ORDER_DEAD_LETTER")
    public Queue orderDeadLetter(){
        return new Queue(ORDER_TIMEOUT_QUEUE_NAME, true, false, false);
    }

    /**
     * 主题交换机
     * @return
     */
    @Bean("ORDER_EXCHANGE")
    public Exchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE_NAME, true, false);
    }


    /**
     * 交换机和普通队列绑定
     * @return
     */
    @Bean
    public Binding orderBinding(@Qualifier("ORDER_QUEUE") Queue orderQueue,@Qualifier("ORDER_EXCHANGE") Exchange orderExchange){
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ORDER_SEND_EXCHANGE_ROUTING_KEY).noargs();
//        return new Binding(ORDER_SEND_QUEUE_NAME, Binding.DestinationType.QUEUE,ORDER_EXCHANGE_NAME,ORDER_SEND_EXCHANGE_ROUTING_KEY,null);

    }

    /**
     * 交换机和死信队列绑定
     * @return
     */
    @Bean
    public Binding orderDelayBinding(@Qualifier("ORDER_DEAD_LETTER") Queue orderDeadLetter,@Qualifier("ORDER_EXCHANGE") Exchange orderExchange){
        return BindingBuilder.bind(orderDeadLetter).to(orderExchange).with(ORDER_TIMEOUT_EXCHANGE_ROUTING_KEY).noargs();
//        return new Binding(ORDER_TIMEOUT_QUEUE_NAME, Binding.DestinationType.QUEUE,ORDER_EXCHANGE_NAME,ORDER_TIMEOUT_EXCHANGE_ROUTING_KEY,null);
    }


}
