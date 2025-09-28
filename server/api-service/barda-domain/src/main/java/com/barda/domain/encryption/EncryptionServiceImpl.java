package com.barda.domain.encryption;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.config.CommonConfig.Encrypt;

/**
 * 实现数据加密/解密服务的类。
 * 该类提供字符串和密码的加密/解密功能。
 */
@Service
public class EncryptionServiceImpl implements EncryptionService {

    private final TextEncryptor textEncryptor;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    /**
     * 构造函数。
     *
     * @param commonConfig 通用配置
     */
    @Autowired
    public EncryptionServiceImpl(CommonConfig commonConfig) {
        Encrypt encrypt = commonConfig.getEncrypt();
        String saltInHex = Hex.encodeHexString(encrypt.getSalt().getBytes());
        this.textEncryptor = Encryptors.text(encrypt.getPassword(), saltInHex);
    }

    /**
     * 加密字符串。
     *
     * @param plaintext 原始字符串
     * @return 已加密的字符串
     */
    @Override
    public String encryptString(String plaintext) {
        if (StringUtils.isEmpty(plaintext)) {
            return plaintext;
        }
        return textEncryptor.encrypt(plaintext);
    }

    /**
     * 解密字符串。
     *
     * @param encryptedText 已加密的字符串
     * @return 原始字符串
     */
    @Override
    public String decryptString(String encryptedText) {
        if (StringUtils.isEmpty(encryptedText)) {
            return encryptedText;
        }
        return textEncryptor.decrypt(encryptedText);
    }

    /**
     * 加密密码。
     *
     * 请注意，此方法不应返回明文密码。
     * 相反，它应返回已加密的密码，以便在需要时进行解密。
     *
     * @param plaintext 原始密码
     * @return 已加密的密码
     */
    @Override
    public String encryptPassword(String plaintext) {
        if (StringUtils.isEmpty(plaintext)) {
            return StringUtils.EMPTY;
        }
        return bCryptPasswordEncoder.encode(plaintext);
    }

    /**
     * 匹配密码。
     *
     * 请注意，此方法不应返回布尔值以外的任何内容。
     * 相反，它应返回一个布尔值，指示提供的密码是否与已存储的密码匹配。
     *
     * @param rawPassword 要匹配的密码
     * @param encodedPassword 已存储的密码
     * @return 密码是否匹配
     */
    @Override
    public boolean matchPassword(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }

}
