package com.barda.sdk.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

/**
 * 一个提供更多的Map操作的实用工具类。
 * 该类在 {@link org.apache.commons.collections4.MapUtils} 的基础上添加了更多的方法。
 */
public class MoreMapUtils {

    /**
     * 获取指定键的值对应的List，如果List不存在，返回默认的List。
     *
     * @param map          要操作的Map
     * @param key          要获取值的键
     * @param defaultValue 默认的List
     * @param <K>          键的类型
     * @param <T>          值的类型
     * @return 值对应的List，如果List不存在，返回默认的List
     */
    public static <K, T> List<T> getList(final Map<? super K, ?> map, final K key, final List<T> defaultValue) {
        List<T> answer = getList(map, key);
        if (answer == null) {
            answer = defaultValue;
        }
        return answer;
    }

    /**
     * 获取指定键的值对应的List。
     *
     * @param map 要操作的Map
     * @param key 要获取值的键
     * @param <K> 键的类型
     * @param <T> 值的类型
     * @return 值对应的List，如果List不存在，返回null
     */
    @SuppressWarnings("unchecked")
    public static <K, T> List<T> getList(final Map<? super K, ?> map, final K key) {
        if (map != null) {
            final Object answer = map.get(key);
            if (answer instanceof List<?>) {
                return (List<T>) answer;
            }
        }
        return null;
    }

    /**
     * 判断Map中是否包含指定键，并且该键的值对应的String类型的值在不区分大小写的情况下等于指定的键。
     *
     * @param map 要操作的Map
     * @param key 要判断的键
     * @return 如果包含，返回true；否则，返回false
     */
    public static boolean containsStringKeyIgnoreCase(Map<?, ?> map, String key) {
        for (Entry<?, ?> entry : map.entrySet()) {
            Object o = entry.getKey();
            if (o instanceof String s && s.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建一个只有一个键值对的Map。
     *
     * @param key   键
     * @param value 值
     * @param <K>   键的类型
     * @param <V>   值的类型
     * @return 只有一个键值对的Map
     */
    public static <K, V> Map<K, V> ofMap(K key, @Nullable V value) {
        HashMap<K, V> result = Maps.newHashMapWithExpectedSize(1);
        result.put(key, value);
        return result;
    }
}