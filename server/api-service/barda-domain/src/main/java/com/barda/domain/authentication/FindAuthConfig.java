package com.barda.domain.authentication;

import javax.annotation.Nullable;

import com.barda.domain.organization.model.Organization;
import com.barda.sdk.auth.AbstractAuthConfig;

/**
 * 用于查找认证配置的记录类。
 *
 * 该类使用 Java 14 的记录（Record）特性来表示一组相关的属性。
 * 它包含了特定于认证的配置和所属的组织。
 */
public record FindAuthConfig(AbstractAuthConfig authConfig, @Nullable Organization organization) {
}