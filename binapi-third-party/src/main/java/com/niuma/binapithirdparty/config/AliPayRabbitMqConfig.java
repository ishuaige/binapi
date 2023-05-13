package com.niuma.binapithirdparty.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
public class AliPayRabbitMqConfig {


    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue alipayQueue(){

        return new Queue(ORDER_SUCCESS_QUEUE_NAME,true,false,false ,null);
    }


    /**
     * 交换机和普通队列绑定
     * @return
     */
    @Bean
    public Binding alipayBinding(){
        return new Binding(ORDER_SUCCESS_QUEUE_NAME, Binding.DestinationType.QUEUE,ORDER_EXCHANGE_NAME,ORDER_SUCCESS_EXCHANGE_ROUTING_KEY,null);
    }

}
