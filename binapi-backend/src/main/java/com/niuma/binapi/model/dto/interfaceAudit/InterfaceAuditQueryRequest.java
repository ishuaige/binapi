package com.niuma.binapi.model.dto.interfaceAudit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.niuma.binapicommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创建请求
 *
 * @author niumazlb
 * @TableName product
 */
@Data
public class InterfaceAuditQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 4489941418648280203L;

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

}