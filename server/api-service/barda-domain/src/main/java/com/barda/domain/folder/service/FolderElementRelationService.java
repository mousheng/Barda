package com.barda.domain.folder.service;

import static com.barda.infra.birelation.BiRelationBizType.FOLDER_ELEMENT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.folder.model.FolderElement;
import com.barda.infra.birelation.BiRelationBizType;
import com.barda.infra.birelation.BiRelationService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 管理文件夹和元素之间的关系的服务类。
 * 该类提供创建、删除和查询文件夹和元素之间的关系的功能。
 */
@Service
public class FolderElementRelationService {

    /**
     * 双向关系服务。
     */
    @Autowired
    private BiRelationService biRelationService;

    /**
     * 根据文件夹 ID 删除文件夹和元素之间的关系。
     *
     * @param folderIds 文件夹 ID 列表
     * @return 一个 Mono，表示是否删除成功
     */
    public Mono<Boolean> deleteByFolderIds(List<String> folderIds) {
        return biRelationService.removeAllBiRelations(FOLDER_ELEMENT, folderIds);
    }

    /**
     * 根据元素 ID 删除文件夹和元素之间的关系。
     *
     * @param elementId 元素 ID
     * @return 一个 Mono，表示是否删除成功
     */
    public Mono<Boolean> deleteByElementId(String elementId) {
        return biRelationService.removeAllBiRelationsByTargetId(FOLDER_ELEMENT, elementId);
    }

    /**
     * 创建文件夹和元素之间的关系。
     *
     * @param folderId 文件夹 ID
     * @param elementId 元素 ID
     * @return 一个 Mono，表示是否创建成功
     */
    public Mono<Void> create(String folderId, String elementId) {
        return biRelationService.addBiRelation(BiRelationBizType.FOLDER_ELEMENT, folderId, elementId, null, null)
                .then();
    }

    /**
     * 根据元素 ID 获取文件夹和元素之间的关系。
     *
     * @param elementIds 元素 ID 列表
     * @return 一个 Flux，表示文件夹和元素之间的关系
     */
    public Flux<FolderElement> getByElementIds(List<String> elementIds) {
        return biRelationService.getByTargetIds(BiRelationBizType.FOLDER_ELEMENT, elementIds)
                .map(biRelation -> new FolderElement(biRelation.getSourceId(), biRelation.getTargetId()));
    }
}
