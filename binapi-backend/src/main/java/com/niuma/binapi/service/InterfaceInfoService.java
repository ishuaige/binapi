package com.niuma.binapi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.niuma.binapi.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.niuma.binapi.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.niuma.binapicommon.model.entity.InterfaceInfo;
import com.niuma.binapicommon.model.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author niumazlb
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2022-10-30 16:12:43
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {


    /**
     * 添加接口信息-用户使用
     * @param interfaceInfoAddRequest
     * @param user
     * @return
     */
    Long userAddInterface(InterfaceInfoAddRequest interfaceInfoAddRequest, User user);

    /**
     * 添加接口信息-管理员使用
     * @param interfaceInfoAddRequest
     * @param user
     * @return
     */
    Long adminAddInterface(InterfaceInfoAddRequest interfaceInfoAddRequest, User user);

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

    /**
     * 搜索分页接口信息
     * @param interfaceInfoQueryRequest
     * @return
     */
    Page<InterfaceInfo> getInterfaceInfoPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest);


}
