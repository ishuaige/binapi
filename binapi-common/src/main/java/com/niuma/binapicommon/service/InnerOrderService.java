package com.niuma.binapicommon.service;

import com.niuma.binapicommon.model.entity.Order;
import com.niuma.binapicommon.model.vo.OrderVO;

import java.util.List;

/**
 * @author niuma
 * @create 2023-05-11 22:57
 */

public interface InnerOrderService {
    /**
     * 获取前 limit 购买数量的接口
     * @param limit
     * @return
     */
    List<Order> listTopBuyInterfaceInfo(int limit);
}
