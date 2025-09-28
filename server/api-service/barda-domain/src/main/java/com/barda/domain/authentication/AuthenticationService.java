package com.barda.domain.authentication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.barda.sdk.auth.EmailAuthConfig;
import com.barda.sdk.constants.AuthSourceConstants;

/**
 * 该接口定义了身份验证服务。
 * 它提供方法来查找和检索身份验证配置。
 */
public interface AuthenticationService {

    /**
     * 默认的电子邮件身份验证配置。
     * 登录和注册都启用。
     */
    EmailAuthConfig DEFAULT_AUTH_CONFIG = new EmailAuthConfig(AuthSourceConstants.EMAIL, true, true, true);

    /**
     * 关闭RSA的电子邮件身份验证配置。
     * 登录和注册都启用。
     */
    EmailAuthConfig DEFAULT_AUTH_CONFIG_DISABLE_RSA = new EmailAuthConfig(AuthSourceConstants.EMAIL, true, true, false);

    /**
     * 根据 authId 查找身份验证配置。
     *
     * @param authId 身份验证 ID
     * @return 包含身份验证配置的 {@link Mono<FindAuthConfig>} 对象
     */
    Mono<FindAuthConfig> findAuthConfigByAuthId(String authId);

    /**
     * 根据来源查找身份验证配置。
     *
     * @param source 身份验证来源
     * @return 包含身份验证配置的 {@link Mono<FindAuthConfig>} 对象
     */
    Mono<FindAuthConfig> findAuthConfigBySource(String source);

    /**
     * 获取所有身份验证配置。
     *
     * @param enableOnly 是否只返回启用的配置
     * @return 包含身份验证配置的 {@link Flux<FindAuthConfig>} 对象
     */
    Flux<FindAuthConfig> findAllAuthConfigs(boolean enableOnly);
}
