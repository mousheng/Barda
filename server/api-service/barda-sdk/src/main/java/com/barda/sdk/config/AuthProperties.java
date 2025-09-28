package com.barda.sdk.config;

import static com.barda.sdk.constants.AuthSourceConstants.GITHUB;
import static com.barda.sdk.constants.AuthSourceConstants.GITHUB_NAME;
import static com.barda.sdk.constants.AuthSourceConstants.GOOGLE;
import static com.barda.sdk.constants.AuthSourceConstants.GOOGLE_NAME;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.barda.sdk.auth.AbstractAuthConfig;
import com.barda.sdk.auth.EmailAuthConfig;
import com.barda.sdk.auth.Oauth2SimpleAuthConfig;
import com.barda.sdk.auth.constants.AuthTypeConstants;

import lombok.Getter;
import lombok.Setter;

/**
 * 身份验证属性类，用于读取 application.yml 或 application.properties 中的 auth 前缀的配置项。
 * 包含了邮箱、Google OAuth2 和 GitHub OAuth2 的配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * 邮箱配置
     */
    private Email email = new Email();

    /**
     * Google OAuth2 配置
     */
    private Oauth2Simple google = new Oauth2Simple();

    /**
     * GitHub OAuth2 配置
     */
    private Oauth2Simple github = new Oauth2Simple();

    /**
     * 身份验证方式的抽象类
     */
    @Data
    public static class AuthWay {

        /**
         * 是否启用该身份验证方式
         */
        protected boolean enable = false;

        /**
         * 是否在注册时启用该身份验证方式
         */
        protected Boolean enableRegister;

        /**
         * 是否启用RSA加密
         */
        protected Boolean RSA = false;

        /**
         * 获取是否在注册时启用该身份验证方式
         *
         * @return true - 启用, false - 禁用
         */
        public boolean isEnableRegister() {
            if (enableRegister == null) {
                return enable;
            }
            return enable && enableRegister;
        }
    }

    /**
     * 邮箱配置
     */
    public static class Email extends AuthWay {
    }


    /**
     * OAuth2 简单配置
     */
    @Setter
    @Getter
    public static class Oauth2Simple extends AuthWay {

        /**
         * OAuth2 客户端 ID
         */
        private String clientId;

        /**
         * OAuth2 客户端密钥
         */
        private String clientSecret;
    }

    /**
     * 获取身份验证配置列表
     *
     * @return 身份验证配置列表
     */
    public List<AbstractAuthConfig> getAuthConfigs() {
        List<AbstractAuthConfig> authConfigs = new ArrayList<>();

        // 邮箱
        if (email.isEnable()) {
            EmailAuthConfig email = new EmailAuthConfig(
                    null,
                    this.email.isEnable(),
                    this.email.isEnableRegister(),
                    this.email.RSA);
            authConfigs.add(email);
        }

        // Google OAuth2
        if (google.isEnable()) {
            Oauth2SimpleAuthConfig googleConfig = new Oauth2SimpleAuthConfig(
                    null,
                    true,
                    google.isEnableRegister(),
                    GOOGLE,
                    GOOGLE_NAME,
                    google.getClientId(),
                    google.getClientSecret(),
                    AuthTypeConstants.GOOGLE);
            authConfigs.add(googleConfig);
        }

        // GitHub OAuth2
        if (github.isEnable()) {
            Oauth2SimpleAuthConfig githubConfig = new Oauth2SimpleAuthConfig(
                    null,
                    true,
                    github.isEnableRegister(),
                    GITHUB,
                    GITHUB_NAME,
                    github.getClientId(),
                    github.getClientSecret(),
                    AuthTypeConstants.GITHUB);
            authConfigs.add(githubConfig);
        }
        return authConfigs;
    }
}
