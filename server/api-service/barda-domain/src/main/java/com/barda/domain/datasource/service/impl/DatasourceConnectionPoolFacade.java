package com.barda.domain.datasource.service.impl;

import static com.barda.domain.plugin.DatasourceMetaInfoConstants.REST_API;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceConnectionHolder;
import com.barda.domain.datasource.service.DatasourceConnectionPool;
import com.barda.domain.plugin.DatasourceMetaInfo;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.plugin.restapi.RestApiDatasourceConfig;
import com.barda.sdk.plugin.restapi.auth.RestApiAuthType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 作为数据源连接池的外观类，提供统一的 API 用于获取或创建数据源的连接。
 * 该类使用 Spring 框架的依赖注入功能来管理数据源连接池的实例。
 */
@Primary
@Service
@Slf4j
public class DatasourceConnectionPoolFacade implements DatasourceConnectionPool {

    /**
     * 用于存储所有数据源连接池的列表
     */
    @Autowired
    private List<DatasourceConnectionPool> pools;

    /**
     * 用于获取数据源元信息的服务
     */
    @Autowired
    private DatasourceMetaInfoService metaInfoService;

    /**
     * 用于存储数据源连接池的键值对
     */
    private Map<Class<? extends DatasourceConnectionPool>, DatasourceConnectionPool> poolMap;

    /**
     * 在应用启动时执行的初始化方法
     */
    @PostConstruct
    public void init() {
        log.info("开始注册数据源连接池...");
        // 过滤出非 DatasourceConnectionPoolFacade 的数据源连接池并存储在 poolMap 中
        poolMap = pools.stream()
                .filter(pool -> !(pool instanceof DatasourceConnectionPoolFacade))
                .collect(Collectors.toMap(DatasourceConnectionPool::getClass, Function.identity()));
        // 记录已注册的数据源连接池
        poolMap.keySet().forEach(aClass -> log.info("注册数据源连接池：{}", aClass));
        log.info("完成注册数据源连接池。");
    }

    /**
     * 获取或创建数据源的连接
     *
     * @param datasource 数据源
     * @return 包含数据源连接的 Mono 对象
     */
    @Override
    public Mono<? extends DatasourceConnectionHolder> getOrCreateConnection(Datasource datasource) {
        // 对于 REST API，数据源连接池的选择基于鉴权类型
        if (datasource.getType().equals(REST_API)) {
            DatasourceConnectionConfig detailConfig = datasource.getDetailConfig();
            if (detailConfig instanceof RestApiDatasourceConfig restApiDatasourceConfig) {
                if (restApiDatasourceConfig.getAuthType() == RestApiAuthType.NO_AUTH
                        || restApiDatasourceConfig.getAuthType() == RestApiAuthType.BASIC_AUTH
                        || restApiDatasourceConfig.getAuthType() == RestApiAuthType.DIGEST_AUTH
                        || restApiDatasourceConfig.getAuthType() == RestApiAuthType.OAUTH2_INHERIT_FROM_LOGIN) {
                    // 无鉴权或基本鉴权、摘要鉴权、OAuth2 继承自登录的情况使用无状态连接池
                    return poolMap.get(StatelessConnectionPool.class).getOrCreateConnection(datasource);
                }
                // 其他鉴权类型的情况使用基于令牌的连接池
                return poolMap.get(TokenBasedConnectionPool.class).getOrCreateConnection(datasource);
            }
        }

        // 通用情况，使用数据源元信息中指定的连接池
        DatasourceMetaInfo metaInfo = metaInfoService.getDatasourceMetaInfo(datasource.getType());
        Class<? extends DatasourceConnectionPool> poolClass = metaInfo.getConnectionPool();
        DatasourceConnectionPool datasourceConnectionPool = poolMap.get(poolClass);
        if (datasourceConnectionPool == null) {
            // 没有找到指定的数据源连接池时抛出异常
            throw new BizException(BizError.INVALID_DATASOURCE_CONFIGURATION, "找不到数据源连接池");
        }
        return datasourceConnectionPool.getOrCreateConnection(datasource);
    }

    /**
     * 获取数据源的性能指标
     *
     * @param datasourceId 数据源 ID
     * @return 包含性能指标的对象
     */
    @Override
    public Object info(String datasourceId) {
        // 目前只返回 ClientBasedConnectionPool 的性能指标
        return poolMap.get(ClientBasedConnectionPool.class).info(datasourceId);
    }
}

