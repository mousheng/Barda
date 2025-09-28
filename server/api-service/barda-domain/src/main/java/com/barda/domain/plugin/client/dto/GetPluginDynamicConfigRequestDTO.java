package com.barda.domain.plugin.client.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * 获取插件动态配置的请求数据传输对象。
 * 该类用于表示获取插件动态配置的请求参数。
 */
@Data
@Builder
public class GetPluginDynamicConfigRequestDTO {

    /**
     * 获取数据源 ID。
     */
    private String dataSourceId;

    /**
     * 获取插件名称。
     */
    private String pluginName;

    /**
     * 获取路径。
     */
    private String path;

    /**
     * 获取数据源配置。
     */
    private Map<String, Object> dataSourceConfig;
}
