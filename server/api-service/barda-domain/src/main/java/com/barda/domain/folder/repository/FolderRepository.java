package com.barda.domain.folder.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.folder.model.Folder;

import reactor.core.publisher.Flux;

/**
 * 该接口表示用于管理 {@link Folder} 实体的 MongoDB 数据库存储库。
 * 它扩展了 Spring Data MongoDB 提供的 {@link ReactiveMongoRepository} 接口，
 * 允许进行反应式 CRUD 操作和自定义查询。
 */
@Repository
public interface FolderRepository extends ReactiveMongoRepository<Folder, String> {

    /**
     * 通过其 ID 查找与特定组织关联的所有文件夹。
     *
     * @param organizationId 组织的 ID
     * @return 与给定组织 ID 关联的 {@link Folder} 实体的 {@link Flux}
     */
    Flux<Folder> findByOrganizationId(String organizationId);
}
