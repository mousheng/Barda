package com.barda.api.framework;


import static com.barda.sdk.exception.BizError.SERVER_NOT_READY;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.barda.api.framework.view.ResponseView;
import com.barda.api.framework.warmup.WarmupHelper;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 该类是状态控制器，用于处理与状态相关的请求。
 * 它使用了Spring的@RestController和@RequestMapping注解来将该类作为RESTful API进行管理。
 * 该类同时支持旧的URL（Url.STATE_URL）和新的URL（NewUrl.STATE_URL）。
 */
@Slf4j
@RestController
@RequestMapping(value = {Url.STATE_URL, NewUrl.STATE_URL})
public class StateController {

    /**
     * 用于执行预热操作的帮助类。
     */
    @Autowired
    private WarmupHelper warmupHelper;

    /**
     * 指示控制器是否已准备就绪的标志。
     * 它使用了volatile关键字来保证在多线程环境下的可见性。
     */
    private volatile boolean ready;

    /**
     * 处理健康检查的请求。
     *
     * @return 包含健康检查结果的响应。
     *         如果控制器尚未准备就绪，则返回503（SERVER_NOT_READY）状态码和相应的错误信息。
     */
    @RequestMapping(value = "/healthCheck", method = RequestMethod.HEAD)
    public Mono<ResponseView<Boolean>> healthCheck() {
        if (!ready) {
            return warmupHelper.warmup()
                    .doOnSuccess(it -> ready = true)
                    .then(ofError(SERVER_NOT_READY, "SERVER_NOT_READY"));
        }
        return Mono.just(ResponseView.success(true));
    }
}
