package com.barda.sdk.plugin.restapi.auth;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.barda.sdk.models.Encrypt;

import lombok.Getter;

/**
 * 表示身份验证配置的抽象类。
 * 该类使用Jackson注解进行多态序列化和反序列化。
 */
@Getter
@JsonTypeInfo(use = Id.NAME, property = "type", visible = true, defaultImpl = DefaultAuthConfig.class)
@JsonSubTypes({
        @Type(value = BasicAuthConfig.class, name = "DIGEST_AUTH"),
        @Type(value = BasicAuthConfig.class, name = "BASIC_AUTH"),
        @Type(value = NoneAuthConfig.class, name = "NO_AUTH"),
        @Type(value = DefaultAuthConfig.class, name = "OAUTH2_INHERIT_FROM_LOGIN")
})
public abstract class AuthConfig implements Encrypt {

    /**
     * 身份验证类型
     */
    protected final RestApiAuthType type;

    /**
     * 构造一个新的身份验证配置对象。
     *
     * @param type 身份验证类型
     */
    protected AuthConfig(RestApiAuthType type) {
        this.type = type;
    }

    /**
     * 与更新的配置合并。
     *
     * @param updatedConfig 更新的身份验证配置
     * @return 合并后的身份验证配置
     */
    public AuthConfig mergeWithUpdatedConfig(@Nullable AuthConfig updatedConfig) {
        return updatedConfig;
    }
}
