package com.barda.domain.organization.service;

import java.util.Collection;

import org.springframework.http.codec.multipart.Part;

import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.model.Organization.OrganizationCommonSettings;
import com.barda.domain.user.model.User;
import com.barda.infra.annotation.NonEmptyMono;
import com.barda.infra.annotation.PossibleEmptyMono;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 组织服务接口。
 * 该接口定义了对 Organization 实体执行 CRUD 操作和其他相关功能的功能。
 */
public interface OrganizationService {

    /**
     * 获取处于企业模式下的组织。
     *
     * @return 匹配的组织（如果存在）
     */
    @PossibleEmptyMono
    Mono<Organization> getOrganizationInEnterpriseMode();

    /**
     * 创建一个新的组织。
     *
     * @param organization 要创建的组织
     * @param creatorUserId 创建者的用户 ID
     * @return 创建的组织
     */
    Mono<Organization> create(Organization organization, String creatorUserId);

    /**
     * 创建一个新的默认组织。
     *
     * @param user 创建者的用户信息
     * @return 创建的组织
     */
    Mono<Organization> createDefault(User user);

    /**
     * 根据 ID 获取组织。
     *
     * @param id 要查找的组织 ID
     * @return 匹配的组织（如果存在）
     */
    Mono<Organization> getById(String id);

    /**
     * 根据 ID 列表获取组织列表。
     *
     * @param ids 要查找的组织 ID 列表
     * @return 匹配的组织列表
     */
    @NonEmptyMono
    Flux<Organization> getByIds(Collection<String> ids);

    /**
     * 获取组织的通用设置。
     *
     * @param orgId 要查找的组织 ID
     * @return 匹配的组织的通用设置
     */
    Mono<OrganizationCommonSettings> getOrgCommonSettings(String orgId);

    /**
     * 上传组织的 logo。
     *
     * @param organizationId 要上传 logo 的组织 ID
     * @param filePart 上传的 logo 文件
     * @return true - 上传成功，false - 上传失败
     */
    Mono<Boolean> uploadLogo(String organizationId, Part filePart);

    /**
     * 删除组织的 logo。
     *
     * @param organizationId 要删除 logo 的组织 ID
     * @return true - 删除成功，false - 删除失败
     */
    Mono<Boolean> deleteLogo(String organizationId);

    /**
     * 更新组织的相关信息。
     *
     * @param orgId 要更新的组织 ID
     * @param updateOrg 要更新的组织信息
     * @return true - 更新成功，false - 更新失败
     */
    Mono<Boolean> update(String orgId, Organization updateOrg);

    /**
     * 删除组织。
     *
     * @param orgId 要删除的组织 ID
     * @return true - 删除成功，false - 删除失败
     */
    Mono<Boolean> delete(String orgId);

    /**
     * 根据来源和第三方公司 ID 获取组织。
     *
     * @param source 来源
     * @param companyId 第三方公司 ID
     * @return 匹配的组织（如果存在）
     */
    Mono<Organization> getBySourceAndTpCompanyId(String source, String companyId);

    /**
     * 获取当前域下的组织。
     *
     * @return 匹配的组织（如果存在）
     */
    @PossibleEmptyMono
    Mono<Organization> getByDomain();

    /**
     * 获取主组织（SAAS模式下作为默认登录配置来源）。
     *
     * @return 主组织（如果存在）
     */
    @PossibleEmptyMono
    Mono<Organization> getPrimaryOrganization();

    /**
     * 更新组织的通用设置。
     *
     * @param orgId 要更新的组织 ID
     * @param key 要更新的设置的键
     * @param value 要更新的设置的值
     * @return true - 更新成功，false - 更新失败
     */
    Mono<Boolean> updateCommonSettings(String orgId, String key, Object value);
}
