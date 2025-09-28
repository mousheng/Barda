package com.barda.api.config;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.barda.sdk.auth.AbstractAuthConfig;
import com.barda.sdk.constants.WorkspaceMode;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 配置视图类。
 * 该类使用 Lombok 的 {@link Getter} 和 {@link SuperBuilder} 注解来生成 getter 方法和超级构建器。
 */
@Getter
@SuperBuilder
public class ConfigView {

    /**
     * 是否为云托管。
     */
    private boolean isCloudHosting;

    /**
     * 认证配置列表。
     * 列表中的元素为 {@link AbstractAuthConfig} 的子类。
     */
    private List<AbstractAuthConfig> authConfigs;

    /**
     * 工作区模式。
     */
    private WorkspaceMode workspaceMode;

    /**
     * 是否为自托管。
     */
    private boolean selfDomain;

    /**
     * Cookie 名称。
     */
    private String cookieName;

    /**
     * product 产品名
     */
    private String product;

    /**
     * 自定义品牌
     */
    private Object branding;
}
