package com.barda.sdk.util;

import static reactor.core.Exceptions.throwIfFatal;

import com.barda.sdk.exception.BaseException;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.exception.PluginError;
import com.barda.sdk.exception.PluginException;

import reactor.core.publisher.Mono;

/**
 * 异常工具类。
 * 该类提供了一组静态方法来处理和创建业务异常和插件异常。
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * 创建一个延迟的业务异常 Mono。
     *
     * @param errorCode  业务错误代码。
     * @param messageKey  错误消息的键。
     * @param args  错误消息的参数。
     * @param <T>  结果的类型。
     * @return 包含业务异常的 Mono。
     */
    public static <T> Mono<T> deferredError(BizError errorCode, String messageKey, Object... args) {
        return Mono.defer(() -> Mono.error(new BizException(errorCode, messageKey, args)));
    }

    /**
     * 创建一个立即的业务异常 Mono。
     *
     * @param errorCode  业务错误代码。
     * @param messageKey  错误消息的键。
     * @param args  错误消息的参数。
     * @param <T>  结果的类型。
     * @return 包含业务异常的 Mono。
     */
    public static <T> Mono<T> ofError(BizError errorCode, String messageKey, Object... args) {
        return Mono.error(new BizException(errorCode, messageKey, args));
    }

    /**
     * 创建一个业务异常。
     *
     * @param errorCode  业务错误代码。
     * @param messageKey  错误消息的键。
     * @param args  错误消息的参数。
     * @return 业务异常。
     */
    public static BizException ofException(BizError errorCode, String messageKey, Object... args) {
        return new BizException(errorCode, messageKey, args);
    }

    /**
     * 创建一个立即的插件异常 Mono。
     *
     * @param error  插件错误代码。
     * @param messageKey  错误消息的键。
     * @param args  错误消息的参数。
     * @param <T>  结果的类型。
     * @return 包含插件异常的 Mono。
     */
    public static <T> Mono<T> ofPluginError(PluginError error, String messageKey, Object... args) {
        return Mono.error(ofPluginException(error, messageKey, args));
    }

    /**
     * 创建一个插件异常。
     *
     * @param error  插件错误代码。
     * @param messageKey  错误消息的键。
     * @param args  错误消息的参数。
     * @return 插件异常。
     */
    public static PluginException ofPluginException(PluginError error, String messageKey, Object... args) {
        return new PluginException(error, messageKey, args);
    }

    /**
     * 包装一个插件异常或业务异常，并将原始异常作为根异常。
     *
     * @param error  插件错误代码。
     * @param messageKey  错误消息的键。
     * @param e  原始异常。
     * @return 包含根异常的插件异常或业务异常。
     */
    public static BaseException wrapException(PluginError error, String messageKey, Throwable e) {
        BaseException baseException = castAsBaseException(e);
        if (baseException != null) {
            return baseException;
        }
        return ofPluginException(error, messageKey, e.getMessage());
    }

    /**
     * 包装一个插件异常或业务异常，并将原始异常作为根异常。
     *
     * @param error  业务错误代码。
     * @param messageKey  错误消息的键。
     * @param e  原始异常。
     * @return 包含根异常的插件异常或业务异常。
     */
    public static BaseException wrapException(BizError error, String messageKey, Throwable e) {
        BaseException baseException = castAsBaseException(e);
        if (baseException != null) {
            return baseException;
        }
        return new BizException(error, messageKey, e.getMessage());
    }

    /**
     * 传播一个插件异常或业务异常，并将原始异常作为根异常。
     *
     * @param error  插件错误代码。
     * @param messageKey  错误消息的键。
     * @param e  原始异常。
     * @param <T>  结果的类型。
     * @return 包含根异常的插件异常或业务异常的 Mono。
     */
    public static <T> Mono<T> propagateError(PluginError error, String messageKey, Throwable e) {
        BaseException baseException = castAsBaseException(e);
        if (baseException != null) {
            return Mono.error(baseException);
        }
        return ofPluginError(error, messageKey, e.getMessage());
    }

    /**
     * 传播一个插件异常或业务异常，并将原始异常作为根异常。
     *
     * @param error  业务错误代码。
     * @param messageKey  错误消息的键。
     * @param e  原始异常。
     * @param <T>  结果的类型。
     * @return 包含根异常的插件异常或业务异常的 Mono。
     */
    public static <T> Mono<T> propagateError(BizError error, String messageKey, Throwable e) {
        BaseException baseException = castAsBaseException(e);
        if (baseException != null) {
            return Mono.error(baseException);
        }
        return ofError(error, messageKey, e.getMessage());
    }

    private static BaseException castAsBaseException(Throwable e) {
        throwIfFatal(e);
        if (e instanceof BaseException baseException) {
            return baseException;
        }
        if (e.getCause() instanceof BaseException baseException) {
            return baseException;
        }
        return null;
    }

}
