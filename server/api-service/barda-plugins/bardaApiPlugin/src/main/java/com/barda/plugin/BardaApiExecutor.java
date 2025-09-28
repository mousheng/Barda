package com.barda.plugin;

import static com.barda.sdk.constants.Authentication.isAnonymousUser;
import static com.barda.sdk.models.QueryExecutionResult.error;
import static com.barda.sdk.models.QueryExecutionResult.success;
import static com.barda.sdk.util.StreamUtils.collectList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.collections4.MapUtils;
import org.pf4j.Extension;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.plugin.common.QueryExecutor;
import com.barda.sdk.plugin.bardaapi.BardaApiDatasourceConfig;
import com.barda.sdk.query.QueryVisitorContext;
import com.barda.sdk.util.CookieHelper;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

/**
 * 实现了 QueryExecutor 接口的 BardaApiExecutor 类。
 * 该类用于执行与 Barda API 的查询操作。
 */
@Extension
public class BardaApiExecutor implements QueryExecutor<BardaApiDatasourceConfig, Object, BardaApiQueryExecutionContext> {
    private static final String QUERY_ORG_USERS = "queryOrgUsers";

    private final String cookieName;

    /**
     * 构造函数，用于创建 BardaApiExecutor 类的实例。
     *
     * @param cookieHelper 用于获取 Cookie 名称的 CookieHelper
     */
    public BardaApiExecutor(CookieHelper cookieHelper) {
        cookieName = cookieHelper.getCookieName();
    }

    /**
     * 构造函数，用于创建 BardaApiExecutor 类的实例。
     *
     * @param connectionConfig    数据源连接配置
     * @param queryConfig         查询配置
     * @param requestParams       请求参数
     * @param queryVisitorContext 查询访问器上下文
     * @return 查询执行上下文的Mono
     */
    @Override
    public Mono<BardaApiQueryExecutionContext> doBuildQueryExecutionContextMono(BardaApiDatasourceConfig connectionConfig, Map<String, Object> queryConfig, Map<String, Object> requestParams, QueryVisitorContext queryVisitorContext) {
        String actionType = MapUtils.getString(queryConfig, "compType", "");
        MultiValueMap<String, HttpCookie> cookies = queryVisitorContext.getCookies();
        if (actionType.equalsIgnoreCase(QUERY_ORG_USERS)) {
            return Mono.just(BardaApiQueryExecutionContext.builder()
                    .actionType(actionType)
                    .visitorId(queryVisitorContext.getVisitorId())
                    .applicationOrgId(queryVisitorContext.getApplicationOrgId())
                    .requestCookies(cookies)
                    .port(queryVisitorContext.getSystemPort())
                    .build());
        }
        throw new PluginException(BardaApiPluginError.BARDA_API_INVALID_REQUEST_TYPE, "BARDA_INTERNAL_INVALID_REQUEST_TYPE");
    }

    /**
     * 执行查询操作。
     *
     * @param o 未使用
     * @param context 查询执行上下文
     * @return 查询执行结果的 Mono
     */
    @Override
    public Mono<QueryExecutionResult> executeQuery(Object o, BardaApiQueryExecutionContext context) {

        String actionType = context.getActionType();
        if (actionType.equals(QUERY_ORG_USERS)) {
            return doListOrgUsers0(context);
        }

        throw new PluginException(BardaApiPluginError.BARDA_API_INVALID_REQUEST_TYPE, "BARDA_INTERNAL_INVALID_REQUEST_TYPE");
    }

    /**
     * 执行查询操作，查询组织用户列表。
     *
     * @param context 查询执行上下文
     * @return 查询执行结果的 Mono
     */
    private Mono<QueryExecutionResult> doListOrgUsers0(BardaApiQueryExecutionContext context) {

        String visitorId = context.getVisitorId();
        if (isAnonymousUser(visitorId)) {
            return Mono.just(QueryExecutionResult.success(emptyList()));
        }

        String url = "http://localhost:" + context.getPort() + "/api/v1/organizations/" + context.getApplicationOrgId() + "/members";

        return WebClient.builder()
                .defaultCookies(injectCookies(context))
                .build()
                .method(HttpMethod.GET)
                .uri(url)
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(BardaResponse.class))
                .map(responseView -> {
                    if (responseView.isSuccess()) {
                        return success(ofNullable(responseView.getData())
                                .map(it -> MapUtils.getObject(it, "members", emptyList()))
                                .orElse(emptyList())
                        );
                    }
                    return error(BardaApiPluginError.BARDA_API_REQUEST_ERROR, "REQUEST_ERROR",
                            responseView.getCode(), responseView.getMessage());
                })
                .onErrorResume(e -> Mono.just(
                        QueryExecutionResult.error(BardaApiPluginError.BARDA_API_REQUEST_ERROR, "BARDA_INTERNAL_REQUEST_ERROR",
                                e.getMessage())));
    }

    /**
     * 注入 Cookie 到请求中。
     *
     * @param request 查询执行上下文
     * @return 用于注入 Cookie 的 Consumer
     */
    private Consumer<MultiValueMap<String, String>> injectCookies(BardaApiQueryExecutionContext request) {
        return currentCookies -> {
            MultiValueMap<String, HttpCookie> requestCookies = request.getRequestCookies();

            requestCookies.entrySet()
                    .stream()
                    .filter(it -> cookieName.equals(it.getKey()))
                    .forEach(entry -> {
                        String cookieName = entry.getKey();
                        List<HttpCookie> httpCookies = entry.getValue();
                        currentCookies.addAll(cookieName, collectList(httpCookies, HttpCookie::getValue));
                    });
        };
    }

    /**
     * 定义了 BardaResponse 内部类，用于表示 Barda API 的响应。
     * 该类包含了响应的状态码、消息和数据。
     */
    @Getter
    @Setter
    private static class BardaResponse {

        /**
         * 定义了 Barda API 成功的状态码。
         */
        public static final int SUCCESS = 1;

        private int code;
        private String message;
        private Map<String, Object> data;

        /**
         * 检查 Barda API 响应是否成功。
         *
         * @return 如果响应成功，返回 true，否则返回 false
         */
        public boolean isSuccess() {
            return code == SUCCESS;
        }
    }


}
