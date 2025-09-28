package com.barda.domain.authentication.context;

import javax.annotation.Nullable;

import com.barda.sdk.auth.AbstractAuthConfig;

import lombok.Getter;
import lombok.Setter;

/**
 * 认证请求上下文的抽象类。
 *
 * 该类包含了与认证相关的配置和上下文信息。
 * 子类可以扩展该类并添加特定于应用的属性和方法。
 */
@Setter
@Getter
public abstract class AuthRequestContext {

    /**
     * 认证配置。
     *
     * 该属性可以被子类覆盖以提供特定于应用的认证配置。
     */
    protected volatile AbstractAuthConfig authConfig;

    /**
     * 组织 ID。
     *
     * 该属性可以被子类覆盖以提供特定于应用的组织 ID。
     * 它被标记为 @Nullable，表示它可以为空。
     */
    @Nullable
    private volatile String orgId;
}
