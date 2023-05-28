package com.niuma.binapi.facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.niuma.binapi.datasource.DataSourceRegistry;
import com.niuma.binapi.datasource.InterfaceInfoDataSource;
import com.niuma.binapi.model.dto.interfaceinfo.InterfaceInfoSearchRequest;
import com.niuma.binapi.model.enums.InterfaceInfoSearchEnum;
import com.niuma.binapicommon.model.entity.InterfaceInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author niuma
 * @create 2023-05-28 14:34
 */
@Component
public class InterfaceInfoSearchFacade {

    @Resource
    DataSourceRegistry dataSourceRegistry;

    public Page<InterfaceInfo> searchAll(InterfaceInfoSearchRequest interfaceInfoSearchRequest) {
        String searchText = interfaceInfoSearchRequest.getSearchText();
        String type = interfaceInfoSearchRequest.getType();
        long current = interfaceInfoSearchRequest.getCurrent();
        long pageSize = interfaceInfoSearchRequest.getPageSize();

        if (StringUtils.isBlank(type)) {
            type = InterfaceInfoSearchEnum.NATIVE.getValue();
        }
        InterfaceInfoDataSource dataSourceByType = dataSourceRegistry.getDataSourceByType(type);
        return dataSourceByType.doSearch(searchText, current, pageSize);
    }
}
