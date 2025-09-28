package com.barda.sdk.config.dynamic;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 该接口表示一个配置。
 *
 * 该接口使用了 Java 8 的函数式接口来表示一个配置，并继承了 Supplier 接口。
 */
public interface Conf<T> extends Supplier<T> {

    /**
     * 然后执行一个函数来转换配置。
     *
     * 该方法返回一个新的 Conf 对象，该对象的 get() 方法返回的结果是通过 mapper 函数转换的结果。
     *
     * @param mapper 一个函数，用来将 T 类型转换为 K 类型
     * @param <K> 转换后的类型
     * @return 一个新的 Conf 对象
     */
    default <K> Conf<K> then(Function<T, K> mapper) {
        return () -> mapper.apply(this.get());
    }
}
