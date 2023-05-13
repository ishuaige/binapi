package com.niuma.binapicommon.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author niuma
 * @create 2023-05-06 13:46
 */
@Data
public class UpdateUserInterfaceInfoDTO implements Serializable {

    private static final long serialVersionUID = 1472097902521779075L;

    private Long userId;

    private Long interfaceId;

    private Long lockNum;
}
