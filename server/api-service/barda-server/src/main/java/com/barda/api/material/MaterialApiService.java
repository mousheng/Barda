package com.barda.api.material;

import java.util.List;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;

import com.barda.api.material.MaterialController.MaterialView;
import com.barda.domain.material.model.MaterialMeta;
import com.barda.domain.material.model.MaterialType;

import reactor.core.publisher.Mono;

/**
 * 物料API服务接口，用于处理与物料相关的操作。
 */
public interface MaterialApiService {

    /**
     * 上传物料。
     *
     * @param filename 物料文件名
     * @param content  物料内容的base64编码
     * @param type     物料类型
     * @return          包含物料元数据的Mono
     */
    Mono<MaterialMeta> upload(String filename, String content, MaterialType type);

    /**
     * 下载物料。
     *
     * @param materialMeta 物料元数据
     * @return              包含物料数据的Publisher
     */
    Publisher<? extends DataBuffer> download(MaterialMeta materialMeta);

    /**
     * 获取物料列表。
     *
     * @return 包含物料视图列表的Mono
     */
    Mono<List<MaterialView>> list();

    /**
     * 删除指定ID的物料。
     *
     * @param id 物料ID
     * @return   空的Mono，表示操作完成
     */
    Mono<Void> delete(String id);
}
