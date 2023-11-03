package com.niuma.binapi.config.mq;


import com.niuma.binapicommon.constant.RabbitMqConstant;

import java.util.Arrays;
import java.util.List;

/**
 * @author user
 */
public class MqProperties {

    /** 虚拟目录 */
    public interface VHOST {

        String BINAPI = "binapi";

        /** 其他系统所使用的虚拟目录 */
        List<String> OTHER_SYSTEM_VHOSTS = Arrays.asList(BINAPI);
    }

    /** 连接池 */
    public interface ConnectionFactory {

        String CONNECTION_FACTORY_SUFFIX = "MqConnectionFactory";

        String BINAPI = VHOST.BINAPI + CONNECTION_FACTORY_SUFFIX;

        static String getConnectionFactory(String vhost) {
            return vhost + CONNECTION_FACTORY_SUFFIX;
        }
    }


    /** 监听器 */
    public interface Listener {

        String LISTENER_SUFFIX = "MqListenerFactory";

        String VHOST_BINAPI = "binapi" + LISTENER_SUFFIX;

        static String getListener(String vhost) {
            return vhost + LISTENER_SUFFIX;
        }
    }

    /** 交换器 */
    public interface Exchange {

        /** 广播交换机  */
        List<String> FANOUT_EXCHANGES = Arrays.asList(


        );

        /** 主题交换器 */
        List<String> TOPIC_EXCHANGES = Arrays.asList(
                RabbitMqConstant.SMS_EXCHANGE_TOPIC_NAME,
                RabbitMqConstant.ORDER_SEND_QUEUE_TOPIC_NAME
        );

    }


    /**
     * 消息监听时，队列绑定关系
     *
     * @user chenshutian
     * @date 2021/5/15
     */
    @lombok.Value
    public static class QueueBinding {

        public String exchange;
        public String queue;
        public String bindingKey;
        public MqExchangeConfigPar mqExchangeConfigPar;

        public QueueBinding(String exchange, String queue) {
            this.exchange = exchange;
            this.queue = queue;
            this.bindingKey = "#";
            mqExchangeConfigPar = null;
        }

        public QueueBinding(String exchange, String queue, String bindingKey) {
            this.exchange = exchange;
            this.queue = queue;
            this.bindingKey = bindingKey;
            mqExchangeConfigPar = null;
        }

        public QueueBinding(String exchange, String queue, String bindingKey, MqExchangeConfigPar mqExchangeConfigPar) {
            this.exchange = exchange;
            this.queue = queue;
            this.bindingKey = bindingKey;
            this.mqExchangeConfigPar = mqExchangeConfigPar;
        }
    }

}
