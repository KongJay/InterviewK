package com.jaychou.interviewk.exception;

import com.jaychou.interviewk.common.ErrorCode;

/**
 * ClassName: BusinessException
 * Package: com.jaychou.interviewk.exception
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/7 - 1:48
 * @Version: v1.0
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
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
