package com.barda.infra.config;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.sdk.config.dynamic.Conf;
import com.barda.sdk.config.dynamic.ConfigInstance;
import com.barda.sdk.util.JsonUtils;

/**
 * 自动重新加载配置实例实现类，实现了ConfigInstance接口。
 */
@Component
@SuppressWarnings("unchecked")
public class AutoReloadConfigInstanceImpl implements ConfigInstance {

    /**
     * 自动重新加载配置工厂实例。
     */
    @Autowired
    private AutoReloadConfigFactory autoReloadConfigFactory;

    /**
     * 存储配置键值对的线程安全哈希映射。
     */
    private final ConcurrentHashMap<ConfKey, Conf<?>> confMap = new ConcurrentHashMap<>();

    /**
     * 根据给定的配置键、默认值和类型，返回一个整数类型的配置对象。
     *
     * @param confKey      配置键
     * @param defaultValue 默认值
     * @return 整数类型的配置对象
     */
    @Override
    public Conf<Integer> ofInteger(String confKey, int defaultValue) {
        return fromJson(confKey, Integer.class, defaultValue);
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个字符串类型的配置对象。
     *
     * @param confKey      配置键
     * @param defaultValue 默认值
     * @return 字符串类型的配置对象
     */
    @Override
    public Conf<String> ofString(String confKey, String defaultValue) {
        return fromJson(confKey, String.class, defaultValue);
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个布尔类型的配置对象。
     *
     * @param confKey      配置键
     * @param defaultValue 默认值
     * @return 布尔类型的配置对象
     */
    @Override
    public Conf<Boolean> ofBoolean(String confKey, boolean defaultValue) {
        return fromJson(confKey, Boolean.class, defaultValue);
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个泛型类型的配置对象。
     *
     * @param confKey      配置键
     * @param tClass       泛型类型
     * @param defaultValue 默认值
     * @return 泛型类型的配置对象
     */
    @Override
    public <T> Conf<T> ofJson(String confKey, Class<T> tClass, T defaultValue) {
        return fromJson(confKey, tClass, defaultValue);
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个泛型列表类型的配置对象。
     *
     * @param confKey      配置键
     * @param defaultValue 默认值
     * @param tClass       泛型类型
     * @return 泛型列表类型的配置对象
     */
    @Override
    public <T> Conf<List<T>> ofList(String confKey, List<T> defaultValue, Class<T> tClass) {
        return fromJsonList(confKey, tClass, defaultValue);
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个字符串列表类型的配置对象。
     *
     * @param confKey      配置键
     * @param defaultValue 默认值
     * @return 字符串列表类型的配置对象
     */
    @Override
    public Conf<List<String>> ofStringList(String confKey, List<String> defaultValue) {
        return fromJsonList(confKey, String.class, defaultValue);
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个整数列表类型的配置对象。
     *
     * @param confKey      配置键
     * @param defaultValue 默认值
     * @return 整数列表类型的配置对象
     */
    @Override
    public Conf<List<Integer>> ofIntList(String confKey, List<Integer> defaultValue) {
        return fromJsonList(confKey, Integer.class, defaultValue);
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个长整数列表类型的配置对象。
     *
     * @param confKey      配置键
     * @param defaultValue 默认值
     * @return 长整数列表类型的配置对象
     */
    @Override
    public Conf<List<Long>> ofLongList(String confKey, List<Long> defaultValue) {
        return fromJsonList(confKey, Long.class, defaultValue);
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个泛型映射类型的配置对象。
     *
     * @param confKey      配置键
     * @param kClass       键的泛型类型
     * @param vClass       值的泛型类型
     * @param defaultValue 默认值
     * @return 泛型映射类型的配置对象
     */
    @Override
    public <K, V> Conf<Map<K, V>> ofMap(String confKey, Class<K> kClass, Class<V> vClass, Map<K, V> defaultValue) {
        return (Conf<Map<K, V>>) confMap.computeIfAbsent(new ConfKey(confKey, defaultValue),
                k -> new AutoReloadConfImpl<>(k.key(), defaultValue, autoReloadConfigFactory, s -> JsonUtils.fromJsonMap(s, kClass, vClass)));
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个泛型类型的配置对象。
     *
     * @param confKey      配置键
     * @param tClass       泛型类型
     * @param defaultValue 默认值
     * @return 泛型类型的配置对象
     */
    private <T> Conf<T> fromJson(String confKey, Class<T> tClass, T defaultValue) {
        return (Conf<T>) confMap.computeIfAbsent(new ConfKey(confKey, defaultValue),
                k -> new AutoReloadConfImpl<>(k.key(), defaultValue, autoReloadConfigFactory, s -> JsonUtils.fromJson(s, tClass)));
    }

    /**
     * 根据给定的配置键、默认值和类型，返回一个泛型列表类型的配置对象。
     *
     * @param confKey      配置键
     * @param tClass       泛型类型
     * @param defaultValue 默认值
     * @return 泛型列表类型的配置对象
     */
    private <T> Conf<List<T>> fromJsonList(String confKey, Class<T> tClass, List<T> defaultValue) {
        return (Conf<List<T>>) confMap.computeIfAbsent(new ConfKey(confKey, defaultValue),
                k -> new AutoReloadConfImpl<>(k.key(), defaultValue, autoReloadConfigFactory, s -> JsonUtils.fromJsonList(s, tClass)));
    }

    /**
     * 配置键记录类，包含键和默认值。
     */
    private record ConfKey(String key, Object defaultValue) {

        /**
         * 判断两个配置键是否相等。
         *
         * @param o 另一个配置键对象
         * @return 如果相等返回true，否则返回false
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ConfKey confKey = (ConfKey) o;
            return Objects.equals(key, confKey.key) && Objects.equals(defaultValue, confKey.defaultValue);
        }

        /**
         * 计算配置键的哈希值。
         *
         * @return 配置键的哈希值
         */
        @Override
        public int hashCode() {
            return Objects.hash(key, defaultValue);
        }
    }
}
