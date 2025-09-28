package com.barda.sdk.auth;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonView;
import com.barda.sdk.auth.constants.AuthTypeConstants;
import com.barda.sdk.auth.constants.Oauth2Constants;
import com.barda.sdk.config.SerializeConfig.JsonViews;

import lombok.Getter;

/**
 * 一个简单的 OAuth2 身份验证配置。
 *
 * 该类继承自 AbstractAuthConfig 类，并使用了 Lombok 库来实现 getter。
 */
@Getter
public class Oauth2SimpleAuthConfig extends AbstractAuthConfig {

    /**
     * 客户端 ID。
     */
    protected String clientId;
    /**
     * 客户端密钥。
     *
     * 该字段使用了 JsonView 注解来实现 JSON 视图的控制。
     */
    @JsonView(JsonViews.Internal.class)
    protected String clientSecret;

    /**
     * 构造函数。
     *
     * @param id ID
     * @param enable 是否启用
     * @param enableRegister 是否启用注册
     * @param source 来源
     * @param sourceName 来源名称
     * @param clientId 客户端 ID
     * @param clientSecret 客户端密钥
     * @param authType 身份验证类型
     */
    @JsonCreator
    public Oauth2SimpleAuthConfig(
            @Nullable String id,
            Boolean enable,
            Boolean enableRegister,
            String source,
            String sourceName,
            String clientId,
            String clientSecret,
            String authType) {
        super(id, source, sourceName, enable, enableRegister, authType);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * 用于 FE 的方法。
     * <p>
     * 我们只在此处渲染客户端 ID，将重定向 URL 和状态由 FE 渲染。
     */
    @SuppressWarnings("unused")
    @JsonView(JsonViews.Public.class)
    public String getAuthorizeUrl() {
        return switch (authType) {
            case AuthTypeConstants.GOOGLE -> Oauth2Constants.GOOGLE_AUTHORIZE_URL;
            case AuthTypeConstants.GITHUB -> Oauth2Constants.GITHUB_AUTHORIZE_URL;
            default -> null;
        };
    }

    @Override
    public void doEncrypt(Function<String, String> encryptFunc) {
        this.clientSecret = encryptFunc.apply(clientSecret);
    }

    @Override
    public void doDecrypt(Function<String, String> decryptFunc) {
        this.clientSecret = decryptFunc.apply(clientSecret);
    }

    @Override
    public void merge(AbstractAuthConfig oldConfig) {
        if (StringUtils.isBlank(this.clientSecret) && oldConfig instanceof Oauth2SimpleAuthConfig oldSimpleConfig) {
            this.clientSecret = oldSimpleConfig.getClientSecret();
        }
    }
}