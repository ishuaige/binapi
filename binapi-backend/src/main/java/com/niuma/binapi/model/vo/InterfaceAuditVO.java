package com.niuma.binapi.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.niuma.binapicommon.model.entity.InterfaceInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author niumazlb
 * @TableName interface_audit
 */
@Data
public class InterfaceAuditVO implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接口信息
     */
    private InterfaceInfo interfaceInfo;


    /**
     * 审批人ID
     */
    private Long approverId;
    /**
     * 申请人ID
     */
    private Long userId;

    /**
     * 审批人账号
     */
    private String approverAccount;
    /**
     * 申请人ID
     */
    private String userAccount;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}