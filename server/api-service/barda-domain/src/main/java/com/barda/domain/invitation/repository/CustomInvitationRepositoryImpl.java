package com.barda.domain.invitation.repository;

import static com.barda.domain.util.QueryDslUtils.fieldName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;
import com.barda.domain.invitation.model.Invitation;
// 在使用 Spring Data JPA 或者其他支持 QueryDSL 的库时，会生成与实体类对应的Q类。这些 Q类用于支持类型安全的查询。
// 如果IDEA提示你找不到该类，说明你配置错误
// 请对照步骤 https://mousheng.github.io/lowcoder_CN/#/developer/debugBackendCode
// 如果你运行 mvn clean package -DskipTests命令成功，仍显示 无法解析符号 'QInvitation'
// 你应该点击idea右侧maven面板的【重新加载所有Maven项目】即可
import com.barda.domain.invitation.model.QInvitation;
import com.barda.sdk.constants.FieldName;

import reactor.core.publisher.Mono;

/**
 * 自定义邀请仓库的实现类。
 * 该类实现了 {@link CustomInvitationRepository} 接口，并提供向邀请中添加被邀请的用户的功能。
 */
@Repository
public class CustomInvitationRepositoryImpl implements CustomInvitationRepository {

    /**
     * 反应式 MongoDB 模板。
     */
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    /**
     * 向指定的邀请中添加被邀请的用户。
     *
     * @param invitationId 邀请ID
     * @param userId 被邀请的用户ID
     * @return 包含更新结果的Mono
     */
    @Override
    public Mono<UpdateResult> addInvitedUser(String invitationId, String userId) {
        final String reactionsField = fieldName(QInvitation.invitation.invitedUserIds);

        Query query = Query.query(Criteria.where(FieldName.ID).is(invitationId));
        return mongoTemplate.updateFirst(query, new Update().addToSet(reactionsField, userId), Invitation.class);
    }
}
