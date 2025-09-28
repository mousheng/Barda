package com.barda.api.authentication.request.form;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.api.authentication.request.AuthRequest;
import com.barda.domain.authentication.context.AuthRequestContext;
import com.barda.api.authentication.request.AuthRequestFactory;
import com.barda.sdk.auth.constants.AuthTypeConstants;

import reactor.core.publisher.Mono;

/**
 * 实现了{@link AuthRequestFactory}接口的表单认证请求工厂类。
 * 该类用于创建表单认证请求并提供对其进行操作的功能。
 */
@Component
public class FormAuthRequestFactory implements AuthRequestFactory<AuthRequestContext> {

    /**
     * 表单认证请求。
     */
    @Autowired
    private FormAuthRequest formAuthRequest;

    /**
     * 创建并返回一个表单认证请求。
     *
     * @param context 身份验证请求上下文
     * @return 包含表单认证请求的Mono
     */
    @Override
    public Mono<AuthRequest> build(AuthRequestContext context) {
        return Mono.just(formAuthRequest);
    }

    /**
     * 获取该工厂支持的身份验证类型。
     *
     * @return 包含支持的身份验证类型的集合
     */
    @Override
    public Set<String> supportedAuthTypes() {
        return Set.of(AuthTypeConstants.FORM);
    }
}
