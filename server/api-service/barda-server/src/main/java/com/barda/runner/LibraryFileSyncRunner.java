package com.barda.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.barda.domain.library.repository.LibraryMetaRepository;
import com.barda.domain.library.service.LibraryStorageService;
import com.barda.sdk.config.LibraryConfig;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 库文件同步启动器。
 * 在应用启动时，从 GridFS 恢复库文件到文件系统。
 */
@Slf4j
@Component
public class LibraryFileSyncRunner implements ApplicationRunner {

    @Autowired
    private LibraryMetaRepository libraryMetaRepository;

    @Autowired
    private LibraryStorageService libraryStorageService;

    @Autowired
    private LibraryConfig libraryConfig;

    @Override
    public void run(ApplicationArguments args) {
        if (!libraryConfig.isEnableFileSystemSync()) {
            log.info("文件系统同步已禁用，跳过库文件恢复");
            return;
        }

        log.info("开始从 GridFS 恢复库文件到文件系统...");

        libraryMetaRepository.findAll()
                .flatMap(libraryMeta -> libraryStorageService.restoreFromGridFs(libraryMeta)
                        .doOnSuccess(success -> {
                            if (success) {
                                log.info("已恢复库文件: {} ({})",
                                        libraryMeta.getFilename(),
                                        libraryMeta.getType());
                            }
                        })
                        .onErrorResume(e -> {
                            log.warn("恢复库文件失败: {} ({}), 错误: {}",
                                    libraryMeta.getFilename(),
                                    libraryMeta.getType(),
                                    e.getMessage());
                            return Mono.just(false);
                        })
                )
                .collectList()
                .doOnSuccess(list -> log.info("库文件恢复完成，共 {} 个文件", list.size()))
                .doOnError(e -> log.error("库文件恢复失败", e))
                .subscribe();
    }
}
