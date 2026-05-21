package com.barda.api.config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.annotation.JsonView;
import com.barda.api.framework.view.ResponseView;
import com.barda.api.usermanagement.OrgApiService;
import com.barda.infra.config.model.ServerConfig;
import com.barda.infra.config.repository.ServerConfigRepository;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.config.SerializeConfig.JsonViews;
import com.barda.sdk.config.dynamic.Conf;
import com.barda.sdk.config.dynamic.ConfigCenter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 配置控制器类。
 * 该类使用 Spring MVC 的注解来定义 RESTful 接口。
 *
 * @RestController 表明该类是一个 RESTful 控制器。
 * @RequestMapping 表明该类处理的 URL 路径。
 * @Slf4j 表明该类使用 SLF4J 进行日志记录。
 */
@RestController
@RequestMapping(value = {Url.CONFIG_URL, NewUrl.CONFIG_URL})
@Slf4j
public class ConfigController {

    /**
     * 通用配置服务。
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 服务器配置数据访问接口。
     */
    @Autowired
    private ServerConfigRepository serverConfigRepository;

    /**
     * 组织 API 服务。
     */
    @Autowired
    private OrgApiService orgApiService;

    /**
     * 配置中心服务。
     */
    @Autowired
    private ConfigCenter configCenter;

    /**
     * 部署 ID 配置项。
     */
    private Conf<String> deploymentIdConf;

    /**
     * 初始化方法。
     * 在该方法中，初始化 deploymentIdConf 配置项。
     */
    @PostConstruct
    public void init() {
        deploymentIdConf = configCenter.deployment().ofString("id", "");
    }

    /**
     * 获取部署 ID。
     *
     * @return 部署 ID
     */
    @GetMapping(value = "/deploymentId")
    public Mono<String> getDeploymentId() {
        return Mono.just(deploymentIdConf.get());
    }

    /**
     * 获取服务器配置。
     *
     * @param key 配置键
     * @return 服务器配置
     */
    @GetMapping("/{key}")
    public Mono<ResponseView<ServerConfig>> getServerConfig(@PathVariable String key) {
        return serverConfigRepository.findByKey(key)
                .defaultIfEmpty(new ServerConfig(key, null))
                .map(ResponseView::success);
    }

    /**
     * 更新服务器配置。
     *
     * @param key 配置键
     * @param updateConfigRequest 更新配置请求
     * @return 更新后的服务器配置
     */
    @PostMapping("/{key}")
    public Mono<ResponseView<ServerConfig>> updateServerConfig(@PathVariable String key, @RequestBody UpdateConfigRequest updateConfigRequest) {
        return serverConfigRepository.upsert(key, updateConfigRequest.value())
                .map(ResponseView::success);
    }

    /**
     * 获取配置信息。
     *
     * @param orgId 可选的组织ID，用于SAAS模式下获取指定组织的登录配置
     * @param exchange 服务器 Web Exchange
     * @return 配置信息
     */
    @JsonView(JsonViews.Public.class)
    @GetMapping
    public Mono<ResponseView<ConfigView>> getConfig(@RequestParam(required = false) String orgId,
                                                     ServerWebExchange exchange) {
        return orgApiService.getOrganizationConfigs(orgId)
                .map(ResponseView::success);
    }

    /**
     * 内部类，用于更新配置请求。
     */
    private record UpdateConfigRequest(String value) {
    }
}
