package com.niuma.binapi.config.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author niuma
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqExchangeConfigPar {

    /**
     * 类型
     */
    private String type;

    /**
     * 是否持久化
     */
    private Boolean durable;

    /**
     * 是否自动删除
     */
    private Boolean autoDelete;

    /**
     * 参数
     */
    private Map<String, Object> arguments;
}
