package com.niuma.binapiorder.service.impl;

import com.niuma.binapicommon.model.entity.Order;
import com.niuma.binapicommon.model.vo.OrderVO;
import com.niuma.binapicommon.service.InnerOrderService;
import com.niuma.binapiorder.service.OrderService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author niuma
 * @create 2023-05-11 23:00
 */
@DubboService
public class InnerOrderServiceImpl implements InnerOrderService {
    @Resource
    OrderService orderService;
    @Override
    public List<Order> listTopBuyInterfaceInfo(int limit) {
        return orderService.listTopBuyInterfaceInfo(limit);
    }
}
