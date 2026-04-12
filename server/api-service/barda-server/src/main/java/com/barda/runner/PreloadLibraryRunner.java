package com.barda.runner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.barda.domain.library.model.LibraryMeta;
import com.barda.domain.library.model.LibraryType;
import com.barda.domain.library.repository.LibraryMetaRepository;
import com.barda.domain.library.service.LibraryStorageService;
import com.barda.api.organization.PrimaryOrgApiService;
import com.barda.sdk.util.MediaTypeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 预装库文件启动任务。
 * 在首次启动时，从预定义的目录加载库文件到共享库。
 */
@Slf4j
@Component
@Order(100)
public class PreloadLibraryRunner implements ApplicationRunner {

    private static final String PRELOAD_MARKER_KEY = "preload_libraries_imported";
    private static final String PRELOAD_CONFIG_FILE = "preload-libraries.json";
    private static final String PRELOAD_LIBRARIES_DIR = "/app/preload-libraries";

    @Autowired
    private LibraryMetaRepository libraryMetaRepository;

    @Autowired
    private LibraryStorageService libraryStorageService;

    @Autowired
    private PrimaryOrgApiService primaryOrgService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        log.info("检查是否需要预装库文件...");

        checkAndImportPreloadLibraries()
                .doOnSuccess(imported -> {
                    if (imported) {
                        log.info("预装库文件导入完成");
                    } else {
                        log.info("预装库文件已导入，跳过");
                    }
                })
                .doOnError(error -> log.error("预装库文件导入失败", error))
                .subscribe();
    }

    /**
     * 检查并导入预装库文件。
     *
     * @return 是否执行了导入
     */
    public Mono<Boolean> checkAndImportPreloadLibraries() {
        return checkPreloadMarker()
                .flatMap(alreadyImported -> {
                    if (alreadyImported) {
                        return Mono.just(false);
                    }

                    // 读取配置文件
                    return loadPreloadConfig()
                            .flatMap(configs -> primaryOrgService.getPrimaryOrgId()
                                    .flatMap(primaryOrgId -> importLibraries(configs, primaryOrgId))
                                    .then(setPreloadMarker())
                                    .thenReturn(true)
                                    // 没有组织时静默跳过预装
                                    .onErrorResume(error -> {
                                        log.warn("预装库文件跳过（无组织）: {}", error.getMessage());
                                        return Mono.just(false);
                                    })
                            );
                });
    }

    /**
     * 检查预装标记。
     *
     * @return 是否已预装
     */
    private Mono<Boolean> checkPreloadMarker() {
        // 检查是否已有共享库文件，如果有则认为已预装
        return libraryMetaRepository.findByType(LibraryType.SHARED)
                .hasElements();
    }

    /**
     * 设置预装标记。
     */
    private Mono<Void> setPreloadMarker() {
        // 标记已通过数据库中存在共享库来判断，无需额外标记
        return Mono.empty();
    }

    /**
     * 加载预装配置。
     *
     * @return 预装配置列表
     */
    private Mono<List<PreloadLibraryConfig>> loadPreloadConfig() {
        return Mono.fromCallable(() -> {
            try {
                ClassPathResource resource = new ClassPathResource(PRELOAD_CONFIG_FILE);
                if (!resource.exists()) {
                    log.warn("预装配置文件不存在: {}", PRELOAD_CONFIG_FILE);
                    return List.of();
                }

                try (InputStream inputStream = resource.getInputStream()) {
                    return objectMapper.readValue(inputStream,
                            new TypeReference<List<PreloadLibraryConfig>>() {});
                }
            } catch (IOException e) {
                log.error("读取预装配置文件失败", e);
                return List.of();
            }
        });
    }

    /**
     * 导入库文件。
     *
     * @param configs 配置列表
     * @param primaryOrgId 主要组织 ID
     * @return 空 Mono
     */
    private Mono<Void> importLibraries(List<PreloadLibraryConfig> configs, String primaryOrgId) {
        return Flux.fromIterable(configs)
                .flatMap(config -> importLibrary(config, primaryOrgId))
                .then();
    }

    /**
     * 导入单个库文件。
     *
     * @param config 配置
     * @param primaryOrgId 主要组织 ID
     * @return 空 Mono
     */
    private Mono<Void> importLibrary(PreloadLibraryConfig config, String primaryOrgId) {
        return Mono.fromCallable(() -> {
                    Path filePath = Paths.get(PRELOAD_LIBRARIES_DIR, config.getFilename());
                    if (!Files.exists(filePath)) {
                        log.warn("预装库文件不存在: {}", filePath);
                        return null;
                    }

                    byte[] content = Files.readAllBytes(filePath);
                    return content;
                })
                .flatMap(content -> {
                    if (content == null) {
                        return Mono.empty();
                    }

                    // 生成带版本号的存储文件名：libraryId@version.ext
                    String storageFilename = config.getLibraryId() + "@" + config.getVersion() +
                            getFileExtension(config.getFilename());

                    LibraryMeta libraryMeta = LibraryMeta.builder()
                            .libraryId(config.getLibraryId())
                            .originalFilename(config.getFilename())
                            .filename(storageFilename)
                            .displayName(config.getDisplayName())
                            .version(config.getVersion())
                            .fileSize((long) content.length)
                            .type(LibraryType.SHARED)
                            .orgId(primaryOrgId)
                            .contentType(MediaTypeUtils.parse(config.getFilename()).toString())
                            .description(config.getDescription())
                            .build();

                    return libraryStorageService.saveToGridFs(libraryMeta, content)
                            .then(libraryStorageService.syncToFileSystem(libraryMeta, content))
                            .then(libraryMetaRepository.save(libraryMeta))
                            .doOnSuccess(saved -> log.info("预装库文件已导入: {} v{} -> {}",
                                    config.getDisplayName(), config.getVersion(), storageFilename))
                            .then();
                })
                .onErrorResume(error -> {
                    log.error("导入库文件失败: {}", config.getFilename(), error);
                    return Mono.empty();
                });
    }

    /**
     * 获取文件扩展名。
     *
     * @param filename 文件名
     * @return 扩展名（包含点）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * 预装库配置。
     */
    @Data
    public static class PreloadLibraryConfig {
        private String libraryId;
        private String displayName;
        private String version;
        private String filename;
        private String description;
    }
}
