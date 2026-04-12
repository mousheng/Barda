package com.barda.api.library;

import static com.barda.infra.constant.NewUrl.LIBRARY_URL;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barda.api.framework.view.ResponseView;
import com.barda.domain.library.model.LibraryMeta;
import com.barda.domain.library.model.LibraryType;
import com.barda.api.library.LibraryApiService;
import com.barda.domain.library.service.LibraryStorageService;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.util.MediaTypeUtils;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * 库文件控制器。
 */
@RestController
@RequestMapping(LIBRARY_URL)
public class LibraryController {

    @Autowired
    private LibraryApiService libraryService;

    @Autowired
    private LibraryStorageService libraryStorageService;

    /**
     * 上传共享库文件。
     * 只有主要组织的管理员可以上传。
     *
     * @param request 上传请求
     * @return 库文件视图
     */
    @PostMapping("/shared/upload")
    public Mono<ResponseView<LibraryView>> uploadSharedLibrary(@RequestBody UploadLibraryRequest request) {
        return libraryService.uploadSharedLibrary(
                        request.getFilename(),
                        request.getContent(),
                        request.getDisplayName(),
                        request.getVersion(),
                        request.getDescription()
                )
                .map(libraryMeta -> ResponseView.success(LibraryView.from(libraryMeta)));
    }

    /**
     * 上传组织私有库文件。
     * 组织管理员可以上传。
     *
     * @param request 上传请求
     * @return 库文件视图
     */
    @PostMapping("/org/upload")
    public Mono<ResponseView<LibraryView>> uploadOrgLibrary(@RequestBody UploadLibraryRequest request) {
        return libraryService.uploadOrgLibrary(
                        request.getFilename(),
                        request.getContent(),
                        request.getDisplayName(),
                        request.getVersion(),
                        request.getDescription()
                )
                .map(libraryMeta -> ResponseView.success(LibraryView.from(libraryMeta)));
    }

    /**
     * 删除共享库文件。
     * 只有主要组织的管理员可以删除。
     *
     * @param filename 文件名
     * @return 空响应
     */
    @DeleteMapping("/shared/{filename}")
    public Mono<ResponseView<Void>> deleteSharedLibrary(@PathVariable String filename) {
        return libraryService.deleteSharedLibrary(filename)
                .thenReturn(ResponseView.success(null));
    }

    /**
     * 删除组织私有库文件。
     * 组织管理员可以删除。
     *
     * @param filename 文件名
     * @return 空响应
     */
    @DeleteMapping("/org/{filename}")
    public Mono<ResponseView<Void>> deleteOrgLibrary(@PathVariable String filename) {
        return libraryService.deleteOrgLibrary(filename)
                .thenReturn(ResponseView.success(null));
    }

    /**
     * 获取共享库文件列表。
     *
     * @return 库文件列表
     */
    @GetMapping("/shared/list")
    public Mono<ResponseView<List<LibraryView>>> listSharedLibraries() {
        return libraryService.listSharedLibraries()
                .map(LibraryView::from)
                .collect(Collectors.toList())
                .map(ResponseView::success);
    }

    /**
     * 获取组织私有库文件列表。
     *
     * @return 库文件列表
     */
    @GetMapping("/org/list")
    public Mono<ResponseView<List<LibraryView>>> listOrgLibraries() {
        return libraryService.listOrgLibraries()
                .map(LibraryView::from)
                .collect(Collectors.toList())
                .map(ResponseView::success);
    }

    /**
     * 获取当前用户可用的所有库文件。
     *
     * @return 库文件列表
     */
    @GetMapping("/available")
    public Mono<ResponseView<List<LibraryView>>> listAvailableLibraries() {
        return libraryService.listAvailableLibraries()
                .map(LibraryView::from)
                .collect(Collectors.toList())
                .map(ResponseView::success);
    }

    /**
     * 下载共享库文件。
     *
     * @param filename 文件名
     * @param response HTTP 响应
     * @return 空 Mono
     */
    @GetMapping("/shared/{filename}")
    public Mono<Void> downloadSharedLibrary(@PathVariable String filename,
                                           ServerHttpResponse response) {
        return downloadLibrary(filename, LibraryType.SHARED, response);
    }

    /**
     * 下载组织私有库文件。
     *
     * @param filename 文件名
     * @param response HTTP 响应
     * @return 空 Mono
     */
    @GetMapping("/org/{filename}")
    public Mono<Void> downloadOrgLibrary(@PathVariable String filename,
                                        ServerHttpResponse response) {
        return downloadLibrary(filename, LibraryType.ORG, response);
    }

    /**
     * 通过库标识符和版本号下载共享库文件。
     *
     * @param libraryId 库标识符
     * @param version 版本号（或 "latest"）
     * @param response HTTP 响应
     * @return 空 Mono
     */
    @GetMapping("/shared/by-id/{libraryId}/{version}")
    public Mono<Void> downloadSharedLibraryByIdAndVersion(@PathVariable String libraryId,
                                                          @PathVariable String version,
                                                          ServerHttpResponse response) {
        return libraryService.findSharedLibraryByIdAndVersion(libraryId, version)
                .switchIfEmpty(Mono.error(new BizException(
                        BizError.INVALID_PARAMETER,
                        "库文件不存在: " + libraryId + " v" + version
                )))
                .flatMap(libraryMeta -> downloadLibraryMeta(libraryMeta, response));
    }

    /**
     * 通过库标识符和版本号下载组织库文件。
     *
     * @param libraryId 库标识符
     * @param version 版本号（或 "latest"）
     * @param response HTTP 响应
     * @return 空 Mono
     */
    @GetMapping("/org/by-id/{libraryId}/{version}")
    public Mono<Void> downloadOrgLibraryByIdAndVersion(@PathVariable String libraryId,
                                                       @PathVariable String version,
                                                       ServerHttpResponse response) {
        return libraryService.findOrgLibraryByIdAndVersion(libraryId, version)
                .switchIfEmpty(Mono.error(new BizException(
                        BizError.INVALID_PARAMETER,
                        "库文件不存在: " + libraryId + " v" + version
                )))
                .flatMap(libraryMeta -> downloadLibraryMeta(libraryMeta, response));
    }

    /**
     * 下载库文件。
     *
     * @param filename 文件名（存储文件名）
     * @param type 库文件类型
     * @param response HTTP 响应
     * @return 空 Mono
     */
    private Mono<Void> downloadLibrary(String filename, LibraryType type,
                                      ServerHttpResponse response) {
        return libraryService.findLibrary(filename, type)
                .switchIfEmpty(Mono.error(new BizException(
                        BizError.INVALID_PARAMETER,
                        "库文件不存在: " + filename
                )))
                .flatMap(libraryMeta -> downloadLibraryMeta(libraryMeta, response));
    }

    /**
     * 下载库文件元数据对应的文件。
     *
     * @param libraryMeta 库文件元数据
     * @param response HTTP 响应
     * @return 空 Mono
     */
    private Mono<Void> downloadLibraryMeta(LibraryMeta libraryMeta, ServerHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        // 使用原始文件名作为下载文件名
        String downloadFilename = libraryMeta.getOriginalFilename() != null
                ? libraryMeta.getOriginalFilename()
                : libraryMeta.getFilename();
        headers.setContentDisposition(
                ContentDisposition.inline().filename(downloadFilename).build()
        );
        headers.setContentType(MediaTypeUtils.parse(downloadFilename));
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(7)));

        return response.writeWith(libraryStorageService.downloadFromGridFs(libraryMeta));
    }

    /**
     * 上传库文件请求。
     */
    @Data
    public static class UploadLibraryRequest {
        /**
         * 文件名。
         */
        private String filename;

        /**
         * Base64 编码的文件内容。
         */
        private String content;

        /**
         * 显示名称。
         */
        private String displayName;

        /**
         * 版本号。
         */
        private String version;

        /**
         * 文件描述。
         */
        private String description;
    }

    /**
     * 库文件视图。
     */
    @Getter
    @Builder
    public static class LibraryView {
        /**
         * 文件 ID。
         */
        private String id;

        /**
         * 库标识符。
         */
        private String libraryId;

        /**
         * 原始文件名。
         */
        private String originalFilename;

        /**
         * 文件名（带版本号）。
         */
        private String filename;

        /**
         * 显示名称。
         */
        private String displayName;

        /**
         * 版本号。
         */
        private String version;

        /**
         * 文件大小（字节）。
         */
        private Long fileSize;

        /**
         * 文件类型。
         */
        private String type;

        /**
         * 文件描述。
         */
        private String description;

        /**
         * 创建时间。
         */
        private Long createdAt;

        /**
         * 从 LibraryMeta 创建视图。
         *
         * @param libraryMeta 库文件元数据
         * @return 库文件视图
         */
        public static LibraryView from(LibraryMeta libraryMeta) {
            return LibraryView.builder()
                    .id(libraryMeta.getId())
                    .libraryId(libraryMeta.getLibraryId())
                    .originalFilename(libraryMeta.getOriginalFilename())
                    .filename(libraryMeta.getFilename())
                    .displayName(libraryMeta.getDisplayName())
                    .version(libraryMeta.getVersion())
                    .fileSize(libraryMeta.getFileSize())
                    .type(libraryMeta.getType().name())
                    .description(libraryMeta.getDescription())
                    .createdAt(libraryMeta.getCreatedAt() != null ?
                            libraryMeta.getCreatedAt().toEpochMilli() : null)
                    .build();
        }
    }
}
