package com.barda.sdk.plugin.restapi.auth;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonView;
import com.barda.sdk.config.SerializeConfig.JsonViews;

import lombok.Getter;

/**
 * 不仅是基本身份验证配置，还包括摘要身份验证配置。
 */
@Getter
public final class BasicAuthConfig extends AuthConfig {

    /**
     * 用户名
     */
    private final String username;

    /**
     * 密码
     */
    @JsonView(JsonViews.Internal.class)
    private String password;

    /**
     * 构造一个新的基本身份验证配置对象。
     *
     * @param username 用户名
     * @param password 密码
     * @param type     身份验证类型
     */
    @JsonCreator
    public BasicAuthConfig(String username, String password, RestApiAuthType type) {
        super(type);
        this.username = username;
        this.password = password;
    }

    /**
     * 加密密码。
     *
     * @param encryptFunc 加密函数
     */
    @Override
    public void doEncrypt(Function<String, String> encryptFunc) {
        this.password = encryptFunc.apply(this.password);
    }

    /**
     * 解密密码。
     *
     * @param decryptFunc 解密函数
     */
    @Override
    public void doDecrypt(Function<String, String> decryptFunc) {
        this.password = decryptFunc.apply(this.password);
    }

    /**
     * 与更新的配置合并。
     *
     * @param updatedConfig 更新的身份验证配置
     * @return 合并后的身份验证配置
     */
    @Override
    public AuthConfig mergeWithUpdatedConfig(@Nullable AuthConfig updatedConfig) {
        // 如果身份验证类型改变，返回新的身份验证配置
        if (!(updatedConfig instanceof BasicAuthConfig basicAuthConfig)) {
            return updatedConfig;
        }
        // 否则合并基本身份验证配置
        return new BasicAuthConfig(basicAuthConfig.getUsername(),
                ObjectUtils.firstNonNull(basicAuthConfig.getPassword(), this.password),
                basicAuthConfig.getType());
    }
}