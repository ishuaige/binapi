package com.niuma.binapigateway.filter;


import com.niuma.binapicommon.model.entity.InterfaceInfo;
import com.niuma.binapicommon.model.entity.User;
import com.niuma.binapicommon.service.InnerInterfaceInfoService;
import com.niuma.binapicommon.service.InnerUserInterfaceInfoService;
import com.niuma.binapicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.niuma.binapiclientsdk.util.SignUtil.genSign;

/**
 * 全局请求过滤器
 * @author niumazlb
 */
@Component
@Slf4j
public class InterfaceFilter implements GatewayFilter, Ordered{

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    @DubboReference
    InnerUserService innerUserService;
    @DubboReference
    InnerUserInterfaceInfoService innerUserInterfaceInfoService;
    @DubboReference
    InnerInterfaceInfoService innerInterfaceInfoService;

    private static final String HOST = "http://localhost:8001";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.请求日志
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().toString();
        String path = HOST+ request.getPath().value();
        log.info("请求唯一ID：" + request.getId());
        log.info("请求方法：" + method);
        log.info("请求路径：" + path);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址：" + sourceAddress);

        //拿到响应体，用作返回
        ServerHttpResponse response = exchange.getResponse();

        // 2.黑白名单
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            return handleNoAuth(response);
        }

        HttpHeaders headers = request.getHeaders();
        // 3.用户鉴权，检验ak sk
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String body = headers.getFirst("body");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");

        // 从数据库中查出该accessKey对应的用户
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error:{}",e.getMessage());
        }
        if(invokeUser == null){
            return handleNoAuth(response);
        }

        if (nonce.length() > 5) {
            return handleNoAuth(response);
        }
        //  应该比较当前时间距离请求头中的时间，判断是否过期
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        final Long FIVE_MINUTES = 60 * 5L;
        if (currentTimeMillis - Long.parseLong(timestamp)>=FIVE_MINUTES) {
            return handleNoAuth(response);
        }

        //  这里的secretKey应该从上面查出的用户中获取
        String secretKey = invokeUser.getSecretKey();
        if (sign == null || !sign.equals(genSign(body, secretKey))) {
            return handleNoAuth(response);
        }

        // 4.判断请求接口是否存在
        //  从数据库中查询，判断接口url和方法，通过远程调用
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path,method);

        } catch (Exception e) {
            log.error("getInterfaceInfo error:{}",e.getMessage());
        }
        if(interfaceInfo == null){
            return handleNoAuth(response);
        }

        // 6.响应日志
        log.info("响应："+response.getStatusCode());

        // 7. 判断是否还有调用次数
        boolean hasAuth = innerUserInterfaceInfoService.checkUserInvokeAuth(invokeUser.getId(), interfaceInfo.getId());
        if(!hasAuth){
            return handleNoAuth(response);
        }

        //请求转发，响应日志
       return handleResponse(exchange,chain,interfaceInfo.getId(),invokeUser.getId());

    }

    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain,Long interfaceInfoId,Long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            HttpStatus statusCode = originalResponse.getStatusCode();

            if(statusCode == HttpStatus.OK){
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    //等调用完接口后才会执行该方法
                    @NotNull
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                //  7.调用成功，次数+1
                                try {
                                    innerUserInterfaceInfoService.invokeCount(interfaceInfoId,userId);
                                } catch (Exception e) {
                                    log.error("invokeCount error:{}",e);
                                }
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);//data 响应结果数据
                                sb2.append(data);
                                log.info("响应结果：{}",data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 调用模拟接口
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            //降级处理返回数据
            return chain.filter(exchange);
        }catch (Exception e){
            log.error("网关响应异常：" + e);
            return chain.filter(exchange);
        }
    }


    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }
    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -2;
    }

}