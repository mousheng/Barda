import log from "loglevel";

export const CACHE_PREFIX = "__cache__";

/**
 * 一个用于缓存函数结果的装饰器，忽略参数。
 *
 * @remarks
 * 缓存存储在 `__cache__xxx` 字段中。
 * `ObjectUtils.setFields` 不会保存此缓存。
 *
 */
export function memo(target: any, propertyKey: string, descriptor: PropertyDescriptor) {
  const originalMethod = descriptor.value;
  const cachePropertyKey = CACHE_PREFIX + propertyKey;
  descriptor.value = function (...args: any[]) {
    const thisObj = this as any;
    if (!thisObj[cachePropertyKey]) {
      // 将结果放入数组中，以表示 `undefined`
      thisObj[cachePropertyKey] = [originalMethod.apply(this, args)];
    }
    return thisObj[cachePropertyKey][0];
  };
}

/**
 * 用于分析和记录函数执行性能的回调函数。
 *
 * @param id 用于标识函数的唯一标识符。
 * @param phase 标识函数执行的阶段，可以是 "mount"、"update" 或 "nested-update"。
 * @param actualDuration 实际执行函数所花费的时间（以毫秒为单位）。
 * @param baseDuration 基准执行函数所花费的时间（以毫秒为单位）。
 * @param startTime 函数执行开始的时间（以毫秒为单位）。
 * @param commitTime 函数执行结束的时间（以毫秒为单位）。
 *
 * @remarks
 * 如果 `actualDuration` 大于 20 毫秒，将使用 `log.warn` 记录警告信息。
 *
 */
export const profilerCallback = (
  id: string,
  phase: "mount" | "update" | "nested-update",
  actualDuration: number,
  baseDuration: number,
  startTime: number,
  commitTime: number
) => {
  if (actualDuration > 20) {
    log.warn(id, phase, actualDuration, baseDuration, startTime, commitTime);
  }
};
