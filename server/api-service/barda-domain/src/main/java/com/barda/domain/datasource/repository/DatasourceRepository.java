package com.barda.domain.datasource.repository;

import static com.barda.sdk.util.JsonUtils.fromJsonMap;
import static com.barda.sdk.util.JsonUtils.toJson;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceCreationSource;
import com.barda.domain.datasource.model.DatasourceDO;
import com.barda.domain.datasource.model.DatasourceStatus;
import com.barda.domain.datasource.service.JsDatasourceHelper;
import com.barda.domain.encryption.EncryptionService;
import com.barda.domain.plugin.client.DatasourcePluginClient;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.infra.mongo.MongoUpsertHelper;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.HasIdAndAuditing;
import com.barda.sdk.models.JsDatasourceConnectionConfig;
import com.barda.sdk.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 该类是数据源仓库的实现，提供对数据源对象的CRUD操作。
 * 所有查询操作都需要进行数据解密，而更新操作需要对整个数据源对象进行数据加密。
 */
@Slf4j
@Repository
public class DatasourceRepository {

    @Autowired
    private DatasourceDORepository repository;

    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    @Autowired
    private DatasourcePluginClient datasourcePluginClient;

    @Autowired
    private JsDatasourceHelper jsDatasourceHelper;

    /**
     * 根据数据源ID查询数据源。
     *
     * @param datasourceId 要查询的数据源ID
     * @return 包含数据源对象的Mono
     */
    public Mono<Datasource> findById(String datasourceId) {
        return repository.findById(datasourceId)
                .flatMap(this::convertToDomainObjectAndDecrypt);
    }

    /**
     * 查询工作区预定义的数据源，按组织ID和类型过滤。
     *
     * @param organizationId 要查询的组织ID
     * @param type           要查询的数据源类型
     * @return 包含数据源对象的Mono
     */
    public Mono<Datasource> findWorkspacePredefinedDatasourceByOrgIdAndType(String organizationId, String type) {
        return repository.findByOrganizationIdAndTypeAndCreationSource(organizationId, type,
                        DatasourceCreationSource.LEGACY_WORKSPACE_PREDEFINED.getValue())
                .flatMap(this::convertToDomainObjectAndDecrypt);
    }

    /**
     * 根据一组数据源ID查询数据源。
     *
     * @param ids 要查询的数据源ID列表
     * @return 包含数据源对象的Flux
     */
    public Flux<Datasource> findAllById(Iterable<String> ids) {
        return repository.findAllById(ids)
                .flatMap(this::convertToDomainObjectAndDecrypt);
    }

    /**
     * 查询指定组织下的所有数据源。
     *
     * @param orgId 要查询的组织ID
     * @return 包含数据源对象的Flux
     */
    public Flux<Datasource> findAllByOrganizationId(String orgId) {
        return repository.findAllByOrganizationId(orgId)
                .flatMap(this::convertToDomainObjectAndDecrypt);
    }

    /**
     * 保存数据源。
     *
     * @param datasource 要保存的数据源
     * @return 包含已保存数据源对象的Mono
     */
    public Mono<Datasource> save(Datasource datasource) {
        return encryptDataAndConvertToDataObject(datasource)
                .flatMap(repository::save)
                .flatMap(this::convertToDomainObjectAndDecrypt);
    }

    /**
     * 将数据源标记为已删除。
     *
     * @param datasourceId 要标记为已删除的数据源ID
     * @return 标记操作是否成功的Mono
     */
    public Mono<Boolean> markDatasourceAsDeleted(String datasourceId) {
        Datasource datasource = new Datasource();
        datasource.setDatasourceStatus(DatasourceStatus.DELETED);
        return mongoUpsertHelper.updateById(datasource, datasourceId);
    }

    /**
     * 保留不存在的和非当前组织的数据源ID。
     *
     * @param datasourceIds 要检查的数据源ID列表
     * @param orgId         要检查的组织ID
     * @return 包含要保留的数据源ID的Flux
     */
    public Flux<String> retainNoneExistAndNonCurrentOrgDatasourceIds(Collection<String> datasourceIds, String orgId) {
        if (CollectionUtils.isEmpty(datasourceIds)) {
            return Flux.empty();
        }
        return repository.findAllById(new HashSet<>(datasourceIds))
                .collectList()
                .map(existDatasources -> {
                    Set<String> result = new HashSet<>(datasourceIds);
                    existDatasources.stream()
                            .filter(datasource -> datasource.getOrganizationId().equals(orgId))
                            .map(HasIdAndAuditing::getId)
                            .forEach(result::remove);
                    return result;
                })
                .flatMapIterable(Function.identity());
    }

    /**
     * 查询指定组织下的数据源数量。
     *
     * @param orgId 要查询的组织ID
     * @return 包含数据源数量的Mono
     */
    public Mono<Long> countByOrganizationId(String orgId) {
        return repository.countByOrganizationId(orgId);
    }

    /**
     * 将数据对象转换为领域对象并进行解密处理。
     *
     * @param datasourceDO 数据对象（{@link DatasourceDO}）
     * @return 经过转换和解密处理后的领域对象（{@link Datasource}）
     */
    @SuppressWarnings("DuplicatedCode")
    private Mono<Datasource> convertToDomainObjectAndDecrypt(DatasourceDO datasourceDO) {

        // 创建一个供应者，生成数据源对象
        Mono<Datasource> datasourceMono = Mono.fromSupplier(() -> {
                    Datasource result = new Datasource();
                    result.setName(datasourceDO.getName());
                    result.setType(datasourceDO.getType());
                    result.setOrganizationId(datasourceDO.getOrganizationId());
                    result.setCreationSource(datasourceDO.getCreationSource());
                    result.setDatasourceStatus(datasourceDO.getDatasourceStatus());
                    result.setId(datasourceDO.getId());
                    result.setCreatedAt(datasourceDO.getCreatedAt());
                    result.setUpdatedAt(datasourceDO.getUpdatedAt());
                    result.setCreatedBy(datasourceDO.getCreatedBy());
                    result.setModifiedBy(datasourceDO.getModifiedBy());
                    return result;
                })
                .cache();

        return datasourceMono
                // 对数据源对象进行处理
                .doOnNext(datasource -> {
                    // 如果数据源类型为 JavaScript 数据源插件
                    if (datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType())) {
                        // 创建 JavaScript 数据源连接配置对象
                        JsDatasourceConnectionConfig jsDatasourceConnectionConfig = new JsDatasourceConnectionConfig();
                        jsDatasourceConnectionConfig.putAll(datasourceDO.getDetailConfig());
                        // 设置数据源的详细配置为 JavaScript 数据源连接配置对象
                        datasource.setDetailConfig(jsDatasourceConnectionConfig);
                    } else {
                        // 解析数据源的详细配置
                        DatasourceConnectionConfig detailConfig =
                                datasourceMetaInfoService.resolveDetailConfig(datasourceDO.getDetailConfig(), datasource.getType());
                        // 设置数据源的详细配置
                        datasource.setDetailConfig(detailConfig);
                    }
                })
                // 延迟直到填充数据源的插件定义完成
                .delayUntil(jsDatasourceHelper::fillPluginDefinition)
                // 对数据源对象进行解密处理
                .doOnNext(datasource -> {
                    // 对数据源的详细配置进行解密
                    DatasourceConnectionConfig decryptedDetailConfig = datasource.getDetailConfig().doDecrypt(encryptionService::decryptString);
                    // 覆盖原有的详细配置
                    datasource.setDetailConfig(decryptedDetailConfig);
                })
                // 在出现错误时记录日志，并返回原始的数据源对象
                .doOnError(throwable -> log.error("resolve detail config error.{},{}", datasourceDO.getType(),
                        JsonUtils.toJson(datasourceDO.getDetailConfig()), throwable))
                .onErrorResume(__ -> datasourceMono);
    }

    /**
     * 将数据源对象加密并转换为数据对象。
     *
     * @param datasource 数据源对象
     * @return 加密并转换后的数据对象（{@link DatasourceDO}）
     */
    @SuppressWarnings("DuplicatedCode")
    private Mono<DatasourceDO> encryptDataAndConvertToDataObject(Datasource datasource) {

        return Mono.fromSupplier(() -> {
                    DatasourceDO result = new DatasourceDO();
                    result.setName(datasource.getName());
                    result.setType(datasource.getType());
                    result.setOrganizationId(datasource.getOrganizationId());
                    result.setCreationSource(datasource.getCreationSource());
                    result.setDatasourceStatus(datasource.getDatasourceStatus());
                    result.setId(datasource.getId());
                    result.setCreatedAt(datasource.getCreatedAt());
                    result.setUpdatedAt(datasource.getUpdatedAt());
                    result.setCreatedBy(datasource.getCreatedBy());
                    result.setModifiedBy(datasource.getModifiedBy());
                    return result;
                })
                // 延迟直到填充数据源的插件定义完成
                .delayUntil(__ -> jsDatasourceHelper.fillPluginDefinition(datasource))
                // 对数据对象进行加密处理
                .doOnNext(datasourceDO -> {
                    // 获取数据源的详细配置
                    DatasourceConnectionConfig detailConfig = datasource.getDetailConfig();
                    // 对详细配置进行加密
                    DatasourceConnectionConfig encryptedConfig = detailConfig.doEncrypt(encryptionService::encryptString);
                    // 覆盖原有的详细配置
                    datasourceDO.setDetailConfig(fromJsonMap(toJson(encryptedConfig)));
                });
    }
}