package com.barda.sdk.config.dynamic;

import java.util.List;
import java.util.Map;

/**
 * 该类表示一个静态的 ConfigInstance 实现。
 *
 * 该类实现了 ConfigInstance 接口，并提供了一组方法来从配置中获取不同类型的配置值。
 */
public class StaticConfigInstanceImpl implements ConfigInstance {

    /**
     * 覆盖的键值对
     */
    private final Map<String, Object> overrideKeyValues;

    /**
     * 构造函数
     *
     * @param overrideKeyValues 覆盖的键值对
     */
    public StaticConfigInstanceImpl(Map<String, Object> overrideKeyValues) {
        this.overrideKeyValues = overrideKeyValues;
    }

    /**
     * 获取一个 Integer 配置值
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    @Override
    public Conf<Integer> ofInteger(String confKey, int defaultValue) {
        return new StaticConf<>(defaultValue, confKey, overrideKeyValues);
    }

    /**
     * 获取一个 String 配置值
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    @Override
    public Conf<String> ofString(String confKey, String defaultValue) {
        return new StaticConf<>(defaultValue, confKey, overrideKeyValues);
    }

    /**
     * 获取一个 Boolean 配置值
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    @Override
    public Conf<Boolean> ofBoolean(String confKey, boolean defaultValue) {
        return new StaticConf<>(defaultValue, confKey, overrideKeyValues);
    }

    /**
     * 获取一个 JSON 配置值
     *
     * @param confKey 配置键
     * @param tClass 要转换的类型
     * @param defaultValue 默认值
     * @param <T> 要转换的类型
     * @return 一个 Conf 对象
     */
    @Override
    public <T> Conf<T> ofJson(String confKey, Class<T> tClass, T defaultValue) {
        return new StaticConf<>(defaultValue, confKey, overrideKeyValues);
    }

    /**
     * 获取一个 List 配置值
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @param tClass 要转换的类型
     * @param <T> 要转换的类型
     * @return 一个 Conf 对象
     */
    @Override
    public <T> Conf<List<T>> ofList(String confKey, List<T> defaultValue, Class<T> tClass) {
        return new StaticConf<>(defaultValue, confKey, overrideKeyValues);
    }

    /**
     * 获取一个 List<String> 配置值
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    @Override
    public Conf<List<String>> ofStringList(String confKey, List<String> defaultValue) {
        return new StaticConf<>(defaultValue, confKey, overrideKeyValues);
    }

    /**
     * 获取一个 List<Integer> 配置值
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    @Override
    public Conf<List<Integer>> ofIntList(String confKey, List<Integer> defaultValue) {
        return new StaticConf<>(defaultValue, confKey, overrideKeyValues);
    }

    /**
     * 获取一个 List<Long> 配置值
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    @Override
    public Conf<List<Long>> ofLongList(String confKey, List<Long> defaultValue) {
        return new StaticConf<>(defaultValue, confKey, overrideKeyValues);
    }

    /**
     * 获取一个 Map 配置值
     *
     * @param confKey 配置键
     * @param kClass 要转换的键的类型
     * @param vClass 要转换的值的类型
     * @param defaultValue 默认值
     * @param <K> 要转换的键的类型
     * @param <V> 要转换的值的类型
     * @return 一个 Conf 对象
     */
    @Override
    public <K, V> Conf<Map<K, V>> ofMap(String confKey, Class<K> kClass, Class<V> vClass, Map<K, V> defaultValue) {
        return new StaticConf<>(defaultValue, confKey, overrideKeyValues);
    }
}
