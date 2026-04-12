package com.barda.api.organization;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.barda.api.home.SessionUserService;
import com.barda.api.usermanagement.OrgDevChecker;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.repository.OrganizationRepository;
import com.barda.sdk.config.LibraryConfig;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 主要组织服务。
 * 负责管理主要组织的识别和设置。
 */
@Slf4j
@Service
public class PrimaryOrgApiService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Autowired
    private LibraryConfig libraryConfig;

    @Autowired
    private SessionUserService sessionUserService;

    @Autowired
    private OrgDevChecker orgDevChecker;

    /**
     * 获取主要组织 ID。
     * 优先级：数据库标记 > 配置文件 > 自动识别第一个组织
     *
     * @return 主要组织 ID
     */
    public Mono<String> getPrimaryOrgId() {
        // 第一层：查找数据库中标记的主要组织
        return mongoTemplate.findOne(
                        Query.query(Criteria.where("isPrimaryOrganization").is(true)),
                        Organization.class
                )
                .map(Organization::getId)
                .switchIfEmpty(Mono.defer(() -> {
                    // 第二层：读取配置文件
                    String configOrgId = libraryConfig.getPrimaryOrgId();
                    if (StringUtils.isNotBlank(configOrgId)) {
                        log.debug("使用配置文件中的主要组织 ID: {}", configOrgId);
                        return Mono.just(configOrgId);
                    }
                    // 第三层：自动选择创建时间最早的组织
                    log.debug("自动选择创建时间最早的组织作为主要组织");
                    return organizationRepository
                            .findAll()
                            .sort(Comparator.comparing(Organization::getCreatedAt))
                            .take(1)
                            .map(Organization::getId)
                            .next()
                            .switchIfEmpty(Mono.error(new BizException(
                                    BizError.INVALID_PARAMETER,
                                    "系统中没有组织"
                            )));
                }));
    }

    /**
     * 获取主要组织。
     *
     * @return 主要组织
     */
    public Mono<Organization> getPrimaryOrg() {
        return getPrimaryOrgId()
                .flatMap(organizationRepository::findById)
                .switchIfEmpty(Mono.error(new BizException(
                        BizError.INVALID_PARAMETER,
                        "主要组织不存在"
                )));
    }

    /**
     * 检查当前用户是否是主要组织的管理员。
     *
     * @return true - 是主要组织管理员，false - 不是
     */
    public Mono<Boolean> isCurrentUserPrimaryOrgAdmin() {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> {
                    if (!orgMember.isAdmin()) {
                        return Mono.just(false);
                    }
                    return getPrimaryOrgId()
                            .map(primaryOrgId -> primaryOrgId.equals(orgMember.getOrgId()));
                })
                .defaultIfEmpty(false);
    }

    /**
     * 检查当前用户是否是主要组织的管理员（抛出异常）。
     *
     * @return 空 Mono
     */
    public Mono<Void> checkCurrentUserPrimaryOrgAdmin() {
        return isCurrentUserPrimaryOrgAdmin()
                .flatMap(isPrimary -> {
                    if (isPrimary) {
                        return Mono.empty();
                    }
                    return Mono.error(new BizException(
                            BizError.NOT_AUTHORIZED,
                            "只有主要组织的管理员可以执行此操作"
                    ));
                });
    }

    /**
     * 设置主要组织。
     * 只有当前主要组织的管理员可以执行此操作。
     *
     * @param newPrimaryOrgId 新的主要组织 ID
     * @return 空 Mono
     */
    public Mono<Void> setPrimaryOrganization(String newPrimaryOrgId) {
        return checkCurrentUserPrimaryOrgAdmin()
                .then(organizationRepository.findById(newPrimaryOrgId))
                .switchIfEmpty(Mono.error(new BizException(
                        BizError.INVALID_PARAMETER,
                        "组织不存在"
                )))
                .flatMap(newOrg -> {
                    // 取消当前主要组织的标记
                    return mongoTemplate.updateMulti(
                                    Query.query(Criteria.where("isPrimaryOrganization").is(true)),
                                    Update.update("isPrimaryOrganization", false),
                                    Organization.class
                            )
                            .then(mongoTemplate.updateFirst(
                                    Query.query(Criteria.where("_id").is(newPrimaryOrgId)),
                                    Update.update("isPrimaryOrganization", true),
                                    Organization.class
                            ))
                            .doOnSuccess(result -> log.info(
                                    "主要组织已更改为: {} ({})",
                                    newOrg.getName(),
                                    newPrimaryOrgId
                            ))
                            .then();
                });
    }
}
