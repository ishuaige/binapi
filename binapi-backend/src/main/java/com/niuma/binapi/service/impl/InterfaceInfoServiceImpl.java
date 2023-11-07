package com.niuma.binapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.niuma.binapi.mapper.InterfaceInfoMapper;
import com.niuma.binapi.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.niuma.binapi.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.niuma.binapi.model.entity.InterfaceAudit;
import com.niuma.binapi.model.entity.InterfaceCharging;
import com.niuma.binapi.model.enums.InterfaceInfoStatusEnum;
import com.niuma.binapi.service.InterfaceAuditService;
import com.niuma.binapi.service.InterfaceChargingService;
import com.niuma.binapi.service.InterfaceInfoService;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.constant.CommonConstant;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.entity.InterfaceInfo;
import com.niuma.binapicommon.model.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
* @author niumazlb
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2022-10-30 16:12:43
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService {


    @Resource
    private InterfaceAuditService interfaceAuditService;
    @Resource
    private InterfaceChargingService interfaceChargingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long userAddInterface(InterfaceInfoAddRequest interfaceInfoAddRequest, User user) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        InterfaceInfo interfaceInfo = saveInfoAndCharging(interfaceInfoAddRequest, user);
        InterfaceAudit interfaceAudit = new InterfaceAudit();
        interfaceAudit.setInterfaceId(interfaceInfo.getId());
        interfaceAudit.setUserId(user.getId());
        boolean saveAudit = interfaceAuditService.save(interfaceAudit);
        if (!saveAudit) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return interfaceInfo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long adminAddInterface(InterfaceInfoAddRequest interfaceInfoAddRequest, User user) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = saveInfoAndCharging(interfaceInfoAddRequest, user);
        Long newInterfaceInfoId = interfaceInfo.getId();
        if (newInterfaceInfoId == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return newInterfaceInfoId;
    }

    private InterfaceInfo saveInfoAndCharging(InterfaceInfoAddRequest interfaceInfoAddRequest, User user){
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        this.validInterfaceInfo(interfaceInfo, true);
        interfaceInfo.setUserId(user.getId());
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.PENDING.getValue());
        boolean saveInfo = this.save(interfaceInfo);
        // 判断接口是否收费，插入收费信息
        if (interfaceInfoAddRequest.isNeedCharge()) {
            InterfaceCharging interfaceCharging = new InterfaceCharging();
            interfaceCharging.setInterfaceId(interfaceInfo.getId());
            interfaceCharging.setCharging(interfaceInfoAddRequest.getCharging());
            interfaceCharging.setAvailablePieces(interfaceInfoAddRequest.getAvailablePieces());
            interfaceCharging.setUserId(user.getId());
            boolean saveCharging = interfaceChargingService.save(interfaceCharging);
            if (!saveCharging) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
        }
        if(!saveInfo){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return interfaceInfo;
    }




    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
     Long id = interfaceInfo.getId();
     String name = interfaceInfo.getName();
     String description = interfaceInfo.getDescription();
     String url = interfaceInfo.getUrl();
     String requestHeader = interfaceInfo.getRequestHeader();
     String responseHeader = interfaceInfo.getResponseHeader();
     Integer status = interfaceInfo.getStatus();
     String method = interfaceInfo.getMethod();

        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name,url) ) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }

    }

    @Override
    public Boolean onlineInterfaceInfo(long id) {
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        if(oldInterfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"该接口不存在");
        }

        //todo 测试接口是否可以使用 这里为模拟 先调用clint的接口模拟一下
//        User user = new User();
//        user.setUsername("");
//        String username = binApiClient.getUserByPost(user);
//        if(StringUtils.isBlank(username)){
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口校验失败");
//        }
        //将接口状态设为 1
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean update = this.updateById(interfaceInfo);
        return update;
    }

    @Override
    public Boolean offlineInterfaceInfo(long id) {
        InterfaceInfo oldInterfaceInfo = this.getById(id);
        if(oldInterfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"该接口不存在");
        }

        //将接口状态设为 0
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean update = this.updateById(interfaceInfo);
        return update;
    }

    @Override
    public Page<InterfaceInfo> getInterfaceInfoPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String description = interfaceInfoQuery.getDescription();
        // description 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);
        interfaceInfoQuery.setName(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.like(StringUtils.isNotBlank(interfaceInfoQueryRequest.getName()), "name", interfaceInfoQueryRequest.getName());
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = this.page(new Page<>(current, size), queryWrapper);
        return interfaceInfoPage;
    }



}




