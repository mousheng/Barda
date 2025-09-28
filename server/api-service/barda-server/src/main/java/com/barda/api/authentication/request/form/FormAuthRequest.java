package com.barda.api.authentication.request.form;

import static com.barda.sdk.util.ExceptionUtils.ofError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.api.authentication.request.AuthRequest;
import com.barda.domain.authentication.context.AuthRequestContext;
import com.barda.domain.authentication.context.FormAuthRequestContext;
import com.barda.domain.encryption.EncryptionService;
import com.barda.domain.user.model.AuthUser;
import com.barda.domain.user.service.UserService;
import com.barda.sdk.auth.AbstractAuthConfig;
import com.barda.sdk.auth.EmailAuthConfig;
import com.barda.sdk.constants.AuthSourceConstants;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;

import reactor.core.publisher.Mono;

/**
 * 实现了{@link AuthRequest}接口的表单认证请求类。
 * 该类用于处理基于表单的身份验证请求。
 */
@Component
public class FormAuthRequest implements AuthRequest {

    /**
     * 用户服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 加密服务。
     */
    @Autowired
    private EncryptionService encryptionService;

    /**
     * 处理身份验证请求。
     *
     * @param authRequestContext 身份验证请求上下文
     * @return 包含授权用户的Mono
     */
    @Override
    public Mono<AuthUser> auth(AuthRequestContext authRequestContext) {
        FormAuthRequestContext context = (FormAuthRequestContext) authRequestContext;

        return Mono.defer(() -> {
                    AbstractAuthConfig authConfig = context.getAuthConfig();
                    // 注册
                    if (context.isRegister()) {
                        // 通过电子邮件注册
                        if (AuthSourceConstants.EMAIL.equals(authConfig.getSource())
                                && authConfig instanceof EmailAuthConfig emailAuthConfig
                                && emailAuthConfig.isEnableRegister()) {
                            return userService.findBySourceAndId(authConfig.getSource(), context.getLoginId())
                                    .flatMap(user -> ofError(BizError.USER_LOGIN_ID_EXIST, "USER_LOGIN_ID_EXIST"));
                        }
                        // 非电子邮件注册
                        return Mono.error(new BizException(BizError.UNSUPPORTED_OPERATION, "BAD_REQUEST"));
                    }
                    // 登录
                    return userService.findBySourceAndId(authConfig.getSource(), context.getLoginId())
                            .switchIfEmpty(ofError(BizError.INVALID_PASSWORD, "INVALID_EMAIL_OR_PASSWORD"))
                            .flatMap(user -> {
                                String raw = context.getPassword();
                                String encoded = user.getPassword();
                                if (!encryptionService.matchPassword(raw, encoded)) {
                                    return ofError(BizError.INVALID_PASSWORD, "INVALID_EMAIL_OR_PASSWORD");
                                }
                                return Mono.empty();
                            });
                })
                .thenReturn(AuthUser.builder().uid(context.getLoginId()).username(context.getLoginId()).build());
    }
}
