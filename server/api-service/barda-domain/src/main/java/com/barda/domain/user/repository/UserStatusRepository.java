package com.barda.domain.user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.user.model.UserStatus;

/**
 * 用户状态仓库接口，继承自ReactiveMongoRepository，用于操作UserStatus集合。
 */
@Repository
public interface UserStatusRepository extends ReactiveMongoRepository<UserStatus, String> {

}
