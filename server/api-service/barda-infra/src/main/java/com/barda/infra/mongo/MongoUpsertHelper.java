package com.barda.infra.mongo;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.barda.sdk.constants.FieldName;
import com.barda.sdk.constants.GlobalContext;
import com.barda.sdk.event.BeforeSaveEvent;
import com.barda.sdk.models.HasIdAndAuditing;

import reactor.core.publisher.Mono;

/**
 * MongoDB 更新和插入辅助工具类。
 */
@Component
public class MongoUpsertHelper {

    /** 用于执行 MongoDB 操作的 ReactiveMongoTemplate。 */
    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    /** MongoDB 转换器。 */
    @Autowired
    private MongoConverter mongoConverter;

    /** 事件发布器，用于发布保存前事件。 */
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 根据 ID 更新资源。
     *
     * @param partialResource 部分资源。
     * @param id              资源 ID。
     * @return 更新是否成功的 Mono。
     */
    public <T extends HasIdAndAuditing> Mono<Boolean> updateById(T partialResource, String id) {
        return update(partialResource, FieldName.ID, id);
    }

    /**
     * 更新指定资源的部分字段。
     *
     * @param partialResource 部分资源。
     * @param uniqueKeyName   唯一键的名称。
     * @param uniqueKeyValue  唯一键的值。
     * @return 更新是否成功的 Mono。
     */
    public <T extends HasIdAndAuditing> Mono<Boolean> update(T partialResource, String uniqueKeyName, String uniqueKeyValue) {
        Query query = new Query(Criteria.where(uniqueKeyName).is(uniqueKeyValue));
        return update(partialResource, query);
    }

    /**
     * 根据查询条件删除数据。
     *
     * @param query   查询条件。
     * @param tClass  数据类型。
     * @return 删除是否成功的 Mono。
     */
    public <T> Mono<Boolean> remove(Query query, Class<T> tClass) {
        return reactiveMongoTemplate.remove(query, tClass)
                .map(deleteResult -> deleteResult.getDeletedCount() > 0);
    }

    /**
     * 更新符合查询条件的数据。
     *
     * @param partialResource 要更新的部分资源。
     * @param query            查询条件。
     * @param <T>              资源类型，必须实现 HasIdAndAuditing 接口。
     * @return 更新是否成功的 Mono。
     */
    public <T extends HasIdAndAuditing> Mono<Boolean> update(T partialResource, Query query) {
        return Mono.deferContextual(ctx -> {
                    // 设置更新时间和更新者
                    partialResource.setUpdatedAt(Instant.now());
                    partialResource.setModifiedBy(ctx.getOrDefault(GlobalContext.VISITOR_ID, GlobalContext.SYSTEM_USER_ID));
                    // 发布保存前事件
                    applicationEventPublisher.publishEvent(new BeforeSaveEvent<>(partialResource));
                    return Mono.just(convertToUpdate(partialResource));
                })
                .flatMap(updateData -> reactiveMongoTemplate.updateFirst(query, updateData, partialResource.getClass()))
                .map(updateResult -> updateResult.getModifiedCount() > 0);
    }

    /**
     * 仅根据给定的ID更新部分资源。
     *
     * @param partialResource 要更新的部分资源。
     * @param id              资源的ID。
     * @param <T>             资源类型，必须实现 HasIdAndAuditing 接口。
     * @return 更新是否成功的 Mono。
     */
    public <T extends HasIdAndAuditing> Mono<Boolean> updatePurely(T partialResource, String id) {
        // 构建查询条件
        Query query = new Query(Criteria.where(FieldName.ID).is(id));
        return updatePurely(partialResource, query);
    }

    /**
     * 只会根据给定的查询条件更新部分资源。
     *
     * @param partialResource 要更新的部分资源。
     * @param query           查询条件。
     * @param <T>             资源类型，必须实现 HasIdAndAuditing 接口。
     * @return 更新是否成功的 Mono。
     * @see #update(HasIdAndAuditing, Query) 查看更多更新方法。
     */
    public <T extends HasIdAndAuditing> Mono<Boolean> updatePurely(T partialResource, Query query) {
        return Mono.just(convertToUpdate(partialResource))
                .flatMap(updateData -> reactiveMongoTemplate.updateFirst(query, updateData, partialResource.getClass()))
                .map(updateResult -> updateResult.getModifiedCount() > 0);
    }

    /**
     * reactiveMongoTemplate#upsert 方法未被使用，因为无法在此处设置 createdAt/createdBy/updatedAt/updatedBy 参数。
     *
     * @param newResource     要插入或更新的资源对象。
     * @param uniqueKeyName   唯一键名。
     * @param uniqueKeyValue 唯一键值。
     * @param <T>             资源类型，必须实现 HasIdAndAuditing 接口。
     * @return 返回 Mono，表示异步操作的结果，包含插入或更新后的资源对象。
     */
    @SuppressWarnings("unchecked")
    public <T extends HasIdAndAuditing> Mono<T> upsertWithAuditingParams(T newResource, String uniqueKeyName, String uniqueKeyValue) {
        Query query = new Query(Criteria.where(uniqueKeyName).is(uniqueKeyValue));
        return reactiveMongoTemplate.findOne(query, (Class<T>) newResource.getClass())
                .flatMap(existingResource -> {
                    newResource.setId(existingResource.getId());
                    newResource.setCreatedAt(existingResource.getCreatedAt());
                    newResource.setCreatedBy(existingResource.getCreatedBy());
                    return reactiveMongoTemplate.save(newResource);
                })
                .switchIfEmpty(Mono.defer(() -> reactiveMongoTemplate.save(newResource)));
    }

    /**
     * reactiveMongoTemplate#upsert 方法未被使用，因为无法在此处设置 createdAt/createdBy/updatedAt/updatedBy 参数。
     *
     * @param newResource 要插入或更新的资源对象。
     * @param criteria    查询条件。
     * @param <T>         资源类型，必须实现 HasIdAndAuditing 接口。
     * @return 返回 Mono，表示异步操作的结果，包含插入或更新后的资源对象。
     */
    @SuppressWarnings("unchecked")
    public <T extends HasIdAndAuditing> Mono<T> upsertWithAuditingParams(T newResource, Criteria criteria) {
        return reactiveMongoTemplate.findOne(new Query(criteria), (Class<T>) newResource.getClass())
                .flatMap(existingResource -> {
                    newResource.setId(existingResource.getId());
                    newResource.setCreatedAt(existingResource.getCreatedAt());
                    newResource.setCreatedBy(existingResource.getCreatedBy());
                    return reactiveMongoTemplate.save(newResource);
                })
                .switchIfEmpty(Mono.defer(() -> reactiveMongoTemplate.save(newResource)));
    }

    /**
     * 用于不需要设置 createdAt/createdBy/updatedAt/updatedBy 参数的情况。
     *
     * @param newResource     要插入或更新的资源对象。
     * @param uniqueKeyName   唯一键的名称。
     * @param uniqueKeyValue  唯一键的值。
     * @param <T>             资源类型。
     * @return 返回 Mono，表示异步操作的结果，true 表示插入或更新成功，false 表示失败。
     */
    public <T> Mono<Boolean> upsert(T newResource, String uniqueKeyName, String uniqueKeyValue) {
        return upsert(newResource, Criteria.where(uniqueKeyName).is(uniqueKeyValue));
    }

    /**
     * 将给定的更新操作应用于满足指定条件的文档，如果不存在则插入新文档。
     *
     * @param update         要应用的更新操作。
     * @param uniqueKeyName  唯一键的名称。
     * @param uniqueKeyValue 唯一键的值。
     * @param collection     目标集合的类。
     * @return 返回 Mono，表示异步操作的结果，true 表示更新成功，false 表示失败。
     */
    public Mono<Boolean> upsert(Update update, String uniqueKeyName, String uniqueKeyValue, Class<?> collection) {
        return upsert(update, Criteria.where(uniqueKeyName).is(uniqueKeyValue), collection);
    }

    /**
     * 将给定的资源对象转换为更新操作，并将其应用于满足指定条件的文档，如果不存在则插入新文档。
     *
     * @param newResource 要插入或更新的资源对象。
     * @param criteria    查询条件。
     * @param <T>         资源类型。
     * @return 返回 Mono，表示异步操作的结果，true 表示更新成功，false 表示失败。
     */
    public <T> Mono<Boolean> upsert(T newResource, Criteria criteria) {
        Update update = convertToUpdate(newResource);
        return upsert(update, criteria, newResource.getClass());
    }

    /**
     * 将给定的更新操作应用于满足指定条件的文档，如果不存在则插入新文档。
     *
     * @param update     要应用的更新操作。
     * @param criteria   查询条件。
     * @param collection 目标集合的类。
     * @return 返回 Mono，表示异步操作的结果，true 表示更新成功，false 表示失败。
     */
    public Mono<Boolean> upsert(Update update, Criteria criteria, Class<?> collection) {
        return reactiveMongoTemplate.upsert(new Query(criteria), update, collection)
                .map(updateResult -> updateResult.getModifiedCount() > 0);
    }

    /**
     * 将给定的资源对象转换为更新操作。
     *
     * @param resource 要转换的资源对象。
     * @return 返回更新操作对象。
     */
    @SuppressWarnings("unchecked")
    private Update convertToUpdate(Object resource) {
        Update updateObj = new Update();
        BasicDBObject basicDBObject = new BasicDBObject();
        mongoConverter.write(resource, basicDBObject);
        Map<String, Object> updateMap = ((DBObject) basicDBObject).toMap();
        updateMap.forEach(updateObj::set);
        return updateObj;
    }

    /**
     * 批量更新部分资源对象。
     *
     * @param partialResourceWithIds 要批量更新的部分资源对象集合。
     * @param <T>                    资源对象类型。
     * @return 如果有更新操作执行，则返回true；否则返回false。
     */
    public <T extends HasIdAndAuditing> Mono<Boolean> bulkUpdate(Collection<PartialResourceWithId<T>> partialResourceWithIds) {
        if (CollectionUtils.isEmpty(partialResourceWithIds)) {
            return Mono.empty();
        }
        var operations = partialResourceWithIds.stream().map(partialResourceWithId -> {
            BasicDBObject doc = new BasicDBObject();
            mongoConverter.write(partialResourceWithId.partialResource, doc);
            var filter = new Document("_id", new ObjectId(partialResourceWithId.id));
            return new UpdateOneModel<Document>(filter, new Document("$set", doc), new UpdateOptions().upsert(false));
        }).toList();
        return reactiveMongoTemplate.getCollection(reactiveMongoTemplate.getCollectionName(partialResourceWithIds.iterator().next().partialResource.getClass()))
                .flatMap(collection -> Mono.from(collection.bulkWrite(operations)))
                .map(bulkWriteResult -> bulkWriteResult.getModifiedCount() > 0);
    }

    /**
     * 批量删除符合条件的文档。
     *
     * @param filters 删除操作的过滤条件集合。
     * @param tClass  要删除的文档类型。
     * @param <T>     文档类型。
     * @return 如果有删除操作执行，则返回true；否则返回false。
     */
    public <T extends HasIdAndAuditing> Mono<Boolean> bulkRemove(Collection<Document> filters, Class<T> tClass) {
        if (CollectionUtils.isEmpty(filters)) {
            return Mono.empty();
        }
        var operations = filters.stream().map(filter -> new DeleteOneModel<Document>(filter)).toList();
        return reactiveMongoTemplate.getCollection(reactiveMongoTemplate.getCollectionName(tClass))
                .flatMap(collection -> Mono.from(collection.bulkWrite(operations)))
                .map(bulkWriteResult -> bulkWriteResult.getDeletedCount() > 0);
    }

    /**
     * 用于批量操作的部分资源对象，包含资源对象和其对应的ID。
     *
     * @param <T> 资源对象类型。
     */
    public record PartialResourceWithId<T>(T partialResource, String id) {
    }

}
