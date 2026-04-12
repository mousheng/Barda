package com.barda.api.library;

import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.api.home.SessionUserService;
import com.barda.api.usermanagement.OrgDevChecker;
import com.barda.domain.library.model.LibraryMeta;
import com.barda.domain.library.model.LibraryType;
import com.barda.domain.library.repository.LibraryMetaRepository;
import com.barda.domain.library.service.LibraryStorageService;
import com.barda.api.organization.PrimaryOrgApiService;
import com.barda.domain.organization.repository.OrganizationRepository;
import com.barda.sdk.config.LibraryConfig;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.util.MediaTypeUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 库文件服务。
 * 负责库文件的上传、删除、查询等业务逻辑。
 */
@Slf4j
@Service
public class LibraryApiService {

    @Autowired
    private LibraryMetaRepository libraryMetaRepository;

    @Autowired
    private LibraryStorageService libraryStorageService;

    @Autowired
    private PrimaryOrgApiService primaryOrgService;

    @Autowired
    private SessionUserService sessionUserService;

    @Autowired
    private OrgDevChecker orgDevChecker;

    @Autowired
    private LibraryConfig libraryConfig;

    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     * 上传共享库文件。
     * 只有主要组织的管理员可以上传。
     *
     * @param filename 文件名
     * @param base64Content Base64 编码的文件内容
     * @param displayName 显示名称
     * @param version 版本号
     * @param description 文件描述
     * @return 库文件元数据
     */
    public Mono<LibraryMeta> uploadSharedLibrary(String filename, String base64Content,
                                                  String displayName, String version, String description) {
        return primaryOrgService.checkCurrentUserPrimaryOrgAdmin()
                .then(validateLibraryFile(filename, base64Content))
                .flatMap(content -> primaryOrgService.getPrimaryOrgId()
                        .flatMap(primaryOrgId -> {
                            // 生成库标识符
                            String libraryId = generateLibraryId(displayName);
                            // 生成带版本号的存储文件名：libraryId@version.ext
                            String storageFilename = generateVersionedFilename(libraryId, version, filename);

                            // 检查文件名是否已存在
                            return libraryMetaRepository.findByFilenameAndType(storageFilename, LibraryType.SHARED)
                                    .flatMap(existing -> Mono.<LibraryMeta>error(new BizException(
                                            BizError.INVALID_PARAMETER,
                                            "该库的此版本已存在: " + displayName + " v" + version
                                    )))
                                    .switchIfEmpty(Mono.defer(() -> {
                                        // 检查共享库总大小
                                        return checkSharedLibrarySize(content.length)
                                                .then(createAndSaveLibrary(
                                                        libraryId,
                                                        filename,
                                                        storageFilename,
                                                        content,
                                                        LibraryType.SHARED,
                                                        primaryOrgId,
                                                        displayName,
                                                        version,
                                                        description
                                                ));
                                    }));
                        })
                );
    }

    /**
     * 上传组织私有库文件。
     * 组织管理员可以上传。
     *
     * @param filename 文件名
     * @param base64Content Base64 编码的文件内容
     * @param displayName 显示名称
     * @param version 版本号
     * @param description 文件描述
     * @return 库文件元数据
     */
    public Mono<LibraryMeta> uploadOrgLibrary(String filename, String base64Content,
                                               String displayName, String version, String description) {
        return orgDevChecker.checkCurrentOrgDev()
                .then(validateLibraryFile(filename, base64Content))
                .flatMap(content -> sessionUserService.getVisitorOrgMemberCache()
                        .flatMap(orgMember -> {
                            String orgId = orgMember.getOrgId();
                            // 生成库标识符
                            String libraryId = generateLibraryId(displayName);
                            // 生成带版本号的存储文件名：libraryId@version.ext
                            String storageFilename = generateVersionedFilename(libraryId, version, filename);

                            // 检查文件名是否已存在
                            return libraryMetaRepository.findByFilenameAndTypeAndOrgId(
                                            storageFilename, LibraryType.ORG, orgId)
                                    .flatMap(existing -> Mono.<LibraryMeta>error(new BizException(
                                            BizError.INVALID_PARAMETER,
                                            "该库的此版本已存在: " + displayName + " v" + version
                                    )))
                                    .switchIfEmpty(Mono.defer(() -> {
                                        // 检查组织库总大小
                                        return checkOrgLibrarySize(orgId, content.length)
                                                .then(createAndSaveLibrary(
                                                        libraryId,
                                                        filename,
                                                        storageFilename,
                                                        content,
                                                        LibraryType.ORG,
                                                        orgId,
                                                        displayName,
                                                        version,
                                                        description
                                                ));
                                    }));
                        })
                );
    }

    /**
     * 删除共享库文件。
     * 只有主要组织的管理员可以删除。
     *
     * @param filename 文件名
     * @return 空 Mono
     */
    public Mono<Void> deleteSharedLibrary(String filename) {
        return primaryOrgService.checkCurrentUserPrimaryOrgAdmin()
                .then(libraryMetaRepository.findByFilenameAndType(filename, LibraryType.SHARED))
                .switchIfEmpty(Mono.error(new BizException(
                        BizError.INVALID_PARAMETER,
                        "共享库文件不存在: " + filename
                )))
                .flatMap(this::deleteLibrary);
    }

    /**
     * 删除组织私有库文件。
     * 组织管理员可以删除。
     *
     * @param filename 文件名
     * @return 空 Mono
     */
    public Mono<Void> deleteOrgLibrary(String filename) {
        return orgDevChecker.checkCurrentOrgDev()
                .then(sessionUserService.getVisitorOrgMemberCache())
                .flatMap(orgMember -> libraryMetaRepository.findByFilenameAndTypeAndOrgId(
                        filename, LibraryType.ORG, orgMember.getOrgId()))
                .switchIfEmpty(Mono.error(new BizException(
                        BizError.INVALID_PARAMETER,
                        "组织库文件不存在: " + filename
                )))
                .flatMap(this::deleteLibrary);
    }

    /**
     * 获取共享库文件列表。
     *
     * @return 库文件元数据列表
     */
    public Flux<LibraryMeta> listSharedLibraries() {
        return libraryMetaRepository.findByType(LibraryType.SHARED);
    }

    /**
     * 获取组织私有库文件列表。
     *
     * @return 库文件元数据列表
     */
    public Flux<LibraryMeta> listOrgLibraries() {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMapMany(orgMember -> libraryMetaRepository.findByTypeAndOrgId(
                        LibraryType.ORG, orgMember.getOrgId()));
    }

    /**
     * 获取当前用户可用的所有库文件。
     *
     * @return 库文件元数据列表
     */
    public Flux<LibraryMeta> listAvailableLibraries() {
        return Flux.concat(
                listSharedLibraries(),
                listOrgLibraries()
        );
    }

    /**
     * 根据文件名和类型查找库文件。
     *
     * @param filename 文件名
     * @param type 库文件类型
     * @return 库文件元数据
     */
    public Mono<LibraryMeta> findLibrary(String filename, LibraryType type) {
        if (type == LibraryType.SHARED) {
            return libraryMetaRepository.findByFilenameAndType(filename, type);
        } else {
            return sessionUserService.getVisitorOrgMemberCache()
                    .flatMap(orgMember -> libraryMetaRepository.findByFilenameAndTypeAndOrgId(
                            filename, type, orgMember.getOrgId()));
        }
    }

    /**
     * 根据库标识符和版本号查找共享库文件。
     *
     * @param libraryId 库标识符
     * @param version 版本号（"latest" 表示最新版本）
     * @return 库文件元数据
     */
    public Mono<LibraryMeta> findSharedLibraryByIdAndVersion(String libraryId, String version) {
        if ("latest".equalsIgnoreCase(version)) {
            return libraryMetaRepository.findByLibraryIdAndTypeOrderByCreatedAtDesc(libraryId, LibraryType.SHARED)
                    .next();  // 获取第一个（最新的）
        } else {
            return libraryMetaRepository.findByLibraryIdAndVersionAndType(libraryId, version, LibraryType.SHARED);
        }
    }

    /**
     * 根据库标识符和版本号查找组织库文件。
     *
     * @param libraryId 库标识符
     * @param version 版本号（"latest" 表示最新版本）
     * @return 库文件元数据
     */
    public Mono<LibraryMeta> findOrgLibraryByIdAndVersion(String libraryId, String version) {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> {
                    String orgId = orgMember.getOrgId();
                    if ("latest".equalsIgnoreCase(version)) {
                        return libraryMetaRepository.findByLibraryIdAndTypeAndOrgIdOrderByCreatedAtDesc(
                                        libraryId, LibraryType.ORG, orgId)
                                .next();  // 获取第一个（最新的）
                    } else {
                        return libraryMetaRepository.findByLibraryIdAndVersionAndTypeAndOrgId(
                                libraryId, version, LibraryType.ORG, orgId);
                    }
                });
    }

    /**
     * 验证库文件。
     *
     * @param filename 文件名
     * @param base64Content Base64 编码的文件内容
     * @return 解码后的文件内容
     */
    private Mono<byte[]> validateLibraryFile(String filename, String base64Content) {
        return Mono.fromCallable(() -> {
            // 验证文件名
            if (StringUtils.isBlank(filename)) {
                throw new BizException(BizError.INVALID_PARAMETER, "文件名不能为空");
            }

            // 验证文件扩展名（只允许 .js 和 .css）
            String lowerFilename = filename.toLowerCase();
            if (!lowerFilename.endsWith(".js") && !lowerFilename.endsWith(".css")) {
                throw new BizException(BizError.INVALID_PARAMETER,
                        "只支持 .js 和 .css 文件");
            }

            // 解码 Base64 内容
            byte[] content;
            try {
                content = Base64.getDecoder().decode(base64Content);
            } catch (IllegalArgumentException e) {
                throw new BizException(BizError.INVALID_PARAMETER, "文件内容格式错误");
            }

            // 验证文件大小
            if (content.length > libraryConfig.getMaxFileSize()) {
                throw new BizException(BizError.INVALID_PARAMETER,
                        String.format("文件大小超过限制 (%d MB)",
                                libraryConfig.getMaxFileSize() / 1024 / 1024));
            }

            return content;
        });
    }

    /**
     * 创建并保存库文件。
     *
     * @param libraryId 库标识符
     * @param originalFilename 原始文件名
     * @param storageFilename 存储文件名（UUID）
     * @param content 文件内容
     * @param type 库文件类型
     * @param orgId 组织 ID
     * @param displayName 显示名称
     * @param version 版本号
     * @param description 文件描述
     * @return 库文件元数据
     */
    private Mono<LibraryMeta> createAndSaveLibrary(String libraryId, String originalFilename, String storageFilename, byte[] content,
                                                   LibraryType type, String orgId,
                                                   String displayName, String version, String description) {
        LibraryMeta libraryMeta = LibraryMeta.builder()
                .libraryId(libraryId)
                .originalFilename(originalFilename)
                .filename(storageFilename)
                .displayName(displayName)
                .version(version)
                .fileSize((long) content.length)
                .type(type)
                .orgId(orgId)
                .contentType(MediaTypeUtils.parse(originalFilename).toString())
                .description(description)
                .build();

        return libraryStorageService.saveToGridFs(libraryMeta, content)
                .then(libraryStorageService.syncToFileSystem(libraryMeta, content))
                .then(libraryMetaRepository.save(libraryMeta))
                .doOnSuccess(saved -> log.info("库文件已保存: {} ({}) -> {} ({})", displayName, version, storageFilename, type));
    }

    /**
     * 生成库标识符。
     * 规则：转小写、去除特殊字符、空格转连字符。
     * 例如："jQuery" -> "jquery", "Bootstrap CSS" -> "bootstrap-css"
     *
     * @param displayName 显示名称
     * @return 库标识符
     */
    private String generateLibraryId(String displayName) {
        if (StringUtils.isBlank(displayName)) {
            throw new BizException(BizError.INVALID_PARAMETER, "显示名称不能为空");
        }
        return displayName.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")  // 只保留字母、数字、空格和连字符
                .replaceAll("\\s+", "-")           // 空格转连字符
                .replaceAll("-+", "-")             // 多个连字符合并为一个
                .replaceAll("^-|-$", "");          // 去除首尾连字符
    }

    /**
     * 生成带版本号的存储文件名。
     * 格式：libraryId@version.ext
     * 例如：jquery@3.7.1.js, bootstrap@5.3.0.css
     *
     * @param libraryId 库标识符
     * @param version 版本号
     * @param originalFilename 原始文件名
     * @return 带版本号的存储文件名
     */
    private String generateVersionedFilename(String libraryId, String version, String originalFilename) {
        int lastDotIndex = originalFilename.lastIndexOf('.');
        String extension = "";
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        return libraryId + "@" + version + extension;
    }

    /**
     * 删除库文件。
     *
     * @param libraryMeta 库文件元数据
     * @return 空 Mono
     */
    private Mono<Void> deleteLibrary(LibraryMeta libraryMeta) {
        // 构造库的URL
        String libraryUrl = "/api/libraries/" +
                (libraryMeta.getType() == LibraryType.SHARED ? "shared" : "org") +
                "/" + libraryMeta.getFilename();

        return libraryStorageService.deleteFromGridFs(libraryMeta)
                .then(libraryStorageService.deleteFromFileSystem(libraryMeta))
                .then(libraryMetaRepository.delete(libraryMeta))
                .then(removeLibraryFromPreloadLists(libraryUrl, libraryMeta.getType(), libraryMeta.getOrgId()))
                .doOnSuccess(v -> log.info("库文件已删除: {} ({}), 并已从预加载列表中移除",
                        libraryMeta.getFilename(), libraryMeta.getType()));
    }

    /**
     * 从所有组织的预加载列表中移除库引用。
     *
     * @param libraryUrl 库的URL
     * @param type 库类型
     * @param orgId 组织ID（仅用于组织库）
     * @return 空 Mono
     */
    private Mono<Void> removeLibraryFromPreloadLists(String libraryUrl, LibraryType type, String orgId) {
        if (type == LibraryType.SHARED) {
            // 共享库：从所有组织的预加载列表中移除
            return organizationRepository.findAll()
                    .flatMap(org -> {
                        var commonSettings = org.getCommonSettings();
                        Object preloadLibsObj = commonSettings.get("preloadLibs");

                        if (preloadLibsObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> preloadLibs = (List<String>) preloadLibsObj;

                            // 检查是否包含该库
                            if (preloadLibs.contains(libraryUrl)) {
                                // 移除该库
                                preloadLibs.remove(libraryUrl);
                                commonSettings.put("preloadLibs", preloadLibs);

                                // 保存更新
                                return organizationRepository.save(org)
                                        .doOnSuccess(v -> log.info("已从组织 {} 的预加载列表中移除库: {}",
                                                org.getId(), libraryUrl))
                                        .then();
                            }
                        }
                        return Mono.empty();
                    })
                    .then();
        } else {
            // 组织库：只从该组织的预加载列表中移除
            return organizationRepository.findById(orgId)
                    .flatMap(org -> {
                        var commonSettings = org.getCommonSettings();
                        Object preloadLibsObj = commonSettings.get("preloadLibs");

                        if (preloadLibsObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> preloadLibs = (List<String>) preloadLibsObj;

                            // 检查是否包含该库
                            if (preloadLibs.contains(libraryUrl)) {
                                // 移除该库
                                preloadLibs.remove(libraryUrl);
                                commonSettings.put("preloadLibs", preloadLibs);

                                // 保存更新
                                return organizationRepository.save(org)
                                        .doOnSuccess(v -> log.info("已从组织 {} 的预加载列表中移除库: {}",
                                                org.getId(), libraryUrl));
                            }
                        }
                        return Mono.empty();
                    })
                    .then();
        }
    }

    /**
     * 检查共享库总大小。
     *
     * @param newFileSize 新文件大小
     * @return 空 Mono
     */
    private Mono<Void> checkSharedLibrarySize(long newFileSize) {
        return libraryMetaRepository.findByType(LibraryType.SHARED)
                .map(LibraryMeta::getFileSize)
                .reduce(0L, Long::sum)
                .flatMap(totalSize -> {
                    if (totalSize + newFileSize > libraryConfig.getMaxSharedLibrarySize()) {
                        return Mono.error(new BizException(BizError.INVALID_PARAMETER,
                                String.format("共享库总大小超过限制 (%d MB)",
                                        libraryConfig.getMaxSharedLibrarySize() / 1024 / 1024)));
                    }
                    return Mono.empty();
                });
    }

    /**
     * 检查组织库总大小。
     *
     * @param orgId 组织 ID
     * @param newFileSize 新文件大小
     * @return 空 Mono
     */
    private Mono<Void> checkOrgLibrarySize(String orgId, long newFileSize) {
        return libraryMetaRepository.findByTypeAndOrgId(LibraryType.ORG, orgId)
                .map(LibraryMeta::getFileSize)
                .reduce(0L, Long::sum)
                .flatMap(totalSize -> {
                    if (totalSize + newFileSize > libraryConfig.getMaxOrgLibrarySize()) {
                        return Mono.error(new BizException(BizError.INVALID_PARAMETER,
                                String.format("组织库总大小超过限制 (%d MB)",
                                        libraryConfig.getMaxOrgLibrarySize() / 1024 / 1024)));
                    }
                    return Mono.empty();
                });
    }
}
