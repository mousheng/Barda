package com.barda.domain.organization.repository;

import java.util.Collection;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.model.OrganizationState;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 组织仓库接口。
 * 该接口定义了对 Organization 实体执行 CRUD 操作的功能。
 * 它继承了 ReactiveMongoRepository 接口，用于提供对 MongoDB 的反应式访问。
 */
@Repository
public interface OrganizationRepository extends ReactiveMongoRepository<Organization, String> {

    /**
     * 根据状态查找第一个组织。
     *
     * @param state 要查找的状态
     * @return 匹配的组织（如果存在）
     */
    Mono<Organization> findFirstByStateMatches(OrganizationState state);

    /**
     * 根据 ID 列表和状态查找组织。
     *
     * @param id ID 列表
     * @param state 要查找的状态
     * @return 匹配的组织列表
     */
    Flux<Organization> findByIdInAndState(Collection<String> id, OrganizationState state);

    /**
     * 根据 ID 和状态查找组织。
     *
     * @param id 要查找的 ID
     * @param state 要查找的状态
     * @return 匹配的组织（如果存在）
     */
    Mono<Organization> findByIdAndState(String id, OrganizationState state);

    /**
     * 根据来源、第三方公司 ID 和状态查找组织。
     *
     * @param source 来源
     * @param tpCompanyId 第三方公司 ID
     * @param state 要查找的状态
     * @return 匹配的组织（如果存在）
     */
    Mono<Organization> findBySourceAndThirdPartyCompanyIdAndState(String source, String tpCompanyId, OrganizationState state);

    /**
     * 根据域和状态查找组织。
     *
     * @param domain 要查找的域
     * @param state 要查找的状态
     * @return 匹配的组织（如果存在）
     */
    Mono<Organization> findByOrganizationDomain_DomainAndState(String domain, OrganizationState state);

    /**
     * 查找所有存在域的组织。
     *
     * @return 存在域的组织列表
     */
    Flux<Organization> findByOrganizationDomainIsNotNull();

    /**
     * 根据是否为主组织和状态查找组织。
     *
     * @param isPrimary 是否为主组织
     * @param state 要查找的状态
     * @return 匹配的组织（如果存在）
     */
    Mono<Organization> findByIsPrimaryOrganizationAndState(Boolean isPrimary, OrganizationState state);

    /**
     * 根据状态查找创建时间最早的组织。
     *
     * @param state 要查找的状态
     * @return 创建时间最早的组织（如果存在）
     */
    Mono<Organization> findFirstByStateOrderByCreatedAtAsc(OrganizationState state);
}
