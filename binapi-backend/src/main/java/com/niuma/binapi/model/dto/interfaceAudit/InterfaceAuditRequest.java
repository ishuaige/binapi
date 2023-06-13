package com.niuma.binapi.model.dto.interfaceAudit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @author niumazlb
 * @TableName product
 */
@Data
public class InterfaceAuditRequest implements Serializable {
    private static final long serialVersionUID = 4489941418648280203L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 备注
     */
    private String remark;



}