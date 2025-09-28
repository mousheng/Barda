package com.barda.domain.material.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.material.model.MaterialMeta;
import com.barda.domain.material.model.MaterialType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * MaterialMeta 的存储库接口，用于执行数据库操作。
 * 该接口继承自 ReactiveMongoRepository，并添加了一些自定义查询方法。
 */
@Repository
public interface MaterialMateRepository extends ReactiveMongoRepository<MaterialMeta, String> {

    /**
     * 根据组织ID查找 MaterialMeta 列表。
     *
     * @param orgId 组织ID
     * @return 包含 MaterialMeta 对象的 Flux
     */
    Flux<MaterialMeta> findByOrgId(String orgId);

    /**
     * 根据组织ID和 Material 类型查找 MaterialMeta 列表。
     *
     * @param orgId 组织ID
     * @param type  Material 类型
     * @return 包含 MaterialMeta 对象的 Flux
     */
    Flux<MaterialMeta> findByOrgIdAndType(String orgId, MaterialType type);

    /**
     * 根据组织ID、文件名和 Material 类型查找 MaterialMeta 列表。
     *
     * @param orgId    组织ID
     * @param filename 文件名
     * @param type     Material 类型
     * @return 包含 MaterialMeta 对象的 Flux
     */
    Flux<MaterialMeta> findByOrgIdAndFilenameAndType(String orgId, String filename, MaterialType type);

    /**
     * 检查特定组织ID和文件名的 MaterialMeta 是否存在。
     *
     * @param orgId    组织ID
     * @param filename 文件名
     * @return 包含布尔值的 Mono，若存在则为 true，否则为 false
     */
    Mono<Boolean> existsByOrgIdAndFilename(String orgId, String filename);
}
