package com.niuma.binapi.config.mq;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rabbitmq.client.ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT;

/**
 * @author niuma
 */
@Configurable
public class RabbitMqConfig implements BeanFactoryAware {
    @Autowired
    private QueueExchangeBinding queueExchangeBinding;

    @Bean(MqProperties.ConnectionFactory.BINAPI)
    @Primary
    public ConnectionFactory defaultConnectionFactory(RabbitProperties rabbitProperties) {
        return newConnectionFactory("/" + MqProperties.VHOST.BINAPI, rabbitProperties);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        AbstractBeanFactory register = (AbstractBeanFactory) beanFactory;
        // 注册交换器
        MqProperties.Exchange.FANOUT_EXCHANGES.forEach(exchange ->
                register.registerSingleton(exchange, new FanoutExchange(exchange, true, false)));
        MqProperties.Exchange.TOPIC_EXCHANGES.forEach(exchange ->
                register.registerSingleton(exchange, new TopicExchange(exchange, true, false)));

        SimpleRabbitListenerContainerFactoryConfigurer configurer = register
                .getBean(SimpleRabbitListenerContainerFactoryConfigurer.class);
        RabbitProperties properties = register.getBean(RabbitProperties.class);
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
            declareQueueAndBindingExchange(connectionFactoryMap.get( entry.getKey()), entry.getValue());
        }
    }

    private String[] declareQueueAndBindingExchange(ConnectionFactory connectionFactory,
                                                    List<MqProperties.QueueBinding> queueBindings) {
        try {
            for (MqProperties.QueueBinding binding : queueBindings) {
                String queue = binding.queue;
                String exchange = binding.exchange;
                Channel channel = connectionFactory.createConnection().createChannel(false);
                channel.queueDeclare(queue, true, false, false, null);
                try {
                    channel.exchangeDeclarePassive(exchange);
                } catch (IOException ex) {
                    channel = connectionFactory.createConnection().createChannel(false);
                    crtExchange(exchange, channel, binding.getMqExchangeConfigPar());
                }
                String routingKey = binding.bindingKey;
                channel.queueBind(queue, exchange, routingKey);
            }
            return queueBindings.stream().map(MqProperties.QueueBinding::getQueue).distinct().toArray(String[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void crtExchange(String exchange, Channel channel, MqExchangeConfigPar mqExchangeConfigPar)
            throws IOException {
        boolean durable = Optional.ofNullable(mqExchangeConfigPar)
                .map(MqExchangeConfigPar::getDurable)
                .orElse(true);
        String exchangeType = Optional.ofNullable(mqExchangeConfigPar)
                .map(MqExchangeConfigPar::getType)
                .orElse(getDefaultExchangeType(exchange));
        Boolean autoDeleted = Optional.ofNullable(mqExchangeConfigPar)
                .map(MqExchangeConfigPar::getAutoDelete)
                .orElse(false);
        Map<String, Object> argMap = Optional.ofNullable(mqExchangeConfigPar)
                .map(MqExchangeConfigPar::getArguments)
                .orElse(new HashMap<>());
        channel.exchangeDeclare(exchange, exchangeType ,durable, autoDeleted, argMap);
    }

    private String getDefaultExchangeType(String exchange) {
        return exchange.contains("fanout") ? "fanout" : "topic";
    }


    /** 通过虚拟目录创建 ConnectionFactory，其他属性无差异 */
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

    /** 创建 ListenerContainerFactory */
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
