package com.barda.domain.plugin.client;

import static com.barda.sdk.constants.GlobalContext.REQUEST;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.barda.domain.plugin.client.dto.DatasourcePluginDefinition;
import com.barda.domain.plugin.client.dto.GetPluginDynamicConfigRequestDTO;
import com.barda.infra.js.NodeServerClient;
import com.barda.infra.js.NodeServerHelper;
import com.barda.sdk.config.CommonConfigHelper;
import com.barda.sdk.exception.ServerException;
import com.barda.sdk.models.DatasourceTestResult;
import com.barda.sdk.models.QueryExecutionResult;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 数据源插件客户端类。
 *
 * 该类提供与数据源插件相关的操作，包括获取插件定义、执行查询和测试数据源配置等功能。
 */
@Slf4j
@Component
public class DatasourcePluginClient implements NodeServerClient {

    /**
     * 定义ExchangeStrategies的静态常量，用于配置WebClient的交换策略。
     */
    private static final ExchangeStrategies EXCHANGE_STRATEGIES = ExchangeStrategies
            .builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1))
            .build();

    /**
     * 定义WebClient的静态常量，用于进行Web请求。
     * <p>
     * 该WebClient使用自定义的ExchangeStrategies进行配置，允许处理无限大小的响应体。
     * </p>
     */
    private static final WebClient WEB_CLIENT = WebClient.builder()
            .exchangeStrategies(EXCHANGE_STRATEGIES)
            .build();

    @Autowired
    private CommonConfigHelper commonConfigHelper;
    @Autowired
    private NodeServerHelper nodeServerHelper;

    private static final String PLUGINS_PATH = "plugins";
    private static final String RUN_PLUGIN_QUERY = "runPluginQuery";
    private static final String VALIDATE_PLUGIN_DATA_SOURCE_CONFIG = "validatePluginDataSourceConfig";
    private static final String GET_PLUGIN_DYNAMIC_CONFIG = "getPluginDynamicConfig";

    /**
     * 安全地获取插件动态配置。
     *
     * @param getPluginDynamicConfigRequestDTOS 获取插件动态配置的请求 DTO 列表
     * @return 获取的插件动态配置的 Mono
     */
    public Mono<List<Object>> getPluginDynamicConfigSafely(List<GetPluginDynamicConfigRequestDTO> getPluginDynamicConfigRequestDTOS) {
        return getPluginDynamicConfig(getPluginDynamicConfigRequestDTOS)
                .onErrorResume(throwable -> {
                    log.error("request /getPluginDynamicConfig error.", throwable);
                    return Mono.just(Collections.emptyList());
                })
                .defaultIfEmpty(Collections.emptyList());
    }

    /**
     * 获取插件动态配置。
     *
     * @param getPluginDynamicConfigRequestDTOS 获取插件动态配置的请求 DTO 列表
     * @return 获取的插件动态配置的 Mono
     */
    public Mono<List<Object>> getPluginDynamicConfig(List<GetPluginDynamicConfigRequestDTO> getPluginDynamicConfigRequestDTOS) {
        if (CollectionUtils.isEmpty(getPluginDynamicConfigRequestDTOS)) {
            return Mono.just(Collections.emptyList());
        }
        return getAcceptLanguage()
                .flatMap(language -> WEB_CLIENT
                        .post()
                        .uri(nodeServerHelper.createUri(GET_PLUGIN_DYNAMIC_CONFIG))
                        .header(HttpHeaders.ACCEPT_LANGUAGE, language)
                        .bodyValue(getPluginDynamicConfigRequestDTOS)
                        .<List<Object>> exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(new ParameterizedTypeReference<>() {
                                });
                            }
                            log.error("request /getPluginDynamicConfig error.{},{}", getPluginDynamicConfigRequestDTOS, response.statusCode().value());
                            return Mono.error(new ServerException("get dynamic config error"));
                        })
                        .timeout(Duration.ofSeconds(10))
                );
    }

    /**
     * 获取指定类型的数据源插件定义。
     *
     * @param type 数据源插件类型
     * @return 获取的数据源插件定义的 Mono
     */
    public Mono<DatasourcePluginDefinition> getDatasourcePluginDefinition(String type) {
        return getDatasourcePluginDefinitions()
                .filter(datasourcePluginDefinition -> datasourcePluginDefinition.getId().equals(type))
                .next();
    }

    /**
     * 获取所有数据源插件定义。
     *
     * @return 获取的数据源插件定义的 Flux
     */
    public Flux<DatasourcePluginDefinition> getDatasourcePluginDefinitions() {
        if (StringUtils.isBlank(commonConfigHelper.getHost())) {
            return Flux.empty();
        }
        return getAcceptLanguage()
                .flatMap(language -> WEB_CLIENT
                        .get()
                        .uri(nodeServerHelper.createUri(PLUGINS_PATH))
                        .header(HttpHeaders.ACCEPT_LANGUAGE, language)
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(new ParameterizedTypeReference<List<DatasourcePluginDefinition>>() {
                                });
                            }
                            log.error("request /plugins error.{}", response.statusCode().value());
                            return Mono.just(Collections.emptyList());
                        })
                        .timeout(Duration.ofSeconds(10))
                )
                .onErrorResume(throwable -> {
                    log.error("request /plugins error", throwable);
                    return Mono.just(Collections.emptyList());
                })
                .defaultIfEmpty(Collections.emptyList())
                .flatMapIterable(Function.identity());
    }

    /**
     * 执行查询。
     *
     * @param pluginName 插件名称
     * @param queryDsl 查询 DSL
     * @param context 查询上下文
     * @param datasourceConfig 数据源配置
     * @return 查询执行结果的 Mono
     */
    @SuppressWarnings("unchecked")
    public Mono<QueryExecutionResult> executeQuery(String pluginName, Object queryDsl, List<Map<String, Object>> context, Object datasourceConfig) {
        return getAcceptLanguage()
                .flatMap(language -> WEB_CLIENT
                        .post()
                        .uri(nodeServerHelper.createUri(RUN_PLUGIN_QUERY))
                        .header(HttpHeaders.ACCEPT_LANGUAGE, language)
                        .bodyValue(Map.of("pluginName", pluginName, "dsl", queryDsl, "context", context, "dataSourceConfig", datasourceConfig))
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(Map.class)
                                        .map(map -> map.get("result"))
                                        .map(QueryExecutionResult::success);
                            }
                            return response.bodyToMono(Map.class)
                                    .map(map -> MapUtils.getString(map, "message"))
                                    .map(QueryExecutionResult::errorWithMessage);
                        }));
    }

    /**
     * 测试数据源配置。
     *
     * @param pluginName 插件名称
     * @param datasourceConfig 数据源配置
     * @return 测试结果的 Mono
     */
    @SuppressWarnings("unchecked")
    public Mono<DatasourceTestResult> test(String pluginName, Object datasourceConfig) {
        return getAcceptLanguage()
                .flatMap(language -> WEB_CLIENT
                        .post()
                        .uri(nodeServerHelper.createUri(VALIDATE_PLUGIN_DATA_SOURCE_CONFIG))
                        .header(HttpHeaders.ACCEPT_LANGUAGE, language)
                        .bodyValue(Map.of("pluginName", pluginName, "dataSourceConfig", datasourceConfig))
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(Map.class)
                                        .map(map -> {
                                            if (MapUtils.getBoolean(map, "success", false)) {
                                                return DatasourceTestResult.testSuccess();
                                            }
                                            return DatasourceTestResult.testFail(MapUtils.getString(map, "message", ""));
                                        });
                            }
                            return response.bodyToMono(Map.class)
                                    .map(map -> DatasourceTestResult.testFail(MapUtils.getString(map, "message", "")));
                        }));
    }

    /**
     * 获取请求的Accept-Language头部信息的Mono。
     *
     * @return 包含Accept-Language头部信息的Mono。
     */
    private Mono<String> getAcceptLanguage() {
        return Mono.deferContextual(contextView -> contextView.<ServerHttpRequest> getOrEmpty(REQUEST)
                .map(request -> request.getHeaders().getFirst(HttpHeaders.ACCEPT_LANGUAGE))
                .map(Mono::just)
                .orElse(Mono.just("")));
    }
}
