package com.barda.api.bizthreshold;

import java.util.Collections;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.sdk.config.dynamic.Conf;
import com.barda.sdk.config.dynamic.ConfigCenter;
import com.barda.sdk.config.dynamic.ConfigInstance;

import reactor.core.publisher.Mono;

/**
 * 业务阈值检查器类，继承自抽象的业务阈值检查器类，提供检查组织、组、应用等的最大数量的功能。
 */
@Service
public class BizThresholdChecker extends AbstractBizThresholdChecker {

    /**
     * 配置中心。
     */
    @Autowired
    private ConfigCenter configCenter;

    /**
     * 单个用户可以拥有的最大组织数量的配置项。
     */
    private Conf<Integer> maxOrgPerUser;
    private Conf<Integer> maxOrgMemberCount;
    private Conf<Integer> maxOrgGroupCount;
    private Conf<Integer> maxOrgAppCount;
    /**
     * 用户组织数量的白名单的配置项。
     */
    private Conf<Map<String, Integer>> userOrgCountWhiteList;
    private Conf<Map<String, Integer>> orgMemberCountWhiteList;
    private Conf<Map<String, Integer>> orgAppCountWhiteList;
    private Conf<Integer> maxDeveloperCount;

    // 省略其他私有成员变量的注释

    /**
     * 初始化方法，在Bean初始化时执行。
     * 从配置中心中读取相关的配置项。
     */
    @PostConstruct
    private void init() {
        ConfigInstance threshold = configCenter.threshold();
        maxOrgPerUser = threshold.ofInteger("maxOrgPerUser", 5);
        userOrgCountWhiteList = threshold.ofMap("userOrgCountWhiteList", String.class, Integer.class, Collections.emptyMap());
        maxOrgMemberCount = threshold.ofInteger("maxOrgMemberCount", 50);
        orgMemberCountWhiteList = threshold.ofMap("orgMemberCountWhiteList", String.class, Integer.class, Collections.emptyMap());
        maxOrgGroupCount = threshold.ofInteger("maxOrgGroupCount", 10);
        maxOrgAppCount = threshold.ofInteger("maxOrgAppCount", 50);
        orgAppCountWhiteList = threshold.ofMap("orgAppCountWhiteList", String.class, Integer.class, Collections.emptyMap());
        maxDeveloperCount = threshold.ofInteger("maxDeveloperCount", 50);
    }

    @Override
    protected int getMaxOrgPerUser() {
        return maxOrgPerUser.get();
    }

    @Override
    protected int getMaxOrgMemberCount() {
        return maxOrgMemberCount.get();
    }

    @Override
    protected int getMaxOrgGroupCount() {
        return maxOrgGroupCount.get();
    }

    @Override
    protected int getMaxOrgAppCount() {
        return maxOrgAppCount.get();
    }

    @Override
    protected Map<String, Integer> getUserOrgCountWhiteList() {
        return userOrgCountWhiteList.get();
    }

    @Override
    protected Map<String, Integer> getOrgMemberCountWhiteList() {
        return orgMemberCountWhiteList.get();
    }

    @Override
    protected Map<String, Integer> getOrgAppCountWhiteList() {
        return orgAppCountWhiteList.get();
    }

    @Override
    protected Mono<Integer> getMaxDeveloperCount() {
        return Mono.just(maxDeveloperCount.get());
    }
}
