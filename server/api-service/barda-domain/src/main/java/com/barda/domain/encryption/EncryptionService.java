package com.barda.domain.encryption;

/**
 * 定义数据加密/解密服务的接口。
 * 该接口提供字符串和密码的加密/解密功能。
 */
public interface EncryptionService {

    /**
     * 加密字符串。
     *
     * @param plaintext 原始字符串
     * @return 已加密的字符串
     */
    String encryptString(String plaintext);

    /**
     * 解密字符串。
     *
     * @param encryptedText 已加密的字符串
     * @return 原始字符串
     */
    String decryptString(String encryptedText);

    /**
     * 加密密码。
     *
     * 请注意，此方法不应返回明文密码。
     * 相反，它应返回已加密的密码，以便在需要时进行解密。
     *
     * @param plaintext 原始密码
     * @return 已加密的密码
     */
    String encryptPassword(String plaintext);

    /**
     * 匹配密码。
     *
     * 请注意，此方法不应返回布尔值以外的任何内容。
     * 相反，它应返回一个布尔值，指示提供的密码是否与已存储的密码匹配。
     *
     * @param password1 要匹配的密码
     * @param password2 已存储的密码
     * @return 密码是否匹配
     */
    boolean matchPassword(String password1, String password2);

}
