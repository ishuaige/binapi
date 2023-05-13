package com.niuma.binapicommon.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author niuma
 * @create 2023-05-04 13:49
 */
@Data
public class UpdateAvailablePiecesDTO implements Serializable {
    private static final long serialVersionUID = 1354230288973784689L;

    /**
     * 计费ID
     */
    private Long chargingId;

    /**
     * 锁定次数
     */
    private Long count;
}
