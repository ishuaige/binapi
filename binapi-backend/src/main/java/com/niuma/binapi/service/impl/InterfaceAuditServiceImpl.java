package com.niuma.binapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.niuma.binapi.mapper.InterfaceAuditMapper;
import com.niuma.binapi.model.dto.interfaceAudit.InterfaceAuditQueryRequest;
import com.niuma.binapi.model.dto.interfaceAudit.InterfaceAuditRequest;
import com.niuma.binapi.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.niuma.binapi.model.entity.InterfaceAudit;
import com.niuma.binapi.model.entity.InterfaceCharging;
import com.niuma.binapi.model.enums.InterfaceInfoAuditStatusEnum;
import com.niuma.binapi.model.enums.InterfaceInfoStatusEnum;
import com.niuma.binapi.model.vo.InterfaceAuditVO;
import com.niuma.binapi.service.InterfaceAuditService;
import com.niuma.binapi.service.InterfaceChargingService;
import com.niuma.binapi.service.InterfaceInfoService;
import com.niuma.binapi.service.UserService;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.entity.InterfaceInfo;
import com.niuma.binapicommon.model.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author niumazlb
 * @description 针对表【interface_audit】的数据库操作Service实现
 * @createDate 2023-06-12 22:26:29
 */
@Service
public class InterfaceAuditServiceImpl extends ServiceImpl<InterfaceAuditMapper, InterfaceAudit>
        implements InterfaceAuditService {
    @Resource
    private InterfaceInfoService interfaceInfoService;
    @Resource
    private InterfaceChargingService interfaceChargingService;
    @Resource
    private UserService userService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditInterface(InterfaceAuditRequest interfaceAuditRequest, User loginUser) {
        if (interfaceAuditRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 修改审核表
        //  1.1 根据id查询接口审核表
        InterfaceAudit interfaceAudit = this.getById(interfaceAuditRequest.getId());
        if (interfaceAudit == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1.2 增加审批人、备注字段，修改审核状态为审核完毕
        interfaceAudit.setRemark(interfaceAuditRequest.getRemark());
        interfaceAudit.setApproverId(loginUser.getId());
        interfaceAudit.setAuditStatus(InterfaceInfoAuditStatusEnum.FINISH.getValue());
        boolean updateAudit = this.updateById(interfaceAudit);

        // 2. 修改接口信息表
        //  2.1 根据id查询接口
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceAudit.getInterfaceId());
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.2 修改接口信息表接口状态为关闭
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean updateInfo = interfaceInfoService.updateById(interfaceInfo);
        // 3. 判断修改情况
        if (!updateInfo || !updateAudit) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean userAddInterface(InterfaceInfoAddRequest interfaceInfoAddRequest, User user) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        interfaceInfo.setUserId(user.getId());
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.PENDING.getValue());
        boolean saveInfo = interfaceInfoService.save(interfaceInfo);
        InterfaceAudit interfaceAudit = new InterfaceAudit();
        interfaceAudit.setInterfaceId(interfaceInfo.getId());
        interfaceAudit.setUserId(user.getId());
        boolean saveAudit = this.save(interfaceAudit);
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
        if (!saveInfo || !saveAudit) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    @Override
    public Page<InterfaceAuditVO> getInterfaceAuditListPage(InterfaceAuditQueryRequest interfaceAuditQueryRequest) {
        Long interfaceId = interfaceAuditQueryRequest.getInterfaceId();
        Long approverId = interfaceAuditQueryRequest.getApproverId();
        Long userId = interfaceAuditQueryRequest.getUserId();
        String remark = interfaceAuditQueryRequest.getRemark();
        Integer auditStatus = interfaceAuditQueryRequest.getAuditStatus();
        long current = interfaceAuditQueryRequest.getCurrent();
        long pageSize = interfaceAuditQueryRequest.getPageSize();
        String sortField = interfaceAuditQueryRequest.getSortField();
        String sortOrder = interfaceAuditQueryRequest.getSortOrder();
        LambdaQueryWrapper<InterfaceAudit> queryWrapper = new LambdaQueryWrapper<>();

        // 审核备注模糊搜索
        queryWrapper.like(remark != null, InterfaceAudit::getRemark, remark);
        // 审核状态精确搜索
        queryWrapper.eq(auditStatus != null, InterfaceAudit::getAuditStatus, auditStatus);
        // 用户id精确搜索
        queryWrapper.eq(userId != null, InterfaceAudit::getUserId, userId);


        //分页获取数据
        Page<InterfaceAudit> interfaceAuditListPage = new Page<>(current, pageSize);
        this.page(interfaceAuditListPage, queryWrapper);

        List<InterfaceAudit> interfaceAuditList = interfaceAuditListPage.getRecords();
        List<InterfaceAuditVO> interfaceAuditVOList = interfaceAuditList.stream().map(interfaceAudit -> {
            InterfaceAuditVO interfaceAuditVO = new InterfaceAuditVO();
            BeanUtils.copyProperties(interfaceAudit, interfaceAuditVO);

            User approver = userService.getById(interfaceAudit.getApproverId());
            interfaceAuditVO.setApproverAccount(approver.getUserAccount());

            User createUser = userService.getById(interfaceAudit.getUserId());
            interfaceAuditVO.setUserAccount(createUser.getUserAccount());

            InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceAudit.getInterfaceId());
            interfaceAuditVO.setInterfaceInfo(interfaceInfo);
            if (interfaceInfo == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            return interfaceAuditVO;
        }).collect(Collectors.toList());

        Page<InterfaceAuditVO> res = new Page<>(interfaceAuditListPage.getCurrent(),interfaceAuditListPage.getSize());
        res.setRecords(interfaceAuditVOList);
        res.setTotal(interfaceAuditListPage.getTotal());
        return res;
    }
}




