package com.barda.sdk.util;

import java.util.UUID;

/**
 * 一个ID生成器的实用工具类。
 * 该类提供了一个静态方法来生成一个UUID并将其转换为不带连字符的字符串。
 */
public class IDUtils {

    /**
     * 生成一个不带连字符的UUID字符串。
     *
     * @return 一个不带连字符的UUID字符串
     */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
