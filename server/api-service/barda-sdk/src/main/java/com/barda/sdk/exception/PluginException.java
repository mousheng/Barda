package com.barda.sdk.exception;

import java.util.Locale;

import com.google.common.base.Preconditions;
import com.barda.sdk.util.LocaleUtils;

import lombok.Getter;

import lombok.Getter;

/**
 * 插件异常类。
 * 继承自 {@link BaseException}，用于在插件中发生错误时抛出。
 */
@Getter
public class PluginException extends BaseException {

    /**
     * 发生的插件错误。
     */
    private final PluginError error;

    /**
     * 用于国际化的消息键。
     */
    private final String messageKey;

    /**
     * 用于国际化的消息参数。
     * 被标记为 transient，因为在序列化时不需要传输。
     */
    private final transient Object[] args;

    /**
     * 构造函数，使用指定的 {@link PluginError}、消息键和参数初始化。
     *
     * @param errorCode 插件错误
     * @param messageKey 消息键
     * @param args 消息参数
     */
    public PluginException(PluginError errorCode, String messageKey, Object... args) {
        super(LocaleUtils.getMessage(Locale.ENGLISH, messageKey, args));
        Preconditions.checkNotNull(errorCode);
        this.error = errorCode;
        this.messageKey = messageKey;
        this.args = args;
    }

    /**
     * 重写 {@link Throwable#getMessage()} 方法，返回英文的国际化消息。
     *
     * @return 英文的国际化消息
     */
    @Override
    public String getMessage() {
        return LocaleUtils.getMessage(Locale.ENGLISH, messageKey, args);
    }

    /**
     * 获取指定 {@link Locale} 的国际化消息。
     *
     * @param locale 本地化
     * @return 指定 {@link Locale} 的国际化消息
     */
    public String getLocaleMessage(Locale locale) {
        return LocaleUtils.getMessage(locale, messageKey, args);
    }
}
