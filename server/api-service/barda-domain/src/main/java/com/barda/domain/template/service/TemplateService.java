package com.barda.domain.template.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.template.model.Template;
import com.barda.domain.template.repository.TemplateRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 模板服务类，提供对模板的操作。
 * 该类使用了 Lombok 库来自动生成 getter 和 log 字段。
 * 该类使用了 Spring Data MongoDB 库来实现对 MongoDB 集合的操作。
 */
@Slf4j
@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository; // 模板仓库

    /**
     * 根据模板 ID 查询模板。
     *
     * @param templateId 模板 ID
     * @return 模板 Mono 对象
     */
    public Mono<Template> getById(String templateId) {
        return templateRepository.findById(templateId);
    }

    /**
     * 根据应用 ID 集合查询模板。
     *
     * @param applicationIds 应用 ID 集合
     * @return 模板 Flux 对象
     */
    public Flux<Template> getByApplicationIds(Collection<String> applicationIds) {
        return templateRepository.findByApplicationIdIn(applicationIds);
    }

    /**
     * 根据应用 ID 查询模板。
     *
     * @param applicationId 应用 ID
     * @return 模板 Mono 对象
     */
    public Mono<Template> getByApplicationId(String applicationId) {
        return templateRepository.findByApplicationId(applicationId);
    }
}
