package com.barda.domain.mongodb;

/**
 * 定义在 MongoDB 写入操作前执行的操作的接口。
 * 实现该接口的类可以提供在 MongoDB 写入操作前执行的自定义逻辑。
 */
public interface BeforeMongodbWrite {

    /**
     * 在 MongoDB 写入操作前执行的操作。
     *
     * @param context 包含 MongoDB 拦截器上下文的对象
     */
    void beforeMongodbWrite(MongodbInterceptorContext context);
}
