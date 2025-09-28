package com.barda.domain.datasource.service;

import java.util.Collection;

import com.barda.domain.datasource.model.Datasource;
import com.barda.sdk.models.DatasourceTestResult;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 数据源服务接口。
 *
 * 该接口定义了与数据源相关的操作，包括创建、更新、查询、删除、测试数据源等功能。
 */
public interface DatasourceService {

    /**
     * 创建数据源。
     *
     * @param datasource 要创建的数据源
     * @param creatorId 创建者的 ID
     * @return 创建的数据源的 Mono
     */
    Mono<Datasource> create(Datasource datasource, String creatorId);

    /**
     * 更新数据源。
     *
     * @param id 要更新的数据源的 ID
     * @param resource 要更新的数据源
     * @return 更新后的数据源的 Mono
     */
    Mono<Datasource> update(String id, Datasource resource);

    /**
     * 获取数据源。
     *
     * @param id 要获取的数据源的 ID
     * @return 获取的数据源的 Mono
     */
    Mono<Datasource> getById(String id);

    /**
     * 删除数据源。
     *
     * @param id 要删除的数据源的 ID
     * @return 删除操作是否成功的 Mono
     */
    Mono<Boolean> delete(String id);

    /**
     * 测试数据源。
     *
     * @param datasource 要测试的数据源
     * @return 测试结果的 Mono
     */
    Mono<DatasourceTestResult> testDatasource(Datasource datasource);

    /**
     * 从 JS 插件配置中删除密码类型键。
     *
     * @param datasource 要处理的数据源
     * @return 处理操作的 Mono
     */
    Mono<Void> removePasswordTypeKeysFromJsDatasourcePluginConfig(Datasource datasource);

    /**
     * 获取指定组织 ID 下的数据源。
     *
     * @param orgId 组织 ID
     * @return 获取的数据源的 Flux
     */
    Flux<Datasource> getByOrgId(String orgId);

    /**
     * 按组织 ID 统计数据源数量。
     *
     * @param orgId 组织 ID
     * @return 统计结果的 Mono
     */
    Mono<Long> countByOrganizationId(String orgId);

    /**
     * 获取工作区预定义的数据源。
     *
     * @param organizationId 组织 ID
     * @param datasourceType 数据源类型
     * @return 获取的数据源的 Mono
     */
    Mono<Datasource> findWorkspacePredefinedDatasource(String organizationId, String datasourceType);

    /**
     * 保留在指定集合中存在且非当前组织的数据源 ID。
     *
     * @param datasourceIds 要检查的数据源 ID 集合
     * @param organizationId 组织 ID
     * @return 要保留的数据源 ID 的 Flux
     */
    Flux<String> retainNoneExistAndNonCurrentOrgDatasourceIds(Collection<String> datasourceIds, String organizationId);
}
