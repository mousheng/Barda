package com.barda.domain.datasource.model;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Getter;
import lombok.Setter;

/**
 * 基于令牌的插件需要保存用户令牌以便将来重用。
 * 该类继承了 HasIdAndAuditing 类，并使用了 Lombok 的 @Getter 和 @Setter 注解来自动生成 getter 和 setter 方法。
 * 该类的数据存储在 "tokenBasedConnection" 集合中。
 */
@Document(collection = "tokenBasedConnection")
@Getter
@Setter
public class TokenBasedConnectionDO extends HasIdAndAuditing {

    /**
     * 该属性存储数据源的 ID。
     */
    private String datasourceId;

    /**
     * 该属性存储基于令牌的连接的详细信息。
     * 它是一个 Map，其中键是令牌的类型，值是令牌的详细信息。
     */
    private Map<String, Object> tokenDetail;

}
