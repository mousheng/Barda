package com.barda.sdk.encryption;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Getter
@Service
@Slf4j
public class RSACryptoServiceImpl implements RSACryptoService {

    public String publicKeyPath = System.getenv("RSA_PUBLIC_KEY_PATH") != null ? 
        System.getenv("RSA_PUBLIC_KEY_PATH") : 
        "/barda-stacks/config/public.key";
    public String privateKeyPath = System.getenv("RSA_PRIVATE_KEY_PATH") != null ? 
        System.getenv("RSA_PRIVATE_KEY_PATH") : 
        "/barda-stacks/config/private.key";

    public String publicKeyString;
    public String privateKeyString;

    public PrivateKey privateKey;
    public PublicKey publicKey;

    public RSACryptoServiceImpl() {
        this(null, null);
    }

    public RSACryptoServiceImpl(String customPublicKeyPath, String customPrivateKeyPath) {
        // 如果提供了自定义路径，使用自定义路径
        if (customPublicKeyPath != null) {
            this.publicKeyPath = customPublicKeyPath;
        }
        if (customPrivateKeyPath != null) {
            this.privateKeyPath = customPrivateKeyPath;
        }

        // 检查RSA加密是否启用
        String rsaEnabled = System.getenv("RSA_ENABLED");
        if ("false".equals(rsaEnabled)) {
            log.info("RSA加密已禁用，跳过密钥生成");
            return;
        }
        
        try {
            if (Files.exists(Paths.get(publicKeyPath)) && Files.exists(Paths.get(privateKeyPath))) {
                publicKey = getPublicKeyFrom(publicKeyPath);
                privateKey = getPrivateKeyFrom(privateKeyPath);
                publicKeyString = Base64.encodeBase64String(publicKey.getEncoded());
                privateKeyString = Base64.encodeBase64String(privateKey.getEncoded());
                log.debug("RSACryptoService初始化，密钥文件已存在");
                return;
            }
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithmType);
            keyGen.initialize(2048);

            final KeyPair key = keyGen.generateKeyPair();
            
            // 确保密钥文件目录存在
            Path publicKeyDir = Paths.get(publicKeyPath).getParent();
            Path privateKeyDir = Paths.get(privateKeyPath).getParent();
            if (publicKeyDir != null && !Files.exists(publicKeyDir)) {
                Files.createDirectories(publicKeyDir);
                log.debug("创建公钥目录: " + publicKeyDir);
            }
            if (privateKeyDir != null && !Files.exists(privateKeyDir)) {
                Files.createDirectories(privateKeyDir);
                log.debug("创建私钥目录: " + privateKeyDir);
            }
            
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(publicKeyPath))) {
                privateKey = key.getPrivate();
                privateKeyString = Base64.encodeBase64String(key.getPrivate().getEncoded());
                log.debug("RSACryptoService初始化，生成私钥:" + privateKeyString);
                dos.write(key.getPublic().getEncoded());
            }

            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(privateKeyPath))) {
                publicKey = key.getPublic();
                publicKeyString = Base64.encodeBase64String(key.getPublic().getEncoded());
                log.debug("RSACryptoService初始化，生成公钥:" + publicKeyString);
                dos.write(key.getPrivate().getEncoded());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public byte[] encrypt(byte[] data) throws Exception {
        if (publicKey == null) {
            log.warn("RSA加密已禁用，返回原始数据");
            return data;
        }
        final Cipher cipher = Cipher.getInstance(algorithmDetails);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    @Override
    public String encrypt(String data) throws Exception {
        return Base64.encodeBase64String(encrypt(data.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public byte[] decrypt(byte[] encryptedData) throws Exception {
        if (privateKey == null) {
            log.warn("RSA解密已禁用，返回原始数据");
            return encryptedData;
        }
        final Cipher cipher = Cipher.getInstance(algorithmDetails);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    @Override
    public String decrypt(String encryptedData) throws Exception {
        return new String(decrypt(Base64.decodeBase64(encryptedData)), StandardCharsets.UTF_8);
    }

    /**
     * 直接解密函数
     *
     * @param encryptedData 已加密的数据
     * @return 如果解密失败，则返回传入数据
     */
    @Override
    public String pureDecryt(String encryptedData) {
        try {
            return decrypt(encryptedData);
        } catch (Exception e) {
            return encryptedData;
        }
    }

    @Override
    public String decryptBase64(String encryptedData) throws Exception {
        return new String(decrypt(Base64.decodeBase64(encryptedData)), StandardCharsets.UTF_8);
    }

    @Override
    public PublicKey getPublicKeyFrom(String publicKeyPath) throws Exception {
        return KeyFactory.getInstance(algorithmType).generatePublic(new X509EncodedKeySpec(Files.readAllBytes(Paths.get(publicKeyPath))));
    }

    @Override
    public PrivateKey getPrivateKeyFrom(String privateKeyPath) throws Exception {
        return KeyFactory.getInstance(algorithmType).generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get(privateKeyPath))));
    }

    @Override
    public String getPUblicKeyString() {
        if (publicKeyString == null) {
            log.warn("RSA加密已禁用，返回空字符串");
            return "";
        }
        return publicKeyString;
    }

}
