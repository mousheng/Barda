package com.barda.domain.folder.service;

import static com.barda.sdk.exception.BizError.NO_RESOURCE_FOUND;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.folder.model.Folder;
import com.barda.domain.folder.repository.FolderRepository;
import com.barda.infra.mongo.MongoUpsertHelper;
import com.barda.sdk.constants.FieldName;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 一个提供文件夹操作的服务类。
 */
@Service
public class FolderService {

    /**
     * 用于访问文件夹存储库的接口。
     */
    @Autowired
    private FolderRepository repository;

    /**
     * 一个用于帮助执行MongoDB upsert操作的助手类。
     */
    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    /**
     * 根据ID更新文件夹。
     *
     * @param id 要更新的文件夹的ID。
     * @param resource 要更新的新文件夹数据。
     * @return 一个Mono，表示是否成功更新了文件夹。
     */
    public Mono<Boolean> updateById(String id, Folder resource) {
        if (id == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "INVALID_PARAMETER", FieldName.ID));
        }

        return mongoUpsertHelper.updateById(resource, id);
    }

    /**
     * 根据ID查找文件夹。
     *
     * @param id 要查找的文件夹的ID。
     * @return 一个Mono，表示找到的文件夹。
     */
    public Mono<Folder> findById(String id) {
        if (id == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "INVALID_PARAMETER", FieldName.ID));
        }

        return repository.findById(id)
                .switchIfEmpty(Mono.error(new BizException(BizError.NO_RESOURCE_FOUND, "FOLDER_NOT_FOUND", id)));
    }

    /**
     * 创建一个新的文件夹。
     *
     * @param folder 要创建的新文件夹数据。
     * @return 一个Mono，表示创建的新文件夹。
     */
    public Mono<Folder> create(Folder folder) {
        return repository.save(folder);
    }

    /**
     * 根据组织ID查找文件夹。
     *
     * @param organizationId 要查找的组织的ID。
     * @return 一个Flux，表示找到的多个文件夹。
     */
    public Flux<Folder> findByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId);
    }

    /**
     * 根据ID删除多个文件夹。
     *
     * @param ids 要删除的多个文件夹的ID集合。
     * @return 一个Mono，表示是否成功删除了文件夹。
     */
    public Mono<Void> deleteAllById(Collection<String> ids) {
        return repository.deleteAllById(ids);
    }

    /**
     * 检查是否存在指定ID的文件夹。
     *
     * @param id 要检查的文件夹的ID。
     * @return 一个Mono，表示是否存在指定ID的文件夹。
     */
    public Mono<Boolean> exist(String id) {
        return findById(id)
                .hasElement()
                .onErrorResume(throwable -> {
                    if (throwable instanceof BizException bizException && bizException.getError() == NO_RESOURCE_FOUND) {
                        return Mono.just(false);
                    }
                    return Mono.error(throwable);
                });
    }
}
