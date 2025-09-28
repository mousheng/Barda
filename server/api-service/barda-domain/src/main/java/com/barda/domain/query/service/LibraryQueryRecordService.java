package com.barda.domain.query.service;

import static com.barda.sdk.exception.BizError.LIBRARY_QUERY_NOT_FOUND;
import static com.barda.sdk.util.ExceptionUtils.deferredError;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.query.model.LibraryQueryRecord;
import com.barda.domain.query.repository.LibraryQueryRecordRepository;

import reactor.core.publisher.Mono;

/**
 * 库查询记录的服务类。
 * 它提供对库查询记录的CRUD操作。
 */
@Service
public class LibraryQueryRecordService {

    @Autowired
    private LibraryQueryRecordRepository libraryQueryRecordRepository;

    /**
     * 插入一个库查询记录。
     *
     * @param libraryQueryRecord 库查询记录
     * @return 插入的库查询记录
     */
    public Mono<LibraryQueryRecord> insert(LibraryQueryRecord libraryQueryRecord) {
        return libraryQueryRecordRepository.save(libraryQueryRecord);
    }

    /**
     * 获取指定库查询ID的所有已发布版本。
     *
     * @param libraryQueryId 库查询ID
     * @return 符合库查询ID的已发布版本的库查询记录列表
     */
    public Mono<List<LibraryQueryRecord>> getByLibraryQueryId(String libraryQueryId) {
        return libraryQueryRecordRepository.findByLibraryQueryId(libraryQueryId)
                .sort(Comparator.comparing(LibraryQueryRecord::getCreatedAt).reversed())
                .collectList();
    }

    /**
     * 获取指定库查询ID列表的所有已发布版本。
     *
     * @param libraryQueryIdList 库查询ID列表
     * @return 符合库查询ID列表的已发布版本的库查询记录列表的Map
     */
    public Mono<Map<String, List<LibraryQueryRecord>>> getByLibraryQueryIdIn(List<String> libraryQueryIdList) {
        return libraryQueryRecordRepository.findByLibraryQueryIdIn(libraryQueryIdList)
                .sort(Comparator.comparing(LibraryQueryRecord::getCreatedAt).reversed())
                .collectList()
                .map(libraryQueryRecords -> libraryQueryRecords.stream()
                        .collect(Collectors.groupingBy(LibraryQueryRecord::getLibraryQueryId)));
    }

    /**
     * 获取指定ID的库查询记录。
     *
     * @param id 库查询记录ID
     * @return 符合库查询记录ID的库查询记录
     */
    public Mono<LibraryQueryRecord> getById(String id) {
        return libraryQueryRecordRepository.findById(id)
                .switchIfEmpty(deferredError(LIBRARY_QUERY_NOT_FOUND, "LIBRARY_QUERY_NOT_FOUND"));
    }

    /**
     * 获取指定库查询ID的最新已发布版本。
     *
     * @param libraryQueryId 库查询ID
     * @return 符合库查询ID的最新已发布版本的库查询记录
     */
    public Mono<LibraryQueryRecord> getLatestRecordByLibraryQueryId(String libraryQueryId) {
        return libraryQueryRecordRepository.findTop1ByLibraryQueryIdOrderByCreatedAtDesc(libraryQueryId);
    }

    /**
     * 删除指定库查询ID的所有库查询记录。
     *
     * @param libraryQueryId 库查询ID
     * @return 删除的库查询记录的数量
     */
    public Mono<Long> deleteAllLibraryQueryTagByLibraryQueryId(String libraryQueryId) {
        return libraryQueryRecordRepository.deleteByLibraryQueryId(libraryQueryId);
    }

    /**
     * 删除指定ID的库查询记录。
     *
     * @param id 库查询记录ID
     * @return 空的Mono
     */
    public Mono<Void> deleteById(String id) {
        return libraryQueryRecordRepository.deleteById(id);
    }
}
