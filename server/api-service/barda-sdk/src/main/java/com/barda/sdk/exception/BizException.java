package com.barda.sdk.exception;

import java.util.Locale;

import com.barda.sdk.util.LocaleUtils;

import lombok.Getter;

/**
 * 业务异常类，继承自 {@link BaseException}。
 *
 * <p>此类用于表示业务相关的异常，包含了业务错误码、国际化的消息键和参数。
 * 它提供了一系列的 getter 方法来获取相关的属性，并重写了 {@link #getMessage()} 方法来返回国际化的消息。
 */
@Getter
public class BizException extends BaseException {

    /**
     * 业务错误码。
     */
    private final BizError error;

    /**
     * 国际化的消息键。
     */
    private final String messageKey;

    /**
     * 国际化的消息参数。
     */
    private final transient Object[] args;

    /**
     * 构造函数。
     *
     * @param error 业务错误码
     * @param messageKey 国际化的消息键
     * @param args 国际化的消息参数
     */
    public BizException(BizError error, String messageKey, Object... args) {
        super(LocaleUtils.getMessage(Locale.ENGLISH, messageKey, args));
        this.error = error;
        this.messageKey = messageKey;
        this.args = args;
    }

    /**
     * 获取 HTTP 状态码。
     *
     * <p>如果 {@link #error} 为 null，返回 500；否则返回 {@link BizError#getHttpErrorCode()}。
     *
     * @return HTTP 状态码
     */
    public int getHttpStatus() {
        return error == null ? 500 : error.getHttpErrorCode();
    }

    /**
     * 获取业务错误码。
     *
     * <p>如果 {@link #error} 为 null，返回 -1；否则返回 {@link BizError#getBizErrorCode()}。
     *
     * @return 业务错误码
     */
    public int getBizErrorCode() {
        return error == null ? -1 : error.getBizErrorCode();
    }

    /**
     * 获取国际化的消息。
     *
     * <p>如果 {@link #error} 为 null，返回父类 {@link BaseException} 的消息；
     * 否则返回使用英语（Locale.ENGLISH）本地化的消息。
     *
     * @return 国际化的消息
     */
    @Override
    public String getMessage() {
        return error == null ? super.getMessage() : LocaleUtils.getMessage(Locale.ENGLISH, messageKey, args);
    }

    /**
     * 获取指定本地化的消息。
     *
     * <p>如果 {@link #error} 为 null，返回父类 {@link BaseException} 的消息；
     * 否则返回使用指定本地化的消息。
     *
     * @param locale 本地化
     * @return 指定本地化的消息
     */
    public String getMessage(Locale locale) {
        return error == null ? super.getMessage() : LocaleUtils.getMessage(locale, messageKey, args);
    }
}
