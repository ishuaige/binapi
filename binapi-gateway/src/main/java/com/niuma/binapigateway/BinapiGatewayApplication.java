package com.niuma.binapigateway;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author niumazlb
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class},scanBasePackages = "com.niuma")
@EnableDubbo
public class BinapiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinapiGatewayApplication.class, args);
    }

}
