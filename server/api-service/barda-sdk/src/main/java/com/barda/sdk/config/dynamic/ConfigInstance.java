package com.barda.sdk.config.dynamic;

import java.util.List;
import java.util.Map;

/**
 * 该接口表示一个配置实例。
 *
 * 该接口定义了一些方法来获取不同类型的配置值。
 */
public interface ConfigInstance {

    /**
     * 获取一个 Conf 对象，该对象的 get() 方法返回的结果是从配置中获取的 Integer 值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    Conf<Integer> ofInteger(String confKey, int defaultValue);

    /**
     * 获取一个 Conf 对象，该对象的 get() 方法返回的结果是从配置中获取的 String 值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    Conf<String> ofString(String confKey, String defaultValue);

    /**
     * 获取一个 Conf 对象，该对象的 get() 方法返回的结果是从配置中获取的 Boolean 值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    Conf<Boolean> ofBoolean(String confKey, boolean defaultValue);

    /**
     * 获取一个 Conf 对象，该对象的 get() 方法返回的结果是从配置中获取的 JSON 值，并将其转换为指定类型。
     *
     * @param confKey 配置键
     * @param tClass 要转换的类型
     * @param defaultValue 默认值
     * @param <T> 要转换的类型
     * @return 一个 Conf 对象
     */
    <T> Conf<T> ofJson(String confKey, Class<T> tClass, T defaultValue);

    /**
     * 获取一个 Conf 对象，该对象的 get() 方法返回的结果是从配置中获取的 List 值，并将其转换为指定类型。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @param tClass 要转换的类型
     * @param <T> 要转换的类型
     * @return 一个 Conf 对象
     */
    <T> Conf<List<T>> ofList(String confKey, List<T> defaultValue, Class<T> tClass);

    /**
     * 获取一个 Conf 对象，该对象的 get() 方法返回的结果是从配置中获取的 List<String> 值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    Conf<List<String>> ofStringList(String confKey, List<String> defaultValue);

    /**
     * 获取一个 Conf 对象，该对象的 get() 方法返回的结果是从配置中获取的 List<Integer> 值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    Conf<List<Integer>> ofIntList(String confKey, List<Integer> defaultValue);

    /**
     * 获取一个 Conf 对象，该对象的 get() 方法返回的结果是从配置中获取的 List<Long> 值。
     *
     * @param confKey 配置键
     * @param defaultValue 默认值
     * @return 一个 Conf 对象
     */
    Conf<List<Long>> ofLongList(String confKey, List<Long> defaultValue);

    /**
     * 获取一个 Conf 对象，该对象的 get() 方法返回的结果是从配置中获取的 Map 值，并将其转换为指定类型。
     *
     * @param confKey 配置键
     * @param kClass 要转换的键的类型
     * @param vClass 要转换的值的类型
     * @param defaultValue 默认值
     * @param <K> 要转换的键的类型
     * @param <V> 要转换的值的类型
     * @return 一个 Conf 对象
     */
    <K, V> Conf<Map<K, V>> ofMap(String confKey, Class<K> kClass, Class<V> vClass, Map<K, V> defaultValue);
}
