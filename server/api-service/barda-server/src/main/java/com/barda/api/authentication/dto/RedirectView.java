package com.barda.api.authentication.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 重定向视图类。
 * 该类使用 Lombok 的 {@link Builder} 和 {@link Getter} 注解来生成构建器和 getter 方法。
 */
@Builder
@Getter
public class RedirectView {

    /**
     * 重定向 URI。
     */
    private String redirectUri;
}
