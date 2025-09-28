package com.barda.api.framework.exception;

import static com.barda.api.framework.view.ResponseView.error;
import static com.barda.sdk.util.LocaleUtils.getLocale;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import com.barda.api.framework.view.ResponseView;
import com.barda.infra.util.LogUtils;
import com.barda.sdk.exception.BaseException;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.exception.PluginError;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.exception.ServerException;
import com.barda.sdk.util.LocaleUtils;

import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

/**
 * 该类是全局的异常处理器，用于处理API和插件的异常。
 * 它使用了Spring的@ControllerAdvice注解来将该类作为控制器的全局异常处理器。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger mainLog = LoggerFactory.getLogger(getClass());
    private final Logger queryErrorLog = LoggerFactory.getLogger("queryError");

    /**
     * 用于执行API性能指标收集的帮助类。
     */
    @Autowired
    private ApiPerfHelper apiPerfHelper;

    /**
     * 处理BizException的异常处理器。
     *
     * @param e 发生的BizException。
     * @param exchange 发生异常的ServerWebExchange。
     * @return 包含错误响应的Mono。
     */
    @ExceptionHandler
    @ResponseBody
    public Mono<ResponseView<?>> catchBizException(BizException e, ServerWebExchange exchange) {

        exchange.getResponse().setStatusCode(HttpStatus.resolve(e.getHttpStatus()));

        return Mono.deferContextual(ctx -> {
            apiPerfHelper.perf(e.getError(), exchange.getRequest().getPath());
            doLog(e, ctx, e.getError().logVerbose());
            Locale locale = getLocale(ctx);
            return Mono.just(error(e.getBizErrorCode(), e.getMessage(locale)));
        });
    }

    /**
     * 处理TimeoutException的异常处理器。
     *
     * @param e 发生的TimeoutException。
     * @param exchange 发生异常的ServerWebExchange。
     * @return 包含错误响应的Mono。
     */
    @ExceptionHandler
    @ResponseBody
    public Mono<ResponseView<?>> catchTimeoutException(TimeoutException e, ServerWebExchange exchange) {
        BizError bizError = BizError.PLUGIN_EXECUTION_TIMEOUT_WITHOUT_TIME;
        exchange.getResponse().setStatusCode(HttpStatus.resolve(bizError.getHttpErrorCode()));
        return Mono.deferContextual(ctx -> {
            apiPerfHelper.perf(bizError, exchange.getRequest().getPath());
            doLog(e, ctx, bizError.logVerbose());
            Locale locale = getLocale(ctx);
            return Mono.just(error(bizError.getBizErrorCode(), LocaleUtils.getMessage(locale, "PLUGIN_EXECUTION_TIMEOUT_WITHOUT_TIME")));
        });
    }

    /**
     * 处理WebExchangeBindException的异常处理器。
     *
     * @param exc 发生的WebExchangeBindException。
     * @param exchange 发生异常的ServerWebExchange。
     * @return 包含错误响应的Mono。
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseBody
    public Mono<ResponseView<?>> catchWebExchangeBindException(WebExchangeBindException exc, ServerWebExchange exchange) {
        BizError bizError = BizError.INVALID_PARAMETER;
        exchange.getResponse().setStatusCode(HttpStatus.resolve(bizError.getHttpErrorCode()));
        apiPerfHelper.perf(bizError, exchange.getRequest().getPath());
        Map<String, String> errors = new HashMap<>();
        exc.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Mono.deferContextual(ctx -> {
            Locale locale = getLocale(ctx);
            return Mono.just(error(bizError.getBizErrorCode(), LocaleUtils.getMessage(locale, "INVALID_PARAMETER", errors.toString())));
        });
    }


    /**
     * 处理ServerWebInputException的异常处理器。
     *
     * @param e 发生的ServerWebInputException。
     * @param exchange 发生异常的ServerWebExchange。
     * @return 包含错误响应的Mono。
     */
    @ExceptionHandler
    @ResponseBody
    public Mono<ResponseView<?>> catchServerWebInputException(ServerWebInputException e, ServerWebExchange exchange) {
        BizError bizError = BizError.INVALID_PARAMETER;
        exchange.getResponse().setStatusCode(HttpStatus.resolve(bizError.getHttpErrorCode()));
        return Mono.deferContextual(ctx -> {
            apiPerfHelper.perf(bizError, exchange.getRequest().getPath());
            doLog(e, ctx, bizError.logVerbose());
            String reason = e.getReason();
            if (e.getMethodParameter() != null) {
                reason = e.getMethodParameter().getParameterName() + "' : " + e.getMethodParameter().getContainingClass().getSimpleName() + (
                        e.getMethodParameter().getMethod() != null ? "." + e.getMethodParameter().getMethod().getName() : "");
            }
            Locale locale = getLocale(ctx);
            return Mono.just(error(bizError.getBizErrorCode(), LocaleUtils.getMessage(locale, "INVALID_PARAMETER_PLZ_CHECK", reason)));
        });
    }

    /**
     * 处理PluginException的异常处理器。
     *
     * @param e 发生的PluginException。
     * @param exchange 发生异常的ServerWebExchange。
     * @return 包含错误响应的Mono。
     */
    @ExceptionHandler
    @ResponseBody
    public Mono<ResponseView<?>> catchPluginException(PluginException e, ServerWebExchange exchange) {
        PluginError pluginError = e.getError();
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return Mono.deferContextual(ctx -> {
            apiPerfHelper.perf(pluginError, exchange.getRequest().getPath());
            doLog(e, ctx, pluginError.logVerbose(), queryErrorLog);
            Locale locale = getLocale(ctx);
            return Mono.just(error(500, e.getLocaleMessage(locale)));
        });
    }

    /**
     * 处理ServerException的异常处理器。
     *
     * @param e 发生的ServerException。
     * @param exchange 发生异常的ServerWebExchange。
     * @return 包含错误响应的Mono。
     */
    @ExceptionHandler
    @ResponseBody
    public Mono<ResponseView<?>> catchServerException(ServerException e, ServerWebExchange exchange) {
        BizError bizError = BizError.INTERNAL_SERVER_ERROR;
        exchange.getResponse().setStatusCode(HttpStatus.resolve(bizError.getHttpErrorCode()));
        return Mono.deferContextual(ctx -> {
            apiPerfHelper.perf(bizError, exchange.getRequest().getPath());
            doLog(e, ctx, bizError.logVerbose());
            return Mono.just(error(bizError.getBizErrorCode(), e.getMessage()));
        });
    }

    /**
     * 处理Exception的异常处理器。
     *
     * @param e 发生的Exception。
     * @param exchange 发生异常的ServerWebExchange。
     * @return 包含错误响应的Mono。
     */
    @ExceptionHandler
    @ResponseBody
    public Mono<ResponseView<?>> catchException(java.lang.Exception e, ServerWebExchange exchange) {
        BizError bizError = BizError.INTERNAL_SERVER_ERROR;
        exchange.getResponse().setStatusCode(HttpStatus.resolve(bizError.getHttpErrorCode()));
        return Mono.deferContextual(ctx -> {
            apiPerfHelper.perf(bizError, exchange.getRequest().getPath());
            doLog(e, ctx, bizError.logVerbose());
            Locale locale = getLocale(ctx);
            return Mono.just(error(bizError.getBizErrorCode(), LocaleUtils.getMessage(locale, "INTERNAL_SERVER_ERROR")));
        });
    }

    /**
     * 执行日志记录的私有方法。
     *
     * @param error 要记录的异常
     * @param ctx 上下文视图
     * @param logVerbose 是否记录详细日志
     */
    private void doLog(Throwable error, ContextView ctx, boolean logVerbose) {
        doLog(error, ctx, logVerbose, mainLog);
    }

    /**
     * 执行日志记录的私有方法。
     *
     * @param error 要记录的异常
     * @param ctx 上下文视图
     * @param logVerbose 是否记录详细日志
     * @param logger 要使用的日志记录器
     */
    private void doLog(Throwable error, ContextView ctx, boolean logVerbose, Logger logger) {
        if (!(error instanceof BaseException)) {
            LogUtils.logOnError(error, (ex) -> mainLog.error("", ex), ctx);
            return;
        }

        if (logVerbose) {
            LogUtils.logOnError(error, (ex) -> logger.error("", ex), ctx);
        } else {
            LogUtils.logOnError(error, (ex) -> logger.error("oops, {}", ex.getMessage()), ctx);
        }
    }
}