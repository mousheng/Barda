package com.barda.api.material;

import static org.apache.commons.io.FileUtils.ONE_GB;
import static org.apache.commons.io.FileUtils.ONE_MB;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

import java.util.Base64;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;

import com.barda.api.home.SessionUserService;
import com.barda.api.material.MaterialController.MaterialView;
import com.barda.api.usermanagement.OrgDevChecker;
import com.barda.domain.material.model.MaterialMeta;
import com.barda.domain.material.model.MaterialType;
import com.barda.domain.material.repository.MaterialMateRepository;
import com.barda.domain.material.service.meta.MaterialMetaService;
import com.barda.domain.material.service.storage.MaterialStorageService;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.config.dynamic.ConfigCenter;
import com.barda.sdk.config.dynamic.ConfigInstanceHelper;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;

import reactor.core.publisher.Mono;

/**
 * 素材API的实现类。
 *
 */
@Service
public class MaterialApiServiceImpl implements MaterialApiService {

    /**
     * 表示单个素材的默认最大大小限制，20MB。
     */
    private static final long DEFAULT_SINGLE_FILE_SIZE_LIMIT = 20 * ONE_MB;

    /**
     * 表示素材所属组织的默认最大存储大小限制，2GB。
     */
    private static final long DEFAULT_TOTAL_STORAGE_SIZE_LIMIT = 2 * ONE_GB;

    /**
     * 素材元数据服务。
     */
    @Autowired
    private MaterialMetaService materialMetaService;

    /**
     * 素材存储服务。
     */
    @Autowired
    private MaterialStorageService materialStorageService;

    /**
     * 会话用户服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 组织开发者检查器。
     */
    @Autowired
    private OrgDevChecker orgDevChecker;

    /**
     * 配置中心。
     */
    @Autowired
    private ConfigCenter configCenter;

    /**
     * 素材匹配仓库。
     */
    @Autowired
    private MaterialMateRepository materialMateRepository;

    /**
     * 通用配置。
     */
    @Autowired
    private CommonConfig commonConfig;

    private ConfigInstanceHelper configInstance;

    /**
     * 初始化方法，从配置中心获取阈值并创建ConfigInstanceHelper实例。
     */
    @PostConstruct
    public void init() {
        configInstance = new ConfigInstanceHelper(configCenter.threshold());
    }

    /**
     * 上传素材。
     *
     * @param filename 文件名
     * @param content  素材内容的Base64编码
     * @param type     素材类型
     * @return 上传成功的素材元数据
     */
    @Override
    public Mono<MaterialMeta> upload(String filename, String content, MaterialType type) {

        // 将Base64编码的素材内容解码为字节数组
        byte[] decode = Base64.getDecoder().decode(content);

        // 进行检查：单个素材的大小是否超出限制
        return checkSingleFileSize(decode.length)
                // 获取当前访问者的组织成员的缓存
                .then(sessionUserService.getVisitorOrgMemberCache())
                // 延迟直到检查当前的组织是否是开发者
                .delayUntil(__ -> orgDevChecker.checkCurrentOrgDev())
                // 延迟直到检查素材所属的组织的总存储大小是否超出限制
                .delayUntil(orgMember -> checkTotalSize(orgMember.getOrgId(), decode.length))
                // 延迟直到删除旧的logo或favicon
                .delayUntil(orgMember -> {
                    if (type == MaterialType.LOGO || type == MaterialType.FAVICON) {
                        //noinspection ConstantConditions
                        // 查询并删除旧的logo或favicon
                        return materialMateRepository.findByOrgIdAndType(orgMember.getOrgId(), type)
                                .delayUntil(materialMeta -> materialMateRepository.deleteById(materialMeta.getId()))
                                .flatMap(materialMeta -> materialStorageService.delete(materialMeta));
                    }
                    // COMMON
                    //noinspection ConstantConditions
                    // 查询并删除同名的素材
                    return materialMateRepository.findByOrgIdAndFilenameAndType(orgMember.getOrgId(), filename, type)
                            .delayUntil(materialMeta -> materialMateRepository.deleteById(materialMeta.getId()))
                            .flatMap(materialMeta -> materialStorageService.delete(materialMeta));
                })
                // 延迟直到将素材元数据保存到数据库
                .flatMap(orgMember -> {
                    MaterialMeta materialMeta = MaterialMeta.builder()
                            .orgId(orgMember.getOrgId())
                            .filename(filename)
                            .size(decode.length)
                            .type(type)
                            .build();
                    return materialMateRepository.save(materialMeta);
                })
                // 延迟直到将素材内容保存到存储服务
                .delayUntil(materialMeta -> materialStorageService.save(materialMeta, decode));
    }

    /**
     * 下载素材。
     *
     * @param materialMeta 素材元数据
     * @return 素材数据流
     */
    @Override
    public Publisher<? extends DataBuffer> download(MaterialMeta materialMeta) {
        // 延迟直到检查素材所属的组织
        return Mono.defer(() -> {
                    if (materialMeta.getType() == MaterialType.LOGO || materialMeta.getType() == MaterialType.FAVICON) {
                        // 如果素材是logo或favicon，直接返回空的Mono
                        return Mono.empty();
                    }
                    // 查询并检查素材所属的组织
                    return checkMaterialOrg(materialMeta.getOrgId());
                })
                // 下载素材并返回数据流
                .thenMany(materialStorageService.download(materialMeta));
    }

    /**
     * 获取素材列表。
     *
     * @return 素材视图列表
     */
    @Override
    public Mono<List<MaterialView>> list() {
        // 获取当前访问者的组织成员的缓存
        return sessionUserService.getVisitorOrgMemberCache()
                // 并行地查询素材所属的组织的素材元数据
                .flatMapMany(orgMember -> materialMetaService.getByOrgId(orgMember.getOrgId()))
                // 将素材元数据转换为素材视图
                .map(materialMeta -> MaterialView.builder()
                        .id(materialMeta.getId())
                        .filename(materialMeta.getFilename())
                        .build()
                )
                // 收集并返回素材视图列表
                .collectList();
    }

    /**
     * 删除素材。
     *
     * @param id 素材ID
     * @return 删除操作的Mono
     */
    @Override
    public Mono<Void> delete(String id) {
        return materialMetaService.findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new BizException(BizError.INVALID_PARAMETER, "10095"))))
                .delayUntil(materialMeta -> checkMaterialOrg(materialMeta.getOrgId()))
                .delayUntil(__ -> orgDevChecker.checkCurrentOrgDev())
                .delayUntil(__ -> materialMetaService.deleteById(id))
                .flatMap(materialMeta -> materialStorageService.delete(materialMeta));
    }

    /**
     * 检查素材所属的组织。
     *
     * @param materialOrgId 素材所属的组织ID
     * @return 空的Mono，如果检查通过，否则返回错误的Mono
     */
    private Mono<Void> checkMaterialOrg(String materialOrgId) {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> {
                    if (orgMember.getOrgId().equals(materialOrgId)) {
                        return Mono.empty();
                    }
                    return Mono.error(new BizException(BizError.INVALID_MATERIAL_REQUEST, "FILE_ORG_NOT_MATCH"));
                });
    }

    /**
     * 检查单个素材的大小是否超出限制。
     *
     * @param size 素材的大小
     * @return 空的Mono，如果检查通过，否则返回错误的Mono
     */
    private Mono<Void> checkSingleFileSize(long size) {
        if (commonConfig.isSelfHost()) {
            return Mono.empty();
        }
        long sizeLimit = configInstance.ofLong("material.single-size-limit", DEFAULT_SINGLE_FILE_SIZE_LIMIT);
        if (size > sizeLimit) {
            return Mono.error(new BizException(BizError.INVALID_MATERIAL_REQUEST, "EXCEEDS_FILE_SIZE_LIMIT", byteCountToDisplaySize(sizeLimit)));
        }
        return Mono.empty();
    }

    /**
     * 检查素材所属的组织的总存储大小是否超出限制。
     *
     * @param orgId  素材所属的组织ID
     * @param newSize 新素材的大小
     * @return 空的Mono，如果检查通过，否则返回错误的Mono
     */
    private Mono<Void> checkTotalSize(String orgId, long newSize) {
        if (commonConfig.isSelfHost()) {
            return Mono.empty();
        }
        return materialMetaService.totalSize(orgId)
                .flatMap(size -> {
                    long totalSizeLimit = configInstance.ofLong("material.total-size-limit", DEFAULT_TOTAL_STORAGE_SIZE_LIMIT);
                    if ((size + newSize) > totalSizeLimit) {
                        return Mono.error(
                                new BizException(BizError.INVALID_MATERIAL_REQUEST, "EXCEEDS_ORG_SIZE_LIMIT",
                                        byteCountToDisplaySize(totalSizeLimit)));
                    }
                    return Mono.empty();
                });
    }
}
