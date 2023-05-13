package com.niuma.binapiorder.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.niuma.binapiorder.model.dto.OrderAddRequest;
import com.niuma.binapiorder.model.dto.OrderQueryRequest;
import com.niuma.binapicommon.model.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.niuma.binapicommon.model.vo.OrderVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author niumazlb
* @description 针对表【order】的数据库操作Service
* @createDate 2023-05-03 15:52:09
*/
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     * @param addOrderRequest
     * @return
     */
    OrderVO addOrder(OrderAddRequest addOrderRequest, HttpServletRequest request);

    /**
     * 获取订单列表
     * @param orderQueryRequest
     * @param request
     * @return
     */
    Page<OrderVO> listPageOrder(OrderQueryRequest orderQueryRequest, HttpServletRequest request);

    /**
     * 获取前 limit 购买数量的接口
     * @param limit
     * @return
     */
    List<Order> listTopBuyInterfaceInfo(int limit);
}
