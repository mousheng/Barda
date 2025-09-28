package com.barda.infra.localcache;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import com.barda.sdk.destructor.DestructorUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 一个提供可重新加载的缓存的类。
 * 该类使用了单例的 ScheduledExecutorService 来定期重新加载缓存。
 *
 * @param <T> 缓存值的类型
 */
@Slf4j
public final class ReloadableCache<T> {

    private CacheValueMonoProvider<T> factory;
    private volatile T cachedValue;
    private String cacheName;

    private ReloadableCache() {
    }

    /**
     * 获取缓存的值，如果值为空，则返回默认值。
     *
     * @param defaultValue 要返回的默认值
     * @return 缓存的值或默认值
     */
    public T getCachedOrDefault(T defaultValue) {
        return firstNonNull(cachedValue, defaultValue);
    }

    /**
     * 获取一个 {@link Mono}，该 Mono 包含了缓存的值。
     * 如果值为空，则使用 {@link CacheValueMonoProvider} 获取值并将其添加到缓存中。
     *
     * @return 一个包含了缓存的值的 {@link Mono}
     */
    public Mono<T> getMonoValue() {
        if (cachedValue == null) {
            return factory.getValue() // 缓存击穿可能在此发生，但在此看来并不是一个大问题
                   .doOnNext(value -> cachedValue = value);
        }
        return Mono.just(cachedValue);
    }

    /**
     * 获取一个新的 {@link ReloadableCacheBuilder} 实例。
     *
     * @param <T> 缓存值的类型
     * @return 一个新的 {@link ReloadableCacheBuilder} 实例
     */
    @CheckReturnValue
    public static <T> ReloadableCacheBuilder<T> newBuilder() {
        return new ReloadableCacheBuilder<>();
    }

    /**
     * 一个用于构建 {@link ReloadableCache} 实例的构建器。
     *
     * @param <T> 缓存值的类型
     */
    public static class ReloadableCacheBuilder<T> {

        private Duration interval;
        private CacheValueMonoProvider<T> factory;
        private String cacheName;

        /**
         * 设置重新加载缓存的间隔。
         *
         * @param interval 间隔
         * @return 构建器本身
         */
        @CheckReturnValue
        public ReloadableCacheBuilder<T> setInterval(Duration interval) {
            this.interval = interval;
            return this;
        }

        /**
         * 设置用于获取缓存值的 {@link CacheValueMonoProvider}。
         *
         * @param factory 工厂
         * @return 构建器本身
         */
        @CheckReturnValue
        public ReloadableCacheBuilder<T> setFactory(CacheValueMonoProvider<T> factory) {
            this.factory = factory;
            return this;
        }

        /**
         * 设置缓存的名称。
         *
         * @param name 名称
         * @return 构建器本身
         */
        @CheckReturnValue
        public ReloadableCacheBuilder<T> setName(String name) {
            this.cacheName = name;
            return this;
        }

        /**
         * 构建一个新的 {@link ReloadableCache} 实例。
         *
         * @return 一个新的 {@link ReloadableCache} 实例
         */
        @CheckReturnValue
        public ReloadableCache<T> build() {
            ensureParams();
            ReloadableCache<T> cache = new ReloadableCache<>();
            cache.factory = this.factory;
            cache.cacheName = this.cacheName;
            startScheduledReloadTask(cache);
            return cache;
        }

        @SuppressWarnings("UnstableApiUsage")
        private void startScheduledReloadTask(ReloadableCache<T> cache) {
            ScheduledExecutorService scheduledExecutor = newSingleThreadScheduledExecutor();
            scheduledExecutor.scheduleAtFixedRate(() -> {
                log.debug("{} scheduled reload...", cacheName);
                try {
                    cache.cachedValue = factory.getValue().block();
                } catch (Exception e) {
                    // 在出错的情况下，不更新值
                    log.error("scheduled load error", e);
                }
            }, 0, interval.toMillis(), TimeUnit.MILLISECONDS);

            DestructorUtil.register(() -> MoreExecutors.shutdownAndAwaitTermination(scheduledExecutor, Duration.ofSeconds(10)),
                    "shutdown and await reload task executor termination.");
        }

        private void ensureParams() {
            Preconditions.checkNotNull(factory);
            Preconditions.checkNotNull(interval);
        }
    }

    /**
     * 一个用于提供缓存值的 {@link Mono} 的函数式接口。
     *
     * @param <T> 缓存值的类型
     */
    @FunctionalInterface
    public interface CacheValueMonoProvider<T> {
        @Nonnull
        Mono<T> getValue();
    }
}
