package com.barda.domain.datasource.service.impl;

import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_GET_STRUCTURE_ERROR;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceStructureDO;
import com.barda.domain.datasource.repository.DatasourceStructureRepository;
import com.barda.domain.datasource.service.DatasourceConnectionPool;
import com.barda.domain.datasource.service.DatasourceService;
import com.barda.domain.datasource.service.DatasourceStructureService;
import com.barda.domain.plugin.DatasourceMetaInfo;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.infra.mongo.MongoUpsertHelper;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.plugin.common.QueryExecutor;
import com.barda.sdk.query.QueryExecutionContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 实现数据源结构服务的类，提供获取、保存数据源结构的操作。
 */
@Component
@Slf4j
public class DatasourceStructureServiceImpl implements DatasourceStructureService {

    /**
     * 用于获取通用配置的服务
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 用于管理数据源的服务
     */
    @Autowired
    private DatasourceService datasourceService;

    /**
     * 用于获取数据源元信息的服务
     */
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    /**
     * 用于管理数据源连接的连接池的服务
     */
    @Autowired
    private DatasourceConnectionPool connectionContextService;

    /**
     * 用于存储数据源结构的仓库
     */
    @Autowired
    private DatasourceStructureRepository datasourceStructureRepository;

    /**
     * 用于帮助执行 MongoDB upsert 操作的辅助类
     */
    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    /**
     * 获取数据源的结构
     *
     * @param datasourceId 数据源 ID
     * @param ignoreCache 是否忽略缓存
     * @return 包含数据源结构的 Mono
     */
    public Mono<DatasourceStructure> getStructure(String datasourceId, boolean ignoreCache) {
        return getStructure0(datasourceId, ignoreCache)
                .defaultIfEmpty(new DatasourceStructure())
                .onErrorMap(e -> {
                    if (e instanceof PluginException) {
                        return e;
                    }

                    return new PluginException(DATASOURCE_GET_STRUCTURE_ERROR,
                            "DATASOURCE_GET_STRUCTURE_ERROR", e.getMessage());
                });
    }

    /**
     * 获取数据源的结构的私有方法
     *
     * @param datasourceId 数据源 ID
     * @param ignoreCache 是否忽略缓存
     * @return 包含数据源结构的 Mono
     */
    private Mono<DatasourceStructure> getStructure0(String datasourceId, boolean ignoreCache) {
        if (ignoreCache) {
            return getLatestAndSave(datasourceId);
        }
        return getFromCache(datasourceId)
                .switchIfEmpty(Mono.defer(() -> getLatestAndSave(datasourceId)));
    }

    /**
     * 获取最新的数据源结构并保存
     *
     * @param datasourceId 数据源 ID
     * @return 包含数据源结构的 Mono
     */
    @SuppressWarnings("ConstantConditions")
    private Mono<DatasourceStructure> getLatestAndSave(String datasourceId) {
        return datasourceService.getById(datasourceId)
                .flatMap(datasource -> {
                    DatasourceMetaInfo metaInfo = datasourceMetaInfoService.getDatasourceMetaInfo(datasource.getType());
                    if (!metaInfo.isHasStructureInfo()) {
                        return Mono.empty();
                    }

                    var queryExecutor = datasourceMetaInfoService.getQueryExecutor(datasource.getType());
                    return getLatestStructure(datasource, queryExecutor)
                            .flatMap(structure -> saveStructure(datasource.getId(), structure));
                });
    }

    /**
     * 从缓存中获取数据源的结构
     *
     * @param datasourceId 数据源 ID
     * @return 包含数据源结构的 Mono
     */
    private Mono<DatasourceStructure> getFromCache(String datasourceId) {
        return datasourceStructureRepository.findByDatasourceId(datasourceId)
                .map(DatasourceStructureDO::getStructure);
    }

    /**
     * 保存数据源的结构
     *
     * @param datasourceId 数据源 ID
     * @param structure 数据源结构
     * @return 包含数据源结构的 Mono
     */
    private Mono<DatasourceStructure> saveStructure(String datasourceId, DatasourceStructure structure) {
        DatasourceStructureDO dataModel = new DatasourceStructureDO();
        dataModel.setDatasourceId(datasourceId);
        dataModel.setStructure(structure);
        return mongoUpsertHelper.upsertWithAuditingParams(dataModel, "datasourceId", datasourceId)
                .thenReturn(structure);
    }

    /**
     * 获取数据源的最新的结构
     *
     * @param datasource 数据源
     * @param queryExecutor 查询执行器
     * @return 包含数据源结构的 Mono
     */
    private Mono<DatasourceStructure> getLatestStructure(Datasource datasource,
            QueryExecutor<? extends DatasourceConnectionConfig, Object, ? extends QueryExecutionContext> queryExecutor) {

        long readStructureTimeout = commonConfig.getQuery().getReadStructureTimeout();
        return connectionContextService.getOrCreateConnection(datasource)
                .flatMap(connectionContext -> queryExecutor.doGetStructure(connectionContext.connection(), datasource.getDetailConfig())
                        .timeout(Duration.ofMillis(readStructureTimeout))
                        .doOnError(connectionContext::onQueryError)
                        .onErrorMap(TimeoutException.class, e -> new BizException(BizError.PLUGIN_EXECUTION_TIMEOUT, "PLUGIN_EXECUTION_TIMEOUT",
                                readStructureTimeout))
                )
                .onErrorMap(e -> {
                    if (e instanceof PluginException) {
                        return e;
                    }
                    log.error("get datasource structure error", e);
                    return new PluginException(DATASOURCE_GET_STRUCTURE_ERROR, "DATASOURCE_GET_STRUCTURE_ERROR", e.getMessage());
                });
    }

}
