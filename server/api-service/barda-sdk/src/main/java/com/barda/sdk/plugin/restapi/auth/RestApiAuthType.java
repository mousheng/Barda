package com.barda.sdk.plugin.restapi.auth;

/**
 * 表示REST API身份验证类型的枚举。
 */
public enum RestApiAuthType {

    /**
     * 无身份验证
     */
    NO_AUTH,

    /**
     * 基本身份验证
     */
    BASIC_AUTH,

    /**
     * 摘要身份验证
     */
    DIGEST_AUTH,

    /**
     * Bearer令牌身份验证
     */
    BEARER_TOKEN_AUTH,

    /**
     * OAuth2身份验证
     */
    OAUTH2,

    /**
     * 从登录中继承的OAuth2身份验证
     */
    OAUTH2_INHERIT_FROM_LOGIN,
}