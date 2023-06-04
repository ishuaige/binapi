package com.niuma.binapiinterface;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class NacosServerInfo implements CommandLineRunner {

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    @Override
    public void run(String... args) throws Exception {
        String dataId = nacosConfigProperties.getName();
        String group = nacosConfigProperties.getGroup();
        String serverAddr = nacosConfigProperties.getServerAddr();
        String namespace = nacosConfigProperties.getNamespace();

        System.out.println("dataId: " + dataId);
        System.out.println("group: " + group);
        System.out.println("serverAddr: " + serverAddr);
        System.out.println("namespace: " + namespace);
    }
}