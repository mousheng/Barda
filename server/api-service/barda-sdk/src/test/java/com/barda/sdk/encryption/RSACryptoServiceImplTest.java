package com.barda.sdk.encryption;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RSACryptoServiceImplTest extends TestCase {
    private RSACryptoServiceImpl rsaCryptoServiceImpl;

    public void setUp() throws Exception {
        super.setUp();
        rsaCryptoServiceImpl = new RSACryptoServiceImpl();
    }

    public void test() {
        String publicKeyPath = "./public.key";
        String privateKeyPath = "./private.key";
        String data = "测试RSA加解密";
        try {
            String encrypted = rsaCryptoServiceImpl.encrypt(data);
            System.out.println("加密后:" + encrypted);
            String decrypted = rsaCryptoServiceImpl.decrypt(encrypted);
            System.out.println("解密后:" + decrypted);

            // 检查是否生成公钥
            Assert.assertTrue(Files.exists(Paths.get(publicKeyPath)));
            // 检查是否生成私钥
            Assert.assertTrue(Files.exists(Paths.get(privateKeyPath)));

            // 检查加解密是否成功
            Assert.assertEquals(data, decrypted);
            // 删除文件
            Assert.assertTrue(new File(publicKeyPath).delete());
            Assert.assertTrue(new File(privateKeyPath).delete());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}