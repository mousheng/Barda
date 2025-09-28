package com.barda.domain.datasource.model;

import static com.barda.domain.datasource.model.DatasourceCreationSource.LEGACY_WORKSPACE_PREDEFINED;
import static com.barda.domain.datasource.model.DatasourceCreationSource.SYSTEM_STATIC;
import static com.barda.domain.plugin.DatasourceMetaInfoConstants.GRAPHQL_API;
import static com.barda.domain.plugin.DatasourceMetaInfoConstants.REST_API;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.barda.domain.plugin.DatasourceMetaInfoConstants;
import com.barda.domain.plugin.client.dto.DatasourcePluginDefinition;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.HasIdAndAuditing;
import com.barda.sdk.models.JsDatasourceConnectionConfig;
import com.barda.sdk.plugin.graphql.GraphQLDatasourceConfig;
import com.barda.sdk.plugin.bardaapi.BardaApiDatasourceConfig;
import com.barda.sdk.plugin.restapi.RestApiDatasourceConfig;
import com.barda.sdk.util.LocaleUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 数据源实体类，继承了通用的ID和审计信息。
 */
@Getter
@Setter
public class Datasource extends HasIdAndAuditing {

    /**
     * 默认状态为NORMAL
     */
    private static final DatasourceStatus DEFAULT_STATUS = DatasourceStatus.NORMAL;
    public static final String QUICK_REST_API_ID = "#QUICK_REST_API";
    public static final String QUICK_GRAPHQL_ID = "#QUICK_GRAPHQL";
    public static final String BARDA_API_ID = "#BARDA_API";

    /**
     * 系统静态数据源的ID集合。
     */
    private static final Set<String> SYSTEM_STATIC_IDS = Set.of(QUICK_REST_API_ID,
            QUICK_GRAPHQL_ID, BARDA_API_ID);

    /**
     * 静态常量QUICK_REST_API，QUICK_GRAPHQL_API和 BARDA_API 用于快速获取常见的数据源实例。
     */
    public static final Datasource QUICK_REST_API;
    public static final Datasource QUICK_GRAPHQL_API;
    public static final Datasource BARDA_API;

    static {
        QUICK_REST_API = new Datasource();
        QUICK_REST_API.setId(QUICK_REST_API_ID);
        QUICK_REST_API.setName("REST API");
        QUICK_REST_API.setType(REST_API);
        QUICK_REST_API.setCreationSource(SYSTEM_STATIC.getValue());
        QUICK_REST_API.setDetailConfig(RestApiDatasourceConfig.EMPTY_CONFIG);

        QUICK_GRAPHQL_API = new Datasource();
        QUICK_GRAPHQL_API.setId(QUICK_GRAPHQL_ID);
        QUICK_GRAPHQL_API.setName("GraphQL API");
        QUICK_GRAPHQL_API.setType(GRAPHQL_API);
        QUICK_GRAPHQL_API.setCreationSource(SYSTEM_STATIC.getValue());
        QUICK_GRAPHQL_API.setDetailConfig(GraphQLDatasourceConfig.EMPTY_CONFIG);

        BARDA_API = new Datasource();
        BARDA_API.setId(BARDA_API_ID);
        BARDA_API.setName("Barda API");
        BARDA_API.setType(DatasourceMetaInfoConstants.BARDA_API);
        BARDA_API.setCreationSource(SYSTEM_STATIC.getValue());
        BARDA_API.setDetailConfig(BardaApiDatasourceConfig.INSTANCE);
    }

    /**
     * 数据源名称。
     */
    private String name;

    /**
     * 数据源类型。
     */
    private String type;

    /**
     * 组织ID。
     */
    private String organizationId;

    /**
     * 创建来源。
     */
    private int creationSource;

    /**
     * 数据源状态。
     */
    private DatasourceStatus datasourceStatus;

    // for js data source plugin
    @Nullable
    @Transient
    private DatasourcePluginDefinition pluginDefinition;

    /**
     * 数据源连接配置。
     */
    @JsonProperty(value = "datasourceConfig")
    private DatasourceConnectionConfig detailConfig;

    /**
     * 将更新的数据源合并到当前数据源。
     *
     * @param updatedDatasource 更新的数据源对象。
     * @return 合并后的数据源对象。
     */
    public Datasource mergeWith(Datasource updatedDatasource) {
        setName(updatedDatasource.getName());
        Optional.of(getDetailConfig())
                .ifPresentOrElse(currentDetailConfig -> {
                            if (updatedDatasource.getDetailConfig() instanceof JsDatasourceConnectionConfig jsDatasourceConnectionConfig) {
                                jsDatasourceConnectionConfig.setType(updatedDatasource.getType());
                            }
                            DatasourceConnectionConfig updatedDetailConfig =
                                    currentDetailConfig.mergeWithUpdatedConfig(updatedDatasource.getDetailConfig());
                            setDetailConfig(updatedDetailConfig);
                        },
                        () -> setDetailConfig(updatedDatasource.getDetailConfig()));
        return this;
    }

    /**
     * 判断数据源是否为系统静态数据源。
     *
     * @return 如果是系统静态数据源，则返回true；否则返回false。
     */
    @JsonIgnore
    public boolean isSystemStatic() {
        return creationSource == SYSTEM_STATIC.getValue();
    }

    /**
     * 根据数据源ID获取其显示名称。
     *
     * @param datasourceId 数据源ID。
     * @param locale 本地化信息。
     * @return 数据源的显示名称。
     */
    public static String getDisplayName(String datasourceId, Locale locale) {
        if (QUICK_REST_API_ID.equals(datasourceId)) {
            return LocaleUtils.getMessage(locale, "QUICK_REST_DATASOURCE_NAME");
        }

        if (QUICK_GRAPHQL_ID.equals(datasourceId)) {
            return LocaleUtils.getMessage(locale, "QUICK_GRAPHQL_DATASOURCE_NAME");
        }

        if (BARDA_API_ID.equals(datasourceId)) {
            return LocaleUtils.getMessage(locale, "BARDA_DATASOURCE_NAME");
        }
        return "";
    }

    /**
     * 判断数据源是否为旧版本的Quick Rest Api。
     *
     * @return 如果是旧版本的Quick Rest Api，则返回true；否则返回false。
     */
    @JsonIgnore
    public boolean isLegacyQuickRestApi() {
        return REST_API.equals(type) && creationSource == LEGACY_WORKSPACE_PREDEFINED.getValue();
    }

    /**
     * 判断数据源是否为旧版本的Barda Api。
     *
     * @return 如果是旧版本的Barda Api，则返回true；否则返回false。
     */
    @JsonIgnore
    public boolean isLegacyBardaApi() {
        return !REST_API.equals(type) && creationSource == LEGACY_WORKSPACE_PREDEFINED.getValue();
    }

    /**
     * 获取组织ID。
     *
     * @return 组织ID。
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * 获取创建时间的毫秒数。
     *
     * @return 创建时间的毫秒数。
     */
    public long getCreateTime() {
        return createdAt.toEpochMilli();
    }

    /**
     * 获取数据源状态，如果状态为空，则返回默认状态。
     *
     * @return 数据源状态。
     */
    public DatasourceStatus getDatasourceStatus() {
        return ObjectUtils.firstNonNull(this.datasourceStatus, DEFAULT_STATUS);
    }

    /**
     * 判断指定的数据源ID是否为系统静态数据源ID。
     *
     * @param datasourceId 数据源ID。
     * @return 如果是系统静态数据源ID，则返回true；否则返回false。
     */
    public static boolean isSystemStaticId(String datasourceId) {
        return SYSTEM_STATIC_IDS.contains(datasourceId);
    }

    /**
     * 判断指定的数据源ID是否不是系统静态数据源ID。
     *
     * @param datasourceId 数据源ID。
     * @return 如果不是系统静态数据源ID，则返回true；否则返回false。
     */
    public static boolean isNotSystemStaticId(String datasourceId) {
        return !isSystemStaticId(datasourceId);
    }
}
