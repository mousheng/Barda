package com.barda.api.home;

import static com.barda.infra.event.EventType.APPLICATION_MOVE;
import static com.barda.sdk.exception.BizError.INVALID_PARAMETER;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barda.api.application.view.ApplicationPermissionView;
import com.barda.api.framework.view.ResponseView;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.domain.application.model.ApplicationType;
import com.barda.domain.folder.model.Folder;
import com.barda.domain.folder.service.FolderService;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.event.EventType;

import reactor.core.publisher.Mono;

/**
 * 控制器类，用于处理文件夹相关的操作。
 *
 */
@RestController
@RequestMapping(NewUrl.FOLDER_URL)
public class FolderController {

    @Autowired
    private FolderService folderService;
    @Autowired
    private FolderApiService folderApiService;
    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    /**
     * 创建文件夹的API。
     *
     * @param folder 要创建的目标文件夹。
     *
     * @return 包含 {@link ResponseView} 的 Mono，表示创建文件夹的结果。
     */
    @PostMapping
    public Mono<ResponseView<FolderInfoView>> create(@RequestBody Folder folder) {
        return folderApiService.create(folder)
                .delayUntil(folderInfoView -> folderApiService.upsertLastViewTime(folderInfoView.getFolderId()))
                .delayUntil(f -> businessEventPublisher.publishFolderCommonEvent(f.getFolderId(), f.getName(), EventType.FOLDER_CREATE))
                .map(ResponseView::success);
    }

    /**
     * 删除文件夹的API。
     *
     * @param folderId 要删除的目标文件夹的ID。
     *
     * @return 包含 {@link ResponseView} 的 Mono，表示删除文件夹的结果。
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseView<Void>> delete(@PathVariable("id") String folderId) {
        return folderApiService.delete(folderId)
                .delayUntil(f -> businessEventPublisher.publishFolderCommonEvent(f.getId(), f.getName(), EventType.FOLDER_DELETE))
                .then(Mono.fromSupplier(() -> ResponseView.success(null)));
    }

    /**
     * 更新文件夹的名称的API。
     *
     * @param folder 要更新的目标文件夹。
     *
     * @return 包含 {@link ResponseView} 的 Mono，表示更新文件夹的结果。
     */
    @PutMapping
    public Mono<ResponseView<FolderInfoView>> update(@RequestBody Folder folder) {
        return folderService.findById(folder.getId())
                .zipWhen(__ -> folderApiService.update(folder))
                .delayUntil(tuple2 -> {
                    Folder old = tuple2.getT1();
                    return businessEventPublisher.publishFolderCommonEvent(folder.getId(), old.getName() + " => " + folder.getName(),
                            EventType.FOLDER_UPDATE);
                })
                .map(tuple2 -> ResponseView.success(tuple2.getT2()));
    }

    /**
     * 获取文件夹下所有元素的API。
     *
     * @param folderId 要获取元素的目标文件夹的ID。
     * @param applicationType 要获取元素的应用类型。
     *
     * @return 包含 {@link ResponseView} 的 Mono，表示获取文件夹下所有元素的结果。
     */
    @GetMapping("/elements")
    public Mono<ResponseView<List<?>>> getElements(@RequestParam(value = "id", required = false) String folderId,
            @RequestParam(value = "applicationType", required = false) ApplicationType applicationType) {
        return folderApiService.getElements(folderId, applicationType)
                .collectList()
                .delayUntil(__ -> folderApiService.upsertLastViewTime(folderId))
                .map(ResponseView::success);
    }

    /**
     * 移动文件夹的API。
     *
     * @param applicationLikeId 要移动的目标文件夹的ID。
     * @param targetFolderId 要移动到的目标文件夹的ID。
     *
     * @return 包含 {@link ResponseView} 的 Mono，表示移动文件夹的结果。
     */
    @PutMapping("/move/{id}")
    public Mono<ResponseView<Void>> move(@PathVariable("id") String applicationLikeId,
            @RequestParam(value = "targetFolderId", required = false) String targetFolderId) {
        return folderApiService.move(applicationLikeId, targetFolderId)
                .then(businessEventPublisher.publishApplicationCommonEvent(applicationLikeId, targetFolderId, APPLICATION_MOVE))
                .then(Mono.fromSupplier(() -> ResponseView.success(null)));
    }

    /**
     * 更新文件夹的权限的API。
     *
     * @param folderId 要更新权限的目标文件夹的ID。
     * @param permissionId 要更新权限的目标权限的ID。
     * @param updatePermissionRequest 要更新的权限信息。
     *
     * @return 包含 {@link ResponseView} 的 Mono，表示更新文件夹的权限的结果。
     */
    @PutMapping("/{folderId}/permissions/{permissionId}")
    public Mono<ResponseView<Void>> updatePermission(@PathVariable String folderId,
            @PathVariable String permissionId,
            @RequestBody UpdatePermissionRequest updatePermissionRequest) {
        ResourceRole role = ResourceRole.fromValue(updatePermissionRequest.role());
        if (role == null) {
            return ofError(INVALID_PARAMETER, "INVALID_PARAMETER", updatePermissionRequest);
        }

        return folderApiService.updatePermission(folderId, permissionId, role)
                .then(Mono.fromSupplier(() -> ResponseView.success(null)));
    }

    /**
     * 删除文件夹的权限的API。
     *
     * @param folderId 要删除权限的目标文件夹的ID。
     * @param permissionId 要删除权限的目标权限的ID。
     *
     * @return 包含 {@link ResponseView} 的 Mono，表示删除文件夹的权限的结果。
     */
    @DeleteMapping("/{folderId}/permissions/{permissionId}")
    public Mono<ResponseView<Void>> removePermission(
            @PathVariable String folderId,
            @PathVariable String permissionId) {

        return folderApiService.removePermission(folderId, permissionId)
                .then(Mono.fromSupplier(() -> ResponseView.success(null)));
    }

    /**
     * 授予文件夹的权限的API。
     *
     * @param folderId 要授予权限的目标文件夹的ID。
     * @param request 要授予的权限信息。
     *
     * @return 包含 {@link ResponseView} 的 Mono，表示授予文件夹的权限的结果。
     */
    @PostMapping("/{folderId}/permissions")
    public Mono<ResponseView<Void>> grantPermission(
            @PathVariable String folderId,
            @RequestBody BatchAddPermissionRequest request) {
        ResourceRole role = ResourceRole.fromValue(request.role());
        if (role == null) {
            return ofError(INVALID_PARAMETER, "INVALID_PARAMETER", request.role());
        }
        return folderApiService.grantPermission(folderId, request.userIds(), request.groupIds(), role)
                .then(Mono.fromSupplier(() -> ResponseView.success(null)));
    }

    /**
     * 获取文件夹的权限的API。
     *
     * @param folderId 要获取权限的目标文件夹的ID。
     *
     * @return 包含 {@link ResponseView} 的 Mono，表示获取文件夹的权限的结果。
     */
    @GetMapping("/{folderId}/permissions")
    public Mono<ResponseView<ApplicationPermissionView>> getApplicationPermissions(@PathVariable String folderId) {
        return folderApiService.getPermissions(folderId)
                .map(ResponseView::success);
    }

    private record BatchAddPermissionRequest(String role, Set<String> userIds, Set<String> groupIds) {
    }

    private record UpdatePermissionRequest(String role) {
    }
}
