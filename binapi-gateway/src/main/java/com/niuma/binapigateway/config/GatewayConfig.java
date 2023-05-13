package com.niuma.binapigateway.config;

import com.niuma.binapigateway.filter.InterfaceFilter;

import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author niuma
 * @create 2023-05-03 21:28
 */
@Configuration
public class GatewayConfig {

    @Resource
    InterfaceFilter interfaceFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("binapi-interface", r -> r.path("/api/interface/**")
                        .filters(f -> f.filter(interfaceFilter))
                        .uri("lb://binapi-interface"))
                .build();
    }


}
