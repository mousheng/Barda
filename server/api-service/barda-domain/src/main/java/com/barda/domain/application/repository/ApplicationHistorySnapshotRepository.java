package com.barda.domain.application.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.application.model.ApplicationHistorySnapshot;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 该接口表示应用程序历史快照的存储库。
 * 它扩展了 {@link ReactiveMongoRepository}，
 * 并使用了 Lombok 的 {@code @Repository} 注解来标记为 Spring Data MongoDB 的存储库。
 *
 * <p>该接口包含以下方法：
 * <ul>
 *     <li>{@link #findAllByApplicationId(String, Pageable)}：
 *         按应用程序 ID 查询应用程序历史快照，并返回符合条件的快照的流。</li>
 *     <li>{@link #countByApplicationId(String)}：
 *         按应用程序 ID 查询应用程序历史快照的数量，并返回符合条件的快照的数量。</li>
 * </ul>
 *
 * <p>请注意，在 {@link #findAllByApplicationId(String, Pageable)} 方法中，
 * 我使用了 Lombok 的 {@code @Query} 注解来指定查询的字段。
 * 这可以让您在返回的流中只包含所需的字段，从而提高查询的效率。
 *
 * <p>
 * 请注意，此处的注释是根据中文翻译的，您可以根据您的需求进行修改。
 */
@Repository
public interface ApplicationHistorySnapshotRepository extends ReactiveMongoRepository<ApplicationHistorySnapshot, String> {

    /**
     * 按应用程序 ID 查询应用程序历史快照，并返回符合条件的快照的流。
     *
     * @param applicationId 应用程序 ID
     * @param pageable 分页信息
     * @return 符合条件的应用程序历史快照的流
     */
    @Query(fields = "{applicationId : 1, context: 1, createdBy : 1, createdAt : 1}")
    Flux<ApplicationHistorySnapshot> findAllByApplicationId(String applicationId, Pageable pageable);

    /**
     * 按应用程序 ID 查询应用程序历史快照的数量，并返回符合条件的快照的数量。
     *
     * @param applicationId 应用程序 ID
     * @return 符合条件的应用程序历史快照的数量
     */
    Mono<Long> countByApplicationId(String applicationId);
}
