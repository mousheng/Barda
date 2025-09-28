package com.barda.domain.plugin.service.impl;

import static com.barda.sdk.exception.BizError.INVALID_DATASOURCE_TYPE;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.domain.datasource.service.impl.ClientBasedConnectionPool;
import com.barda.domain.datasource.service.impl.StatelessConnectionPool;
import com.barda.domain.datasource.service.impl.TokenBasedConnectionPool;
import com.barda.domain.plugin.DatasourceMetaInfo;
import com.barda.domain.plugin.DatasourceMetaInfoConstants;
import com.barda.domain.plugin.client.DatasourcePluginClient;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.sdk.constants.ConfigTypes;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.plugin.common.DatasourceConnector;
import com.barda.sdk.plugin.common.QueryExecutor;
import com.barda.sdk.query.QueryExecutionContext;

import reactor.core.publisher.Flux;

/**
 * 数据源元信息服务的实现类，用于提供数据源元信息和操作方法。
 */
@SuppressWarnings("unused")
@Component
public class DatasourceMetaInfoServiceImpl implements DatasourceMetaInfoService {

    /**
     * PostgreSQL 数据源元信息。
     */
    private static final DatasourceMetaInfo POSTGRES = DatasourceMetaInfo.builder()
            .type("postgres")
            .displayName("PostgreSQL")
            .pluginExecutorKey("postgres-plugin")
            .hasStructureInfo(true)
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * REST API 数据源元信息。
     */
    private static final DatasourceMetaInfo REST_API = DatasourceMetaInfo.builder()
            .type(DatasourceMetaInfoConstants.REST_API)
            .displayName("REST API")
            .pluginExecutorKey("restapi-plugin")
            .connectionPool(TokenBasedConnectionPool.class)
            .build();

    /**
     * MongoDB 数据源元信息。
     */
    private static final DatasourceMetaInfo MONGODB = DatasourceMetaInfo.builder()
            .type("mongodb")
            .displayName("MongoDB")
            .pluginExecutorKey("mongo-plugin")
            .hasStructureInfo(true)
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * MySQL 数据源元信息。
     */
    private static final DatasourceMetaInfo MYSQL = DatasourceMetaInfo.builder()
            .type(DatasourceMetaInfoConstants.MYSQL)
            .displayName("MySQL")
            .pluginExecutorKey("mysql-plugin")
            .hasStructureInfo(true)
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * Barda API 数据源元信息。
     */
    private static final DatasourceMetaInfo BARDA_API = DatasourceMetaInfo.builder()
            .type(DatasourceMetaInfoConstants.BARDA_API)
            .displayName("Barda API")
            .pluginExecutorKey("barda-api-plugin")
            .connectionPool(StatelessConnectionPool.class)
            .build();

    /**
     * Elasticsearch 数据源元信息。
     */
    private static final DatasourceMetaInfo ES = DatasourceMetaInfo.builder()
            .type("es")
            .displayName("Elasticsearch")
            .pluginExecutorKey("es-plugin")
            .hasStructureInfo(false)
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * Redis 数据源元信息。
     */
    private static final DatasourceMetaInfo REDIS = DatasourceMetaInfo.builder()
            .type("redis")
            .displayName("Redis")
            .pluginExecutorKey("redis-plugin")
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * Microsoft SQL Server 数据源元信息。
     */
    private static final DatasourceMetaInfo SQL_SERVER = DatasourceMetaInfo.builder()
            .type("mssql")
            .displayName("Microsoft SQL Server")
            .pluginExecutorKey("mssql-plugin")
            .hasStructureInfo(true)
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * Oracle 数据源元信息。
     */
    private static final DatasourceMetaInfo ORACLE = DatasourceMetaInfo.builder()
            .type("oracle")
            .displayName("Oracle")
            .pluginExecutorKey("oracle-plugin")
            .hasStructureInfo(true)
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * MariaDB 数据源元信息。
     */
    private static final DatasourceMetaInfo MARIA_DB = DatasourceMetaInfo.builder()
            .type("mariadb")
            .displayName("MariaDB")
            .pluginExecutorKey("mysql-plugin")
            .hasStructureInfo(true)
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * SMTP 数据源元信息。
     */
    private static final DatasourceMetaInfo SMTP = DatasourceMetaInfo.builder()
            .type(ConfigTypes.SMTP)
            .displayName("SMTP")
            .pluginExecutorKey("smtp-plugin")
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * ClickHouse 数据源元信息。
     */
    private static final DatasourceMetaInfo CLICKHOUSE = DatasourceMetaInfo.builder()
            .type("clickHouse")
            .displayName("ClickHouse")
            .pluginExecutorKey("clickHouse-plugin")
            .hasStructureInfo(true)
            .connectionPool(ClientBasedConnectionPool.class)
            .build();


    /**
     * Snowflake 数据源元信息。
     */
    private static final DatasourceMetaInfo SNOWFLAKE = DatasourceMetaInfo.builder()
            .type("snowflake")
            .displayName("Snowflake")
            .pluginExecutorKey("snowflake-plugin")
            .hasStructureInfo(true)
            .connectionPool(ClientBasedConnectionPool.class)
            .build();

    /**
     * Google Sheets 数据源元信息。
     */
    private static final DatasourceMetaInfo GOOGLE_SHEETS = DatasourceMetaInfo.builder()
            .type("googleSheets")
            .displayName("Google Sheets")
            .pluginExecutorKey("googleSheets-plugin")
            .connectionPool(StatelessConnectionPool.class).build();

    /**
     * GraphQL 数据源元信息。
     */
    private static final DatasourceMetaInfo GRAPHQL = DatasourceMetaInfo.builder()
            .type("graphql")
            .displayName("GraphQL")
            .pluginExecutorKey("graphql-plugin")
            .connectionPool(StatelessConnectionPool.class).build();

    /**
     * 数据源元信息列表。
     */
    private static final ArrayList<DatasourceMetaInfo> datasourceMetaInfos = new ArrayList<>();

    static {
        Field[] allFields = DatasourceMetaInfoServiceImpl.class.getDeclaredFields();
        for (Field field : allFields) {
            if (Modifier.isPrivate(field.getModifiers()) && field.getType() == DatasourceMetaInfo.class) {
                field.setAccessible(true);
                try {
                    DatasourceMetaInfo o = (DatasourceMetaInfo) field.get(null);
                    datasourceMetaInfos.add(o);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 插件管理器。
     */
    @Autowired
    private PluginManager pluginManager;

    /**
     * 数据源插件客户端。
     */
    @Autowired
    private DatasourcePluginClient datasourcePluginClient;

    /**
     * 获取所有基于 Java 的支持的数据源元信息列表。
     *
     * @return 基于 Java 的支持的数据源元信息列表
     */
    @Override
    public List<DatasourceMetaInfo> getJavaBasedSupportedDatasourceMetaInfos() {
        return datasourceMetaInfos;
    }

    /**
     * 判断指定类型的数据源是否是基于 Java 的插件。
     *
     * @param type 数据源类型
     * @return 如果是基于 Java 的数据源插件则返回 true，否则返回 false
     */
    @Override
    public boolean isJavaDatasourcePlugin(String type) {
        return "bardaApi".equals(type)
                || getJavaBasedSupportedDatasourceMetaInfos()
                .stream()
                .anyMatch(datasourceMetaInfo -> datasourceMetaInfo.getType().equals(type));
    }

    /**
     * 判断指定类型的数据源是否是基于 JavaScript 的插件。
     *
     * @param type 数据源类型
     * @return 如果是基于 JavaScript 的数据源插件则返回 true，否则返回 false
     */
    @Override
    public boolean isJsDatasourcePlugin(String type) {
        return !isJavaDatasourcePlugin(type);
    }

    /**
     * 获取所有支持的数据源元信息列表。
     *
     * @return 所有支持的数据源元信息列表
     */
    @Override
    public Flux<DatasourceMetaInfo> getAllSupportedDatasourceMetaInfos() {
        Flux<DatasourceMetaInfo> datasourceMetaInfoFlux = datasourcePluginClient.getDatasourcePluginDefinitions()
                .map(datasourcePluginDTO -> DatasourceMetaInfo.builder()
                        .type(datasourcePluginDTO.getId())
                        .displayName(datasourcePluginDTO.getName())
                        .definition(datasourcePluginDTO)
                        .build());

        return Flux.fromIterable(getJavaBasedSupportedDatasourceMetaInfos())
                .concatWith(datasourceMetaInfoFlux);
    }

    /**
     * 根据数据源类型获取数据源连接器。
     *
     * @param datasourceType 数据源类型
     * @return 数据源连接器
     * @throws BizException 如果找不到对应的插件则抛出业务异常
     */
    @SuppressWarnings("unchecked")
    @Override
    public DatasourceConnector<Object, ? extends DatasourceConnectionConfig> getDatasourceConnector(String datasourceType) {
        DatasourceMetaInfo datasourceMetaInfo = getDatasourceMetaInfo(datasourceType);
        var executorList = pluginManager.getExtensions(DatasourceConnector.class, datasourceMetaInfo.getPluginExecutorKey());
        if (executorList.isEmpty()) {
            throw new BizException(BizError.NO_RESOURCE_FOUND, "PLUGIN_NOT_FOUND", datasourceMetaInfo.getPluginExecutorKey());
        }
        return executorList.get(0);
    }

    /**
     * 根据数据源类型获取数据源元信息。
     *
     * @param datasourceType 数据源类型
     * @return 数据源元信息
     * @throws BizException 如果数据源类型无效则抛出业务异常
     */
    @Override
    public DatasourceMetaInfo getDatasourceMetaInfo(String datasourceType) {
        return getJavaBasedSupportedDatasourceMetaInfos()
                .stream()
                .filter(value -> value.getType().equalsIgnoreCase(datasourceType))
                .findFirst()
                .orElseThrow(() -> new BizException(INVALID_DATASOURCE_TYPE, "INVALID_DATASOURCE_TYPE", datasourceType));
    }

    /**
     * 根据数据源类型获取查询执行器。
     *
     * @param datasourceType 数据源类型
     * @return 查询执行器
     * @throws BizException 如果找不到对应的插件则抛出业务异常
     */
    @SuppressWarnings("unchecked")
    @Override
    public QueryExecutor<? extends DatasourceConnectionConfig, Object, ? extends QueryExecutionContext> getQueryExecutor(String datasourceType) {
        DatasourceMetaInfo datasourceMetaInfo = getDatasourceMetaInfo(datasourceType);
        var executorList = pluginManager.getExtensions(QueryExecutor.class, datasourceMetaInfo.getPluginExecutorKey());
        if (executorList.isEmpty()) {
            throw new BizException(BizError.NO_RESOURCE_FOUND, "PLUGIN_NOT_FOUND", datasourceMetaInfo.getPluginExecutorKey());
        }
        return executorList.get(0);
    }

    /**
     * 根据数据源详细配置参数解析数据源连接配置。
     *
     * @param datasourceDetailMap 数据源详细配置参数
     * @param datasourceType      数据源类型
     * @return 数据源连接配置
     */
    @Override
    public DatasourceConnectionConfig resolveDetailConfig(Map<String, Object> datasourceDetailMap, String datasourceType) {
        return getDatasourceConnector(datasourceType).resolveConfig(datasourceDetailMap);
    }
}
