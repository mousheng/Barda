package com.barda.domain.datasource.service;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.plugin.client.DatasourcePluginClient;
import com.barda.domain.plugin.client.dto.GetPluginDynamicConfigRequestDTO;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.sdk.models.JsDatasourceConnectionConfig;

import reactor.core.publisher.Mono;

/**
 * JS 数据源帮助器类。
 * <p>
 * 该类提供与 JS 数据源相关的操作，包括填充插件定义、处理动态数据源配置和查询配置等功能。
 */
@Component
public class JsDatasourceHelper {

    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;
    @Autowired
    private DatasourcePluginClient datasourcePluginClient;

    /**
     * 在合并之前，对数据源的详细配置进行加密、解密和移除密码。
     *
     * @param datasource 要处理的数据源
     * @return 处理操作的 Mono
     */
    public Mono<Void> fillPluginDefinition(Datasource datasource) {
        return Mono.defer(() -> {
            if (datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType())
                    && datasource.getDetailConfig() instanceof JsDatasourceConnectionConfig jsDatasourceConfig
                    && ObjectUtils.anyNull(datasource.getPluginDefinition(), jsDatasourceConfig.getDefinition(), jsDatasourceConfig.getType())) {

                return datasourcePluginClient.getDatasourcePluginDefinition(datasource.getType())
                        .doOnNext(datasourcePluginDTO -> {
                            datasource.setPluginDefinition(datasourcePluginDTO);
                            jsDatasourceConfig.setDefinition(datasourcePluginDTO);
                            jsDatasourceConfig.setType(datasource.getType());
                        })
                        .then();
            }
            return Mono.empty();
        });
    }

    /**
     * 处理动态数据源配置的额外部分。
     *
     * @param datasource 要处理的数据源
     * @return 处理操作的 Mono
     */
    public Mono<Void> processDynamicDatasourceConfigExtra(Datasource datasource) {
        if (datasourceMetaInfoService.isJavaDatasourcePlugin(datasource.getType())) {
            return Mono.empty();
        }
        return fillPluginDefinition(datasource)
                .then(Mono.fromSupplier(datasource::getPluginDefinition))
                .flatMap(datasourcePluginDefinition -> {
                    if (!datasourcePluginDefinition.isDatasourceConfigExtraDynamic()) {
                        return Mono.empty();
                    }
                    GetPluginDynamicConfigRequestDTO getPluginDynamicConfigRequestDTO = GetPluginDynamicConfigRequestDTO.builder()
                            .pluginName(datasource.getType())
                            .path("$.dataSourceConfig.extra")
                            .dataSourceConfig((JsDatasourceConnectionConfig) datasource.getDetailConfig())
                            .build();
                    return datasourcePluginClient.getPluginDynamicConfig(List.of(getPluginDynamicConfigRequestDTO))
                            .flatMap(list -> {
                                if (list.size() == 1) {
                                    Object datasourceConfigExtra = list.get(0);
                                    JsDatasourceConnectionConfig jsDatasourceConnectionConfig =
                                            (JsDatasourceConnectionConfig) datasource.getDetailConfig();
                                    jsDatasourceConnectionConfig.put("extra", datasourceConfigExtra);
                                }
                                return Mono.empty();
                            });
                });
    }

    /**
     * 处理动态查询配置。
     *
     * @param datasource 要处理的数据源
     * @return 处理操作的 Mono
     */
    public Mono<Void> processDynamicQueryConfig(Datasource datasource) {
        if (datasourceMetaInfoService.isJavaDatasourcePlugin(datasource.getType())) {
            return Mono.empty();
        }
        return fillPluginDefinition(datasource)
                .then(Mono.fromSupplier(datasource::getPluginDefinition))
                .flatMap(datasourcePluginDefinition -> {
                    if (!datasourcePluginDefinition.isQueryConfigDynamic()) {
                        return Mono.empty();
                    }
                    GetPluginDynamicConfigRequestDTO getPluginDynamicConfigRequestDTO = GetPluginDynamicConfigRequestDTO.builder()
                            .pluginName(datasource.getType())
                            .path("$.queryConfig")
                            .dataSourceConfig((JsDatasourceConnectionConfig) datasource.getDetailConfig())
                            .build();
                    return datasourcePluginClient.getPluginDynamicConfigSafely(List.of(getPluginDynamicConfigRequestDTO))
                            .flatMap(list -> {
                                if (list.size() == 1) {
                                    Object queryConfig = list.get(0);
                                    datasourcePluginDefinition.put("queryConfig", queryConfig);
                                }
                                return Mono.empty();
                            });
                });
    }
}
