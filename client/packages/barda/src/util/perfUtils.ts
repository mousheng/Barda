import { CALM_DOWN_TIMEOUT } from "constants/perf";
import log from "loglevel";

// 是否打印性能统计信息
const SHOW_COST_INFO = false;

class StopWatch {
  startTime: number;
  key: string;

  constructor(key: string) {
    this.key = key;
    this.startTime = performance.now();
  }

  end() {
    addPerfMetric(this.key, performance.now() - this.startTime);
  }
}

class Counter {
  count: number = 0;
  total: number = 0;

  toString() {
    return `运行次数： ${this.count}, 运行总时长：${this.total}, 平均时长：${this.total / this.count}`;
  }
}

let records: Record<string, Counter> = {};

function clearAndOutput() {
  Object.keys(records).forEach((key) => {
    log.log(`函数名: ${key}, ${records[key]}`);
  });
  records = {};
}

let inited = false;

function init() {
  if (!inited) {
    setInterval(clearAndOutput, 2000);
  }
  inited = true;
}

function addPerfMetric(key: string, costMs: number): void {
  init();
  if (!records[key]) {
    records[key] = new Counter();
  }
  records[key].count += 1;
  records[key].total += costMs;
}

/**
 * 用法示例：
 * const stopWatch = startStopWatch("测试示例");
 * stopWatch.end();
 */
export function startStopWatch(key: string) {
  return new StopWatch(key);
}

/**
 * 作为函数装饰器使用，每两秒打印一次性能统计信息
 * 用法：
 * @perfMethod
 * selectedComps() {
 *   业务代码
 * };
 */
export function perfMethod(target: any, propertyKey: string, descriptor: PropertyDescriptor) {
  const originalMethod = descriptor.value;
  descriptor.value = function (...args: any[]) {
    const stopWatch = startStopWatch(propertyKey);
    const result = originalMethod.apply(this, args);
    stopWatch.end();
    return result;
  };
}


export function statPerf<T>(logstr: string, fn: () => T): T {
  if (!SHOW_COST_INFO) {
    return fn();
  }
  const stopWatch = startStopWatch(logstr);
  const result = fn();
  stopWatch.end();
  return result;
}

/**
 * 打印输入函数的运行时间
 * 使用时需要打开上面的开关（SHOW_COST_INFO）
 * 使用示例：
 * showCost("测试示例", () => {
 *   业务代码
 * });
 * 
 * @param logstr 日志标识字符串
 * @param fn 要执行的函数
 * @param printInterval 间隔打印，如果是1则每次打印
 */
export function showCost<T>(logstr: string, fn: () => T, printInterval: number = 10): T {
  if (!SHOW_COST_INFO) {
    return fn();
  }
  const startTime = performance.now();
  const result = fn();
  const costTime = performance.now() - startTime;

  if (!records[logstr]) {
    records[logstr] = new Counter();
  }
  const counter = records[logstr];
  counter.count += 1;
  counter.total += costTime;

  if (counter.count % printInterval === 0) {
    console.info(`标识: ${logstr} \t本次耗时: ${costTime}ms, \t运行次数: ${counter.count}, \t运行总时长: ${counter.total}ms, \t平均时长：${(counter.total / counter.count).toFixed(2)}ms`);
  }

  return result;
}

export function cost(fn: (...args: any[]) => any): number {
  const start = performance.now();
  fn();
  return performance.now() - start;
}

export const MarkStart = "start";
export const MarkAppEditorFirstRender = "app-editor-first-render";
export const MarkAppEditorMounted = "app-editor-mounted";
export const MarkAppDSLLoaded = "app-dsl-loaded";
export const MarkAppInitialized = "app-initialized";
export const MarkAppCalmDown = "app-calm-down";

export const MeasureCalmDown = "app-calm-down-from-dsl-loaded";

export function perfMark(name: string) {
  if (performance.mark === undefined) {
    return;
  }
  return performance.mark(name);
}

export function perfMeasure(name: string, startMark: string, endMark: string, detail?: any) {
  if (
    performance.measure === undefined ||
    performance.getEntriesByName === undefined ||
    performance.getEntriesByName(startMark, "mark").length === 0 ||
    performance.getEntriesByName(endMark, "mark").length === 0
  ) {
    return;
  }
  return performance.measure(name, {
    start: startMark,
    end: endMark,
    detail,
  });
}

export function perfClear() {
  performance?.clearMarks();
  performance?.clearMeasures();
}

const markOffset: { [key: string]: number } = {
  [MarkAppCalmDown]: -CALM_DOWN_TIMEOUT,
};

const measureDurationOffset: { [key: string]: number } = {
  [MeasureCalmDown]: -CALM_DOWN_TIMEOUT,
};

interface PrintPerfParams {
  measure?: boolean;  // 是否打印测量结果
  mark?: boolean;     // 是否打印标记点
  format?: "table" | "json" | "prettyJSON";  // 输出格式
}

function printPerf(params?: PrintPerfParams) {
  const { measure = true, mark = true, format = "json" } = params || {};
  const data = [];
  if (mark) {
    const entries = performance.getEntriesByType("mark");
    for (const { name, startTime } of entries) {
      const offset = markOffset[name] || 0;
      data.push({
        type: "mark",
        name,
        startTime: startTime + offset,
      });
    }
  }

  if (measure) {
    const entries = performance.getEntriesByType("measure") as PerformanceMeasure[];
    for (const { name, duration, detail } of entries) {
      const offset = measureDurationOffset[name] || 0;
      data.push({
        type: "measure",
        name,
        duration: duration + offset,
        detail: format === "table" ? JSON.stringify(detail) : detail,
      });
    }
  }

  if (format === "table") {
    return log.info(data);
  }
  const out = format === "prettyJSON" ? JSON.stringify(data, null, 4) : JSON.stringify(data);
  log.log(out);
}

window.printPerf = printPerf;
