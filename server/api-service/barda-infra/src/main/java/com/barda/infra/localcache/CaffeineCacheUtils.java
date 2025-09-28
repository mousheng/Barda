package com.barda.infra.localcache;

import java.util.function.Supplier;

import com.github.benmanes.caffeine.cache.Cache;

import reactor.core.publisher.Mono;

/**
 * CaffeineCacheUtils 类提供了一些用于操作Caffeine缓存的工具方法。
 */
public class CaffeineCacheUtils {

    /**
     * 根据给定的缓存、键和值加载器，获取缓存中的值。如果缓存中存在该键对应的值，则直接返回；否则，使用值加载器加载值并将其存入缓存。
     * <p>
     * 注意：创建缓存时不应使用值加载器。以下是一个创建缓存的示例：
     * <pre>
     * Cache&lt;Object, Object&gt; cache = Caffeine.newBuilder()
     *     .maximumSize()
     *     .expireAfterWrite()
     *     ...
     *     .build();
     * </pre>
     *
     * @param cache 缓存对象，不应包含值加载器
     * @param key   要获取的值的键
     * @param valueMonoSupplier 值加载器，当缓存中不存在键对应的值时，使用此加载器加载值
     * @return 一个 Mono<V> 对象，表示缓存中的值或通过值加载器加载的值
     */
    public static <K, V> Mono<V> getCacheValueMono(Cache<K, V> cache, K key, Supplier<Mono<V>> valueMonoSupplier) {
        V v = cache.getIfPresent(key);
        if (v != null) {
            return Mono.just(v);
        }
        return valueMonoSupplier.get()
                .doOnNext(value -> cache.put(key, value));
    }
}

