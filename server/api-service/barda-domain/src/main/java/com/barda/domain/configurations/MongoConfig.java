package com.barda.domain.configurations;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongoV3Driver;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.barda.domain.user.model.User;
import com.barda.sdk.config.MaterialProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * MongoDB 配置类。
 *
 * 该类提供 MongoDB 相关的配置和 bean 定义，包括数据源、模板、审计等功能。
 */
@Slf4j
@Configuration
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories(basePackages = {"com.barda.infra", "com.barda.domain"})
public class MongoConfig {

    /**
     * 素材属性。
     */
    @Autowired
    private MaterialProperties materialProperties;

    /**
     * 映射 MongoDB 转换器。
     */
    @Autowired
    private MappingMongoConverter mappingMongoConverter;

    /**
     * 初始化方法。
     *
     * 该方法在 bean 初始化时执行，用于设置 MongoDB 转换器的键分隔符。
     */
    @PostConstruct
    public void init() {
        mappingMongoConverter.setMapKeyDotReplacement("##OB_REPLACE##");
    }

    /**
     * Mongock 应用运行器。
     *
     * 该方法创建并返回 Mongock 应用运行器，用于执行数据库迁移和版本控制。
     */
    @Bean
    public MongockSpring5.MongockApplicationRunner mongockApplicationRunner(ApplicationContext springContext, MongoTemplate mongoTemplate) {
        SpringDataMongoV3Driver springDataMongoV3Driver = SpringDataMongoV3Driver.withDefaultLock(mongoTemplate);
        springDataMongoV3Driver.setWriteConcern(WriteConcern.JOURNALED.withJournal(false));
        springDataMongoV3Driver.setReadConcern(ReadConcern.LOCAL);

        return MongockSpring5.builder()
                .setDriver(springDataMongoV3Driver)
                .addChangeLogsScanPackages(List.of("com.barda.runner.migrations"))
                .setSpringContext(springContext)
                .buildApplicationRunner();
    }

    /**
     * 审计提供者。
     *
     * 该方法创建并返回 ReactiveAuditorAware 实例，用于在审计操作中注入创建者和修改者。
     */
    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    @Bean
    public ReactiveAuditorAware<String> auditorProvider() {
        return () -> ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> (User) securityContext.getAuthentication().getPrincipal())
                .map(User::getId);
    }

    /**
     * 主 MongoDB 模板。
     */
    @Bean
    @Primary
    public ReactiveMongoTemplate reactiveMongoTemplate(ReactiveMongoDatabaseFactory mongoDbFactory, MappingMongoConverter
            mappingMongoConverter) {
        return new ReactiveMongoTemplate(mongoDbFactory, mappingMongoConverter);
    }

    /**
     * 从 MongoDB 模板（次要优先）。
     */
    @Bean("reactiveMongoSlaveTemplate")
    public ReactiveMongoTemplate reactiveMongoSlaveTemplate(ReactiveMongoDatabaseFactory mongoDbFactory,
            MappingMongoConverter mappingMongoConverter) {
        ReactiveMongoTemplate mongoTemplate = new ReactiveMongoTemplate(mongoDbFactory, mappingMongoConverter);
        mongoTemplate.setReadPreference(ReadPreference.secondaryPreferred());
        return mongoTemplate;
    }

    /**
     * 素材 GridFs 模板。
     */
    @Bean("materialGridFsTemplate")
    public ReactiveGridFsTemplate reactiveGridFsTemplate(ReactiveMongoDatabaseFactory factory, MappingMongoConverter converter) {
        return new ReactiveGridFsTemplate(factory, converter, materialProperties.getMongodbGridFs().getBucketName());
    }

    /**
     * 用于 Mongock 的 MongoDB 模板。
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MappingMongoConverter mappingMongoConverter) {
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter);
    }
}
