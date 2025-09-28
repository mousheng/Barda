package com.barda.api;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.barda.sdk.config.CommonConfig;

import lombok.extern.slf4j.Slf4j;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;
import reactor.tools.agent.ReactorDebugAgent;

/**
 * 服务器应用程序的入口点。
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "com.barda.api.framework.configuration")
@EnableScheduling
@EnableConfigurationProperties
public class ServerApplication {

    /**
     * 通用配置。
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 初始化方法，在应用程序启动时执行。
     * 如果在通用配置中启用了BlockHound，则在此处安装并启用BlockHound。
     * 还启用了Reactor调试代理和Hooks以进行操作调试。
     */
    @PostConstruct
    public void init() {
        if (commonConfig.isBlockHoundEnable()) {
            log.info("启用BlockHound。");
            BlockHound.builder()
                    .allowBlockingCallsInside("com.mongodb.internal.connection.DefaultAuthenticator", "authenticateAsync")
                    .install();

            ReactorDebugAgent.init();
            Hooks.onOperatorDebug();
        }
    }

    /**
     * 主方法，应用程序的入口点。
     *
     * @param args 命令行参数。
     */
    public static void main(String[] args) {

        // 启用任务调度器的指标
        Schedulers.enableMetrics();

        // 运行Spring Boot应用程序
        new SpringApplicationBuilder(ServerApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }

}
