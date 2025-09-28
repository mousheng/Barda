package com.barda.infra.util;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple5;
import reactor.util.function.Tuples;

/**
 * 一个提供合并元组的实用工具类。
 */
public class TupleUtils {

    /**
     * 合并一个元素和一个二元组。
     *
     * @param u 要合并的元素
     * @param t 要合并的二元组
     * @param <U> 元素的类型
     * @param <T1> 二元组的第一个元素的类型
     * @param <T2> 二元组的第二个元素的类型
     * @return 合并后的三元组
     */
    public static <U, T1, T2> Tuple3<U, T1, T2> merge(U u, Tuple2<T1, T2> t) {
        return Tuples.of(u, t.getT1(), t.getT2());
    }

    /**
     * 合并一个二元组和一个元素。
     *
     * @param t 要合并的二元组
     * @param u 要合并的元素
     * @param <T1> 二元组的第一个元素的类型
     * @param <T2> 二元组的第二个元素的类型
     * @param <U> 元素的类型
     * @return 合并后的三元组
     */
    public static <T1, T2, U> Tuple3<T1, T2, U> merge(Tuple2<T1, T2> t, U u) {
        return Tuples.of(t.getT1(), t.getT2(), u);
    }

    /**
     * 合并一个元素和一个三元组。
     *
     * @param u 要合并的元素
     * @param t 要合并的三元组
     * @param <U> 元素的类型
     * @param <T1> 三元组的第一个元素的类型
     * @param <T2> 三元组的第二个元素的类型
     * @param <T3> 三元组的第三个元素的类型
     * @return 合并后的四元组
     */
    public static <U, T1, T2, T3> Tuple4<U, T1, T2, T3> merge(U u, Tuple3<T1, T2, T3> t) {
        return Tuples.of(u, t.getT1(), t.getT2(), t.getT3());
    }

    /**
     * 合并一个三元组和一个元素。
     *
     * @param t 要合并的三元组
     * @param u 要合并的元素
     * @param <T1> 三元组的第一个元素的类型
     * @param <T2> 三元组的第二个元素的类型
     * @param <T3> 三元组的第三个元素的类型
     * @param <U> 元素的类型
     * @return 合并后的四元组
     */
    public static <T1, T2, T3, U> Tuple4<T1, T2, T3, U> merge(Tuple3<T1, T2, T3> t, U u) {
        return Tuples.of(t.getT1(), t.getT2(), t.getT3(), u);
    }

    /**
     * 合并一个元素和一个四元组。
     *
     * @param u 要合并的元素
     * @param t 要合并的四元组
     * @param <U> 元素的类型
     * @param <T1> 四元组的第一个元素的类型
     * @param <T2> 四元组的第二个元素的类型
     * @param <T3> 四元组的第三个元素的类型
     * @param <T4> 四元组的第四个元素的类型
     * @return 合并后的五元组
     */
    public static <U, T1, T2, T3, T4> Tuple5<U, T1, T2, T3, T4> merge(U u, Tuple4<T1, T2, T3, T4> t) {
        return Tuples.of(u, t.getT1(), t.getT2(), t.getT3(), t.getT4());
    }

    /**
     * 合并一个四元组和一个元素。
     *
     * @param t 要合并的四元组
     * @param u 要合并的元素
     * @param <T1> 四元组的第一个元素的类型
     * @param <T2> 四元组的第二个元素的类型
     * @param <T3> 四元组的第三个元素的类型
     * @param <T4> 四元组的第四个元素的类型
     * @param <U> 元素的类型
     * @return 合并后的五元组
     */
    public static <T1, T2, T3, T4, U> Tuple5<T1, T2, T3, T4, U> merge(Tuple4<T1, T2, T3, T4> t, U u) {
        return Tuples.of(t.getT1(), t.getT2(), t.getT3(), t.getT4(), u);
    }
}
