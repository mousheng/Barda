package com.barda.domain.mongodb;

/**
 * 定义在 MongoDB 读取操作后执行的操作的接口。
 * 实现该接口的类可以提供在 MongoDB 读取操作后执行的自定义逻辑。
 */
public interface AfterMongodbRead {

    /**
     * 在 MongoDB 读取操作后执行的操作。
     *
     * @param context 包含 MongoDB 拦截器上下文的对象
     */
    void afterMongodbRead(MongodbInterceptorContext context);
}
