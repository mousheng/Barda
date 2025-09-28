package com.barda.sdk.util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * 枚举工具类。
 * 该类提供了一组静态方法来操作和检查枚举值。
 */
public class EnumUtils {

    /**
     * 检查枚举值是否存在重复。
     *
     * @param values  要检查的枚举值数组。
     * @param valueExtractor  提取枚举值的方法。
     * @param <T>  枚举值的类型。
     * @throws RuntimeException 如果存在重复的枚举值，则抛出此异常。
     */
    public static <T extends Enum<T>> void checkDuplicates(T[] values, Function<T, Integer> valueExtractor) {
        Set<Integer> seen = new HashSet<>();
        for (T enum0 : values) {
            boolean add = seen.add(valueExtractor.apply(enum0));
            if (!add) {
                throw new RuntimeException("duplicated enum value: " + enum0);
            }
        }
    }
}
