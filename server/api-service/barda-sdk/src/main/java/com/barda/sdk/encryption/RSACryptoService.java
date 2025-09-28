package com.barda.sdk.encryption;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * RSA 加密解密服务接口。
 * 该接口定义了 RSA 算法的公钥和私钥路径，算法名称，
 * 以及对数据进行加密和解密的各种方法。
 */
public interface RSACryptoService {
    /**
     * 算法类型
     */
    String algorithmType = "RSA";

    /**
     * RSA 算法详情。
     */
    String algorithmDetails = "RSA/ECB/PKCS1Padding";

    /**
     * 加密原始字节数组。
     *
     * @param data 原始数据
     * @return 加密后的数据
     * @throws Exception 可能的加密异常
     */
    byte[] encrypt(byte[] data) throws Exception;

    /**
     * 加密原始字符串。
     *
     * @param data 原始数据
     * @return 加密后的数据
     * @throws Exception 可能的加密异常
     */
    String encrypt(String data) throws Exception;

    /**
     * 解密已加密的字节数组。
     *
     * @param encryptedData 已加密的数据
     * @return 解密后的数据
     * @throws Exception 可能的解密异常
     */
    byte[] decrypt(byte[] encryptedData) throws Exception;

    /**
     * 解密已加密的字符串。
     *
     * @param encryptedData 已加密的数据
     * @return 解密后的数据
     * @throws Exception 可能的解密异常
     */
    String decrypt(String encryptedData) throws Exception;

    /**
     * 直接解密函数
     *
     * @param encryptedData 已加密的数据
     * @return 如果解密失败，则返回传入数据
     */
    String pureDecryt(String encryptedData);

    /**
     * 解密已加密的 Base64 字符串。
     *
     * @param encryptedData 已加密的 Base64 数据
     * @return 解密后的数据
     * @throws Exception 可能的解密异常
     */
    String decryptBase64(String encryptedData) throws Exception;

    /**
     * 从指定路径获取公钥。
     *
     * @param publicKeyPath 公钥路径
     * @return 公钥
     * @throws Exception 可能的读取公钥异常
     */
    PublicKey getPublicKeyFrom(String publicKeyPath) throws Exception;

    /**
     * 从指定路径获取私钥。
     *
     * @param privateKeyPath 私钥路径
     * @return 私钥
     * @throws Exception 可能的读取私钥异常
     */
    PrivateKey getPrivateKeyFrom(String privateKeyPath) throws Exception;

    /**
     * 获取公钥字符串。
     *
     * @return 公钥字符串
     */
    String getPUblicKeyString();
}
