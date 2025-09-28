package com.barda.sdk.models;

/**
 * 一个记录类，表示本地化消息。
 * 它包含一个消息键和可变数量的参数来格式化消息。
 *
 * @param messageKey 消息键
 * @param args 格式化参数
 */
public record LocaleMessage(String messageKey, Object... args) {

    /**
     * 构造函数，创建一个没有格式化参数的LocaleMessage实例。
     *
     * @param messageKey 消息键
     */
    public LocaleMessage(String messageKey) {
        this(messageKey, new Object[0]);
    }
}
