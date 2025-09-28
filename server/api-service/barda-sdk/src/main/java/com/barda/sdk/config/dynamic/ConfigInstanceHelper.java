package com.barda.sdk.config.dynamic;

import java.util.List;
import java.util.Map;

/**
 * 该记录类是一个 ConfigInstance 帮助器类。
 *
 * 该类提供了一组便捷的方法来从 ConfigInstance 中获取不同类型的配置值。
 */
public record ConfigInstanceHelper(ConfigInstance configInstance) {

    /**
     * 获取一个 Integer 值，如果在配置中找不到该值，则返回默认值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Integer 值
     */
    public int ofInteger(String confKey, int defaultValue) {
        return configInstance.ofInteger(confKey, defaultValue).get();
    }

    /**
     * 获取一个 Long 值，如果在配置中找不到该值，则返回默认值。
     *
     * 注意：该方法假设配置中的值是 JSON 格式的，并将其转换为 Long 类型。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Long 值
     */
    public long ofLong(String confKey, long defaultValue) {
        return configInstance.ofJson(confKey, Long.class, defaultValue).get();
    }

    /**
     * 获取一个 String 值，如果在配置中找不到该值，则返回默认值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 String 值
     */
    public String ofString(String confKey, String defaultValue) {
        return configInstance.ofString(confKey, defaultValue).get();
    }

    /**
     * 获取一个 Boolean 值，如果在配置中找不到该值，则返回默认值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Boolean 值
     */
    public boolean ofBoolean(String confKey, boolean defaultValue) {
        return configInstance.ofBoolean(confKey, defaultValue).get();
    }

    /**
     * 获取一个 JSON 值，并将其转换为指定类型，如果在配置中找不到该值，则返回默认值。
     *
     * @param confKey 配置键
     * @param tClass 要转换的类型
     * @param defaultValue 默认值
     * @param <T> 要转换的类型
     * @return 一个 JSON 值
     */
    public <T> T ofJson(String confKey, Class<T> tClass, T defaultValue) {
        return configInstance.ofJson(confKey, tClass, defaultValue).get();
    }

    /**
     * 获取一个 List 值，并将其转换为指定类型，如果在配置中找不到该值，则返回默认值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @param tClass 要转换的类型
     * @param <T> 要转换的类型
     * @return 一个 List 值
     */
    public <T> List<T> ofList(String confKey, List<T> defaultValue, Class<T> tClass) {
        return configInstance.ofList(confKey, defaultValue, tClass).get();
    }

    /**
     * 获取一个 List<String> 值，如果在配置中找不到该值，则返回默认值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 List<String> 值
     */
    public List<String> ofStringList(String confKey, List<String> defaultValue) {
        return configInstance.ofStringList(confKey, defaultValue).get();
    }

    /**
     * 获取一个 List<Integer> 值，如果在配置中找不到该值，则返回默认值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 List<Integer> 值
     */
    public List<Integer> ofIntList(String confKey, List<Integer> defaultValue) {
        return configInstance.ofIntList(confKey, defaultValue).get();
    }

    /**
     * 获取一个 List<Long> 值，如果在配置中找不到该值，则返回默认值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 List<Long> 值
     */
    public List<Long> ofLongList(String confKey, List<Long> defaultValue) {
        return configInstance.ofLongList(confKey, defaultValue).get();
    }

    /**
     * 获取一个 Map 值，并将其转换为指定类型，如果在配置中找不到该值，则返回默认值。
     *
     * @param confKey 配置键
     * @param kClass 要转换的键的类型
     * @param vClass 要转换的值的类型
     * @param defaultValue 默认值
     * @param <K> 要转换的键的类型
     * @param <V> 要转换的值的类型
     * @return 一个 Map 值
     */
    public <K, V> Map<K, V> ofMap(String confKey, Class<K> kClass, Class<V> vClass, Map<K, V> defaultValue) {
        return configInstance.ofMap(confKey, kClass, vClass, defaultValue).get();
    }
}
