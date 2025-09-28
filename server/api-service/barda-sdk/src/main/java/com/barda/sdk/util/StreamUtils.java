package com.barda.sdk.util;

import static java.util.function.Function.identity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * StreamUtils 类提供流操作的实用工具。
 * 该类包含了从集合中收集元素并将其转换为集合、映射或其他数据结构的各种方法。
 */
public class StreamUtils {

    /**
     * 从集合中收集元素并将其转换为 Set。
     *
     * @param <T> 集合元素的类型
     * @param <K> 键的类型
     * @param collection 要收集元素的集合
     * @param mapper 用于将集合元素映射为键的函数
     * @return 包含收集的键的 Set
     */
    public static <T, K> Set<K> collectSet(Collection<T> collection, Function<T, K> mapper) {
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toSet());
    }

    /**
     * 从集合中收集元素并将其转换为 List。
     *
     * @param <T> 集合元素的类型
     * @param <K> 值的类型
     * @param collection 要收集元素的集合
     * @param mapper 用于将集合元素映射为值的函数
     * @return 包含收集的值的 List
     */
    public static <T, K> List<K> collectList(Collection<T> collection, Function<T, K> mapper) {
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * 从集合中收集元素并将其转换为 Map。
     *
     * @param <T> 集合元素的类型
     * @param <K> 键的类型
     * @param <V> 值的类型
     * @param collection 要收集元素的集合
     * @param keyMapper 用于将集合元素映射为键的函数
     * @param valueMapper 用于将集合元素映射为值的函数
     * @return 包含收集的键值对的 Map
     */
    public static <T, K, V> Map<K, V> collectMap(Stream<T> collection, Function<T, K> keyMapper,
            Function<T, V> valueMapper) {
        return collection.collect(Collectors.toMap(keyMapper, valueMapper, (a, b) -> b));
    }

    /**
     * 从给定的流中收集元素，并根据提供的键映射函数生成一个映射，值为流中的元素本身。
     *
     * <p>
     * 该方法将流中的元素收集到一个映射中，键由提供的键映射函数生成，值为流中的元素。
     * </p>
     *
     * @param <T> 流中元素的类型。
     * @param <K> 生成的映射中键的类型。
     * @param collection 一个流，包含要收集到映射中的元素。
     * @param keyMapper 一个函数，接受流中的元素并返回对应的键。
     * @return 一个映射，其中键由键映射函数生成，值为流中的元素。
     * @throws NullPointerException 如果流或键映射函数为空时抛出。
     */
    public static <T, K> Map<K, T> collectMap(Stream<T> collection, Function<T, K> keyMapper) {
        return collectMap(collection, keyMapper, identity());
    }


    /**
     * 从给定的集合中收集元素，并根据提供的键映射函数和值映射函数生成一个映射。
     *
     * <p>
     * 该方法将集合中的元素收集到一个映射中，键由提供的键映射函数生成，值由提供的值映射函数生成。
     * </p>
     *
     * @param <T> 集合中元素的类型。
     * @param <K> 生成的映射中键的类型。
     * @param <V> 生成的映射中值的类型。
     * @param collection 一个集合，包含要收集到映射中的元素。
     * @param keyMapper 一个函数，接受集合中的元素并返回对应的键。
     * @param valueMapper 一个函数，接受集合中的元素并返回对应的值。
     * @return 一个映射，其中键由键映射函数生成，值由值映射函数生成。
     * @throws NullPointerException 如果集合、键映射函数或值映射函数为空时抛出。
     */
    public static <T, K, V> Map<K, V> collectMap(Collection<T> collection, Function<T, K> keyMapper,
            Function<T, V> valueMapper) {
        return collectMap(collection.stream(), keyMapper, valueMapper);
    }


    /**
     * 创建一个 Predicate，用于从集合中收集元素并将其转换为 Map，
     * 并根据键来消除重复的元素。
     *
     * @param <T> 集合元素的类型
     * @param <K> 键的类型
     * @param keyExtractor 用于从集合元素中提取键的函数
     * @return 用于消除重复元素的 Predicate
     */
    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 创建一个 Collector，用于从集合中收集元素并将其转换为 Map，
     * 并处理 null 值。
     *
     * @param <T> 集合元素的类型
     * @param <K> 键的类型
     * @param <U> 值的类型
     * @param keyMapper 用于将集合元素映射为键的函数
     * @param valueMapper 用于将集合元素映射为值的函数
     * @return 用于处理 null 值的 Collector
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMapNullFriendly(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        @SuppressWarnings("unchecked")
        U none = (U) new Object();
        return Collectors.collectingAndThen(
                Collectors.<T, K, U> toMap(keyMapper,
                        valueMapper.andThen(v -> v == null ? none : v)
                ), map -> {
                    map.replaceAll((k, v) -> v == none ? null : v);
                    return map;
                });
    }

    /**
     * 创建一个 Collector，用于从集合中收集元素并将其转换为 Map，
     * 并处理 null 值和键值对的合并。
     *
     * @param <T> 集合元素的类型
     * @param <K> 键的类型
     * @param <U> 值的类型
     * @param keyMapper 用于将集合元素映射为键的函数
     * @param valueMapper 用于将集合元素映射为值的函数
     * @param mergeFunction 用于合并键值对的函数
     * @return 用于处理 null 值和键值对的合并的 Collector
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMapNullFriendly(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper,
            BinaryOperator<U> mergeFunction) {
        @SuppressWarnings("unchecked")
        U none = (U) new Object();
        return Collectors.collectingAndThen(
                Collectors.<T, K, U> toMap(keyMapper,
                        valueMapper.andThen(v -> v == null ? none : v),
                        mergeFunction
                ), map -> {
                    map.replaceAll((k, v) -> v == none ? null : v);
                    return map;
                });
    }
}
