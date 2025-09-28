package com.barda.sdk.models;

import java.util.List;

import com.barda.sdk.plugin.restapi.DataUtils.MultipartFormDataType;
import com.barda.sdk.plugin.restapi.MultipartFormData;

/**
 * 一个表示REST API请求正文中包含文件数据的数据类。
 * 它继承自Property类，并使用Lombok库的@Getter注解来自动生成getter方法。
 */
public class RestBodyFormFileData extends Property {

    /**
     * 包含的文件数据列表。
     */
    private List<MultipartFormData> fileData;

    /**
     * 构造函数，创建一个RestBodyFormFileData实例。
     *
     * @param key 属性的键
     * @param value 属性的值
     */
    public RestBodyFormFileData(String key, String value) {
        super(key, value);
    }

    /**
     * 构造函数，创建一个RestBodyFormFileData实例。
     *
     * @param key 属性的键
     * @param value 属性的值
     * @param type 属性的类型
     */
    public RestBodyFormFileData(String key, String value, String type) {
        super(key, value, type);
    }

    /**
     * 构造函数，创建一个RestBodyFormFileData实例，并包含文件数据。
     *
     * @param key 属性的键
     * @param fileData 包含的文件数据列表
     */
    public RestBodyFormFileData(String key, List<MultipartFormData> fileData) {
        super(key, null, MultipartFormDataType.FILE.name());
        this.fileData = fileData;
    }

    /**
     * 获取包含的文件数据列表。
     *
     * @return 包含的文件数据列表
     */
    public List<MultipartFormData> getFileData() {
        return fileData;
    }
}
