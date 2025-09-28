package com.barda.api.material;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.barda.domain.asset.service.AssetService;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;

import reactor.core.publisher.Mono;

/**
 * 资产控制器类，用于处理与资产相关的HTTP请求。
 */
@RestController
@RequestMapping(value = {Url.ASSET_URL, NewUrl.ASSET_URL})
public class AssetController {

    /**
     * 用于处理资产相关操作的服务。
     */
    private final AssetService service;

    /**
     * 资产控制器的构造函数。
     *
     * @param service 用于处理资产相关操作的服务
     */
    public AssetController(AssetService service) {
        this.service = service;
    }

    /**
     * 获取指定ID的资产。
     *
     * @param id        资产ID
     * @param exchange  服务器WebExchange
     * @return           空的Mono，表示操作完成
     */
    @GetMapping("/{id}")
    public Mono<Void> getById(@PathVariable String id, ServerWebExchange exchange) {
        // 设置响应头部的Cache-Control，以便客户端可以缓存图片
        exchange.getResponse().getHeaders().set(HttpHeaders.CACHE_CONTROL, "public, max-age=7776000, immutable");

        // 调用服务来生成图片响应
        return service.makeImageResponse(exchange, id);
    }
}
