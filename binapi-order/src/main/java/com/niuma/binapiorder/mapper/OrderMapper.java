package com.niuma.binapiorder.mapper;

import com.niuma.binapicommon.model.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author niumazlb
* @description 针对表【order】的数据库操作Mapper
* @createDate 2023-05-03 15:52:09
* @Entity com.niuma.binapicommon.model.entity.Order
*/
public interface OrderMapper extends BaseMapper<Order> {
    /**
     * 获取前 limit 购买数量的接口
     * @param limit
     * @return
     */
    List<Order> listTopBuyInterfaceInfo(int limit);

}




