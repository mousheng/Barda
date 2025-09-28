package com.barda.api.authentication.service;

import org.springframework.web.server.ServerWebExchange;

import com.barda.api.authentication.dto.AuthConfigRequest;
import com.barda.domain.user.model.AuthUser;

import reactor.core.publisher.Mono;

/**
 * 身份验证API服务接口，用于处理身份验证相关的操作。
 */
public interface AuthenticationApiService {

    /**
     * 使用基于表单的身份验证方式进行身份验证。
     *
     * @param loginId  登录ID（可以是电子邮件或手机号）。
     * @param password 密码。
     * @param source   身份验证来源。
     * @param register 是否注册新用户。
     * @param authId   身份验证ID。
     * @return 身份验证后的用户信息。
     */
    Mono<AuthUser> authenticateByForm(String loginId, String password, String source, boolean register, String authId);

    /**
     * 使用基于OAuth2的身份验证方式进行身份验证。
     *
     * @param authId     身份验证ID。
     * @param source     身份验证来源。
     * @param code       授权码。
     * @param redirectUrl 重定向URL。
     * @return 身份验证后的用户信息。
     */
    Mono<AuthUser> authenticateByOauth2(String authId, String source, String code, String redirectUrl);

    /**
     * 登录或注册用户。
     *
     * @param authUser  身份验证后的用户信息。
     * @param exchange  服务器WebExchange。
     * @param invitationId 邀请ID。
     * @return 登录或注册操作的结果。
     */
    Mono<Void> loginOrRegister(AuthUser authUser, ServerWebExchange exchange, String invitationId);

    /**
     * 启用身份验证配置。
     *
     * @param authConfigRequest 身份验证配置请求。
     * @return 启用操作的结果。
     */
    Mono<Boolean> enableAuthConfig(AuthConfigRequest authConfigRequest);

    /**
     * 禁用身份验证配置。
     *
     * @param authId 身份验证ID。
     * @return 禁用操作的结果。
     */
    Mono<Boolean> disableAuthConfig(String authId);
}
