package com.niuma.binapicommon.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 短信服务传输对象
 * @author niuma
 * @create 2023-04-28 21:16
 */
@Data
@AllArgsConstructor
public class SmsDTO implements Serializable {

    private static final long serialVersionUID = 8504215015474691352L;
    String phoneNum;

    String code;
}
