package com.niuma.binapi.service;

import com.niuma.binapiclientsdk.client.BinApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author niuma
 * @create 2023-03-28 16:25
 */
@SpringBootTest
public class BinApiClientTest {

    @Test
    public void testRenjian(){
        BinApiClient binApiClient = new BinApiClient("niuma","asdfg");
        String renjian = binApiClient.renjian();
        System.out.println(renjian);

    }

}
