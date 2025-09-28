package com.barda.sdk.plugin.restapi.auth;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 表示默认身份验证配置类，继承自AuthConfig。
 */
public class DefaultAuthConfig extends AuthConfig {

    /**
     * 构造一个新的默认身份验证配置对象。
     *
     * @param type 身份验证类型
     */
    @JsonCreator
    protected DefaultAuthConfig(RestApiAuthType type) {
        super(type);
    }
}
