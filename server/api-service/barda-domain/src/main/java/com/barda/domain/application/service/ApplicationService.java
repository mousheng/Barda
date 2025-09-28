package com.barda.domain.application.service;


import static com.barda.domain.application.ApplicationUtil.getDependentModulesFromDsl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.barda.domain.application.model.Application;
import com.barda.domain.application.model.ApplicationStatus;
import com.barda.domain.application.repository.ApplicationRepository;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.infra.annotation.NonEmptyMono;
import com.barda.infra.mongo.MongoUpsertHelper;
import com.barda.sdk.constants.FieldName;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.models.HasIdAndAuditing;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 应用服务类，提供应用相关的操作。
 *
 */
@Lazy
@Service
@Slf4j
public class ApplicationService {

    /**
     * 用于执行MongoDB upsert操作的帮助类。
     */
    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    /**
     * 用于处理应用权限的服务类。
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 应用仓库类，提供应用数据访问操作。
     */
    @Autowired
    private ApplicationRepository repository;

    /**
     * 根据ID查找应用。
     *
     * @param id 应用ID
     * @return 应用Mono对象
     */
    public Mono<Application> findById(String id) {
        if (id == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "INVALID_PARAMETER", FieldName.ID));
        }

        return repository.findByIdWithDsl(id)
                .switchIfEmpty(Mono.error(new BizException(BizError.NO_RESOURCE_FOUND, "CANT_FIND_APPLICATION", id)));
    }

    /**
     * 根据ID查找应用，不使用DSL。
     *
     * @param id 应用ID
     * @return 应用Mono对象
     */
    public Mono<Application> findByIdWithoutDsl(String id) {
        if (id == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "INVALID_PARAMETER", FieldName.ID));
        }

        return repository.findById(id)
                .switchIfEmpty(Mono.error(new BizException(BizError.NO_RESOURCE_FOUND, "CANT_FIND_APPLICATION", id)));
    }

    /**
     * 根据ID更新应用。
     *
     * @param applicationId 应用ID
     * @param application 应用对象
     * @return 更新是否成功的Mono对象
     */
    public Mono<Boolean> updateById(String applicationId, Application application) {
        if (applicationId == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "INVALID_PARAMETER", FieldName.ID));
        }

        return mongoUpsertHelper.updateById(application, applicationId);
    }

    /**
     * 根据ID更新应用的已发布应用DSL。
     *
     * @param applicationId 应用ID
     * @param applicationDSL 应用DSL
     * @return 更新是否成功的Mono对象
     */
    public Mono<Boolean> updatePublishedApplicationDSL(String applicationId, Map<String, Object> applicationDSL) {
        Application application = Application.builder().publishedApplicationDSL(applicationDSL).build();
        return mongoUpsertHelper.updateById(application, applicationId);
    }

    /**
     * 发布应用。
     *
     * @param applicationId 应用ID
     * @return 应用Mono对象
     */
    public Mono<Application> publish(String applicationId) {
        return findById(applicationId)
                .flatMap(newApplication -> { // copy editingApplicationDSL to publishedApplicationDSL
                    Map<String, Object> editingApplicationDSL = newApplication.getEditingApplicationDSL();
                    return updatePublishedApplicationDSL(applicationId, editingApplicationDSL)
                            .thenReturn(newApplication);
                });
    }

    /**
     * 创建应用。
     *
     * @param newApplication 新应用对象
     * @param visitorId 创建者ID
     * @return 应用Mono对象
     */
    public Mono<Application> create(Application newApplication, String visitorId) {
        return repository.save(newApplication)
                .delayUntil(app -> resourcePermissionService.addApplicationPermissionToUser(app.getId(), visitorId, ResourceRole.OWNER));
    }

    /**
     * 根据组织ID查找应用，使用DSL。
     * If you don't need dsl, please use {@link #findByOrganizationIdWithoutDsl(String)}
     * @param organizationId 组织ID
     * @return 应用Flux对象
     */
    public Flux<Application> findByOrganizationIdWithDsl(String organizationId) {
        return repository.findByOrganizationIdWithDsl(organizationId);
    }

    /**
     * 根据组织ID查找应用，不使用DSL。
     *
     * @param organizationId 组织ID
     * @return 应用Flux对象
     */
    public Flux<Application> findByOrganizationIdWithoutDsl(String organizationId) {
        return repository.findByOrganizationId(organizationId);
    }

    /**
     * 统计指定组织ID和应用状态的应用数量。
     *
     * @param orgId 组织ID
     * @param applicationStatus 应用状态
     * @return 应用数量Mono对象
     */
    public Mono<Long> countByOrganizationId(String orgId, ApplicationStatus applicationStatus) {
        return repository.countByOrganizationIdAndApplicationStatus(orgId, applicationStatus);
    }

    /**
     * 根据应用ID列表查找应用。
     *
     * @param applicationIds 应用ID列表
     * @return 应用Flux对象
     */
    public Flux<Application> findByIdIn(List<String> applicationIds) {
        return repository.findByIdIn(applicationIds);
    }

    /**
     * 获取应用的依赖模块。
     *
     * @param applicationId 应用ID
     * @param viewMode 查看模式
     * @return 应用列表Mono对象
     */
    public Mono<List<Application>> getAllDependentModulesFromApplicationId(String applicationId, boolean viewMode) {
        return findById(applicationId)
                .flatMap(app -> getAllDependentModulesFromApplication(app, viewMode));
    }

    /**
     * 获取应用的依赖模块。
     *
     * @param application 应用对象
     * @param viewMode 查看模式
     * @return 应用列表Mono对象
     */
    public Mono<List<Application>> getAllDependentModulesFromApplication(Application application, boolean viewMode) {
        Map<String, Object> dsl = viewMode ? application.getLiveApplicationDsl() : application.getEditingApplicationDSL();
        return getAllDependentModulesFromDsl(dsl);
    }

    /**
     * 获取应用的依赖模块。
     *
     * @param dsl 应用DSL
     * @return 应用列表Mono对象
     */
    public Mono<List<Application>> getAllDependentModulesFromDsl(Map<String, Object> dsl) {
        Set<String> circularDependencyCheckSet = Sets.newHashSet();
        return Mono.just(getDependentModulesFromDsl(dsl))
                .doOnNext(circularDependencyCheckSet::addAll)
                .flatMapMany(moduleSet -> findByIdIn(Lists.newArrayList(moduleSet)))
                .onErrorContinue((e, i) -> log.warn("get dependent modules on error continue , {}", e.getMessage()))
                .expandDeep(module -> getDependentModules(module, circularDependencyCheckSet))
                .collectList();
    }

    private Flux<Application> getDependentModules(Application module, Set<String> circularDependencyCheckSet) {
        return Flux.fromIterable(module.getLiveModules())
                .filter(moduleId -> !circularDependencyCheckSet.contains(moduleId))
                .doOnNext(circularDependencyCheckSet::add)
                .collectList()
                .flatMapMany(this::findByIdIn)
                .onErrorContinue((e, i) -> log.warn("get dependent modules on error continue , {}", e.getMessage()));
    }

    /**
     * 设置应用是否对所有人公开。
     *
     * @param applicationId 应用ID
     * @param publicToAll 是否对所有人公开
     * @return 更新是否成功的Mono对象
     */
    public Mono<Boolean> setApplicationPublicToAll(String applicationId, boolean publicToAll) {
        Application application = Application.builder()
                .publicToAll(publicToAll)
                .build();
        return mongoUpsertHelper.updateById(application, applicationId);
    }

    /**
     * 获取公开的应用ID列表。
     *
     * @param applicationIds 应用ID列表
     * @return 公开的应用ID集合Mono对象
     */
    @NonEmptyMono
    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    public Mono<Set<String>> getPublicApplicationIds(Collection<String> applicationIds) {
        return repository.findByPublicToAllIsTrueAndIdIn(applicationIds)
                .map(HasIdAndAuditing::getId)
                .collect(Collectors.toSet());
    }
}
