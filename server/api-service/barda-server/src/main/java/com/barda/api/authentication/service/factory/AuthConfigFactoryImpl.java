package com.barda.api.authentication.service.factory;

import static com.barda.sdk.constants.AuthSourceConstants.GITHUB;
import static com.barda.sdk.constants.AuthSourceConstants.GITHUB_NAME;
import static com.barda.sdk.constants.AuthSourceConstants.GOOGLE;
import static com.barda.sdk.constants.AuthSourceConstants.GOOGLE_NAME;
import static com.barda.sdk.constants.AuthSourceConstants.FEISHU;
import static com.barda.sdk.constants.AuthSourceConstants.FEISHU_NAME;
import static com.barda.sdk.constants.AuthSourceConstants.DINGTALK;
import static com.barda.sdk.constants.AuthSourceConstants.DINGTALK_NAME;
import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.barda.api.authentication.dto.AuthConfigRequest;
import com.barda.sdk.auth.AbstractAuthConfig;
import com.barda.sdk.auth.EmailAuthConfig;
import com.barda.sdk.auth.Oauth2SimpleAuthConfig;
import com.barda.sdk.auth.constants.AuthTypeConstants;

/**
 * 身份验证配置工厂实现类，用于创建和管理身份验证配置。
 */
@Component
public class AuthConfigFactoryImpl implements AuthConfigFactory {

    /**
     * 根据认证配置请求构建认证配置对象。
     *
     * @param authConfigRequest 认证配置请求对象
     * @param enable            是否启用
     * @return 构建的认证配置对象
     * @throws UnsupportedOperationException 如果未知的认证类型
     */
    @Override
    public AbstractAuthConfig build(AuthConfigRequest authConfigRequest, boolean enable) {
        return switch (authConfigRequest.getAuthType()) {
            case AuthTypeConstants.FORM -> buildEmailAuthConfig(authConfigRequest, enable);
            case AuthTypeConstants.GITHUB -> buildOauth2SimpleAuthConfig(GITHUB, GITHUB_NAME, authConfigRequest, enable);
            case AuthTypeConstants.GOOGLE -> buildOauth2SimpleAuthConfig(GOOGLE, GOOGLE_NAME, authConfigRequest, enable);
            case AuthTypeConstants.FEISHU -> buildOauth2SimpleAuthConfig(FEISHU, FEISHU_NAME, authConfigRequest, enable);
            case AuthTypeConstants.DINGTALK -> buildOauth2SimpleAuthConfig(DINGTALK, DINGTALK_NAME, authConfigRequest, enable);
            default -> throw new UnsupportedOperationException(authConfigRequest.getAuthType());
        };
    }

    /**
     * 获取支持的认证类型集合。
     *
     * @return 支持的认证类型集合
     */
    @Override
    public Set<String> supportAuthTypes() {
        return Set.of(
                AuthTypeConstants.FORM,
                AuthTypeConstants.GITHUB,
                AuthTypeConstants.GOOGLE,
                AuthTypeConstants.FEISHU,
                AuthTypeConstants.DINGTALK
        );
    }


    /**
     * 创建基于电子邮件的身份验证配置。
     *
     * @param authConfigRequest 身份验证配置请求。
     * @param enable            是否启用该身份验证配置。
     * @return 创建的基于电子邮件的身份验证配置。
     */
    private EmailAuthConfig buildEmailAuthConfig(AuthConfigRequest authConfigRequest, boolean enable) {
        Boolean enableRegister = MapUtils.getBoolean(authConfigRequest, "enableRegister");
        return new EmailAuthConfig(authConfigRequest.getId(), enable, enableRegister);
    }

    /**
     * 创建基于OAuth2的简单身份验证配置。
     *
     * @param source       身份验证来源。
     * @param sourceName   身份验证来源的名称。
     * @param authConfigRequest 身份验证配置请求。
     * @param enable       是否启用该身份验证配置。
     * @return 创建的基于OAuth2的简单身份验证配置。
     */
    private Oauth2SimpleAuthConfig buildOauth2SimpleAuthConfig(String source, String sourceName, AuthConfigRequest authConfigRequest,
            boolean enable) {
        return new Oauth2SimpleAuthConfig(
                authConfigRequest.getId(),
                enable,
                authConfigRequest.isEnableRegister(),
                source,
                sourceName,
                requireNonNull(authConfigRequest.getClientId(), "clientId can not be null."),
                authConfigRequest.getClientSecret(),
                authConfigRequest.getAuthType());
    }
}
