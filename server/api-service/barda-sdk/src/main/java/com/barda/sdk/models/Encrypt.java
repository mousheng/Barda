package com.barda.sdk.models;

import java.util.function.Function;

/**
 * 加密接口。
 * 定义了对字符串进行加密和解密的默认方法。
 */
public interface Encrypt {

    /**
     * 执行加密操作。
     *
     * @param encryptFunc 用于加密的函数。
     */
    default void doEncrypt(Function<String, String> encryptFunc) {
    }

    /**
     * 执行解密操作。
     *
     * @param decryptFunc 用于解密的函数。
     */
    default void doDecrypt(Function<String, String> decryptFunc) {
    }
}
