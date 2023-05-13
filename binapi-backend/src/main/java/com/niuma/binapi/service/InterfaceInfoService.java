package com.niuma.binapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.niuma.binapicommon.model.entity.InterfaceInfo;

/**
* @author niumazlb
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2022-10-30 16:12:43
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add 是否为创建校验
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 上线接口
     * @param id
     * @return
     */
    Boolean onlineInterfaceInfo(long id);

    /**
     * 下线接口
     * @param id
     * @return
     */
    Boolean offlineInterfaceInfo(long id);


}
