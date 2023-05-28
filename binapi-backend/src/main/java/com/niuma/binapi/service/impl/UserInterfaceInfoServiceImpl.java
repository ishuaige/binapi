package com.niuma.binapi.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.niuma.binapi.mapper.UserInterfaceInfoMapper;
import com.niuma.binapi.model.entity.InterfaceCharging;
import com.niuma.binapi.model.vo.InterfaceInfoVO;
import com.niuma.binapi.model.vo.UserInterfaceInfoVO;
import com.niuma.binapi.service.InterfaceChargingService;
import com.niuma.binapi.service.UserInterfaceInfoService;
import com.niuma.binapi.service.UserService;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.constant.UserConstant;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.dto.UpdateUserInterfaceInfoDTO;
import com.niuma.binapicommon.model.entity.InterfaceInfo;
import com.niuma.binapicommon.model.entity.User;
import com.niuma.binapicommon.model.entity.UserInterfaceInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author niumazlb
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2022-11-21 14:33:34
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Resource
    InterfaceChargingService interfaceChargingService;

    @Resource
    InterfaceInfoServiceImpl interfaceInfoService;

    @Resource
    UserInterfaceInfoMapper userInterfaceInfoMapper;
    @Resource
    UserService userService;

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {


        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = userInterfaceInfo.getId();
        Long userId = userInterfaceInfo.getUserId();
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        Integer totalNum = userInterfaceInfo.getTotalNum();
        Integer leftNum = userInterfaceInfo.getLeftNum();
        Integer status = userInterfaceInfo.getStatus();
        Date createTime = userInterfaceInfo.getCreateTime();
        Date updateTime = userInterfaceInfo.getUpdateTime();
        Integer isDelete = userInterfaceInfo.getIsDelete();

        // 创建时，所有参数必须非空
        if (add) {
            if (interfaceInfoId <= 0 || userId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (leftNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UpdateWrapper<UserInterfaceInfo> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq("interfaceInfoId", interfaceInfoId);
        queryWrapper.eq("userId", userId);
        queryWrapper.setSql("leftNum = leftNum-1,totalNum = totalNum+1");
        return this.update(queryWrapper);

    }

    @Override
    public List<InterfaceInfoVO> interfaceInvokeTopAnalysis(int limit) {
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(limit);
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", interfaceInfoIdObjMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        List<InterfaceInfoVO> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            int totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum();
            interfaceInfoVO.setTotalNum(totalNum);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return interfaceInfoVOList;
    }

    @Override
    public boolean updateUserInterfaceInfo(UpdateUserInterfaceInfoDTO updateUserInterfaceInfoDTO) {
        Long userId = updateUserInterfaceInfoDTO.getUserId();
        Long interfaceId = updateUserInterfaceInfoDTO.getInterfaceId();
        Long lockNum = updateUserInterfaceInfoDTO.getLockNum();

        if(interfaceId == null || userId == null || lockNum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserInterfaceInfo one = this.getOne(
                new QueryWrapper<UserInterfaceInfo>()
                        .eq("userId", userId)
                        .eq("interfaceInfoId", interfaceId)
        );

        if (one != null) {
            // 说明是增加数量
            return this.update(
                    new UpdateWrapper<UserInterfaceInfo>()
                            .eq("userId", userId)
                            .eq("interfaceInfoId", interfaceId)
                            .setSql("leftNum = leftNum + " + lockNum)
            );
        } else {
            // 说明是第一次购买
            UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(interfaceId);
            userInterfaceInfo.setLeftNum(Math.toIntExact(lockNum));
            return this.save(userInterfaceInfo);
        }

    }

    @Override
    public boolean checkUserInvokeAuth(Long userId, Long interfaceInfoId) {
//        QueryWrapper<InterfaceCharging> chargingQueryWrapper = new QueryWrapper<>();
//        chargingQueryWrapper.eq("interfaceId", interfaceInfoId);
//        long count = interfaceChargingService.count(chargingQueryWrapper);
//        if (count <= 0) {
//            // 不需要计费
//            return true;
//        }

        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("interfaceInfoId", interfaceInfoId);
        UserInterfaceInfo interfaceInfo = this.getOne(queryWrapper);
        if (interfaceInfo == null) {
            return false;
        }
        return interfaceInfo.getLeftNum() > 0;
    }

    @Override
    public List<UserInterfaceInfoVO> getInterfaceInfoByUserId(Long userId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 判断用户是否有权限
        if(!loginUser.getId().equals(userId) && !loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 获取用户可调用接口列表
        QueryWrapper<UserInterfaceInfo> userInterfaceInfoQueryWrapper= new QueryWrapper<>();
        userInterfaceInfoQueryWrapper.eq("userId",loginUser.getId());
        List<UserInterfaceInfo> userInterfaceInfoList = this.list(userInterfaceInfoQueryWrapper);

        Map<Long, List<UserInterfaceInfo>> interfaceIdUserInterfaceInfoMap = userInterfaceInfoList.stream().collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        Set<Long> interfaceIds = interfaceIdUserInterfaceInfoMap.keySet();
        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        if(CollectionUtil.isEmpty(interfaceIds)){
            return new ArrayList<>();
        }
        interfaceInfoQueryWrapper.in("id",interfaceIds);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(interfaceInfoQueryWrapper);
        List<UserInterfaceInfoVO> userInterfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            UserInterfaceInfoVO userInterfaceInfoVO = new UserInterfaceInfoVO();
            // 复制接口信息
            BeanUtils.copyProperties(interfaceInfo, userInterfaceInfoVO);
            userInterfaceInfoVO.setInterfaceStatus(interfaceInfo.getStatus());

            // 复制用户调用接口信息
            List<UserInterfaceInfo> userInterfaceInfos = interfaceIdUserInterfaceInfoMap.get(interfaceInfo.getId());
            UserInterfaceInfo userInterfaceInfo = userInterfaceInfos.get(0);
            BeanUtils.copyProperties(userInterfaceInfo, userInterfaceInfoVO);
            return userInterfaceInfoVO;
        }).collect(Collectors.toList());
        return userInterfaceInfoVOList;
    }

}




