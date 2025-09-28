package com.barda.domain.solutions;

import static com.barda.sdk.exception.BizError.TEMPLATE_NOT_CORRECT;
import static com.barda.sdk.exception.BizError.TEMPLATE_NOT_EXIST;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static java.util.Objects.isNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.barda.domain.application.model.Application;
import com.barda.domain.application.model.ApplicationStatus;
import com.barda.domain.application.service.ApplicationService;
import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceCreationSource;
import com.barda.domain.datasource.service.DatasourceService;
import com.barda.domain.query.model.ApplicationQuery;
import com.barda.domain.template.model.Template;
import com.barda.domain.template.service.TemplateService;
import com.barda.infra.annotation.NonEmptyMono;
import com.barda.infra.util.TupleUtils;
import com.barda.sdk.util.JsonUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 该类提供与模板相关的解决方案。
 */
@Lazy
@Service
public class TemplateSolution {

    private static final int RANDOM_LENGTH = 6; // 用于生成新数据源名称的随机字符串长度

    @Autowired
    private TemplateService templateService; // 模板服务

    @Autowired
    private DatasourceService datasourceService; // 数据源服务

    @Autowired
    private ApplicationService applicationService; // 应用服务

    /**
     * 从模板创建应用。
     *
     * @param templateId 模板ID
     * @param orgId      组织ID
     * @param visitorId  访问者ID
     * @return 新创建的应用
     */
    public Mono<Application> createFromTemplate(String templateId, String orgId, String visitorId) {
        return templateService.getById(templateId)
                .switchIfEmpty(deferredError(TEMPLATE_NOT_EXIST, "TEMPLATE_NOT_EXIST"))
                .zipWith(Mono.just(orgId))
                .zipWhen(tuple -> applicationService.findById(tuple.getT1().getApplicationId())
                                .switchIfEmpty(deferredError(TEMPLATE_NOT_EXIST, "TEMPLATE_NOT_EXIST")),
                        TupleUtils::merge)
                .zipWhen(tuple -> copyDatasourceFromTemplateToCurrentOrganization(tuple.getT2(), tuple.getT3(), visitorId), TupleUtils::merge)
                .flatMap(tuple -> {
                    Template template = tuple.getT1();
                    String organizationId = tuple.getT2();
                    Application templateApplication = tuple.getT3();
                    List<Pair<String, String>> datasourceIdMap = tuple.getT4();
                    String dsl = JsonUtils.toJson(templateApplication.getLiveApplicationDsl());
                    for (Pair<String, String> stringStringPair : datasourceIdMap) {
                        dsl = dsl.replace(stringStringPair.getLeft(), stringStringPair.getRight());
                    }
                    Map<String, Object> applicationDSL = JsonUtils.fromJsonMap(dsl);
                    Application application = Application.builder()
                            .applicationStatus(ApplicationStatus.NORMAL)
                            .organizationId(organizationId)
                            .name(template.getName())
                            .editingApplicationDSL(applicationDSL)
                            .publishedApplicationDSL(applicationDSL)
                            .build();
                    return applicationService.create(application, visitorId);
                });
    }

    /**
     * 获取模板应用ID集合。
     *
     * @param applicationIds 应用ID集合
     * @return 模板应用ID集合
     */
    @NonEmptyMono
    public Mono<Set<String>> getTemplateApplicationIds(Collection<String> applicationIds) {
        return templateService.getByApplicationIds(applicationIds)
                .map(Template::getApplicationId)
                .collect(Collectors.toSet());
    }

    /**
     * 从模板复制数据源到当前组织。
     *
     * @param currentOrganizationId 当前组织ID
     * @param application           模板应用
     * @param visitorId             访问者ID
     * @return 新复制的数据源ID对集合
     */
    private Mono<List<Pair<String, String>>> copyDatasourceFromTemplateToCurrentOrganization(String currentOrganizationId, Application application,
            String visitorId) {
        Set<ApplicationQuery> queries = application.getLiveQueries();
        if (isNull(queries)) {
            return ofError(TEMPLATE_NOT_CORRECT, "TEMPLATE_NOT_CORRECT");
        }
        Set<String> datasourceIds = queries.stream()
                .map(query -> query.getBaseQuery().getDatasourceId())
                .collect(Collectors.toSet());
        return Flux.fromIterable(datasourceIds)
                .flatMap(datasourceId -> doCopyDatasource(currentOrganizationId, datasourceId, visitorId)
                        .map(copiedDatasourceId -> Pair.of(datasourceId, copiedDatasourceId)))
                .collectList();
    }

    /**
     * 复制数据源。
     *
     * @param organizationId 组织ID
     * @param datasourceId   数据源ID
     * @param visitorId      访问者ID
     * @return 新复制的数据源ID
     */
    @SuppressWarnings({"ConstantConditions"})
    private Mono<String> doCopyDatasource(String organizationId, String datasourceId, String visitorId) {
        return datasourceService.getById(datasourceId)
                .flatMap(datasource -> {
                    if (datasource.isSystemStatic()) {
                        return Mono.just(datasource.getId());
                    }

                    // return new QUICK_REST_API id for legacy quick rest api
                    if (datasource.isLegacyQuickRestApi()) {
                        return Mono.just(Datasource.QUICK_REST_API.getId());
                    }

                    if (datasource.isLegacyBardaApi()) {
                        return Mono.just(Datasource.BARDA_API.getId());
                    }
                    return createNewDatasourceFrom(organizationId, visitorId, datasource);
                });
    }

    /**
     * 从模板创建新的数据源。
     *
     * @param organizationId 组织ID
     * @param visitorId      访问者ID
     * @param datasource     模板数据源
     * @return 新创建的数据源ID
     */
    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    @Nonnull
    private Mono<String> createNewDatasourceFrom(String organizationId, String visitorId, Datasource datasource) {
        Datasource copyDatasource = new Datasource();
        copyDatasource.setName(generateCopyDatasourceName(datasource.getName()));
        copyDatasource.setType(datasource.getType());
        copyDatasource.setDetailConfig(datasource.getDetailConfig());
        copyDatasource.setCreationSource(DatasourceCreationSource.CLONE_FROM_TEMPLATE.getValue());
        copyDatasource.setOrganizationId(organizationId);
        return datasourceService.create(copyDatasource, visitorId)

                .map(Datasource::getId);
    }

    /**
     * 为新复制的数据源生成名称。
     *
     * @param name 模板数据源名称
     * @return 新复制的数据源名称
     */
    private String generateCopyDatasourceName(String name) {
        return name + "_" + RandomStringUtils.random(RANDOM_LENGTH, true, false);
    }
}
