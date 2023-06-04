package com.niuma.binapithirdparty;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author niuma
 * @create 2023-05-29 22:06
 */
@SpringBootTest
public class YuCongMingTest {
    @Resource
    YuCongMingClient yuCongMingClient;

    @Test
    public void testChat(){
        DevChatRequest request = new DevChatRequest();
        request.setModelId(1654785040361893889L);
        request.setMessage("你是？");
        BaseResponse<DevChatResponse> devChatResponseBaseResponse =
                yuCongMingClient.doChat(request);
        System.out.println(devChatResponseBaseResponse.getData().getContent());
        //我是一个AI语言模型，可以热心地回答用户的各种问题。
    }

}
