package com.barda.sdk.constants;

/**
 * 身份验证类，提供对用户身份的验证和鉴权功能。
 */
public class Authentication {

    /**
     * 匿名用户的用户名
     */
    public static String ANONYMOUS_USER = "anonymous";

    /**
     * 匿名用户的用户 ID
     */
    public static String ANONYMOUS_USER_ID = "anonymousId";

    /**
     * 判断是否为匿名用户
     *
     * @param userId 用户 ID
     * @return true - 匿名用户, false - 非匿名用户
     */
    public static boolean isAnonymousUser(String userId) {
        return ANONYMOUS_USER_ID.equals(userId);
    }

    /**
     * 判断是否不是匿名用户
     *
     * @param userId 用户 ID
     * @return true - 非匿名用户, false - 匿名用户
     */
    public static boolean isNotAnonymousUser(String userId) {
        return !isAnonymousUser(userId);
    }
}
