package com.niuma.binapi.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.niuma.binapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author niumazlb
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2022-11-21 14:33:34
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




