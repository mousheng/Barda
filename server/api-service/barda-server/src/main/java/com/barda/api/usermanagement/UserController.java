package com.barda.api.usermanagement;

import static com.barda.sdk.exception.BizError.INVALID_USER_STATUS;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import com.barda.sdk.encryption.RSACryptoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.barda.api.authentication.dto.OrganizationDomainCheckResult;
import com.barda.api.framework.view.ResponseView;
import com.barda.api.home.SessionUserService;
import com.barda.api.home.UserHomeApiService;
import com.barda.api.usermanagement.view.UpdateUserRequest;
import com.barda.api.usermanagement.view.UserProfileView;
import com.barda.domain.user.constant.UserStatusType;
import com.barda.domain.user.model.User;
import com.barda.domain.user.model.UserDetail;
import com.barda.domain.user.service.UserService;
import com.barda.domain.user.service.UserStatusService;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.exception.BizError;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 用户控制器。
 *
 */
@Slf4j
@RestController
@RequestMapping(value = {Url.USER_URL, NewUrl.USER_URL})
public class UserController {

    /**
     * 用于获取当前会话用户的服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 用户相关的服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 用户主页API的服务。
     */
    @Autowired
    private UserHomeApiService userHomeApiService;

    /**
     * 组织API的服务。
     */
    @Autowired
    private OrgApiService orgApiService;

    /**
     * 用户状态的服务。
     */
    @Autowired
    private UserStatusService userStatusService;

    /**
     * 用户API的服务。
     */
    @Autowired
    private UserApiService userApiService;

    /**
     * 通用配置。
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * RSA加密解密的服务。
     */
    @Autowired
    private RSACryptoService rsaCryptoService;

    /**
     * 获取当前用户的个人资料。
     */
    @GetMapping("/me")
    public Mono<ResponseView<?>> getUserProfile(ServerWebExchange exchange) {
        return sessionUserService.getVisitor()
                .flatMap(user -> userHomeApiService.buildUserProfileView(user, exchange))
                .flatMap(view -> orgApiService.checkOrganizationDomain()
                        .flatMap(OrganizationDomainCheckResult::buildOrganizationDomainCheckView)
                        .switchIfEmpty(Mono.just(ResponseView.success(view))));
    }

    /**
     * 标记新用户向导已显示。
     */
    @PutMapping("/newUserGuidanceShown")
    public Mono<ResponseView<Boolean>> newUserGuidanceShown() {
        return sessionUserService.getVisitorId()
                .flatMap(userHomeApiService::markNewUserGuidanceShown)
                .map(ResponseView::success);
    }

    /**
     * 标记用户的状态。
     */
    @PutMapping("/mark-status")
    public Mono<ResponseView<Boolean>> markStatus(@RequestBody MarkUserStatusRequest request) {
        UserStatusType userStatusType = UserStatusType.fromValue(request.type());
        if (userStatusType == null) {
            return ofError(INVALID_USER_STATUS, "INVALID_USER_STATUS", request.type());
        }

        return sessionUserService.getVisitorId()
                .flatMap(visitorId -> userStatusService.mark(visitorId, userStatusType, request.value()))
                .map(ResponseView::success);
    }

    /**
     * 更新用户。
     */
    @PutMapping
    public Mono<ResponseView<UserProfileView>> update(@RequestBody UpdateUserRequest updateUserRequest, ServerWebExchange exchange) {
        return sessionUserService.getVisitorId()
                .flatMap(uid -> {
                    User updateUser = new User();
                    if (StringUtils.isNotBlank(updateUserRequest.getName())) {
                        updateUser.setName(updateUserRequest.getName());
                        updateUser.setHasSetNickname(true);
                    }
                    return userService.update(uid, updateUser);
                })
                .flatMap(user -> userHomeApiService.buildUserProfileView(user, exchange))
                .map(ResponseView::success);
    }

    /**
     * 上传用户的头像。
     */
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseView<Boolean>> uploadProfilePhoto(@RequestPart("file") Mono<Part> fileMono) {
        return fileMono.zipWith(sessionUserService.getVisitor())
                .flatMap(tuple -> userService.saveProfilePhoto(tuple.getT1(), tuple.getT2()))
                .map(ResponseView::success);
    }

    /**
     * 删除用户的头像。
     */
    @DeleteMapping("/photo")
    public Mono<ResponseView<Void>> deleteProfilePhoto() {
        return sessionUserService.getVisitor()
                .flatMap(visitor -> userService.deleteProfilePhoto(visitor)
                        .map(ResponseView::success));
    }

    /**
     * 获取用户的头像。
     */
    @GetMapping("/photo")
    public Mono<Void> getProfilePhoto(ServerWebExchange exchange) {
        return sessionUserService.getVisitorId()
                .flatMap(userId -> getProfilePhoto(exchange, userId));
    }

    /**
     * 获取指定用户的头像。
     */
    @GetMapping("/photo/{userId}")
    public Mono<Void> getProfilePhoto(ServerWebExchange exchange, @PathVariable String userId) {
        return userService.getUserAvatar(exchange, userId)
                .switchIfEmpty(Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND)));
    }

    /**
     * 更新用户的密码。
     */
    @PutMapping("/password")
    public Mono<ResponseView<Boolean>> updatePassword(@RequestBody UpdatePasswordRequest request) {
        if (StringUtils.isBlank(request.oldPassword()) || StringUtils.isBlank(request.newPassword())) {
            return ofError(BizError.INVALID_PARAMETER, "PASSWORD_EMPTY");
        }
        return sessionUserService.getVisitorId()
                .flatMap(user -> userService.updatePassword(user,
                        rsaCryptoService.pureDecryt(request.oldPassword()),
                        rsaCryptoService.pureDecryt(request.newPassword())))
                .map(ResponseView::success);
    }

    /**
     * 重置用户的密码。
     */
    @PostMapping("/reset-password")
    public Mono<ResponseView<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!commonConfig.isEnterpriseMode()) {
            return ofError(BizError.UNSUPPORTED_OPERATION, "BAD_REQUEST");
        }
        if (StringUtils.isBlank(request.userId())) {
            return ofError(BizError.INVALID_PARAMETER, "INVALID_USER_ID");
        }
        return userApiService.resetPassword(request.userId())
                .map(ResponseView::success);
    }

    /**
     * 设置用户的密码。
     */
    @PostMapping("/password")
    public Mono<ResponseView<Boolean>> setPassword(@RequestParam String password) {
        if (StringUtils.isBlank(password)) {
            return ofError(BizError.INVALID_PARAMETER, "PASSWORD_EMPTY");
        }
        return sessionUserService.getVisitorId()
                .flatMap(user -> userService.setPassword(user, password))
                .map(ResponseView::success);
    }

    /**
     * 获取当前用户的详细信息。
     */
    @GetMapping("/currentUser")
    public Mono<ResponseView<UserDetail>> getCurrentUser(ServerWebExchange exchange) {
        return sessionUserService.getVisitor()
                .flatMap(user -> userService.buildUserDetail(user, false))
                .map(ResponseView::success);
    }

    /**
     * 获取指定用户的详细信息。
     */
    @GetMapping("/userDetail/{id}")
    public Mono<ResponseView<?>> getUserDetail(@PathVariable("id") String userId) {
        return userApiService.getUserDetailById(userId)
                .map(ResponseView::success);
    }

    public record ResetPasswordRequest(String userId) {
    }

    public record UpdatePasswordRequest(String oldPassword, String newPassword) {
    }

    private record MarkUserStatusRequest(String type, Object value) {
    }
}
