package com.barda.api.configurations;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.barda.domain.application.repository.ApplicationRepository;
import com.barda.domain.organization.repository.OrganizationRepository;
import com.barda.domain.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SeedMongoData {

    @Bean
    ApplicationRunner init(UserRepository userRepository,
            OrganizationRepository organizationRepository,
            ApplicationRepository applicationRepository,
            ReactiveMongoTemplate mongoTemplate) {
        return args -> {};
    }
}
