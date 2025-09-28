package com.barda.sdk.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.barda.sdk.constants.WorkspaceMode;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 通用配置类，用于读取 application.yml 或 application.properties 中的 common 前缀的配置项。
 * 包含了域、工作区、加密、安全、版本、BlockHound 等配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "common")
public class CommonConfig {

    /**
     * 域配置
     */
    private Domain domain = new Domain();

    /**
     * 工作区配置
     */
    private Workspace workspace = new Workspace();

    /**
     * 加密配置
     */
    private Encrypt encrypt = new Encrypt();

    /**
     * 是否为云模式
     */
    private boolean cloud;

    /**
     * 安全配置
     */
    private Security security = new Security();

    /**
     * 版本号
     */
    private String version;

    /**
     * 是否启用 BlockHound
     */
    private boolean blockHoundEnable;

    /**
     * Cookie 名称
     */
    private String cookieName;
    /**
     * 产品名称
     */
    private String product;

    /**
     * 查询请求的最大大小 (MB)
     */
    private int maxQueryRequestSizeInMb = 10;

    /**
     * 查询响应的最大大小 (MB)
     */
    private int maxQueryResponseSizeInMb = 10;

    /**
     * 查询配置
     */
    private Query query = new Query();

    /**
     * Cookie 配置
     */
    private Cookie cookie = new Cookie();

    /**
     * JavaScript 执行器配置
     */
    private JsExecutor jsExecutor = new JsExecutor();

    /**
     * 禁止的主机列表
     */
    private Set<String> disallowedHosts = new HashSet<>();

    /**
     * 是否为自托管模式
     *
     * @return true - 自托管, false - 云模式
     */
    public boolean isSelfHost() {
        return !isCloud();
    }

    /**
     * 是否为企业模式
     *
     * @return true - 企业模式, false - 单租户模式
     */
    public boolean isEnterpriseMode() {
        return workspace.getMode() == WorkspaceMode.ENTERPRISE;
    }

    /**
     * 域配置
     */
    @Data
    public static class Domain {
        /**
         * 默认域
         */
        private String defaultValue;
    }

    /**
     * 加密配置
     */
    @Data
    public static class Encrypt {
        /**
         * 密码
         */
        private String password = "abcd";

        /**
         * 盐
         */
        private String salt = "abcd";
    }

    /**
     * 安全配置
     */
    @Setter
    public static class Security {
        /**
         * 允许的 CORS 域
         */
        private List<String> corsAllowedDomains;

        /**
         * 支持的 Docker 环境文件中的 CORS 域
         */
        private String corsAllowedDomainString;

        /**
         * 获取所有允许的 CORS 域
         *
         * @return 所有允许的 CORS 域
         */
        public List<String> getAllCorsAllowedDomains() {
            List<String> all = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(corsAllowedDomains)) {
                all.addAll(corsAllowedDomains);
            }
            if (StringUtils.isNotBlank(corsAllowedDomainString)) {
                List<String> domains = Arrays.stream(corsAllowedDomainString.split(",")).toList();
                all.addAll(domains);
            }
            return all;
        }
    }

    /**
     * 工作区配置
     */
    @Data
    public static class Workspace {

        /**
         * 工作区模式
         */
        private WorkspaceMode mode = WorkspaceMode.SAAS;

        /**
         * 企业组织 ID
         */
        private String enterpriseOrgId;
    }

    /**
     * Cookie 配置
     */
    @Data
    public static class Cookie {

        /**
         * Cookie 最大存活时间 (秒)
         */
        private long maxAgeInSeconds = Duration.ofDays(30).toSeconds();
    }

    /**
     * JavaScript 执行器配置
     */
    @Data
    public static class JsExecutor {
        /**
         * 主机
         */
        private String host;
    }

    /**
     * 查询配置
     */
    @Getter
    @Setter
    public static class Query {
        /**
         * 读取结构超时时间 (毫秒)
         */
        private long readStructureTimeout = 15000;
    }
}