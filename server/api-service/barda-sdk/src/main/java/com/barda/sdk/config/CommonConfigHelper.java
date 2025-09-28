package com.barda.sdk.config;

import org.springframework.stereotype.Component;

import com.barda.sdk.config.dynamic.ConfigCenter;
import com.barda.sdk.config.dynamic.ConfigInstanceHelper;

/**
 * 通用配置帮助类，提供对通用配置的辅助功能。
 */
@Component
public class CommonConfigHelper {

    /**
     * 通用配置
     */
    private final CommonConfig commonConfig;

    /**
     * 配置实例帮助类
     */
    private final ConfigInstanceHelper configInstanceHelper;

    /**
     * 构造函数
     *
     * @param commonConfig 通用配置
     * @param configCenter 配置中心
     */
    public CommonConfigHelper(CommonConfig commonConfig, ConfigCenter configCenter) {
        this.commonConfig = commonConfig;
        this.configInstanceHelper = new ConfigInstanceHelper(configCenter.deployment());
    }

    /**
     * 获取主机
     *
     * @return 主机
     */
    public String getHost() {
        return configInstanceHelper.ofString("js-executor.host", commonConfig.getJsExecutor().getHost());
    }
}
