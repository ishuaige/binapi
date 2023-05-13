package com.niuma.binapiinterface;

import com.niuma.binapiclientsdk.client.BinApiClient;
import com.niuma.binapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class BinapiInterfaceApplicationTests {
    @Resource
    private BinApiClient binApiClient;

    @Test
    void contextLoads() {

        User user = new User();
        user.setUsername("niuma");
        String userByPost = binApiClient.getUserByPost(user);
        System.out.println(userByPost);

    }

}
