package com.barda.domain.organization.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.barda.domain.mongodb.MongodbInterceptorContext;
import com.barda.sdk.auth.AbstractAuthConfig;
import com.barda.sdk.config.SerializeConfig.JsonViews;
import com.barda.sdk.util.JsonUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 组织域类。
 * 该类表示一个组织域，并实现了 EnterpriseConnectionConfig 接口，用于提供企业连接配置的功能。
 */
public class OrganizationDomain implements EnterpriseConnectionConfig {

    /**
     * 组织域。
     */
    @Getter
    @Setter
    private String domain;

    /**
     * 授权配置列表。
     * 该列表用于存储该组织域的授权配置。
     */
    @Setter
    @Getter
    @Transient
    private List<AbstractAuthConfig> configs = new ArrayList<>();

    /**
     * 仅用于 MongoDB (反)序列化。
     * 该列表用于在 MongoDB 存储中存储授权配置的 JSON 格式。
     */
    private List<Object> authConfigs = new ArrayList<>();

    /**
     * 在 MongoDB 写入操作前执行的操作。
     * 该方法用于对授权配置列表中的每一项进行加密。
     *
     * @param context 包含 MongoDB 拦截器上下文的对象
     */
    void beforeMongodbWrite(MongodbInterceptorContext context) {
        this.configs.forEach(authConfig -> authConfig.doEncrypt(s -> context.encryptionService().encryptString(s)));
        authConfigs = JsonUtils.fromJsonSafely(JsonUtils.toJsonSafely(configs, JsonViews.Internal.class), new TypeReference<>() {
        }, new ArrayList<>());
    }

    /**
     * 在 MongoDB 读取操作后执行的操作。
     * 该方法用于对授权配置列表中的每一项进行解密。
     *
     * @param context 包含 MongoDB 拦截器上下文的对象
     */
    void afterMongodbRead(MongodbInterceptorContext context) {
        this.configs = JsonUtils.fromJsonSafely(JsonUtils.toJson(authConfigs), new TypeReference<>() {
        }, new ArrayList<>());
        this.configs.forEach(authConfig -> authConfig.doDecrypt(s -> context.encryptionService().decryptString(s)));
    }
}
