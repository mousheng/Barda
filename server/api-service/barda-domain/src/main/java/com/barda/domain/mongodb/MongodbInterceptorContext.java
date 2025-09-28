package com.barda.domain.mongodb;

import com.barda.domain.encryption.EncryptionService;

/**
 * MongoDB 拦截器上下文类。
 * 该类用于在 MongoDB 写入和读取操作前后传递所需的上下文信息。
 */
public record MongodbInterceptorContext(EncryptionService encryptionService) {
}
