package com.barda.api.material;

import static com.barda.infra.constant.NewUrl.MATERIAL_URL;

import java.time.Duration;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barda.api.framework.view.ResponseView;
import com.barda.domain.material.model.MaterialType;
import com.barda.domain.material.service.meta.MaterialMetaService;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.util.MediaTypeUtils;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * 素材控制器。
 *
 */
@RestController
@RequestMapping(MATERIAL_URL)
public class MaterialController {

    private static final String DOWNLOAD_TYPE = "download";
    private static final String PREVIEW_TYPE = "preview";

    /**
     * 素材API服务。
     */
    @Autowired
    private MaterialApiService materialApiService;

    /**
     * 素材元数据服务。
     */
    @Autowired
    private MaterialMetaService materialMetaService;

    /**
     * 上传素材。
     *
     * @param uploadMaterialRequestDTO 上传素材的请求数据
     * @return 上传成功的素材视图
     */
    @PostMapping
    public Mono<ResponseView<MaterialView>> upload(@RequestBody UploadMaterialRequestDTO uploadMaterialRequestDTO) {
        return materialApiService.upload(uploadMaterialRequestDTO.getFilename(), uploadMaterialRequestDTO.getContent(),
                        uploadMaterialRequestDTO.getType())
                .map(materialMeta -> {
                    MaterialView view = MaterialView.builder()
                            .id(materialMeta.getId())
                            .filename(materialMeta.getFilename())
                            .build();
                    return ResponseView.success(view);
                });
    }

    /**
     * 下载素材。
     *
     * @param id 素材的ID
     * @param type 下载类型，可以是"download"或"preview"
     * @param serverHttpResponse 服务器的HTTP响应
     * @return 空的Mono
     */
    @GetMapping("/{id}")
    public Mono<Void> download(@PathVariable String id,
            @RequestParam(value = "type", defaultValue = DOWNLOAD_TYPE) String type,
            ServerHttpResponse serverHttpResponse) {
        return materialMetaService.findById(id)
                .switchIfEmpty(Mono.error(new BizException(BizError.INVALID_PARAMETER, "FILE_NOT_EXIST")))
                .doOnNext(materialMeta -> {
                    HttpHeaders headers = serverHttpResponse.getHeaders();
                    if (PREVIEW_TYPE.equals(type)) {
                        headers.setContentDisposition(ContentDisposition.inline().filename(materialMeta.getFilename()).build());
                    } else {
                        headers.setContentDisposition(ContentDisposition.attachment().filename(materialMeta.getFilename()).build());
                    }
                    headers.setContentType(MediaTypeUtils.parse(materialMeta.getFilename()));
                    headers.setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
                })
                .flatMap(materialMeta -> serverHttpResponse.writeWith(materialApiService.download(materialMeta)))
                .then();
    }

    /**
     * 获取素材列表。
     *
     * @return 素材视图列表
     */
    @GetMapping("/list")
    public Mono<ResponseView<List<MaterialView>>> getFileList() {
        return materialApiService.list()
                .map(ResponseView::success);
    }

    /**
     * 删除素材。
     *
     * @param id 素材的ID
     * @return 删除成功的结果
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseView<Boolean>> delete(@PathVariable String id) {
        return materialApiService.delete(id)
                .thenReturn(ResponseView.success(true));
    }

    /**
     * 素材视图。
     */
    @Getter
    @Builder
    public static class MaterialView {
        private String id;
        private String filename;
    }

    /**
     * 上传素材的请求数据。
     */
    @Data
    public static class UploadMaterialRequestDTO {

        private String filename;
        private String content;// in base64
        private MaterialType type;
    }
}
