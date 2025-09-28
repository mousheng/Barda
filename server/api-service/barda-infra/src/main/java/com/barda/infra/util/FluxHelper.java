package com.barda.infra.util;

import org.springframework.data.domain.Pageable;

import reactor.core.publisher.Flux;

/**
 * Flux助手类。
 * 它提供静态方法来处理和转换Flux流，并实现分页功能。
 */
public class FluxHelper {

    /**
     * 获取所有分页数据。
     *
     * @param pageFetcher 页面获取器
     * @param first       初始页面
     * @param <T>         元素类型
     * @return 所有分页数据
     */
    public static <T> Flux<T> getAllPageByPage(PageFetcher<T> pageFetcher, Pageable first) {
        // 获取初始页面数据并缓存
        Flux<T> currentFluxPage = pageFetcher.fetch(first).cache();
        // 递归获取所有分页数据
        return currentFluxPage.hasElements()
               .flatMapMany(hasElement -> {
                    if (hasElement) {
                        // 如果有下一页，则拼接下一页数据
                        return currentFluxPage.concatWith(getAllPageByPage(pageFetcher, first.next()));
                    }
                    // 如果没有下一页，则返回当前页数据
                    return currentFluxPage;
                });
    }

    /**
     * 页面获取器接口。
     * 它定义了获取分页数据的方法。
     *
     * @param <T> 元素类型
     */
    public interface PageFetcher<T> {

        /**
         * 获取指定页面的数据。
         *
         * @param pageable 页面信息
         * @return 指定页面的数据
         */
        Flux<T> fetch(Pageable pageable);
    }
}
