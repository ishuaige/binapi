package com.niuma.binapi.config.mq;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rabbitmq.client.ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT;

/**
 * RabbitMq配置-新
 *
 * @author niuma
 */
@Component
public class RabbitMqConfig implements BeanFactoryAware {
    @Resource
    private QueueExchangeBinding queueExchangeBinding;

    /**
     * 默认的连接工厂
     *
     * @param rabbitProperties
     * @return
     */
    @Bean(MqProperties.ConnectionFactory.BINAPI)
    @Primary
    public ConnectionFactory defaultConnectionFactory(RabbitProperties rabbitProperties) {
        return newConnectionFactory("/" + MqProperties.VHOST.BINAPI, rabbitProperties);
    }

    /**
     * Aware接口介绍 https://juejin.cn/post/7110856807039369246
     *
     * @param beanFactory
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        AbstractBeanFactory register = (AbstractBeanFactory) beanFactory;
        // 注册交换器
        MqProperties.ExchangeInfo.EXCHANGE_LIST.forEach(exchange -> register.registerSingleton(exchange.getName(), exchange));

        SimpleRabbitListenerContainerFactoryConfigurer configurer = register
                .getBean(SimpleRabbitListenerContainerFactoryConfigurer.class);
        RabbitProperties properties = register.getBean(RabbitProperties.class);
        // key -> 连接工厂名 value -> 连接工厂
        Map<String, ConnectionFactory> connectionFactoryMap = MqProperties.VHOST.OTHER_SYSTEM_VHOSTS.stream()
                .collect(Collectors.toMap(Function.identity(),
                        vhost -> {
                            ConnectionFactory connectionFactory = newConnectionFactory("/" + vhost, properties);
                            // 注册连接工厂
                            register.registerSingleton(MqProperties.ConnectionFactory.getConnectionFactory(vhost), connectionFactory);
                            // 注册监听器
                            SimpleRabbitListenerContainerFactory mqListenerContainerFactory =
                                    getMqListenerContainerFactory(configurer, connectionFactory);
                            register.registerSingleton(MqProperties.Listener.getListener(vhost), mqListenerContainerFactory);
                            return connectionFactory;
                        }));

        for (Map.Entry<String, List<MqProperties.QueueBinding>> entry : queueExchangeBinding.getQueueExchangeBindingMap().entrySet()) {
            declareQueueAndBindingExchange(connectionFactoryMap.get(entry.getKey()), entry.getValue());
        }
    }

    /**
     * 声明队列和绑定交换机
     *
     * @param connectionFactory 连接工厂，多个消息队列有不同的连接工厂
     * @param queueBindings     队列绑定参数
     * @return
     */
    private String[] declareQueueAndBindingExchange(ConnectionFactory connectionFactory,
                                                    List<MqProperties.QueueBinding> queueBindings) {
        try {
            RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
            // 注册、创建交换器
            MqProperties.ExchangeInfo.EXCHANGE_LIST.forEach(rabbitAdmin::declareExchange);
            // 创建队列
            MqProperties.QueueInfo.QUEUE_LIST.forEach(rabbitAdmin::declareQueue);

            for (MqProperties.QueueBinding binding : queueBindings) {
                String queue = binding.queue;
                String exchange = binding.exchange;
                String routingKey = binding.bindingKey;
                Channel channel = connectionFactory.createConnection().createChannel(false);
                channel.queueBind(queue, exchange, routingKey);
            }
            return queueBindings.stream().map(MqProperties.QueueBinding::getQueue).distinct().toArray(String[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过虚拟目录创建 ConnectionFactory，其他属性无差异
     * @param vhost 虚拟目录名
     * @param rabbitProperties mq的参数
     * @return
     */
    private ConnectionFactory newConnectionFactory(String vhost, RabbitProperties rabbitProperties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitProperties.getHost());
        connectionFactory.setPort(rabbitProperties.getPort());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.setVirtualHost(vhost);
        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);
        connectionFactory.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT * 3);
        connectionFactory.setConnectionCacheSize(10);
        connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        connectionFactory.setChannelCacheSize(10);
        return connectionFactory;
    }

    /**
     * 创建 ListenerContainerFactory
     * <p>
     *     SimpleRabbitListenerContainerFactory 是用于创建简单的消息监听容器的工厂类。
     * 它提供了一种统一的方式来创建消息监听容器，可以设置并发消费者数量、线程池大小、消息确认模式等参数。
     * 通过使用 SimpleRabbitListenerContainerFactory，可以方便地创建适合不同需求的消息监听容器。
     * </p>
     */
    public SimpleRabbitListenerContainerFactory getMqListenerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                              ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory listenerContainerFactory = new SimpleRabbitListenerContainerFactory();
        // 初始化默认的配置
        configurer.configure(listenerContainerFactory, connectionFactory);
        //设置手动ack
        listenerContainerFactory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        // 从springBoot2.0开始默认为250；消费端处理比较快, RabbitMQ官网推荐30；消费端处理比较耗时则设置为1
        listenerContainerFactory.setPrefetchCount(10);
        // 即每个Listener容器将开启n个线程去处理消息
        listenerContainerFactory.setConcurrentConsumers(1);
        // MQ节点异常问题QueuesNotAvailableException 参考文档：https://blog.csdn.net/u012988901/article/details/122538836
        listenerContainerFactory.setMissingQueuesFatal(false);

        return listenerContainerFactory;
    }
}
