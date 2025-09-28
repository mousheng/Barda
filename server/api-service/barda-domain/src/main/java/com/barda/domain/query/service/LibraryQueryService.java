package com.barda.domain.query.service;

import static com.barda.sdk.exception.BizError.LIBRARY_QUERY_NOT_FOUND;
import static com.barda.sdk.util.ExceptionUtils.deferredError;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.query.model.BaseQuery;
import com.barda.domain.query.model.LibraryQuery;
import com.barda.domain.query.model.LibraryQueryRecord;
import com.barda.domain.query.repository.LibraryQueryRepository;
import com.barda.infra.mongo.MongoUpsertHelper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 库查询的服务类。
 * 它提供对库查询的CRUD操作。
 */
@Service
public class LibraryQueryService {

    @Autowired
    private LibraryQueryRepository libraryQueryRepository;

    @Autowired
    private LibraryQueryRecordService libraryQueryRecordService;

    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    /**
     * 获取指定ID的库查询。
     *
     * @param libraryQueryId 库查询ID
     * @return 符合库查询ID的库查询
     */
    public Mono<LibraryQuery> getById(String libraryQueryId) {
        return libraryQueryRepository.findById(libraryQueryId)
                .switchIfEmpty(deferredError(LIBRARY_QUERY_NOT_FOUND, "LIBRARY_QUERY_NOT_FOUND"));
    }

    /**
     * 获取指定名称的库查询。
     *
     * @param libraryQueryName 库查询名称
     * @return 符合库查询名称的库查询
     */
    public Mono<LibraryQuery> getByName(String libraryQueryName) {
        return libraryQueryRepository.findByName(libraryQueryName)
                .switchIfEmpty(deferredError(LIBRARY_QUERY_NOT_FOUND, "LIBRARY_QUERY_NOT_FOUND"));
    }

    /**
     * 获取指定组织ID的所有库查询。
     *
     * @param organizationId 组织ID
     * @return 符合组织ID的库查询的Flux流
     */
    public Flux<LibraryQuery> getByOrganizationId(String organizationId) {
        return libraryQueryRepository.findByOrganizationId(organizationId);
    }

    /**
     * 插入一个库查询。
     *
     * @param libraryQuery 库查询
     * @return 插入的库查询
     */
    public Mono<LibraryQuery> insert(LibraryQuery libraryQuery) {
        return libraryQueryRepository.save(libraryQuery);
    }

    /**
     * 更新指定ID的库查询。
     *
     * @param libraryQueryId 库查询ID
     * @param libraryQuery 库查询
     * @return 更新是否成功
     */
    public Mono<Boolean> update(String libraryQueryId, LibraryQuery libraryQuery) {
        return mongoUpsertHelper.updateById(libraryQuery, libraryQueryId);
    }

    /**
     * 删除指定ID的库查询。
     *
     * @param libraryQueryId 库查询ID
     * @return 空的Mono
     */
    public Mono<Void> delete(String libraryQueryId) {
        return libraryQueryRepository.deleteById(libraryQueryId);
    }

    /**
     * 获取指定库查询ID的编辑中的基础查询。
     *
     * @param libraryQueryId 库查询ID
     * @return 符合库查询ID的编辑中的基础查询
     */
    public Mono<BaseQuery> getEditingBaseQueryByLibraryQueryId(String libraryQueryId) {
        return getById(libraryQueryId).map(LibraryQuery::getQuery);
    }

    /**
     * 获取指定库查询ID的实时基础查询。
     *
     * @param libraryQueryId 库查询ID
     * @return 符合库查询ID的实时基础查询
     */
    public Mono<BaseQuery> getLiveBaseQueryByLibraryQueryId(String libraryQueryId) {
        return libraryQueryRecordService.getLatestRecordByLibraryQueryId(libraryQueryId)
                .map(LibraryQueryRecord::getQuery)
                .switchIfEmpty(getById(libraryQueryId)
                        .map(LibraryQuery::getQuery));
    }

    /**
     * 获取指定库查询ID的实时DSL。
     *
     * @param libraryQueryId 库查询ID
     * @return 符合库查询ID的实时DSL的Map
     */
    public Mono<Map<String, Object>> getLiveDSLByLibraryQueryId(String libraryQueryId) {
        return libraryQueryRecordService.getLatestRecordByLibraryQueryId(libraryQueryId)
                .map(LibraryQueryRecord::getLibraryQueryDSL)
                .switchIfEmpty(getById(libraryQueryId)
                        .map(LibraryQuery::getLibraryQueryDSL));
    }
}
