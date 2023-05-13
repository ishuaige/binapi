package com.niuma.binapi.service.inner;

import com.niuma.binapicommon.service.InnerUserInterfaceInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author niumazlb
 * @create 2022-11-16 14:09
 */
@SpringBootTest
class InnerUserInterfaceInfoServiceImplTest {

    @Resource
    InnerUserInterfaceInfoService userInterfaceInfoService;
    @Test
    void shangxianjiekou() {

        Boolean shangxianjiekou = userInterfaceInfoService.invokeCount(1l,1l);
        System.out.println(shangxianjiekou);
    }

}