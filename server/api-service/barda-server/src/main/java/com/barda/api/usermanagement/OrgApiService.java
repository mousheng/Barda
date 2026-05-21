package com.barda.api.usermanagement;

import javax.annotation.Nullable;

import org.springframework.http.codec.multipart.Part;

import com.barda.api.authentication.dto.OrganizationDomainCheckResult;
import com.barda.api.config.ConfigView;
import com.barda.api.usermanagement.view.OrgMemberListView;
import com.barda.api.usermanagement.view.OrgView;
import com.barda.api.usermanagement.view.UpdateOrgRequest;
import com.barda.api.usermanagement.view.UpdateRoleRequest;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.model.Organization.OrganizationCommonSettings;
import com.barda.infra.annotation.NonEmptyMono;

import reactor.core.publisher.Mono;

/**
 * OrgApiService 提供了管理组织及其成员的 API。
 */
public interface OrgApiService {


    /**
     * 让当前用户离开指定的组织。
     * 该方法返回一个 {@link Mono<Boolean>} 对象，表示操作是否成功。
     * 如果操作成功，则返回 {@code true}，否则返回 {@code false}。
     *
     * @param orgId 要离开的组织的 ID，不能为空
     * @return 一个 {@link Mono<Boolean>} 对象，表示操作是否成功
     * @throws IllegalArgumentException 如果 {@code orgId} 为空
     */
    Mono<Boolean> leaveOrganization(String orgId);

    /**
     * 获取组织成员列表。
     *
     * @param orgId 组织 ID
     * @param page 页码
     * @param count 每页的成员数量
     * @return 包含成员列表的 OrgMemberListView 的 Mono 对象
     */
    @NonEmptyMono
    Mono<OrgMemberListView> getOrganizationMembers(String orgId, int page, int count);

    /**
     * 更新组织成员的角色。
     *
     * @param orgId 组织 ID
     * @param updateRoleRequest 包含用户 ID 和新角色的请求
     * @return 如果角色更新成功则返回 true 的 Mono 对象，否则返回 false
     */
    Mono<Boolean> updateRoleForMember(String orgId, UpdateRoleRequest updateRoleRequest);

    /**
     * 切换当前用户的当前组织。
     *
     * @param orgId 下一个当前组织的 ID
     * @return 如果切换成功则返回 true 的 Mono 对象，否则返回 false
     */
    Mono<Boolean> switchCurrentOrganizationTo(String orgId);

    /**
     * 删除组织的 logo。
     *
     * @param orgId 组织 ID
     * @return 如果删除成功则返回 true 的 Mono 对象，否则返回 false
     */
    Mono<Boolean> deleteLogo(String orgId);

    /**
     * 上传组织的 logo。
     *
     * @param orgId 组织 ID
     * @param fileMono 包含 logo 文件的 Mono 对象
     * @return 如果上传成功则返回 true 的 Mono 对象，否则返回 false
     */
    Mono<Boolean> uploadLogo(String orgId, Mono<Part> fileMono);

    /**
     * 从组织中移除指定用户。
     *
     * @param orgId 组织 ID
     * @param userId 用户 ID
     * @return 如果移除成功则返回 true 的 Mono 对象，否则返回 false
     */
    Mono<Boolean> removeUserFromOrg(String orgId, String userId);

    /**
     * 删除组织。
     *
     * @param orgId 组织 ID
     * @return 如果删除成功则返回 true 的 Mono 对象，否则返回 false
     */
    Mono<Boolean> removeOrg(String orgId);

    /**
     * 创建组织。
     *
     * @param organization 组织对象
     * @return 包含组织视图的 OrgView 的 Mono 对象
     */
    Mono<OrgView> create(Organization organization);

    /**
     * 更新组织信息。
     *
     * @param orgId 组织 ID
     * @param updateOrgRequest 包含更新信息的请求
     * @return 如果更新成功则返回 true 的 Mono 对象，否则返回 false
     */
    Mono<Boolean> update(String orgId, UpdateOrgRequest updateOrgRequest);

    /**
     * 检查组织域名。
     *
     * @return 包含组织域名检查结果的 OrganizationDomainCheckResult 的 Mono 对象
     */
    Mono<OrganizationDomainCheckResult> checkOrganizationDomain();

    /**
     * 获取组织的通用设置。
     *
     * @param orgId 组织 ID
     * @return 包含组织通用设置的 OrganizationCommonSettings 的 Mono 对象
     */
    Mono<OrganizationCommonSettings> getOrgCommonSettings(String orgId);

    /**
     * 更新组织的通用设置。
     *
     * @param orgId 组织 ID
     * @param key 设置的键
     * @param value 设置的值
     * @return 如果更新成功则返回 true 的 Mono 对象，否则返回 false
     */
    Mono<Boolean> updateOrgCommonSettings(String orgId, String key, Object value);

    /**
     * 尝试将用户添加到组织并切换组织。
     *
     * @param orgId 组织 ID
     * @param userId 用户 ID
     * @return 如果操作成功则返回 true 的 Mono 对象，否则返回 false
     */
    Mono<Boolean> tryAddUserToOrgAndSwitchOrg(String orgId, String userId);

    /**
     * 获取组织配置。
     *
     * @param orgId 可选的组织ID，用于SAAS模式下获取指定组织的登录配置
     * @return 包含组织配置的 ConfigView 的 Mono 对象
     */
    Mono<ConfigView> getOrganizationConfigs(@Nullable String orgId);
}

