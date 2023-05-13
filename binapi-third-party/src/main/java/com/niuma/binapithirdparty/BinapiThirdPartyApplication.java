package com.niuma.binapithirdparty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author niumazlb
 */
@SpringBootApplication(scanBasePackages = "com.niuma")
@MapperScan("com.niuma.binapithirdparty.mapper")
public class BinapiThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinapiThirdPartyApplication.class, args);
    }

}
