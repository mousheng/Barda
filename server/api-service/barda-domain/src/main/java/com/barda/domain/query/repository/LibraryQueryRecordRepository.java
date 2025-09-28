package com.barda.domain.query.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.query.model.LibraryQueryRecord;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 库查询记录的MongoDB Repository接口。
 * 它扩展了ReactiveMongoRepository<LibraryQueryRecord, String>，提供对库查询记录的CRUD操作。
 */
@Repository
public interface LibraryQueryRecordRepository extends ReactiveMongoRepository<LibraryQueryRecord, String> {

    /**
     * 根据库查询ID删除库查询记录。
     *
     * @param libraryQueryId 库查询ID
     * @return 被删除的库查询记录的数量
     */
    Mono<Long> deleteByLibraryQueryId(String libraryQueryId);

    /**
     * 根据库查询ID查询库查询记录。
     *
     * @param libraryQueryId 库查询ID
     * @return 符合库查询ID的库查询记录的Flux流
     */
    Flux<LibraryQueryRecord> findByLibraryQueryId(String libraryQueryId);

    /**
     * 根据库查询ID列表查询库查询记录。
     *
     * @param ids 库查询ID列表
     * @return 符合库查询ID列表的库查询记录的Flux流
     */
    Flux<LibraryQueryRecord> findByLibraryQueryIdIn(List<String> ids);

    /**
     * 查询指定库查询ID的最新库查询记录。
     *
     * @param libraryQueryId 库查询ID
     * @return 符合库查询ID的最新库查询记录
     */
    Mono<LibraryQueryRecord> findTop1ByLibraryQueryIdOrderByCreatedAtDesc(String libraryQueryId);
}