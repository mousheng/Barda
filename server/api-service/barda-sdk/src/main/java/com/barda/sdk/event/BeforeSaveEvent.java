package com.barda.sdk.event;

/**
 * 保存前事件记录类，提供对保存前事件的定义。
 *
 * @param <T> 事件源的类型
 */
public record BeforeSaveEvent<T>(T source) {

    /**
     * 创建一个新的 BeforeSaveEvent 实例。
     *
     * @param source 事件源
     */
    public BeforeSaveEvent(T source) {
        this.source = source;
    }
}
