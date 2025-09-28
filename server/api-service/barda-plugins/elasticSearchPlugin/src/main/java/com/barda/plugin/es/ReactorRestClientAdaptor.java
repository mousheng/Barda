package com.barda.plugin.es;

import java.io.Closeable;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;

import com.google.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 适配RestClient的回调以便将其转换为Reactor Mono。
 *
 * <p>
 * ReactorRestClientAdaptor是将RestClient的回调转换为Reactor Mono的便捷类。
 * 它通过包装RestClient并提供Mono-based的API来实现此目的。
 * </p>
 */
@Slf4j
public record ReactorRestClientAdaptor(@NotNull RestClient restClient) implements Closeable {

    /**
     * 构造器。
     *
     * @param restClient RestClient实例
     */
    public ReactorRestClientAdaptor(@NotNull RestClient restClient) {
        Preconditions.checkArgument(restClient != null);
        this.restClient = restClient;
    }

    /**
     * 执行异步的REST请求并返回Mono。
     *
     * @param request REST请求
     * @return Mono<Response>
     */
    public Mono<Response> request(Request request) {
        return Mono.create(sink -> restClient.performRequestAsync(request, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                sink.success(response);
            }

            @Override
            public void onFailure(Exception exception) {
                log.error("执行REST请求时出错。", exception);
                sink.error(exception);
            }
        }));
    }

    /**
     * 关闭RestClient。
     *
     * <p>
     * 关闭RestClient以释放相关的资源。
     * </p>
     *
     * @throws IOException 如果关闭时发生IO异常
     */
    @Override
    public void close() throws IOException {
        this.restClient.close();
    }
}