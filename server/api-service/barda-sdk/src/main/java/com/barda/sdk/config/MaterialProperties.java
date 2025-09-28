package com.barda.sdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * MaterialProperties类用于绑定配置属性前缀为"material"的属性。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "material")
public class MaterialProperties {

    /**
     * MongodbGridFs的配置属性。
     */
    private Mongodb mongodbGridFs = new Mongodb();

    /**
     * Mongodb类用于存储MongoDB的GridFS配置。
     */
    @Data
    public static class Mongodb {
        /**
         * GridFS存储桶名称。
         */
        private String bucketName;
    }
}
