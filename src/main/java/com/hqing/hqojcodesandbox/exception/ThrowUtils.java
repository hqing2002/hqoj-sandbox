package com.hqing.hqojcodesandbox.exception;


/**
 * 抛异常工具类
 *
 * @author <a href="https://github.com/hqing2002">Hqing</a>
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
