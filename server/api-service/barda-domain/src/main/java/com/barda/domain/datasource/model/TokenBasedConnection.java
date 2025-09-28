package com.barda.domain.datasource.model;

import com.barda.sdk.models.HasIdAndAuditing;
import com.barda.sdk.models.TokenBasedConnectionDetail;

import lombok.Getter;
import lombok.Setter;

/**
 * 基于令牌的插件需要保存用户令牌以供将来重用。
 * 该类表示基于令牌的连接，继承了 HasIdAndAuditing 类。
 * 它使用了 Lombok 的 @Getter 和 @Setter 注解来自动生成 getter 和 setter 方法。
 */
@Getter
@Setter
public class TokenBasedConnection extends HasIdAndAuditing {

    /**
     * 该属性存储数据源的 ID。
     */
    private String datasourceId;

    /**
     * 该属性存储基于令牌的连接的详细信息。
     */
    private TokenBasedConnectionDetail tokenDetail;

    /**
     * 检查该基于令牌的连接是否已过期。
     *
     * @return true 如果该连接已过期，false 否则
     */
    public boolean isStale() {
        return tokenDetail.isStale();
    }
}
