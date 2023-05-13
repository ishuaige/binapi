package com.niuma.binapi;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.niuma")
@MapperScan("com.niuma.binapi.mapper")
@EnableDubbo
public class BinapiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinapiBackendApplication.class, args);

    }

}
