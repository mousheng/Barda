package com.barda.api.framework.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.barda.sdk.exception.BizError;

import lombok.Getter;

/**
 * 通用响应视图类。
 * 包含状态码、消息和数据。
 * 使用Lombok的@JsonInclude和@Getter注解来生成JSON序列化和getter方法。
 *
 * @param <T> 响应数据中元素的类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class ResponseView<T> {

    /**
     * 成功状态码
     */
    public static final int SUCCESS = 1;

    /**
     * 状态码
     */
    private final int code;

    /**
     * 消息
     */
    private final String message;

    /**
     * 响应数据
     */
    private final T data;

    /**
     * 私有构造函数，用于创建ResponseView实例。
     *
     * @param code 状态码
     * @param message 消息
     * @param data 响应数据
     */
    protected ResponseView(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建一个成功的ResponseView实例。
     *
     * @param code 状态码
     * @param data 响应数据
     * @param <T> 响应数据中元素的类型
     * @return 成功的ResponseView实例
     */
    public static <T> ResponseView<T> success(int code, T data) {
        return new ResponseView<>(code, "", data);
    }

    /**
     * 创建一个成功的ResponseView实例。
     * 状态码默认为SUCCESS。
     *
     * @param data 响应数据
     * @param <T> 响应数据中元素的类型
     * @return 成功的ResponseView实例
     */
    public static <T> ResponseView<T> success(T data) {
        return success(SUCCESS, data);
    }

    /**
     * 创建一个失败的ResponseView实例。
     *
     * @param code 状态码
     * @param message 消息
     * @param <T> 响应数据中元素的类型
     * @return 失败的ResponseView实例
     */
    public static <T> ResponseView<T> error(int code, String message) {
        return new ResponseView<>(code, message, null);
    }

    /**
     * 创建一个失败的ResponseView实例。
     *
     * @param code 状态码
     * @param message 消息
     * @param data 响应数据
     * @param <T> 响应数据中元素的类型
     * @return 失败的ResponseView实例
     */
    public static <T> ResponseView<T> error(int code, String message, T data) {
        return new ResponseView<>(code, message, data);
    }

    /**
     * 判断响应是否成功。
     *
     * @return true表示成功，false表示失败
     */
    public boolean isSuccess() {
        return this.code == SUCCESS || this.code == BizError.REDIRECT.getBizErrorCode();
    }
}
