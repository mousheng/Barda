package com.barda.domain.asset.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.barda.sdk.models.HasIdAndAuditing;

/**
 * Asset类表示具有特定内容类型和数据的数字资产。
 * 继承自HasIdAndAuditing，表示一个具有ID和审计功能的资产。
 */
@Document
public class Asset extends HasIdAndAuditing {

    /**
     * 资产的MIME类型。
     */
    private final String contentType;

    /**
     * 资产数据，以字节数组形式存储。
     */
    private final byte[] data;

    /**
     * 私有构造函数，用于创建Asset实例。
     *
     * @param contentType 资产的MIME类型
     * @param data        资产数据
     */
    @JsonCreator
    private Asset(String contentType, byte[] data) {
        this.contentType = contentType;
        this.data = data;
    }

    /**
     * 静态工厂方法，用于从MediaType和字节数组创建Asset实例。
     *
     * @param mediaType 资产的MediaType
     * @param data      资产数据
     * @return 新创建的Asset实例
     */
    public static Asset from(MediaType mediaType, byte[] data) {
        return new Asset(mediaType == null ? null : mediaType.toString(), data);
    }

    /**
     * 获取资产的MIME类型。
     *
     * @return 资产的MIME类型
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 获取资产数据。
     *
     * @return 资产数据，以字节数组形式返回
     */
    public byte[] getData() {
        return data;
    }
}
