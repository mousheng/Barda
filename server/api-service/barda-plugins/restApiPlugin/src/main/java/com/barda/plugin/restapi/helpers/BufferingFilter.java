package com.barda.plugin.restapi.helpers;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import reactor.core.publisher.Mono;

/**
 * 这是一个过滤器，用于在内存中加载请求正文并计算正文的内容长度。
 * 然后，它将此内容长度作为头部添加到原始请求中。
 */
public class BufferingFilter implements ExchangeFilterFunction {

    @Override
    @NonNull
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, ExchangeFunction next) {
        return next.exchange(ClientRequest
                .from(request)
                .body((message, context) -> request
                        .body()
                        .insert(new BufferingRequestDecorator(message), context))
                .build());
    }

    /**
     * 一个私有的内部类，用于在请求正文中添加缓冲功能。
     * 它继承自 ClientHttpRequestDecorator 并重写了 writeWith 方法来计算正文的长度并添加到头部。
     */
    private static class BufferingRequestDecorator extends ClientHttpRequestDecorator {

        public BufferingRequestDecorator(ClientHttpRequest delegate) {
            super(delegate);
        }

        @Override
        @NonNull
        public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
            return DataBufferUtils
                    .join(body)
                    .flatMap(dataBuffer -> {
                        int length = dataBuffer.readableByteCount();
                        this.getDelegate().getHeaders().setContentLength(length);
                        return super.writeWith(body);
                    });
        }
    }
}