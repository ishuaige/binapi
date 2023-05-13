package com.niuma.binapithirdparty.schedule;


import com.niuma.binapicommon.constant.LockConstant;
import com.niuma.binapicommon.constant.RedisConstant;
import com.niuma.binapithirdparty.utils.OrderPaySuccessMqUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * 支付成功消息重发任务
 * @author niumazlb
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class SendOrderPaySuccessInfoFailedSchedule {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate redisTemplate;


    @Resource
    private OrderPaySuccessMqUtils orderPaySuccessMqUtils;

    /**
     * 消息可靠性保证（发送端可靠性保证）
     * 每分钟从生产者redis中重新发送发送订单支付成功的消息
     */
    @Scheduled(cron = "*/60 * * * * ?")
    public void sendFailedOrderPaySuccess(){
        RLock lock = redissonClient.getLock(LockConstant.ORDER_PAY_SUCCESS);
        try {
            // 为加锁等待20秒时间，并在加锁成功10秒钟后自动解开
            boolean tryLock = lock.tryLock(20, 10, TimeUnit.SECONDS);
            if (tryLock){
                //重新向mq中发送订单消息
                Set keys = redisTemplate.keys(RedisConstant.ORDER_PAY_SUCCESS_INFO + "*");
                for (Object key : keys) {
                    String orderSn = (String) redisTemplate.opsForValue().get(key);
                    //删除reids中的该条记录
                    redisTemplate.delete(key.toString());
                    orderPaySuccessMqUtils.sendOrderPaySuccess(orderSn);
                }
            }
        } catch (InterruptedException e) {
            log.error("===定时任务:获取失败生产者发送消息redis出现bug===");
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }
}
