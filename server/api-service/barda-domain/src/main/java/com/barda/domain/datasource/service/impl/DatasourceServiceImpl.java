package com.barda.domain.datasource.service.impl;

import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static com.barda.sdk.util.LocaleUtils.getLocale;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.time.Duration;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.barda.domain.application.model.ApplicationStatus;
import com.barda.domain.application.repository.ApplicationRepository;
import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.repository.DatasourceRepository;
import com.barda.domain.datasource.service.DatasourceService;
import com.barda.domain.datasource.service.JsDatasourceHelper;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.domain.plugin.client.DatasourcePluginClient;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.sdk.constants.FieldName;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.DatasourceTestResult;
import com.barda.sdk.models.JsDatasourceConnectionConfig;
import com.barda.sdk.util.LocaleUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 实现数据源服务的类，提供创建、更新、删除、查询等数据源相关的操作。
 */
@Slf4j
@Service
public class DatasourceServiceImpl implements DatasourceService {

    private static final Duration DEFAULT_TEST_CONNECTION_TIMEOUT = Duration.ofSeconds(10);
    private static final String INVALID_PARAMETER_CODE = "INVALID_PARAMETER";

    /**
     * 用于获取数据源元信息的服务
     */
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    /**
     * 用于获取应用的仓库
     */
    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * 用于管理数据源的资源权限的服务
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 用于存储数据源的仓库
     */
    @Autowired
    private DatasourceRepository repository;

    /**
     * 用于与数据源插件进行交互的客户端
     */
    @Autowired
    private DatasourcePluginClient datasourcePluginClient;

    /**
     * 用于帮助处理 JavaScript 数据源的辅助类
     */
    @Autowired
    private JsDatasourceHelper jsDatasourceHelper;

    // 以下是实现 DatasourceService 接口中的方法
    @Override
    public Mono<Datasource> create(Datasource datasource, String creatorId) {
        // 以下是创建数据源的实现
        // 1. 验证数据源 ID 是否为空
        if (datasource.getId() != null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, INVALID_PARAMETER_CODE, FieldName.ID));
        }

        // 2. 验证数据源的其他属性
        return Mono.just(datasource)
                .flatMap(this::validateDatasource)
                .flatMap(this::trySaveDatasource)
                .delayUntil(savedDatasource -> resourcePermissionService.addDataSourcePermissionToUser(savedDatasource.getId(), creatorId,
                        ResourceRole.OWNER));
    }

    @Override
    public Mono<Datasource> update(String datasourceId, Datasource updatedDatasource) {
        // 以下是更新数据源的实现
        // 1. 验证数据源 ID 是否为空
        if (datasourceId == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, INVALID_PARAMETER_CODE, FieldName.ID));
        }

        // 2. 获取要更新的数据源并合并更新的数据源
        return repository.findById(datasourceId)
                .delayUntil(jsDatasourceHelper::fillPluginDefinition)
                .map(currentDatasource -> currentDatasource.mergeWith(updatedDatasource))
                // 3. 验证数据源的其他属性
                .flatMap(this::validateDatasource)
                // 4. 保存更新的数据源
                .flatMap(this::trySaveDatasource);
    }

    @Override
    public Mono<Datasource> getById(String id) {
        // 以下是根据 ID 获取数据源的实现
        // 1. 处理快速 REST API 和快速 GraphQL API 的特殊情况
        if (StringUtils.equals(id, Datasource.QUICK_REST_API_ID)) {
            return Mono.just(Datasource.QUICK_REST_API);
        }

        if (StringUtils.equals(id, Datasource.QUICK_GRAPHQL_ID)) {
            return Mono.just(Datasource.QUICK_GRAPHQL_API);
        }

        if (StringUtils.equals(id, Datasource.BARDA_API_ID)) {
            return Mono.just(Datasource.BARDA_API);
        }

        // 2. 获取数据源
        return repository.findById(id);
    }

    // 以下是私有方法

    private Mono<Datasource> validateDatasource(Datasource datasource) {
        // 以下是验证数据源的实现
        // 1. 验证数据源的组织 ID 是否为空
        if (datasource.getOrganizationId() == null) {
            throw new BizException(BizError.INVALID_PARAMETER, INVALID_PARAMETER_CODE, FieldName.ORGANIZATION_ID);
        }

        // 2. 验证数据源的名称是否为空
        if (StringUtils.isBlank(datasource.getName())) {
            throw new BizException(BizError.INVALID_PARAMETER, INVALID_PARAMETER_CODE, FieldName.NAME);
        }

        // 3. 验证数据源的类型是否为空
        if (datasource.getType() == null) {
            throw new BizException(BizError.DATASOURCE_PLUGIN_ID_NOT_GIVEN, "DATASOURCE_PLUGIN_ID_NOT_GIVEN");
        }

        // 4. 验证 JavaScript 数据源插件的数据源
        if (datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType())) {
            return Mono.just(datasource);
        }

        // 5. 验证非 JavaScript 数据源插件的数据源
        return Mono.deferContextual(ctx -> {
            Locale locale = getLocale(ctx);
            return Mono.just(datasourceMetaInfoService.getDatasourceConnector(datasource.getType()))
                    .flatMap(datasourceConnector -> {
                        DatasourceConnectionConfig detailConfig = datasource.getDetailConfig();
                        Set<String> errorMsgKeySet = datasourceConnector.doValidateConfig(detailConfig);
                        Set<String> errorMsgSet = errorMsgKeySet.stream()
                                .map(key -> LocaleUtils.getMessage(locale, key))
                                .collect(Collectors.toSet());
                        if (isNotEmpty(errorMsgKeySet)) {
                            return ofError(BizError.INVALID_DATASOURCE_CONFIGURATION, "INVALID_DATASOURCE_CONFIGURATION",
                                    Joiner.on('\n').join(errorMsgSet));
                        }

                        return Mono.just(datasource);
                    });
        });
    }

    @Nonnull
    private Mono<Datasource> trySaveDatasource(Datasource datasource) {
        // 以下是保存数据源的实现

        return repository.save(datasource)
                .onErrorMap(error -> {
                    if (error instanceof DuplicateKeyException) {
                        return new BizException(BizError.DUPLICATE_DATABASE_NAME, "DUPLICATE_DATABASE_NAME", datasource.getName());
                    }
                    return error;
                });
    }

    @Override
    public Mono<DatasourceTestResult> testDatasource(Datasource testDatasource) {
        // 以下是测试数据源的实现

        Mono<Datasource> datasourceMono = Mono.just(testDatasource);

        // 1. 处理测试数据源的 ID 是否为空的情况
        if (testDatasource.getId() != null) {
            datasourceMono = getById(testDatasource.getId())
                    .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, "NOT_AUTHORIZED"))
                    .delayUntil(jsDatasourceHelper::fillPluginDefinition)
                    .map(datasource -> datasource.mergeWith(testDatasource));
        }

        // 2. 验证数据源并测试数据源的连接
        return datasourceMono
                .flatMap(this::validateDatasource)
                .flatMap(datasource -> {
                    // 3. 处理 JavaScript 数据源插件的数据源
                    if (datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType())) {
                        return testDatasourceByNodeJs(datasource);
                    }
                    // 4. 处理非 JavaScript 数据源插件的数据源
                    return testDatasourceLocally(datasource);
                });
    }

    private Mono<DatasourceTestResult> testDatasourceLocally(Datasource datasource) {
        // 以下是通过本地方式测试数据源的实现

        return datasourceMetaInfoService.getDatasourceConnector(datasource.getType())
                .doTestConnection(datasource.getDetailConfig())
                .timeout(DEFAULT_TEST_CONNECTION_TIMEOUT)
                .onErrorResume(error -> Mono.just(DatasourceTestResult.testFail(error)));
    }

    private Mono<DatasourceTestResult> testDatasourceByNodeJs(Datasource datasource) {
        // 以下是通过 Node.js 方式测试数据源的实现

        return datasourcePluginClient.test(datasource.getType(), datasource.getDetailConfig());
    }

    @Override
    public Mono<Void> removePasswordTypeKeysFromJsDatasourcePluginConfig(Datasource datasource) {
        // 以下是从 JavaScript 数据源插件的数据源中删除密码类型的键的实现

        return jsDatasourceHelper.fillPluginDefinition(datasource)
                .doFinally(__ -> {
                    if (datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType())
                            && datasource.getDetailConfig() instanceof JsDatasourceConnectionConfig jsDatasourceConnectionConfig) {
                        jsDatasourceConnectionConfig.removePasswords();
                    }
                });
    }

    @Override
    public Flux<Datasource> getByOrgId(String orgId) {
        // 以下是根据组织 ID 获取数据源的实现

        return repository.findAllByOrganizationId(orgId);
    }

    @Override
    public Mono<Long> countByOrganizationId(String orgId) {
        // 以下是根据组织 ID 统计数据源的实现

        return repository.countByOrganizationId(orgId);
    }

    @Override
    public Mono<Datasource> findWorkspacePredefinedDatasource(String organizationId, String type) {
        // 以下是查找工作区预定义的数据源的实现

        return repository.findWorkspacePredefinedDatasourceByOrgIdAndType(organizationId, type);
    }

    @Override
    public Flux<String> retainNoneExistAndNonCurrentOrgDatasourceIds(Collection<String> datasourceIds, String orgId) {
        // 以下是保留不存在的和非当前组织的数据源 ID 的实现

        if (CollectionUtils.isEmpty(datasourceIds)) {
            return Flux.empty();
        }
        return repository.retainNoneExistAndNonCurrentOrgDatasourceIds(datasourceIds, orgId);
    }

    @Override
    public Mono<Boolean> delete(String datasourceId) {
        // 以下是删除数据源的实现

        return stillUsedInApplications(datasourceId)
                .flatMap(stillUsedInApplications -> {
                    if (Boolean.TRUE.equals(stillUsedInApplications)) {
                        return Mono.error(new BizException(BizError.DATASOURCE_DELETE_FAIL_DUE_TO_REMAINING_QUERIES,
                                "DATASOURCE_DELETE_FAIL_DUE_TO_REMAINING_QUERIES"));
                    }
                    return Mono.empty();
                })
                .then(repository.markDatasourceAsDeleted(datasourceId));
    }

    @Nonnull
    private Mono<Boolean> stillUsedInApplications(String datasourceId) {
        // 以下是检查数据源是否在应用中使用的实现

        return applicationRepository.findByDatasourceId(datasourceId)
                .filter(application -> application.getApplicationStatus() != ApplicationStatus.DELETED)
                .hasElements();
    }
}

