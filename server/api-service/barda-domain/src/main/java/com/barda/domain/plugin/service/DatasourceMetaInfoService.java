package com.barda.domain.plugin.service;

import java.util.List;
import java.util.Map;

import com.barda.domain.plugin.DatasourceMetaInfo;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.plugin.common.DatasourceConnector;
import com.barda.sdk.plugin.common.QueryExecutor;
import com.barda.sdk.query.QueryExecutionContext;

import reactor.core.publisher.Flux;

/**
 * 数据源元信息服务接口。
 *
 * 该接口提供与数据源元信息相关的操作，包括获取数据源元信息、检查数据源类型等功能。
 */
public interface DatasourceMetaInfoService {

    /**
     * 获取指定数据源类型的元信息。
     *
     * @param datasourceType 数据源类型
     * @return 获取的数据源元信息
     */
    DatasourceMetaInfo getDatasourceMetaInfo(String datasourceType);

    /**
     * 获取所有基于 Java 的受支持的数据源元信息。
     *
     * @return 获取的基于 Java 的数据源元信息列表
     */
    List<DatasourceMetaInfo> getJavaBasedSupportedDatasourceMetaInfos();

    /**
     * 检查数据源类型是否为基于 Java 的插件。
     *
     * @param type 数据源类型
     * @return 是否为基于 Java 的插件
     */
    boolean isJavaDatasourcePlugin(String type);

    /**
     * 检查数据源类型是否为基于 JS 的插件。
     *
     * @param type 数据源类型
     * @return 是否为基于 JS 的插件
     */
    boolean isJsDatasourcePlugin(String type);

    /**
     * 获取所有受支持的数据源元信息。
     *
     * @return 获取的受支持的数据源元信息的 Flux
     */
    Flux<DatasourceMetaInfo> getAllSupportedDatasourceMetaInfos();

    /**
     * 获取指定数据源类型的连接器。
     *
     * @param datasourceType 数据源类型
     * @return 获取的连接器
     */
    DatasourceConnector<Object,? extends DatasourceConnectionConfig> getDatasourceConnector(String datasourceType);

    /**
     * 获取指定数据源类型的查询执行器。
     *
     * @param datasourceType 数据源类型
     * @return 获取的查询执行器
     */
    QueryExecutor<? extends DatasourceConnectionConfig, Object,? extends QueryExecutionContext> getQueryExecutor(String datasourceType);

    /**
     * 解析数据源的详细配置。
     *
     * @param datasourceDetailMap 数据源的详细配置 Map
     * @param datasourceType 数据源类型
     * @return 解析后的详细配置
     */
    DatasourceConnectionConfig resolveDetailConfig(Map<String, Object> datasourceDetailMap, String datasourceType);
}
