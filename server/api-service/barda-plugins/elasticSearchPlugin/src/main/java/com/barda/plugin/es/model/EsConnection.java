package com.barda.plugin.es.model;

import java.io.Closeable;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import com.barda.plugin.es.ReactorRestClientAdaptor;

/**
 * 基于 ReactorRestClientAdaptor 的 Elasticsearch 连接记录。
 * 实现了 {@link Closeable} 接口，以便在使用完毕后关闭连接。
 */
public record EsConnection(@NotNull ReactorRestClientAdaptor reactorRestClientAdaptor) implements Closeable {

    /**
     * 基于 ReactorRestClientAdaptor 创建 EsConnection 实例。
     *
     * @param reactorRestClientAdaptor 非空的 ReactorRestClientAdaptor 实例
     * @throws IllegalArgumentException 如果 {@code reactorRestClientAdaptor} 为 null
     */
    public EsConnection(@NotNull ReactorRestClientAdaptor reactorRestClientAdaptor) {
        Preconditions.checkArgument(reactorRestClientAdaptor != null);
        this.reactorRestClientAdaptor = reactorRestClientAdaptor;
    }

    /**
     * 关闭此 Elasticsearch 连接。
     * 关闭时，将关闭内部的 ReactorRestClientAdaptor。
     *
     * @throws IOException 如果关闭时发生 I/O 异常
     */
    @Override
    public void close() throws IOException {
        this.reactorRestClientAdaptor.close();
    }
}
