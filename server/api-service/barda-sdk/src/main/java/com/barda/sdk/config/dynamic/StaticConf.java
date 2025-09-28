package com.barda.sdk.config.dynamic;

import java.util.Map;

/**
 * 该类表示一个静态配置。
 *
 * 该类实现了 Conf 接口，并提供了一个 get() 方法来获取配置值。
 *
 * @param <T> 配置值的类型
 */
public class StaticConf<T> implements Conf<T> {

    /**
     * 配置值
     */
    private final T value;

    /**
     * 配置键
     */
    private final String confKey;

    /**
     * 覆盖的键值对
     */
    private final Map<String, Object> overrideKeyValues;

    /**
     * 构造函数
     *
     * @param value 配置值
     * @param confKey 配置键
     * @param overrideKeyValues 覆盖的键值对
     */
    public StaticConf(T value, String confKey, Map<String, Object> overrideKeyValues) {
        this.value = value;
        this.confKey = confKey;
        this.overrideKeyValues = overrideKeyValues;
    }

    /**
     * 获取配置值
     *
     * 如果在覆盖的键值对中找到了该配置键，则返回覆盖的值，否则返回原始值。
     *
     * @return 配置值
     */
    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        Object overrideValue = overrideKeyValues.get(confKey);
        return overrideValue != null ? (T) overrideValue : value;
    }
}
