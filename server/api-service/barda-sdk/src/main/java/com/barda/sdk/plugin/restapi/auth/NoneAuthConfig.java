package com.barda.sdk.plugin.restapi.auth;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 表示无身份验证配置类，继承自AuthConfig。
 */
public class NoneAuthConfig extends AuthConfig {

    /**
     * 构造一个新的无身份验证配置对象。
     *
     * @param type 身份验证类型
     */
    @JsonCreator
    public NoneAuthConfig(RestApiAuthType type) {
        super(type);
    }
}
