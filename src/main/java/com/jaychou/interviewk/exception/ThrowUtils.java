package com.jaychou.interviewk.exception;

import com.jaychou.interviewk.common.ErrorCode;

/**
 * ClassName: ThrowUtils
 * Package: com.jaychou.interviewk.exception
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/8 - 21:58
 * @Version: v1.0
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param errorCode
     * @param message
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }


}
