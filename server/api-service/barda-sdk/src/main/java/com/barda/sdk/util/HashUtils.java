package com.barda.sdk.util;

import com.google.common.hash.Hashing;

/**
 * 哈希工具类。
 * 该类提供了一组静态方法来生成哈希值。
 */
public class HashUtils {

    /**
     * 计算 SHA-256 哈希值。
     *
     * @param content  要计算哈希值的字节数组。
     * @return 哈希值的字符串表示。
     *
     * @deprecated 自 Guava 29.0 起，此方法已被废弃，请使用 {@link Hashing#sha256().hashBytes(content).toString()}。
     */
    @SuppressWarnings("UnstableApiUsage")
    public static String hash(byte[] content) {
        return Hashing.sha256().hashBytes(content).toString();
    }
}
