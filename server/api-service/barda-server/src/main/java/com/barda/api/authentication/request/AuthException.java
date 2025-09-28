package com.barda.api.authentication.request;

/**
 * 自定义的认证异常类。
 * 该类继承自 {@link RuntimeException}，并在构造函数中将传入的对象转换为字符串作为异常信息。
 */
public class AuthException extends RuntimeException {

    /**
     * 构造函数。
     *
     * @param o 用于生成异常信息的对象
     */
    public AuthException(Object o) {
        super(o.toString());
    }
}
