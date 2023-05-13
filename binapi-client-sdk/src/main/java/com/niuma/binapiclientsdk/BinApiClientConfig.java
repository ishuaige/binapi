package com.niuma.binapiclientsdk;

import com.niuma.binapiclientsdk.client.BinApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author niumazlb
 * @create 2022-11-09 20:38
 */
@Configuration
@ConfigurationProperties("binapi-client")
@Data
@ComponentScan
public class BinApiClientConfig {


    private String accessKey;
    private String secretKey;

    @Bean
    public BinApiClient binApiClient(){
        BinApiClient binApiClient = new BinApiClient(accessKey,secretKey);
        return binApiClient;
    }

}
