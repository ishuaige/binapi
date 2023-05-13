package com.niuma.binapicommon.exception;


import com.niuma.binapicommon.common.ErrorCode;

import java.io.Serializable;

/**
 * 自定义异常类
 *
 * @author niuma
 */
public class BusinessException extends RuntimeException implements Serializable {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
