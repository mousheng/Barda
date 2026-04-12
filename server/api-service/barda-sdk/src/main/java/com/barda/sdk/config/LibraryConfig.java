package com.barda.sdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 库文件管理配置。
 * <p>
 * 自定义组件的 JS 库（React、ReactDOM、Babel 等）通过 Vite build 时自动下载
 * 到前端构建产物中，Nginx 直接从 /static/lib/ 提供，无需后端网络检测。
 */
@Data
@Component
@ConfigurationProperties(prefix = "library")
public class LibraryConfig {

    /**
     * 主要组织 ID（可选配置）。
     * 如果数据库中没有标记主要组织，则使用此配置。
     */
    private String primaryOrgId;

    /**
     * 单个文件大小限制（字节）。
     * 默认 10MB。
     */
    private long maxFileSize = 10 * 1024 * 1024;

    /**
     * 共享库总大小限制（字节）。
     * 默认 100MB。
     */
    private long maxSharedLibrarySize = 100 * 1024 * 1024;

    /**
     * 组织私有库总大小限制（字节）。
     * 默认 50MB。
     */
    private long maxOrgLibrarySize = 50 * 1024 * 1024;

    /**
     * 静态文件根目录。
     * 默认为 /barda/client/static/lib/
     */
    private String staticFileRoot = "/barda/client/static/lib/";

    /**
     * 是否启用文件系统同步。
     * 默认为 true。
     */
    private boolean enableFileSystemSync = true;
}
