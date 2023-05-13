package com.niuma.binapicommon.service;

import com.niuma.binapicommon.model.dto.UpdateUserInterfaceInfoDTO;
import com.niuma.binapicommon.model.entity.InterfaceInfo;

/**
 * @author niuma
 * @create 2023-02-27 15:40
 */
public interface InnerInterfaceInfoService {

    /**
     * 获取调用的接口信息
     * @param url
     * @param method
     * @return
     */
    InterfaceInfo getInterfaceInfo(String url, String method);

    /**
     * 根据ID获取接口信息
     * @param id
     * @return
     */
    InterfaceInfo getInterfaceInfoById(Long id);

}
