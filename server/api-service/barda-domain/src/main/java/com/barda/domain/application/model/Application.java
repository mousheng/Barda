package com.barda.domain.application.model;


import static com.google.common.base.Suppliers.memoize;
import static com.barda.domain.application.ApplicationUtil.getContainerSizeFromDSL;
import static com.barda.domain.application.ApplicationUtil.getDependentModulesFromDsl;
import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.barda.domain.query.model.ApplicationQuery;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.models.HasIdAndAuditing;
import com.barda.sdk.util.JsonUtils;

import lombok.Builder;

/**
 * 应用类，继承了HasIdAndAuditing，表示应用在数据库中有ID和审计信息。
 */
@Document
public class Application extends HasIdAndAuditing {

    /**
     * 应用所属的组织ID。
     */
    private final String organizationId;

    /**
     * 应用名称。
     */
    private final String name;

    /**
     * 应用类型，使用Integer表示，可以从ApplicationType枚举中获取。
     */
    private final Integer applicationType;

    /**
     * 应用状态，使用ApplicationStatus枚举表示。
     */
    private final ApplicationStatus applicationStatus;

    /**
     * 已发布的应用DSL，使用Map<String, Object>表示。
     */
    private final Map<String, Object> publishedApplicationDSL;

    /**
     * 应用是否对所有人公开。
     */
    private final Boolean publicToAll;

    /**
     * 正在编辑的应用DSL，使用Map<String, Object>表示。
     */
    private Map<String, Object> editingApplicationDSL;

    /**
     * 正在编辑的应用查询集合，使用Supplier<Set<ApplicationQuery>>表示，
     * 并使用memoize()方法进行了懒加载和缓存。
     */
    @Transient
    private final Supplier<Set<ApplicationQuery>> editingQueries =
            memoize(() -> Optional.ofNullable(editingApplicationDSL)
                    .map(map -> map.get("queries"))
                    .map(queries -> JsonUtils.fromJsonSet(JsonUtils.toJson(queries), ApplicationQuery.class))
                    .orElse(Collections.emptySet()));

    /**
     * 已发布的应用查询集合，使用Supplier<Set<ApplicationQuery>>表示，
     * 并使用memoize()方法进行了懒加载和缓存。
     */
    @Transient
    private final Supplier<Set<ApplicationQuery>> liveQueries =
            memoize(() -> JsonUtils.fromJsonSet(JsonUtils.toJson(getLiveApplicationDsl().get("queries")), ApplicationQuery.class));

    /**
     * 正在编辑的应用所依赖的模块集合，使用Supplier<Set<String>>表示，
     * 并使用memoize()方法进行了懒加载和缓存。
     */
    @Transient
    private final Supplier<Set<String>> editingModules = memoize(() -> getDependentModulesFromDsl(editingApplicationDSL));

    /**
     * 已发布的应用所依赖的模块集合，使用Supplier<Set<String>>表示，
     * 并使用memoize()方法进行了懒加载和缓存。
     */
    @Transient
    private final Supplier<Set<String>> liveModules = memoize(() -> getDependentModulesFromDsl(getLiveApplicationDsl()));

    /**
     * 已发布的应用的容器尺寸，使用Supplier<Object>表示，
     * 并使用memoize()方法进行了懒加载和缓存。
     */
    @Transient
    private final Supplier<Object> liveContainerSize = memoize(() -> {
        if (ApplicationType.APPLICATION.getValue() == getApplicationType()) {
            return null;
        }
        return getContainerSizeFromDSL(getLiveApplicationDsl());
    });

    /**
     * 应用的构造器，使用Builder模式，并使用@JsonCreator和@JsonProperty进行反序列化。
     */
    @Builder
    @JsonCreator
    public Application(@JsonProperty("orgId") String organizationId,
            @JsonProperty("name") String name,
            @JsonProperty("applicationType") Integer applicationType,
            @JsonProperty("applicationStatus") ApplicationStatus applicationStatus,
            @JsonProperty("publishedApplicationDSL") Map<String, Object> publishedApplicationDSL,
            @JsonProperty("publicToAll") Boolean publicToAll,
            @JsonProperty("editingApplicationDSL") Map<String, Object> editingApplicationDSL) {
        this.organizationId = organizationId;
        this.name = name;
        this.applicationType = applicationType;
        this.applicationStatus = applicationStatus;
        this.publishedApplicationDSL = publishedApplicationDSL;
        this.publicToAll = publicToAll;
        this.editingApplicationDSL = editingApplicationDSL;
    }

    /**
     * 获取正在编辑的应用查询集合。
     */
    public Set<ApplicationQuery> getEditingQueries() {
        return editingQueries.get();
    }

    /**
     * 获取已发布的应用查询集合。
     */
    public Set<ApplicationQuery> getLiveQueries() {
        return liveQueries.get();
    }

    /**
     * 获取正在编辑的应用所依赖的模块集合。
     */
    public Set<String> getEditingModules() {
        return editingModules.get();
    }

    /**
     * 获取已发布的应用所依赖的模块集合。
     */
    public Set<String> getLiveModules() {
        return liveModules.get();
    }

    /**
     * 判断应用是否对所有人公开。
     */
    public boolean isPublicToAll() {
        return BooleanUtils.toBooleanDefaultIfNull(publicToAll, false);
    }

    /**
     * 根据视图模式和查询ID获取应用查询。
     * 如果查询不存在，则抛出BizException。
     */
    public ApplicationQuery getQueryByViewModeAndQueryId(boolean isViewMode, String queryId) {
        return (isViewMode ? getLiveQueries() : getEditingQueries())
                .stream()
                .filter(query -> queryId.equals(query.getId()))
                .findFirst()
                .orElseThrow(() -> new BizException(BizError.QUERY_NOT_FOUND, "LIBRARY_QUERY_NOT_FOUND"));
    }

    /**
     * 获取已发布的应用DSL。
     * 如果没有已发布的应用DSL，则返回正在编辑的应用DSL。
     */
    @Transient
    @JsonIgnore
    public Map<String, Object> getLiveApplicationDsl() {
        return MapUtils.isEmpty(publishedApplicationDSL) ? editingApplicationDSL : publishedApplicationDSL;
    }

    /**
     * 获取应用所属的组织ID。
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * 获取应用名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 获取应用状态。
     */
    public ApplicationStatus getApplicationStatus() {
        return this.applicationStatus;
    }

    /**
     * 获取应用类型。
     * 如果应用类型为空，则返回ApplicationType.APPLICATION的默认值。
     */
    public int getApplicationType() {
        return ofNullable(applicationType).orElse(ApplicationType.APPLICATION.getValue());
    }

    /**
     * 获取正在编辑的应用DSL。
     */
    public Map<String, Object> getEditingApplicationDSL() {
        return editingApplicationDSL;
    }

    /**
     * 获取已发布的应用的容器尺寸。
     * 如果应用类型为ApplicationType.APPLICATION，则返回null。
     */
    public Object getLiveContainerSize() {
        return liveContainerSize.get();
    }
}
