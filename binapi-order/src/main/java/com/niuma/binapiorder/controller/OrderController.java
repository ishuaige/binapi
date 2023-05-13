package com.niuma.binapiorder.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.niuma.binapicommon.common.BaseResponse;
import com.niuma.binapicommon.common.ResultUtils;
import com.niuma.binapiorder.model.dto.OrderAddRequest;
import com.niuma.binapiorder.model.dto.OrderQueryRequest;
import com.niuma.binapicommon.model.vo.OrderVO;
import com.niuma.binapiorder.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author niuma
 * @create 2023-05-03 15:53
 */
@RestController
@RequestMapping("/")
public class OrderController {

    @Resource
    OrderService orderService;

    @PostMapping("/addOrder")
    public BaseResponse<OrderVO> addOrder(@RequestBody OrderAddRequest addOrderRequest, HttpServletRequest request) {
        OrderVO orderVO = orderService.addOrder(addOrderRequest, request);
        return ResultUtils.success(orderVO);
    }

    @GetMapping("/list")
    public BaseResponse<Page<OrderVO>> listPageOrder(OrderQueryRequest orderQueryRequest, HttpServletRequest request){
        Page<OrderVO> orderPage = orderService.listPageOrder(orderQueryRequest, request);
        return ResultUtils.success(orderPage);
    }
}
