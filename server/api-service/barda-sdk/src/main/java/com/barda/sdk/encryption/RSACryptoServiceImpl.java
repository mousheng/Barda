package com.barda.sdk.encryption;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Getter
@Service
@Slf4j
public class RSACryptoServiceImpl implements RSACryptoService {

    public String publicKeyPath = System.getProperty("user.dir") + "/public.key";
    public String privateKeyPath = System.getProperty("user.dir") + "/private.key";

    public String publicKeyString;
    public String privateKeyString;

    public PrivateKey privateKey;
    public PublicKey publicKey;

    public RSACryptoServiceImpl() {
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
        final Cipher cipher = Cipher.getInstance(algorithmDetails);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    @Override
    public String encrypt(String data) throws Exception {
        return Base64.encodeBase64String(encrypt(data.getBytes()));
    }

    @Override
    public byte[] decrypt(byte[] encryptedData) throws Exception {
        final Cipher cipher = Cipher.getInstance(algorithmDetails);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    @Override
    public String decrypt(String encryptedData) throws Exception {
        return new String(decrypt(Base64.decodeBase64(encryptedData)));
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
        return new String(decrypt(Base64.decodeBase64(encryptedData)));
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
        return publicKeyString;
    }

}
