package com.barda.domain.library.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.library.model.LibraryMeta;
import com.barda.domain.library.model.LibraryType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 库文件元数据仓库。
 */
@Repository
public interface LibraryMetaRepository extends ReactiveMongoRepository<LibraryMeta, String> {

    /**
     * 根据文件名和类型查找库文件。
     *
     * @param filename 文件名
     * @param type 库文件类型
     * @return 库文件元数据
     */
    Mono<LibraryMeta> findByFilenameAndType(String filename, LibraryType type);

    /**
     * 根据文件名、类型和组织 ID 查找库文件。
     *
     * @param filename 文件名
     * @param type 库文件类型
     * @param orgId 组织 ID
     * @return 库文件元数据
     */
    Mono<LibraryMeta> findByFilenameAndTypeAndOrgId(String filename, LibraryType type, String orgId);

    /**
     * 根据类型查找所有库文件。
     *
     * @param type 库文件类型
     * @return 库文件元数据列表
     */
    Flux<LibraryMeta> findByType(LibraryType type);

    /**
     * 根据类型和组织 ID 查找库文件。
     *
     * @param type 库文件类型
     * @param orgId 组织 ID
     * @return 库文件元数据列表
     */
    Flux<LibraryMeta> findByTypeAndOrgId(LibraryType type, String orgId);

    /**
     * 根据组织 ID 查找所有库文件。
     *
     * @param orgId 组织 ID
     * @return 库文件元数据列表
     */
    Flux<LibraryMeta> findByOrgId(String orgId);

    /**
     * 根据库标识符、版本号和类型查找库文件。
     *
     * @param libraryId 库标识符
     * @param version 版本号
     * @param type 库文件类型
     * @return 库文件元数据
     */
    Mono<LibraryMeta> findByLibraryIdAndVersionAndType(String libraryId, String version, LibraryType type);

    /**
     * 根据库标识符、版本号、类型和组织 ID 查找库文件。
     *
     * @param libraryId 库标识符
     * @param version 版本号
     * @param type 库文件类型
     * @param orgId 组织 ID
     * @return 库文件元数据
     */
    Mono<LibraryMeta> findByLibraryIdAndVersionAndTypeAndOrgId(String libraryId, String version, LibraryType type, String orgId);

    /**
     * 根据库标识符和类型查找所有版本。
     *
     * @param libraryId 库标识符
     * @param type 库文件类型
     * @return 库文件元数据列表
     */
    Flux<LibraryMeta> findByLibraryIdAndTypeOrderByCreatedAtDesc(String libraryId, LibraryType type);

    /**
     * 根据库标识符、类型和组织 ID 查找所有版本。
     *
     * @param libraryId 库标识符
     * @param type 库文件类型
     * @param orgId 组织 ID
     * @return 库文件元数据列表
     */
    Flux<LibraryMeta> findByLibraryIdAndTypeAndOrgIdOrderByCreatedAtDesc(String libraryId, LibraryType type, String orgId);
}
