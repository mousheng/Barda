package com.barda.infra.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.MDC;

import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

/**
 * 日志工具类。
 * 它提供静态方法来处理和记录日志，并在日志中包含上下文信息。
 */
public class LogUtils {

    /**
     * 上下文映射的键。
     */
    public static final String CONTEXT_MAP = "context-map";

    /**
     * 创建一个函数，将键值对添加到上下文映射中。
     *
     * @param key   键
     * @param value 值
     * @return 一个函数，将键值对添加到上下文映射中
     */
    public static Function<Context, Context> putLogContext(String key, String value) {
        return ctx -> {
            Optional<Map<String, String>> optionalContextMap = ctx.getOrEmpty(CONTEXT_MAP);

            if (optionalContextMap.isPresent()) {
                optionalContextMap.get().put(key, value);
                return ctx;
            }

            Map<String, String> ctxMap = new HashMap<>();
            ctxMap.put(key, value);

            return ctx.put(CONTEXT_MAP, ctxMap);
        };
    }

    /**
     * 创建一个消费者，在onNext信号时记录日志。
     *
     * @param log 日志记录器
     * @return 一个消费者，在onNext信号时记录日志
     */
    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> log) {
        return signal -> {
            if (signal.getType()!= SignalType.ON_NEXT) {
                return;
            }

            Optional<Map<String, String>> maybeContextMap = signal.getContextView().getOrEmpty(CONTEXT_MAP);

            if (maybeContextMap.isEmpty()) {
                log.accept(signal.get());
                return;
            }

            MDC.setContextMap(maybeContextMap.get());
            try {
                log.accept(signal.get());
            } finally {
                MDC.clear();
            }
        };
    }

    /**
     * 创建一个消费者，在onError信号时记录日志。
     *
     * @param log 日志记录器
     * @return 一个消费者，在onError信号时记录日志
     */
    public static <T> Consumer<Signal<T>> logOnError(Consumer<Throwable> log) {
        return signal -> {
            if (!signal.isOnError()) {
                return;
            }

            logOnError(signal.getThrowable(), log, signal.getContextView());
        };
    }

    /**
     * 在发生错误时记录日志。
     *
     * @param throwable    发生的错误
     * @param logConsumer  日志记录器
     * @param ctx           上下文
     */
    public static void logOnError(Throwable throwable, Consumer<Throwable> logConsumer, ContextView ctx) {
        Optional<Map<String, String>> maybeContextMap = ctx.getOrEmpty(CONTEXT_MAP);

        if (maybeContextMap.isEmpty()) {
            logConsumer.accept(throwable);
            return;
        }

        MDC.setContextMap(maybeContextMap.get());
        try {
            logConsumer.accept(throwable);
        } finally {
            MDC.clear();
        }
    }
}