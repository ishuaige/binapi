package com.niuma.binapiorder.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.dto.UnLockAvailablePiecesDTO;
import com.niuma.binapicommon.service.InnerInterfaceChargingService;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;

import static com.niuma.binapicommon.constant.RabbitMqConstant.ORDER_TIMEOUT_QUEUE_NAME;

/**
 * 订单超时监听
 *
 * @author niuma
 * @create 2023-05-04 15:03
 */
@Component
@Slf4j
public class OrderTimeOutListener {

    @Resource
    OrderService orderService;
    @Resource
    OrderLockService orderLockService;
    @DubboReference
    InnerInterfaceChargingService interfaceChargingService;

    /**
     * 监听死信队列
     *
     * @param order
     * @param message
     * @param channel
     */
    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queues = ORDER_TIMEOUT_QUEUE_NAME)
    public void delayListener(Order order, Message message, Channel channel) throws IOException {
        try {
            log.error("监听到订单超时死信队列消息==>{}",order);
            Long orderId = order.getId();
            Order dbOrder = orderService.getById(orderId);
            // 没有生成订单，但是库存已经扣了
            if (dbOrder == null) {
                unLockAvailablePieces(order);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            Integer status = dbOrder.getStatus();
            if (OrderStatusEnum.TOBEPAID.getValue() == status) {
                String orderNumber = order.getOrderNumber();
                //超时未支付,更新订单表,订单锁表
                orderService.update(new UpdateWrapper<Order>().eq("id", orderId).set("status", OrderStatusEnum.FAILURE.getValue()));
                orderLockService.update(new UpdateWrapper<OrderLock>().eq("orderNumber", orderNumber).set("lockStatus", LockOrderStatusEnum.UNLOCK.getValue()));
                //解锁库存
                unLockAvailablePieces(order);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("订单超时死信队列报错：{}",e.getMessage());
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    private void unLockAvailablePieces(Order order) {
        Long count = order.getTotal();
        UnLockAvailablePiecesDTO unLockAvailablePiecesDTO = new UnLockAvailablePiecesDTO();
        unLockAvailablePiecesDTO.setCount(count);
        unLockAvailablePiecesDTO.setInterfaceId(order.getInterfaceId());
        boolean unLockAvailablePieces = interfaceChargingService.unLockAvailablePieces(unLockAvailablePiecesDTO);
        if (!unLockAvailablePieces) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单超时解锁库存失败");
        }
    }
}
