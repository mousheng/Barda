package com.barda.api.usermanagement;


import static com.barda.sdk.constants.Authentication.isAnonymousUser;
import static com.barda.sdk.exception.BizError.INVITED_USER_NOT_LOGIN;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barda.api.framework.view.ResponseView;
import com.barda.api.home.SessionUserService;
import com.barda.api.usermanagement.view.InvitationVO;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 邀请控制器类。
 * 该类使用 Spring 注解来声明为 REST 控制器并将其映射到特定的 URL 上。
 */
@RestController
@RequestMapping(value = {Url.INVITATION_URL, NewUrl.INVITATION_URL})
@Slf4j
public class InvitationController {

    @Autowired
    private InvitationApiService invitationApiService;

    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 创建邀请。
     *
     * @param orgId 被邀请的组织 ID
     * @return 创建的邀请的视图
     */
    @PostMapping
    public Mono<ResponseView<InvitationVO>> create(@RequestParam String orgId) {
        return invitationApiService.create(orgId)
                .map(ResponseView::success);
    }

    /**
     * 获取邀请。
     *
     * @param invitationId 邀请 ID
     * @return 邀请的视图
     */
    @GetMapping("/{invitationId}")
    public Mono<ResponseView<InvitationVO>> get(@PathVariable String invitationId) {
        return invitationApiService.getInvitationView(invitationId)
                .map(ResponseView::success);
    }

    /**
     * 邀请用户加入组织。
     *
     * @param invitationId 邀请 ID
     * @return 邀请结果
     */
    @GetMapping("/{invitationId}/invite")
    public Mono<ResponseView<?>> inviteUser(@PathVariable String invitationId) {
        return sessionUserService.getVisitorId()
                .flatMap(visitorId -> {
                            if (isAnonymousUser(visitorId)) {
                                return invitationApiService.getInvitationView(invitationId)
                                        .map(invitationVO -> ResponseView.error(INVITED_USER_NOT_LOGIN.getBizErrorCode(), "", invitationVO));
                            }
                            return invitationApiService.inviteUser(invitationId)
                                    .map(ResponseView::success);
                        }
                );
    }
}