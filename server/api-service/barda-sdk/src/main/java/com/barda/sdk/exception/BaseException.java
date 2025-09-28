package com.barda.sdk.exception;

/**
 * 基础异常类，提供对基础异常的定义。
 * 该类是一个标记接口 (marker interface)，表示它不包含任何方法或属性，
 * 但可以用来标记具有某些共同特征的类。
 */
public class BaseException extends RuntimeException {

    /**
     * 创建一个新的 BaseException 实例。
     *
     * @param message 异常消息
     */
    public BaseException(String message) {
        super(message);
    }
}