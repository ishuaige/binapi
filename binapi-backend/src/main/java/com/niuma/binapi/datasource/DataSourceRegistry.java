package com.niuma.binapi.datasource;

import com.niuma.binapi.model.enums.InterfaceInfoSearchEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author niuma
 * @create 2023-05-28 14:36
 */
@Component
public class DataSourceRegistry {

    @Resource
    private NativeDataSource nativeDataSource;

    @Resource
    private Apiaa1DataSource apiaa1DataSource;

    Map<String, InterfaceInfoDataSource> typeDataSourceMap;

    public InterfaceInfoDataSource getDataSourceByType(String type) {
        if (typeDataSourceMap == null) {
            return null;
        }
        return typeDataSourceMap.get(type);
    }

    @PostConstruct
    public void doInit() {
        typeDataSourceMap = new HashMap() {
            {
                put(InterfaceInfoSearchEnum.NATIVE.getValue(), nativeDataSource);
                put(InterfaceInfoSearchEnum.APIAA1.getValue(), apiaa1DataSource);
            }
        };
    }
}