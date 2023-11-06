package com.niuma.binapi.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName interface_audit
 */
@TableName(value ="interface_audit")
@Data
public class InterfaceAudit implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 审批人ID
     */
    private Long approverId;
    /**
     * 申请人ID
     */
    private Long userId;
    /**
     * 备注
     */
    private String remark;

    /**
     * 审批状态
     */
    private Integer auditStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}