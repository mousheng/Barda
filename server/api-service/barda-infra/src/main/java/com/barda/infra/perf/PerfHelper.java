package com.barda.infra.perf;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

/**
 * 性能帮助类，提供计数器、仪表和计时器的封装。
 * 该类使用了 Spring Boot 的 MeterRegistry 进行指标的收集和上报。
 */
@Slf4j
@Component
public class PerfHelper {

    /**
     * Spring Boot 的 MeterRegistry 实例，用于指标的收集和上报。
     */
    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 计数器方法，使用事件的名称和标签来计数。
     *
     * @param event 事件
     * @param tags  标签
     */
    public void count(PerfEvent event, Iterable<Tag> tags) {
        count(event.perfKey(), tags);
    }

    /**
     * 计数器方法，使用事件的名称、标签和指定计数来计数。
     *
     * @param event 事件
     * @param tags  标签
     * @param count 计数
     */
    public void count(PerfEvent event, Iterable<Tag> tags, int count) {
        count(event.perfKey(), tags, count);
    }

    /**
     * 私有计数器方法，使用名称和标签来计数。
     *
     * @param name 名称
     * @param tags 标签
     */
    private void count(String name, Iterable<Tag> tags) {
        count(name, tags, 1);
    }

    /**
     * 私有计数器方法，使用名称、标签和指定计数来计数。
     *
     * @param name  名称
     * @param tags  标签
     * @param count 计数
     */
    private void count(String name, Iterable<Tag> tags, double count) {
        try {
            Counter counter = meterRegistry.counter(name, tags);
            counter.increment(count);
            counter.count();
        } catch (Exception e) {
            log.warn("计数器记录出错。名称: {}, 标签: {}, 数量: {}, 错误: {}", name, tags, count, e.getMessage());
        }
    }

    /**
     * 安全地记录仪表值。
     *
     * @param event            事件
     * @param tags             标签
     * @param t                值
     * @param toDoubleFunction 值转换函数
     */
    public <T> void gaugeSafely(PerfEvent event, Iterable<Tag> tags, T t, ToDoubleFunction<T> toDoubleFunction) {
        try {
            meterRegistry.gauge(event.perfKey(), tags, t, toDoubleFunction);
        } catch (Exception e) {
            log.warn("仪表记录出错。事件: {}, 标签: {}, 错误: {}", event, tags, e.getMessage());
        }
    }

    /**
     * 记录整型仪表值。
     *
     * @param event  事件
     * @param tags   标签
     * @param number 整型值
     */
    public void gaugeInt(PerfEvent event, Iterable<Tag> tags, int number) {
        try {
            AtomicInteger gauge = meterRegistry.gauge(event.perfKey(), tags, new AtomicInteger(number));
            if (gauge != null) {
                gauge.set(number);
            }
        } catch (Exception e) {
            log.warn("仪表记录出错。事件: {}, 标签: {}, 错误: {}", event, tags, e.getMessage());
        }
    }

    /**
     * 记录 Runnable 任务的执行时间。
     *
     * @param name 任务名称
     * @param tags 标签
     * @param f    Runnable 任务
     */
    public void recordRunnableTime(String name, Iterable<Tag> tags, Runnable f) {
        Timer timer = Timer.builder(name)
                .tags(tags)
                .publishPercentiles(0.95)
                .register(meterRegistry);
        timer.record(f);
    }

    /**
     * 记录指定时长的任务执行时间。
     *
     * @param name 任务名称
     * @param tags 标签
     * @param cost 执行时长
     */
    public void recordTime(String name, Iterable<Tag> tags, Duration cost) {
        Timer timer = Timer.builder(name)
                .tags(tags)
                .publishPercentiles(0.95)
                .register(meterRegistry);
        timer.record(cost);
    }

    /**
     * 记录 Callable 任务的执行时间并返回任务的执行结果。
     *
     * @param name     任务名称
     * @param tags     标签
     * @param callable Callable 任务
     * @return 任务的执行结果
     * @throws Exception 任务执行出错
     */
    public <T> T recordCallableTime(String name, Iterable<Tag> tags, Callable<T> callable) throws Exception {
        Timer timer = Timer.builder(name)
                .tags(tags)
                .publishPercentiles(0.95)
                .register(meterRegistry);
        return timer.recordCallable(callable);
    }
}
