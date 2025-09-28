package com.barda.api.query;

import static com.barda.sdk.constants.GlobalContext.CLIENT_IP;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.barda.api.home.SessionUserService;
import com.barda.api.query.view.LibraryQueryRequestFromJs;
import com.barda.api.query.view.QueryExecutionRequest;
import com.barda.api.query.view.QueryResultView;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.util.CookieHelper;
import com.barda.sdk.util.LocaleUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = {Url.QUERY_URL, NewUrl.QUERY_URL})
public class QueryController {

    @Autowired
    private ApplicationQueryApiService applicationQueryApiService;

    @Autowired
    private LibraryQueryApiService libraryQueryApiService;

    @Autowired
    private CookieHelper cookieHelper;

    @Autowired
    private SessionUserService sessionUserService;

    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    /**
     * 执行查询并返回结果。
     *
     * @param exchange ServerWebExchange
     * @param queryExecutionRequest 查询执行请求
     * @return 查询结果视图的 Mono 对象
     */
    @PostMapping("/execute")
    public Mono<QueryResultView> execute(ServerWebExchange exchange,
            @RequestBody QueryExecutionRequest queryExecutionRequest) {
        return Mono.deferContextual(contextView -> {
            Locale locale = LocaleUtils.getLocale(contextView);
            return getQueryResult(exchange, queryExecutionRequest)
                    .map(result -> new QueryResultView(result, locale))
                    .onErrorResume(throwable -> {
                        if (throwable instanceof BizException bizException && bizException.getError() == BizError.LOGIN_EXPIRED) {
                            String cookieToken = cookieHelper.getCookieToken(exchange);
                            return sessionUserService.removeUserSession(cookieToken)
                                    .then(businessEventPublisher.publishUserLogoutEvent())
                                    .then(Mono.error(throwable));
                        }
                        return Mono.error(throwable);
                    });
        });
    }

    /**
     * 从 JavaScript 执行库查询并返回结果。
     *
     * @param exchange ServerWebExchange
     * @param queryExecutionRequest 库查询执行请求
     * @return 查询结果视图的 Mono 对象
     */
    @PostMapping("/execute-from-node")
    public Mono<QueryResultView> executeLibraryQueryFromJs(ServerWebExchange exchange,
            @RequestBody LibraryQueryRequestFromJs queryExecutionRequest) {
        return Mono.deferContextual(contextView -> {
            Locale locale = LocaleUtils.getLocale(contextView);

            String ip = contextView.getOrDefault(CLIENT_IP, "");
            if (!checkIp(ip)) {
                return ofError(BizError.NOT_AUTHORIZED, "NOT_AUTHORIZED");
            }
            return libraryQueryApiService.executeLibraryQueryFromJs(exchange, queryExecutionRequest)
                    .map(result -> new QueryResultView(result, locale));
        });
    }

    /**
     * 检查 IP 地址是否合法。
     *
     * @param ip IP 地址
     * @return true 表示合法，false 表示不合法
     */
    private boolean checkIp(String ip) {
        return "127.0.0.1".equals(ip) || "localhost".equals(ip);
    }

    /**
     * 获取查询结果。
     *
     * @param exchange ServerWebExchange
     * @param queryExecutionRequest 查询执行请求
     * @return 查询执行结果的 Mono 对象
     */
    private Mono<QueryExecutionResult> getQueryResult(ServerWebExchange exchange, QueryExecutionRequest queryExecutionRequest) {
        if (queryExecutionRequest.isApplicationQueryRequest()) {
            return applicationQueryApiService.executeApplicationQuery(exchange, queryExecutionRequest);
        }
        return libraryQueryApiService.executeLibraryQuery(exchange, queryExecutionRequest);
    }
}
