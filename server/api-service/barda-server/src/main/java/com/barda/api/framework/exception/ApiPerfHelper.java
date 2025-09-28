package com.barda.api.framework.exception;

import static java.util.Optional.ofNullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;

import com.barda.infra.perf.PerfEvent;
import com.barda.infra.perf.PerfHelper;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.PluginError;

import io.micrometer.core.instrument.Tags;

/**
 * 该类是API性能助手，用于提供API和插件的性能指标。
 * 它使用了Spring的@Component注解来将该类作为Spring Bean进行管理。
 */
@Component
public class ApiPerfHelper {

    private static final String URL_TAG = "url";
    private static final String ERROR_CODE_TAG = "errorCode";

    /**
     * 用于执行性能指标收集的帮助类。
     */
    @Autowired
    private PerfHelper perfHelper;

    /**
     * 记录API业务错误的性能指标。
     *
     * @param error 发生的API业务错误。
     * @param requestPath 发生API业务错误的请求路径。
     */
    public void perf(BizError error, RequestPath requestPath) {
        perfHelper.count(PerfEvent.API_ERROR_CODE,
                Tags.of(ERROR_CODE_TAG, String.valueOf(error.getBizErrorCode()),
                        URL_TAG, ofNullable(requestPath).map(RequestPath::pathWithinApplication).map(PathContainer::value).orElse("unknownUrl"))
        );
    }

    /**
     * 记录插件错误的性能指标。
     *
     * @param error 发生的插件错误。
     * @param requestPath 发生插件错误的请求路径。
     */
    public void perf(PluginError error, RequestPath requestPath) {
        perfHelper.count(PerfEvent.PLUGIN_ERROR_CODE,
                Tags.of(ERROR_CODE_TAG, error.name(),
                        URL_TAG, ofNullable(requestPath).map(RequestPath::pathWithinApplication).map(PathContainer::value).orElse("unknownUrl"))
        );
    }
}
