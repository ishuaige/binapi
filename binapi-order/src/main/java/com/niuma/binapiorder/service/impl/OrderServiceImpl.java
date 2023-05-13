package com.niuma.binapiorder.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.constant.CookieConstant;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.dto.UpdateAvailablePiecesDTO;
import com.niuma.binapicommon.model.entity.InterfaceInfo;
import com.niuma.binapicommon.model.vo.UserVO;
import com.niuma.binapicommon.service.InnerInterfaceChargingService;
import com.niuma.binapicommon.service.InnerInterfaceInfoService;
import com.niuma.binapicommon.service.InnerUserService;
import com.niuma.binapiorder.mapper.OrderMapper;
import com.niuma.binapiorder.model.dto.OrderAddRequest;
import com.niuma.binapiorder.model.dto.OrderQueryRequest;
import com.niuma.binapicommon.model.entity.Order;
import com.niuma.binapiorder.model.entity.OrderLock;
import com.niuma.binapiorder.model.enums.LockOrderStatusEnum;
import com.niuma.binapiorder.model.enums.OrderStatusEnum;
import com.niuma.binapicommon.model.vo.OrderVO;
import com.niuma.binapiorder.service.OrderLockService;
import com.niuma.binapiorder.service.OrderService;
import com.niuma.binapiorder.utils.OrderMqUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author niumazlb
 * @description 针对表【order】的数据库操作Service实现
 * @createDate 2023-05-03 15:52:09
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {


    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceChargingService interfaceChargingService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderLockService orderLockService;

    @Resource
    OrderMqUtils orderMqUtils;


    ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 200, 10, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(100000), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO addOrder(OrderAddRequest orderAddRequest, HttpServletRequest request) {

        OrderVO orderVO = null;
        try {
            if (orderAddRequest == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }

            Long interfaceId = orderAddRequest.getInterfaceId();
            Long userId = orderAddRequest.getUserId();
            Long count = orderAddRequest.getCount();
            Double charging = orderAddRequest.getCharging();
            Double totalAmount = orderAddRequest.getTotalAmount();
            Long chargingId = orderAddRequest.getChargingId();
            if (null == interfaceId || null == userId || null == count || null == charging || null == totalAmount || null == chargingId) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }

            UserVO userVO = getLoginUser(request);
            if (userVO == null || !userId.equals(userVO.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 异步获取接口信息
            InterfaceInfo interfaceInfo = new InterfaceInfo();
            CompletableFuture<Void> getInterfaceInfoFuture = CompletableFuture.runAsync(() -> {
                InterfaceInfo interfaceInfoById = innerInterfaceInfoService.getInterfaceInfoById(interfaceId);
                interfaceInfo.setDescription(interfaceInfoById.getDescription());
                interfaceInfo.setName(interfaceInfoById.getName());
            }, executor);

            // 生成订单号
            String orderNum = generateOrderNum(userId);
            // 总价 保留两位小数 四舍五入
            double temp = charging * count;
            BigDecimal bd = new BigDecimal(temp);
            double finalPrice = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (finalPrice != totalAmount) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格错误");
            }

            // 锁定库存
            UpdateAvailablePiecesDTO updateAvailablePiecesDTO = new UpdateAvailablePiecesDTO();
            updateAvailablePiecesDTO.setChargingId(chargingId);
            updateAvailablePiecesDTO.setCount(count);
            boolean lock_order;
            try {
                lock_order = interfaceChargingService.updateAvailablePieces(updateAvailablePiecesDTO);
            } catch (BusinessException e) {
                throw e;
            }
            if (!lock_order) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "锁定订单失败");
            }

            // 持久化订单锁
            OrderLock orderLock = new OrderLock();
            orderLock.setOrderNumber(orderNum);
            orderLock.setChargingId(chargingId);
            orderLock.setUserId(userId);
            orderLock.setLockNum(count);
            orderLock.setLockStatus(LockOrderStatusEnum.LOCK.getValue());
            orderLockService.save(orderLock);

            // 创建订单
            Order order = new Order();
            order.setInterfaceId(interfaceId);
            order.setUserId(userId);
            order.setOrderNumber(orderNum);
            order.setTotal(count);
            order.setCharging(charging);
            order.setTotalAmount(finalPrice);
            order.setStatus(OrderStatusEnum.TOBEPAID.getValue());
            this.save(order);

            // 将订单发送到延迟队列
            orderMqUtils.sendOrderSnInfo(order);

            // 等待异步任务返回
            CompletableFuture.allOf(getInterfaceInfoFuture).get();

            orderVO = new OrderVO();
            orderVO.setInterfaceId(interfaceId);
            orderVO.setUserId(userId);
            orderVO.setOrderNumber(orderNum);
            orderVO.setTotal(count);
            orderVO.setCharging(charging);
            orderVO.setTotalAmount(totalAmount);
            orderVO.setStatus(order.getStatus());
            orderVO.setInterfaceDesc(interfaceInfo.getDescription());
            orderVO.setInterfaceName(interfaceInfo.getName());
            DateTime date = DateUtil.date();
            orderVO.setCreateTime(date);
            orderVO.setExpirationTime(DateUtil.offset(date, DateField.MINUTE, 30));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建订单失败:" + e.getMessage());
        }

        return orderVO;
    }



    @Override
    public Page<OrderVO> listPageOrder(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        Integer type = Integer.parseInt(orderQueryRequest.getType());
        long current = orderQueryRequest.getCurrent();
        long pageSize = orderQueryRequest.getPageSize();
        String sortField = orderQueryRequest.getSortField();
        String sortOrder = orderQueryRequest.getSortOrder();
        if (!OrderStatusEnum.getValues().contains(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVO = getLoginUser(request);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userVO.getId()).eq("status",type);
        Page<Order> page = new Page<>(current,pageSize);
        Page<Order> orderPage = this.page(page, queryWrapper);

        Page<OrderVO> orderVOPage = new Page<>(orderPage.getCurrent(),orderPage.getSize(),orderPage.getTotal());

        List<OrderVO> orderVOList = orderPage.getRecords().stream().map(order -> {
            Long interfaceId = order.getInterfaceId();
            InterfaceInfo interfaceInfo = innerInterfaceInfoService.getInterfaceInfoById(interfaceId);
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setInterfaceName(interfaceInfo.getName());
            orderVO.setInterfaceDesc(interfaceInfo.getDescription());
            orderVO.setExpirationTime(DateUtil.offset(order.getCreateTime(), DateField.MINUTE, 30));
            return orderVO;
        }).collect(Collectors.toList());
        orderVOPage.setRecords(orderVOList);
        return orderVOPage;
    }

    @Override
    public List<Order> listTopBuyInterfaceInfo(int limit) {

        return orderMapper.listTopBuyInterfaceInfo(limit);
    }


    /**
     * 生成订单号
     *
     * @return
     */
    private String generateOrderNum(Long userId) {
        String timeId = IdWorker.getTimeId();
        String substring = timeId.substring(0, timeId.length() - 15);
        return substring + RandomUtil.randomNumbers(5) + userId;
    }

    private UserVO getLoginUser(HttpServletRequest request) {
        // 获取登录用户 并校验登录用户与购买用户是否匹配
        Cookie[] cookies = request.getCookies();
        String cookie = null;
        for (Cookie cookieItem : cookies) {
            if (CookieConstant.COOKIE_KEY.equals(cookieItem.getName())) {
                cookie = cookieItem.getValue();
                break;
            }
        }
        if (StringUtils.isBlank(cookie)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        UserVO userVO = innerUserService.getLoginUser(cookie);
        return userVO;
    }
}




