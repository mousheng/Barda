package com.barda.api.datasource;

import static com.barda.sdk.exception.BizError.INVALID_DATASOURCE_CONFIGURATION;
import static com.barda.sdk.util.ExceptionUtils.ofException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.sdk.models.JsDatasourceConnectionConfig;
import com.barda.sdk.util.JsonUtils;

/**
 * 该类是用于将{@link UpsertDatasourceRequest}映射为{@link Datasource}的映射器。
 * 它使用了Spring的@Component注解来将该类作为Spring Bean进行管理。
 */
@Component
public class UpsertDatasourceRequestMapper {

    /**
     * 用于获取数据源元信息的服务。
     */
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    /**
     * 将{@link UpsertDatasourceRequest}映射为{@link Datasource}。
     *
     * @param dto 包含数据源配置信息的请求。
     * @return 映射后的{@link Datasource}。
     */
    public Datasource resolve(UpsertDatasourceRequest dto) {

        if (StringUtils.isBlank(dto.getName())) {
            throw ofException(INVALID_DATASOURCE_CONFIGURATION, "DATASOURCE_NAME_EMPTY");
        }

        if (StringUtils.isBlank(dto.getType())) {
            throw ofException(INVALID_DATASOURCE_CONFIGURATION, "INVALID_DATASOURCE_TYPE_0");
        }

        if (StringUtils.isBlank(dto.getOrganizationId())) {
            throw ofException(INVALID_DATASOURCE_CONFIGURATION, "INVALID_DATASOURCE_ORG_ID");
        }

        Datasource datasource = new Datasource();
        datasource.setId(dto.getId());
        datasource.setName(dto.getName());
        datasource.setType(dto.getType());
        datasource.setOrganizationId(dto.getOrganizationId());
        if (datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType())) {
            datasource.setDetailConfig(JsonUtils.fromJson(JsonUtils.toJson(dto.getDatasourceConfig()), JsDatasourceConnectionConfig.class));
        } else {
            datasource.setDetailConfig(datasourceMetaInfoService.resolveDetailConfig(dto.getDatasourceConfig(), dto.getType()));
        }
        return datasource;
    }
}
