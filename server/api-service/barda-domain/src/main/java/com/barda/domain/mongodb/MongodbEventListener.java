package com.barda.domain.mongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.barda.domain.encryption.EncryptionService;
import com.barda.infra.mongo.MongoUpsertHelper;
import com.barda.sdk.event.BeforeSaveEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * MongoDB 事件监听器。
 * 该类继承了 AbstractMongoEventListener，并实现了在 MongoDB 写入和读取操作前后执行自定义逻辑的功能。
 */
@Slf4j
@Component
public class MongodbEventListener<E> extends AbstractMongoEventListener<E> {

    /**
     * 用于对敏感数据进行加密的服务。
     */
    @Autowired
    private EncryptionService encryptionService;

    /**
     * 在 MongoDB 写入操作前执行的操作。
     *
     * @param event 包含 MongoDB 写入操作前的事件
     */
    @Override
    public void onBeforeConvert(BeforeConvertEvent<E> event) {
        E source = event.getSource();

        if (source instanceof BeforeMongodbWrite beforeMongodbWrite) {
            beforeMongodbWrite.beforeMongodbWrite(new MongodbInterceptorContext(encryptionService));
        }
    }

    /**
     * 在 MongoDB 读取操作后执行的操作。
     *
     * @param event 包含 MongoDB 读取操作后的事件
     */
    @Override
    public void onAfterConvert(AfterConvertEvent<E> event) {
        E source = event.getSource();

        if (source instanceof AfterMongodbRead afterMongodbRead) {
            afterMongodbRead.afterMongodbRead(new MongodbInterceptorContext(encryptionService));
        }
    }

    /**
     * 仅用于 {@link MongoUpsertHelper}
     *
     * @param beforeSaveEvent 包含 MongoDB 保存操作前的事件
     */
    @EventListener
    public <T> void onBeforeSaveEvent(BeforeSaveEvent<T> beforeSaveEvent) {
        T source = beforeSaveEvent.source();

        if (source instanceof BeforeMongodbWrite beforeMongodbWrite) {
            beforeMongodbWrite.beforeMongodbWrite(new MongodbInterceptorContext(encryptionService));
        }
    }
}
