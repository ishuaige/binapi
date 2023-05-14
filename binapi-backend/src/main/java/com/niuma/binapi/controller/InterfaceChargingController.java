package com.niuma.binapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.niuma.binapi.annotation.AuthCheck;
import com.niuma.binapicommon.constant.CommonConstant;
import com.niuma.binapi.model.dto.interfaceCharging.InterfaceChargingAddRequest;
import com.niuma.binapi.model.dto.interfaceCharging.InterfaceChargingQueryRequest;
import com.niuma.binapi.model.dto.interfaceCharging.InterfaceChargingUpdateRequest;
import com.niuma.binapi.model.entity.InterfaceCharging;
import com.niuma.binapi.service.InterfaceChargingService;
import com.niuma.binapi.service.UserService;
import com.niuma.binapiclientsdk.client.BinApiClient;
import com.niuma.binapicommon.common.BaseResponse;
import com.niuma.binapicommon.common.DeleteRequest;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.common.ResultUtils;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 接口管理
 *
 * @author niuma
 */
@RestController
@RequestMapping("/interfaceCharging")
@Slf4j
public class InterfaceChargingController {

    @Resource
    private InterfaceChargingService interfaceChargingService;

    @Resource
    private UserService userService;


    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceChargingAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceCharging(@RequestBody InterfaceChargingAddRequest interfaceChargingAddRequest, HttpServletRequest request) {
        if (interfaceChargingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceCharging interfaceCharging = new InterfaceCharging();
        BeanUtils.copyProperties(interfaceChargingAddRequest, interfaceCharging);
        // 校验
//        interfaceChargingService.validInterfaceCharging(interfaceCharging, true);
        User loginUser = userService.getLoginUser(request);
        interfaceCharging.setUserId(loginUser.getId());
        boolean result = interfaceChargingService.save(interfaceCharging);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceChargingId = interfaceCharging.getId();
        return ResultUtils.success(newInterfaceChargingId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceCharging(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceCharging oldInterfaceCharging = interfaceChargingService.getById(id);
        if (oldInterfaceCharging == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceCharging.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceChargingService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param interfaceChargingUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateInterfaceCharging(@RequestBody InterfaceChargingUpdateRequest interfaceChargingUpdateRequest,
                                                         HttpServletRequest request) {
        if (interfaceChargingUpdateRequest == null || interfaceChargingUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceCharging interfaceCharging = new InterfaceCharging();
        BeanUtils.copyProperties(interfaceChargingUpdateRequest, interfaceCharging);
        // 参数校验
//        interfaceChargingService.validInterfaceCharging(interfaceCharging, false);
        User user = userService.getLoginUser(request);
        long id = interfaceChargingUpdateRequest.getId();
        // 判断是否存在
        InterfaceCharging oldInterfaceCharging = interfaceChargingService.getById(id);
        if (oldInterfaceCharging == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceCharging.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = interfaceChargingService.updateById(interfaceCharging);
        return ResultUtils.success(result);
    }


    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceCharging> getInterfaceChargingById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceCharging interfaceCharging = interfaceChargingService.getById(id);
        return ResultUtils.success(interfaceCharging);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceChargingQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceCharging>> listInterfaceCharging(InterfaceChargingQueryRequest interfaceChargingQueryRequest) {
        InterfaceCharging interfaceChargingQuery = new InterfaceCharging();
        if (interfaceChargingQueryRequest != null) {
            BeanUtils.copyProperties(interfaceChargingQueryRequest, interfaceChargingQuery);
        }
        QueryWrapper<InterfaceCharging> queryWrapper = new QueryWrapper<>(interfaceChargingQuery);
        List<InterfaceCharging> interfaceChargingList = interfaceChargingService.list(queryWrapper);
        return ResultUtils.success(interfaceChargingList);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceChargingQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceCharging>> listInterfaceChargingByPage(InterfaceChargingQueryRequest interfaceChargingQueryRequest, HttpServletRequest request) {
        if (interfaceChargingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceCharging interfaceChargingQuery = new InterfaceCharging();
        BeanUtils.copyProperties(interfaceChargingQueryRequest, interfaceChargingQuery);
        long current = interfaceChargingQueryRequest.getCurrent();
        long size = interfaceChargingQueryRequest.getPageSize();
        String sortField = interfaceChargingQueryRequest.getSortField();
        String sortOrder = interfaceChargingQueryRequest.getSortOrder();

        // content 需支持模糊搜索

        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceCharging> queryWrapper = new QueryWrapper<>(interfaceChargingQuery);

        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceCharging> interfaceChargingPage = interfaceChargingService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(interfaceChargingPage);
    }


    // endregion

}
