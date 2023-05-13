package com.niuma.binapiorder.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName order_lock
 */
@TableName(value ="order_lock")
@Data
public class OrderLock implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 计费id
     */
    private Long chargingId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 锁定数量
     */
    private Long lockNum;

    /**
     * 锁定状态(1-已锁定  0-已解锁 2-扣减)
     */
    private Integer lockStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}