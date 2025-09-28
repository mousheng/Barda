package com.barda.api.authentication.util;

import java.util.Random;

/**
 * 短信验证码相关的实用工具类。
 * 该类提供一些静态方法来生成、验证短信验证码。
 */
public class SmsVerificationUtils {

    /**
     * 验证码的长度。
     * 该值可以根据需要进行调整。
     */
    private static final int VERIFY_SIZE = 6;

    /**
     * 用于生成验证码的数字字符集。
     * 该字符集可以根据需要进行扩展。
     */
    private static final String NUMBER_VERIFY_CODES = "012356789";

    /**
     * 验证码的有效期（单位：分钟）。
     * 该值可以根据需要进行调整。
     */
    public static final int VERIFY_CODE_EXPIRE_MINUTES = 10;

    public static final Random rand = new Random();

    /**
     * 生成一个随机的验证码。
     * 该方法使用 {@link Random} 类来生成一个指定长度的验证码，
     * 验证码由 {@link #NUMBER_VERIFY_CODES} 中的字符组成。
     *
     * @return 随机生成的验证码
     */
    public static String genVerificationCode() {
        int codesLen = NUMBER_VERIFY_CODES.length();
        StringBuilder verifyCode = new StringBuilder(VERIFY_SIZE);
        for (int i = 0; i < VERIFY_SIZE; i++) {
            verifyCode.append(NUMBER_VERIFY_CODES.charAt(rand.nextInt(codesLen - 1)));
        }
        return verifyCode.toString();
    }
}
