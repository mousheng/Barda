package com.barda.sdk.models;

/**
 * 一个表示带有版本信息的模型的接口。
 * 它包含一个默认方法version()，返回模型的默认版本号"0.0.1"。
 */
public interface VersionedModel {

    /**
     * 获取模型的版本号。
     *
     * @return 模型的版本号，默认为"0.0.1"
     */
    default String version() {
        return "0.0.1";
    }
}
