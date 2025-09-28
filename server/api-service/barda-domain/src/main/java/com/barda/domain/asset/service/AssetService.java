package com.barda.domain.asset.service;

import org.springframework.http.codec.multipart.Part;
import org.springframework.web.server.ServerWebExchange;

import com.barda.domain.asset.model.Asset;

import reactor.core.publisher.Mono;

/**
 * AssetService接口定义了资产管理的基本操作。
 */
public interface AssetService {

    /**
     * 根据ID获取资产。
     *
     * @param id 资产ID。
     * @return 包含资产的Mono。
     */
    Mono<Asset> getById(String id);

    Mono<Asset> upload(Part filePart, int i, boolean isThumbnail);

    /**
     * 根据资产ID删除资产。
     *
     * @param assetId 资产ID。
     * @return 表示删除操作完成的Mono。
     */
    Mono<Void> remove(String assetId);

    /**
     * 生成图像响应。
     *
     * @param exchange ServerWebExchange实例。
     * @param assetId 资产ID。
     * @return 表示响应操作完成的Mono。
     */
    Mono<Void> makeImageResponse(ServerWebExchange exchange, String assetId);

}
