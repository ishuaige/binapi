package com.niuma.binapiorder.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.niuma.binapicommon.constant.RedisConstant;
import com.niuma.binapicommon.model.dto.UpdateUserInterfaceInfoDTO;
import com.niuma.binapicommon.service.InnerUserInterfaceInfoService;
import com.niuma.binapicommon.model.entity.Order;
import com.niuma.binapiorder.model.entity.OrderLock;
import com.niuma.binapiorder.model.enums.LockOrderStatusEnum;
import com.niuma.binapiorder.model.enums.OrderStatusEnum;
import com.niuma.binapiorder.service.OrderLockService;
import com.niuma.binapiorder.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.niuma.binapicommon.constant.RabbitMqConstant.ORDER_SUCCESS_QUEUE_NAME;

/**
 * @author niuma
 * @create 2023-05-06 9:00
 */
@Component
@Slf4j
public class AlipaySuccessListener {

    @Resource
    OrderService orderService;

    @Resource
    OrderLockService orderLockService;

    @DubboReference
    InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Resource
    RedisTemplate redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queues = ORDER_SUCCESS_QUEUE_NAME)
    public void alipaySuccess(String orderNum, Message message, Channel channel) throws IOException {
        try {
            // 消息到达队列，就可以删掉了
            redisTemplate.delete(RedisConstant.ORDER_PAY_SUCCESS_INFO + orderNum);
            // 看看这个消息消费过没有，防止重复消费
            Object o = redisTemplate.opsForValue().get(RedisConstant.ORDER_PAY_RABBITMQ + orderNum);
            if(o != null){
                //消费过了
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            }

            log.info("支付宝支付成功：{}",orderNum);
            if(StringUtils.isNotBlank(orderNum)){
                // 修改订单状态
                orderService.update(new UpdateWrapper<Order>().eq("orderNumber",orderNum).set("status", OrderStatusEnum.DONE.getValue()));
                // 修改订单锁状态
                orderLockService.update(new UpdateWrapper<OrderLock>().eq("orderNumber",orderNum).set("lockStatus", LockOrderStatusEnum.DEDUCT.getValue()));

                Order order = orderService.getOne(new QueryWrapper<Order>().eq("orderNumber", orderNum));
                OrderLock orderLock = orderLockService.getOne(new QueryWrapper<OrderLock>().eq("orderNumber", orderNum));
                UpdateUserInterfaceInfoDTO updateUserInterfaceInfoDTO = new UpdateUserInterfaceInfoDTO();
                updateUserInterfaceInfoDTO.setUserId(order.getUserId());
                updateUserInterfaceInfoDTO.setInterfaceId(order.getInterfaceId());
                updateUserInterfaceInfoDTO.setLockNum(orderLock.getLockNum());
                //更新用户接口调用次数
                innerUserInterfaceInfoService.updateUserInterfaceInfo(updateUserInterfaceInfoDTO);
            }
            // 消费成功，设置redis中的状态
            redisTemplate.opsForValue().set(RedisConstant.ORDER_PAY_RABBITMQ + orderNum,true,30, TimeUnit.MINUTES);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            log.error("<======处理支付宝支付成功监听器出错======>");
            e.printStackTrace();
            redisTemplate.delete(RedisConstant.ORDER_PAY_RABBITMQ + orderNum);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
