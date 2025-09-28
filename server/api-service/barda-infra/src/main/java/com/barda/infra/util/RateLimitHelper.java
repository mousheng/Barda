package com.barda.infra.util;

import java.time.Duration;
import java.util.Set;

import com.google.common.collect.Sets;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;

/**
 * 一个帮助类，用于处理请求限制和速率限制。
 */
public class RateLimitHelper {

    /**
     * 1分钟内每部电话最多1个OTP发送请求。
     */
    public static final Set<RequestLimitRule> OTP_SEND_BY_PHONE_LIMIT_RULES = Sets.newHashSet(
            RequestLimitRule.of(Duration.ofMinutes(1), 1));

    /**
     * 1分钟内每IP最多100个OTP发送请求。
     */
    public static final Set<RequestLimitRule> OTP_SEND_BY_IP_LIMIT_RULES = Sets.newHashSet(
            RequestLimitRule.of(Duration.ofMinutes(1), 100));

    /**
     * 1分钟内每部电话最多100个OTP验证请求。
     */
    public static final Set<RequestLimitRule> OTP_VERIFY_BY_PHONE_LIMIT_RULES = Sets.newHashSet(
            RequestLimitRule.of(Duration.ofMinutes(1), 100));

    /**
     * OTP发送的键前缀。
     */
    public static final String OTP_SEND = "OPT_SEND:";

    /**
     * OTP验证的键前缀。
     */
    public static final String OTP_VERIFTY = "OTP_VERIFTY:";

    /**
     * 构建请求限制键。
     *
     * @param biz 业务类型
     * @param key 键
     * @return 构建的键
     */
    public static String buildLimitKey(String biz, String key) {
        return "RateLimit:" + biz + key;
    }
}
