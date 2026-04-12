package com.barda.domain.library.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.stereotype.Service;

import com.barda.domain.library.model.LibraryMeta;
import com.barda.sdk.config.LibraryConfig;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 库文件存储服务。
 * 负责库文件的 GridFS 存储和文件系统同步。
 */
@Slf4j
@Service
public class LibraryStorageService {

    @Autowired
    @Qualifier("materialGridFsTemplate")
    private ReactiveGridFsTemplate gridFsTemplate;

    @Autowired
    private LibraryConfig libraryConfig;

    /**
     * 保存库文件到 GridFS。
     *
     * @param libraryMeta 库文件元数据
     * @param content 文件内容
     * @return 是否成功
     */
    public Mono<Boolean> saveToGridFs(LibraryMeta libraryMeta, byte[] content) {
        return gridFsTemplate.store(
                        Mono.just(new DefaultDataBufferFactory().wrap(content)),
                        libraryMeta.getGridFsPath()
                )
                .doOnSuccess(id -> log.info("文件已保存到 GridFS: {}", libraryMeta.getGridFsPath()))
                .thenReturn(true);
    }

    /**
     * 从 GridFS 下载库文件。
     *
     * @param libraryMeta 库文件元数据
     * @return 文件内容流
     */
    public Publisher<? extends DataBuffer> downloadFromGridFs(LibraryMeta libraryMeta) {
        return gridFsTemplate.findFirst(queryByPath(libraryMeta.getGridFsPath()))
                .flatMap(gridFsTemplate::getResource)
                .flatMapMany(ReactiveGridFsResource::getContent);
    }

    /**
     * 从 GridFS 删除库文件。
     *
     * @param libraryMeta 库文件元数据
     * @return 空 Mono
     */
    public Mono<Void> deleteFromGridFs(LibraryMeta libraryMeta) {
        return gridFsTemplate.delete(queryByPath(libraryMeta.getGridFsPath()))
                .doOnSuccess(v -> log.info("文件已从 GridFS 删除: {}", libraryMeta.getGridFsPath()));
    }

    /**
     * 同步文件到文件系统。
     *
     * @param libraryMeta 库文件元数据
     * @param content 文件内容
     * @return 是否成功
     */
    public Mono<Boolean> syncToFileSystem(LibraryMeta libraryMeta, byte[] content) {
        if (!libraryConfig.isEnableFileSystemSync()) {
            log.debug("文件系统同步已禁用");
            return Mono.just(false);
        }

        return Mono.fromCallable(() -> {
                    Path filePath = getFileSystemPath(libraryMeta);

                    // 创建父目录
                    Files.createDirectories(filePath.getParent());

                    // 写入文件
                    Files.write(filePath, content,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);

                    log.info("文件已同步到文件系统: {}", filePath);
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.warn("文件系统同步失败: {}, 错误: {}",
                            libraryMeta.getFilename(), e.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * 从文件系统删除文件。
     *
     * @param libraryMeta 库文件元数据
     * @return 是否成功
     */
    public Mono<Boolean> deleteFromFileSystem(LibraryMeta libraryMeta) {
        if (!libraryConfig.isEnableFileSystemSync()) {
            return Mono.just(false);
        }

        return Mono.fromCallable(() -> {
                    Path filePath = getFileSystemPath(libraryMeta);
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        log.info("文件已从文件系统删除: {}", filePath);
                        return true;
                    }
                    return false;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.warn("文件系统删除失败: {}, 错误: {}",
                            libraryMeta.getFilename(), e.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * 从 GridFS 恢复文件到文件系统。
     *
     * @param libraryMeta 库文件元数据
     * @return 是否成功
     */
    public Mono<Boolean> restoreFromGridFs(LibraryMeta libraryMeta) {
        return Flux.from(downloadFromGridFs(libraryMeta))
                .reduce(new DefaultDataBufferFactory().allocateBuffer(), (acc, buffer) -> {
                    acc.write(buffer);
                    return acc;
                })
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .flatMap(content -> syncToFileSystem(libraryMeta, content));
    }

    /**
     * 获取文件系统路径。
     *
     * @param libraryMeta 库文件元数据
     * @return 文件系统路径
     */
    private Path getFileSystemPath(LibraryMeta libraryMeta) {
        return Paths.get(libraryConfig.getStaticFileRoot(), libraryMeta.getFileSystemPath());
    }

    /**
     * 构建 GridFS 查询。
     *
     * @param path GridFS 路径
     * @return 查询对象
     */
    private Query queryByPath(String path) {
        return Query.query(Criteria.where("filename").is(path));
    }
}
