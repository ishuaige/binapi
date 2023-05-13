package com.niuma.binapi.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;

/**
 * @author niuma
 * @create 2023-05-03 23:07
 */
@SpringBootTest
public class JwtTest {
    @Test
    public void testJwt() {
        String token = JWTUtil.createToken(new HashMap() {{
            put("name", "jj");
        }}, JWTSignerUtil.hs256("niuma".getBytes()));
        String sign = JWT.create()
                .addPayloads(new HashMap() {{
                    put("name", "jj");
                }})
                .setSigner(JWTSignerUtil.hs256("niuma".getBytes()))
                .setExpiresAt(DateUtil.offsetHour(new Date(), 3)).sign();
        System.out.println(token);
        JWT jwt = JWTUtil.parseToken(token);
        JWT jwt1 = JWTUtil.parseToken(sign);
        System.out.println(jwt);
    }

}
