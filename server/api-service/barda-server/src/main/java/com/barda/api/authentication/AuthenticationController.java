package com.barda.api.authentication;

import java.util.List;

import com.barda.sdk.encryption.RSACryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.annotation.JsonView;
import com.barda.api.authentication.dto.AuthConfigRequest;
import com.barda.api.authentication.service.AuthenticationApiService;
import com.barda.api.framework.view.ResponseView;
import com.barda.api.home.SessionUserService;
import com.barda.api.usermanagement.UserController;
import com.barda.api.usermanagement.UserController.UpdatePasswordRequest;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.domain.authentication.AuthenticationService;
import com.barda.domain.authentication.FindAuthConfig;
import com.barda.infra.constant.NewUrl;
import com.barda.sdk.auth.AbstractAuthConfig;
import com.barda.sdk.config.SerializeConfig.JsonViews;
import com.barda.sdk.constants.AuthSourceConstants;
import com.barda.sdk.util.CookieHelper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 身份验证控制器类，提供身份验证相关的API。
 */
@Slf4j
@RestController
@RequestMapping(value = {NewUrl.CUSTOM_AUTH})
public class AuthenticationController {

    /**
     * 身份验证API服务。
     */
    @Autowired
    private AuthenticationApiService authenticationApiService;

    /**
     * 会话用户服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * Cookie帮助器。
     */
    @Autowired
    private CookieHelper cookieHelper;

    /**
     * 业务事件发布器。
     */
    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    /**
     * 身份验证服务。
     */
    @Autowired
    private AuthenticationService authenticationService;

    /**
     * RSA加密解密服务。
     */
    @Autowired
    private RSACryptoService rsaCryptoService;


    /**
     * 使用密码通过电子邮件或手机登录；或者通过电子邮件注册。
     *
     * @see UserController#updatePassword(UpdatePasswordRequest)
     */
    @PostMapping("/form/login")
    public Mono<ResponseView<Boolean>> formLogin(@RequestBody FormLoginRequest formLoginRequest,
                                                 @RequestParam(required = false) String invitationId,
                                                 ServerWebExchange exchange) {
        return authenticationApiService.authenticateByForm(
                        rsaCryptoService.pureDecryt(formLoginRequest.loginId()),
                        rsaCryptoService.pureDecryt(formLoginRequest.password()),
                        formLoginRequest.source(), formLoginRequest.register(), formLoginRequest.authId())
                .flatMap(user -> authenticationApiService.loginOrRegister(user, exchange, invitationId))
                .thenReturn(ResponseView.success(true));
    }

    /**
     * 使用第三方登录。
     *
     * @param authId       授权ID（可选）
     * @param source       授权来源（可选）
     * @param code         授权码
     * @param invitationId 邀请ID（可选）
     * @param redirectUrl  重定向URL（可选）
     * @param exchange     ServerWebExchange对象
     * @return 包含布尔值的Mono，表示操作是否成功的响应视图
     */
    @PostMapping("/tp/login")
    public Mono<ResponseView<Boolean>> loginWithThirdParty(
            @RequestParam(required = false) String authId,
            @RequestParam(required = false) String source,
            @RequestParam String code,
            @RequestParam(required = false) String invitationId,
            @RequestParam(required = false) String redirectUrl,
            ServerWebExchange exchange) {
        return authenticationApiService.authenticateByOauth2(authId, source, code, redirectUrl)
                .flatMap(authUser -> authenticationApiService.loginOrRegister(authUser, exchange, invitationId))
                .thenReturn(ResponseView.success(true));
    }

    /**
     * 用户登出。
     *
     * @param exchange ServerWebExchange对象
     * @return 包含布尔值的Mono，表示操作是否成功的响应视图
     */
    @PostMapping("/logout")
    public Mono<ResponseView<Boolean>> logout(ServerWebExchange exchange) {
        String cookieToken = cookieHelper.getCookieToken(exchange);
        return sessionUserService.removeUserSession(cookieToken)
                .then(businessEventPublisher.publishUserLogoutEvent())
                .thenReturn(ResponseView.success(true));
    }

    /**
     * 启用认证配置。
     *
     * @param authConfigRequest 认证配置请求对象
     * @return 包含空值的Mono，表示操作是否成功的响应视图
     */
    @PostMapping("/config")
    public Mono<ResponseView<Void>> enableAuthConfig(@RequestBody AuthConfigRequest authConfigRequest) {
        return authenticationApiService.enableAuthConfig(authConfigRequest)
                .thenReturn(ResponseView.success(null));
    }

    /**
     * 禁用指定ID的认证配置。
     *
     * @param id 认证配置ID
     * @return 包含空值的Mono，表示操作是否成功的响应视图
     */
    @DeleteMapping("/config/{id}")
    public Mono<ResponseView<Void>> disableAuthConfig(@PathVariable("id") String id) {
        return authenticationApiService.disableAuthConfig(id)
                .thenReturn(ResponseView.success(null));
    }

    /**
     * 获取所有认证配置。
     *
     * @return 包含认证配置列表的Mono，表示操作是否成功的响应视图
     */
    @JsonView(JsonViews.Public.class)
    @GetMapping("/configs")
    public Mono<ResponseView<List<AbstractAuthConfig>>> getAllConfigs() {
        return authenticationService.findAllAuthConfigs(false)
                .map(FindAuthConfig::authConfig)
                .collectList()
                .map(ResponseView::success);
    }

    /**
     * 表单登录请求。
     *
     * @param loginId  登录ID，目前为手机号或邮箱。
     * @param password 密码
     * @param register 是否注册
     * @param source   登录来源，可以是 {@link AuthSourceConstants#PHONE} 或 {@link AuthSourceConstants#EMAIL}
     * @param authId   授权ID
     */
    public record FormLoginRequest(String loginId, String password, boolean register, String source, String authId) {
    }
}
