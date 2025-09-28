package com.barda.domain.material.service.storage;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;

import com.barda.domain.material.model.MaterialMeta;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * 提供关于 Material 存储的服务接口。
 * 该接口定义了保存、下载和删除 Material 的基本操作。
 */
public interface MaterialStorageService {
    /**
     * 自定义的调度器，用于材料传输的异步处理。
     */
    Scheduler SCHEDULER = Schedulers.newBoundedElastic(50, 1000, "material-transport");

    /**
     * 保存 Material 数据。
     *
     * @param materialMeta Material 的元数据
     * @param content      Material 的内容，以字节数组表示
     * @return 包含保存操作结果的 Mono，若保存成功则为 true，否则为 false
     */
    Mono<Boolean> save(MaterialMeta materialMeta, byte[] content);

    /**
     * 下载 Material 数据。
     *
     * @param materialMeta Material 的元数据
     * @return 包含 DataBuffer 的 Publisher，用于传输 Material 的内容
     */
    Publisher<? extends DataBuffer> download(MaterialMeta materialMeta);

    /**
     * 删除 Material 数据。
     *
     * @param materialMeta Material 的元数据
     * @return 表示删除操作完成的 Mono
     */
    Mono<Void> delete(MaterialMeta materialMeta);
}
