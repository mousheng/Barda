package com.barda.domain.material.service.meta;

import com.barda.domain.material.model.MaterialMeta;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 提供关于 MaterialMeta 实体的服务接口。
 * 该接口定义了对 MaterialMeta 实体的基本 CRUD 操作。
 */
public interface MaterialMetaService {

    /**
     * 创建一个新的 MaterialMeta 实体。
     *
     * @param resource 要创建的 MaterialMeta 实体
     * @return 包含创建的 MaterialMeta 实体的 Mono
     */
    Mono<MaterialMeta> create(MaterialMeta resource);

    /**
     * 根据 ID 查找 MaterialMeta 实体。
     *
     * @param id 要查找的 MaterialMeta 实体的 ID
     * @return 包含找到的 MaterialMeta 实体的 Mono，如果没有找到则为空
     */
    Mono<MaterialMeta> findById(String id);

    /**
     * 检查在特定组织中是否存在具有给定文件名的 MaterialMeta 实体。
     *
     * @param orgId    组织的 ID
     * @param filename 文件名
     * @return 包含检查结果的 Mono，若存在则为 true，否则为 false
     */
    Mono<Boolean> existsByOrgIdAndFilename(String orgId, String filename);

    /**
     * 计算特定组织中所有 MaterialMeta 实体的总大小。
     *
     * @param orgId 组织的 ID
     * @return 包含总大小的 Mono，以字节为单位
     */
    Mono<Long> totalSize(String orgId);

    /**
     * 获取特定组织中的所有 MaterialMeta 实体。
     *
     * @param orgId 组织的 ID
     * @return 包含所有找到的 MaterialMeta 实体的 Flux
     */
    Flux<MaterialMeta> getByOrgId(String orgId);

    /**
     * 根据 ID 删除 MaterialMeta 实体。
     *
     * @param id 要删除的 MaterialMeta 实体的 ID
     * @return 表示删除操作完成的 Mono
     */
    Mono<Void> deleteById(String id);
}
