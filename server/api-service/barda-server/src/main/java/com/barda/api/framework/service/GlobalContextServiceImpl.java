package com.barda.api.framework.service;

import static java.util.Optional.ofNullable;

import java.util.Locale;
import java.util.Locale.LanguageRange;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * 全局上下文服务的实现类。
 * 实现了{@link GlobalContextService}接口，
 * 提供了获取客户端区域设置的相关方法。
 */
@Service
public class GlobalContextServiceImpl implements GlobalContextService {

    @Override
    public Locale getClientLocale(ServerHttpRequest request) {
        // 从HTTP请求头部中获取accept-language
        // 如果存在，则解析为LanguageRange并返回第一个元素
        // 然后将LanguageRange转换为Locale
        // 如果不存在accept-language头部，则返回默认的Locale.ENGLISH
        return ofNullable(request.getHeaders().getFirst("accept-language"))
                .map(LanguageRange::parse)
                .filter(CollectionUtils::isNotEmpty)
                .map(languageRanges -> languageRanges.iterator().next())
                .map(LanguageRange::getRange)
                .map(Locale::forLanguageTag)
                .orElse(Locale.ENGLISH);
    }

    @Override
    public Locale getClientLocale(ServerRequest request) {
        // 从请求头部中获取accept-language
        // 如果存在，则解析为LanguageRange并返回第一个元素
        // 然后将LanguageRange转换为Locale
        // 如果不存在accept-language头部，则返回默认的Locale.ENGLISH
        return ofNullable(request.headers().firstHeader("accept-language"))
                .map(LanguageRange::parse)
                .filter(CollectionUtils::isNotEmpty)
                .map(languageRanges -> languageRanges.iterator().next())
                .map(LanguageRange::getRange)
                .map(Locale::forLanguageTag)
                .orElse(Locale.ENGLISH);
    }
}
