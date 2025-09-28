package com.barda.infra.util;

import java.util.List;

import reactor.core.publisher.Mono;

/**
 * 一个Mono类型实用工具类，用于处理Reactor的Mono类型。
 */
public class MonoUtils {

    /**
     * 如果Mono中包含的列表为空，则返回一个空的Mono。
     *
     * @param <T> 列表元素的类型
     * @param mono 要检查的Mono
     * @return 如果Mono中包含的列表为空，则返回一个空的Mono，否则返回包含相同列表的Mono
     */
    public static <T> Mono<List<T>> emptyMonoIfEmptyList(Mono<List<T>> mono) {
        return mono.flatMap(value -> {
            if (value.isEmpty()) {
                return Mono.empty();
            }
            return Mono.just(value);
        });
    }

    /**
     * 如果传入的对象为空，则返回一个空的Mono。
     *
     * @param <T> 对象的类型
     * @param t 要检查的对象
     * @return 如果传入的对象为空，则返回一个空的Mono，否则返回包含相同对象的Mono
     */
    public static <T> Mono<T> emptyIfNull(T t) {
        if (t == null) {
            return Mono.empty();
        }
        return Mono.just(t);
    }
}
