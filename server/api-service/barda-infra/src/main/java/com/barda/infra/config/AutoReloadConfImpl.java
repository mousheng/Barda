package com.barda.infra.config;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.barda.sdk.config.dynamic.Conf;

/**
 * 自动重新加载配置实现类，实现了 Conf 接口。
 *
 * 该类用于从 AutoReloadConfigFactory 获取配置并提供对配置的访问。
 *
 */
class AutoReloadConfImpl<T> implements Conf<T> {

    /**
     * 配置项的键。
     */
    private final String confKey;

    /**
     * 配置项的默认值。
     */
    private final T defaultValue;

    /**
     * 用于将 JSON 字符串解析为指定类型的值解析器。
     */
    private final Function<String, T> valueResolver;

    /**
     * 自动重新加载配置工厂的实例，用于从工厂中获取配置。
     */
    private final AutoReloadConfigFactory autoReloadConfigFactory;

    /**
     * 用于存储上次获取的配置值。
     *
     * 该值用于检查是否需要重新加载配置。
     */
    private volatile String previousStrValue;

    /**
     * 用于存储当前的配置值。
     *
     * 该值用于返回最新的配置值。
     */
    private volatile T currentValue;

    /**
     * 构造函数，用于初始化 AutoReloadConfImpl 类的实例。
     *
     * @param confKey 配置项的键
     * @param defaultValue 配置项的默认值
     * @param autoReloadConfigFactory 自动重新加载配置工厂的实例
     * @param strValueResolver 用于将 JSON 字符串解析为指定类型的值解析器
     */
    public AutoReloadConfImpl(String confKey, T defaultValue,
            AutoReloadConfigFactory autoReloadConfigFactory,
            Function<String, T> strValueResolver) {
        this.confKey = confKey;
        this.defaultValue = defaultValue;
        this.autoReloadConfigFactory = autoReloadConfigFactory;
        this.valueResolver = strValueResolver;
    }

    /**
     * 获取配置项的值。
     *
     * 该方法从 AutoReloadConfigFactory 获取指定键的配置值，并使用 valueResolver 解析为指定类型的值。
     * 如果在上次获取值后未发生更改，则返回上次获取的值。
     *
     * @return 配置项的值
     */
    @Override
    public T get() {
        String currentStrValue = autoReloadConfigFactory.getValue(confKey);
        if (currentStrValue == null) {
            return defaultValue;
        }

        if (StringUtils.equals(previousStrValue, currentStrValue)) {
            return firstNonNull(currentValue, defaultValue);
        }

        previousStrValue = currentStrValue;
        try {
            currentValue = valueResolver.apply(currentStrValue);
            return firstNonNull(currentValue, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
