package com.barda.api.home;

import javax.annotation.Nullable;

import com.barda.api.application.view.ApplicationInfoView;
import com.barda.api.usermanagement.view.UserProfileView;
import com.barda.domain.application.model.ApplicationStatus;
import com.barda.domain.application.model.ApplicationType;
import com.barda.domain.user.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.server.ServerWebExchange;

/**
 * 用户主页API服务接口。
 *
 */
public interface UserHomeApiService {

    /**
     * 构建用户个人资料视图。
     *
     * @param user 用户信息。
     * @param exchange ServerWebExchange。
     *
     * @return 用户个人资料视图的Mono。
     */
    Mono<UserProfileView> buildUserProfileView(User user, ServerWebExchange exchange);

    /**
     * 标记新用户引导已显示。
     *
     * @param userId 用户ID。
     *
     * @return true表示标记成功，false表示标记失败。
     */
    Mono<Boolean> markNewUserGuidanceShown(String userId);

    /**
     * 获取用户主页视图。
     *
     * @param applicationType 应用类型。
     *
     * @return 用户主页视图的Mono。
     */
    Mono<UserHomepageView> getUserHomePageView(ApplicationType applicationType);

    /**
     * 获取当前组织成员的授权应用列表。
     *
     * @param applicationType 应用类型。
     * @param applicationStatus 应用状态。
     * @param withContainerSize 是否包含容器大小。
     *
     * @return 应用信息视图的Flux。
     */
    Flux<ApplicationInfoView> getAllAuthorisedApplications4CurrentOrgMember(@Nullable ApplicationType applicationType,
            @Nullable ApplicationStatus applicationStatus, boolean withContainerSize);
}
