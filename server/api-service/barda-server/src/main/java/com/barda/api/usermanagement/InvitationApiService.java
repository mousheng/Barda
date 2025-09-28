package com.barda.api.usermanagement;

import static com.barda.sdk.exception.BizError.INVITED_ORG_DELETED;
import static com.barda.sdk.exception.BizError.INVITER_NOT_FOUND;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofException;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.api.home.SessionUserService;
import com.barda.api.usermanagement.view.InvitationVO;
import com.barda.api.bizthreshold.AbstractBizThresholdChecker;
import com.barda.domain.invitation.model.Invitation;
import com.barda.domain.invitation.service.InvitationService;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.organization.service.OrganizationService;
import com.barda.domain.user.model.User;
import com.barda.domain.user.service.UserService;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;

import reactor.core.publisher.Mono;

/**
 * 邀请 API 服务类。
 * 该类使用 Spring 注解来声明为服务。
 */
@Service
public class InvitationApiService {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private OrgApiService orgApiService;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionUserService sessionUserService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrgMemberService orgMemberService;

    @Autowired
    private AbstractBizThresholdChecker bizThresholdChecker;

    /**
     * 邀请用户加入组织。
     *
     * @param invitationId 邀请 ID
     * @return 是否邀请成功
     */
    public Mono<Boolean> inviteUser(String invitationId) {
        return sessionUserService.getVisitorId()
                .zipWith(invitationService.getById(invitationId)
                        .switchIfEmpty(deferredError(BizError.INVALID_INVITATION_CODE, "INVALID_INVITATION_CODE", invitationId)))
                .flatMap(tuple -> {
                    String visitorId = tuple.getT1();
                    Invitation invitation = tuple.getT2();
                    String orgId = invitation.getInvitedOrganizationId();

                    return tryJoinOrg(visitorId, orgId)
                            .handle((joinOrgResult, sink) -> {
                                if (joinOrgResult.alreadyInOrg()) {
                                    sink.error(ofException(BizError.ALREADY_IN_ORGANIZATION, "ALREADY_IN_ORGANIZATION"));
                                    return;
                                }
                                sink.next(joinOrgResult);
                            })
                            .then(orgApiService.switchCurrentOrganizationTo(orgId));
                });
    }

    /**
     * 尝试加入组织。
     *
     * @param visitorId 访客ID。
     * @param orgId     组织ID。
     * @return 包含加入组织结果的Mono。
     */
    private Mono<JoinOrgResult> tryJoinOrg(String visitorId, String orgId) {
        return organizationService.getById(orgId)
                .switchIfEmpty(deferredError(INVITED_ORG_DELETED, "INVITED_ORG_DELETED"))
                .then(orgMemberService.getOrgMember(orgId, visitorId)
                        .hasElement()
                        .flatMap(inOrg -> {
                            if (inOrg) {
                                // 如果已经在组织中，则返回已在组织中的结果
                                return Mono.just(new JoinOrgResult(true, false));
                            }

                            // 否则，尝试加入组织
                            return bizThresholdChecker.checkMaxOrgCount(visitorId)
                                    .then(bizThresholdChecker.checkMaxOrgMemberCount(orgId))
                                    .then(invitationService.inviteToOrg(visitorId, orgId))
                                    .map(result -> new JoinOrgResult(false, result));
                        }));
    }

    /**
     * 获取邀请视图。
     *
     * @param invitationId 邀请ID。
     * @return 包含邀请视图的Mono。
     */
    public Mono<InvitationVO> getInvitationView(String invitationId) {
        return invitationService.getById(invitationId)
                .switchIfEmpty(deferredError(BizError.INVALID_INVITATION_CODE, "INVALID_INVITATION_CODE", invitationId))
                .flatMap(invitation -> Mono.zip(getUserMono(invitation), getOrgMono(invitation))
                        .map(tuple -> InvitationVO.from(invitation, tuple.getT1(), tuple.getT2())));
    }

    /**
     * 获取组织的Mono。
     *
     * @param invitation 邀请对象。
     * @return 包含组织对象的Mono。
     */
    @Nonnull
    private Mono<Organization> getOrgMono(Invitation invitation) {
        return organizationService.getById(invitation.getInvitedOrganizationId())
                .switchIfEmpty(deferredError(INVITED_ORG_DELETED, "INVITED_ORG_DELETED"));
    }

    /**
     * 获取用户的Mono。
     *
     * @param invitation 邀请对象。
     * @return 包含用户对象的Mono。
     */
    @Nonnull
    private Mono<User> getUserMono(Invitation invitation) {
        return userService.findById(invitation.getCreateUserId())
                .switchIfEmpty(deferredError(INVITER_NOT_FOUND, "INVITED_ORG_DELETED"));
    }

    /**
     * 创建邀请。
     *
     * @param orgId 组织ID。
     * @return 包含邀请视图的Mono。
     */
    public Mono<InvitationVO> create(String orgId) {
        return sessionUserService.getVisitor()
                .zipWith(organizationService.getById(orgId)
                        .switchIfEmpty(Mono.error(new BizException(BizError.INVALID_ORG_ID, "INVALID_ORG_ID"))))
                .flatMap(tuple2 -> {
                    User user = tuple2.getT1();
                    Organization org = tuple2.getT2();
                    Invitation invitation = Invitation
                            .builder()
                            .createUserId(user.getId())
                            .invitedOrganizationId(orgId)
                            .build();
                    return invitationService.create(invitation)
                            .flatMap(i -> Mono.just(InvitationVO.from(i, user, org)));
                });
    }

    private record JoinOrgResult(boolean alreadyInOrg, boolean success) {

    }
}
