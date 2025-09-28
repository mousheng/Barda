package com.barda.api.framework.exception;

import static com.barda.sdk.util.ExceptionUtils.ofException;
import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;
import com.barda.api.framework.service.GlobalContextService;
import com.barda.api.framework.view.ResponseView;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.util.LocaleUtils;

import io.lettuce.core.RedisCommandTimeoutException;
import reactor.core.publisher.Mono;

/**
 * 该类是自定义的错误Web异常处理器，用于处理API和插件的错误。
 * 它使用了Spring的@Component和@Order注解来将该类作为Spring Bean进行管理，并指定了其执行顺序。
 * 该类继承自DefaultErrorWebExceptionHandler，并重写了其中的方法来实现自定义的错误处理逻辑。
 */
@Component
@Order(-2)
public class CustomErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    private final Logger errorFileLog = LoggerFactory.getLogger("errorFile");

    /**
     * 用于执行API性能指标收集的帮助类。
     */
    @Autowired
    private ApiPerfHelper apiPerfHelper;

    /**
     * 用于提供全局上下文的服务。
     */
    @Autowired
    private GlobalContextService globalContextService;

    /**
     * 构造函数，用于初始化自定义的错误Web异常处理器。
     *
     * @param errorAttributes 用于获取错误属性的类。
     * @param resources 用于获取资源的类。
     * @param serverProperties 用于获取服务器属性的类。
     * @param applicationContext 用于获取应用程序上下文的类。
     * @param viewResolvers 用于获取视图解析器的类。
     * @param serverCodecConfigurer 用于获取服务器编解码器配置器的类。
     */
    @Autowired
    
    public CustomErrorWebExceptionHandler(ErrorAttributes errorAttributes, Resources resources,
            ServerProperties serverProperties, ApplicationContext applicationContext,
            ObjectProvider<ViewResolver> viewResolvers,
            ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, resources, serverProperties.getError(), applicationContext);
        this.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        this.setMessageWriters(serverCodecConfigurer.getWriters());
        this.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return route(all(), this::render);
    }

    /**
     * 处理错误请求的入口方法。
     *
     * @param request 发生错误的请求。
     * @return 包含错误响应的Mono。
     */
    private Mono<ServerResponse> render(ServerRequest request) {
        Map<String, Object> error = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        int httpErrorCode = getHttpStatus(error);

        HttpStatus resolve = HttpStatus.resolve(httpErrorCode);
        if (resolve == null || !resolve.is5xxServerError()) {
            String errorMsg = String.valueOf(error.get("error"));
            return ServerResponse.status(httpErrorCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(ResponseView.error(httpErrorCode, errorMsg)));
        }

        Throwable throwable = getError(request);
        BizException bizException = parseBizException(throwable);
        apiPerfHelper.perf(bizException.getError(), request.requestPath());
        if (throwable != null) {
            errorFileLog.error("oops {}, {}", throwable.getClass(), throwable.getMessage());
        }

        Locale locale = globalContextService.getClientLocale(request);
        return ServerResponse.status(bizException.getError().getHttpErrorCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(ResponseView.error(bizException.getError().getBizErrorCode(),
                        LocaleUtils.getMessage(locale, bizException.getMessageKey(), bizException.getArgs()))));
    }

    /**
     * 解析并转换Throwable为BizException。
     *
     * @param error 发生的Throwable。
     * @return 转换后的BizException。
     */
    private BizException parseBizException(Throwable error) {
        if (error instanceof RedisCommandTimeoutException) {
            return ofException(BizError.INFRA_REDIS_TIMEOUT, "INFRA_REDIS_TIMEOUT");
        }
        if (error instanceof MongoTimeoutException) {
            return ofException(BizError.INFRA_MONGO_TIMEOUT, "INFRA_MONGODB_TIMEOUT");
        }
        if (error instanceof MongoExecutionTimeoutException) {
            return ofException(BizError.INFRA_MONGO_TIMEOUT, "INFRA_MONGODB_TIMEOUT");
        }
        if (error instanceof MongoSocketReadTimeoutException) {
            return ofException(BizError.INFRA_MONGO_TIMEOUT, "INFRA_MONGODB_TIMEOUT");
        }
        if (error instanceof BizException bizException) {
            return bizException;
        }
        return ofException(BizError.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }
}
