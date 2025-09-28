import { evalAndReduceWithExposing } from "comps/utils";
import {
  CALM_DOWN_TIMEOUT,
  CLEAR_ACTION_QUEUE_TIMEOUT,
  ENABLE_ACTION_PRIORITY,
} from "constants/perf";
import _ from "lodash";
import log from "loglevel";
import { CompAction, CompActionTypes, CompConstructor } from "barda-core";
import { useEffect, useMemo, useState } from "react";
import { PriorityQueue, Queue } from "typescript-collections";
import { JSONValue } from "util/jsonTypes";
import {
  MarkAppCalmDown,
  MarkAppDSLLoaded,
  MeasureCalmDown,
  perfMark,
  perfMeasure,
} from "util/perfUtils";
import { wrapWithPromiseHandling } from "util/promiseUtils";
import { cancelIdleCallback, requestIdleCallback } from "util/scheduleUtils";
import { PartialReduceContext, reduceInContext } from "./reduceContext";

interface ActionHandlerParams {
  action?: CompAction;
  reduceFn: (action: CompAction) => void;
}

/**
 * 支持异步模式的动作处理程序。
 * 发送 ASYNC 进入动作队列，发送 ASYNC_END 开始执行动作直到队列为空。
 * 非 ASYNC 动作将同步执行。
 */
export function actionHandlerGenerator() {
  const deferQueue: Queue<CompAction> = new Queue();
  const statsData = {
    total: 0,
    groups: 0,
  };

  const actionHandler = (params: ActionHandlerParams) => {
    const { action, reduceFn } = params;
    statsData.total += 1;

    function clearDeferQueue() {
      const size = deferQueue.size();
      if (size <= 0) {
        return;
      }
      // log.info("clear action queue:", size);
      statsData.groups += 1;
      let next;
      while ((next = deferQueue.dequeue())) {
        reduceFn(next);
      }
    }

    if (!action) {
      clearDeferQueue();
    } else if (action.priority === "defer" && ENABLE_ACTION_PRIORITY) {
      // Low priority action, enqueue, wait for subsequent high priority action or timeout processing
      deferQueue.enqueue(action);
    } else {
      // High priority action, clear the queue and process it immediately
      clearDeferQueue();
      reduceFn(action);
      statsData.groups += 1;
    }

    return deferQueue.size();
  };

  const stats = () => {
    return {
      ...statsData,
      ratio: ((1 - statsData.groups / statsData.total) * 100).toFixed(2) + "%",
    };
  };
  return [actionHandler, stats] as const;
}

// 按调用顺序减少嵌套的 dispatch，然后评估以更新 comp 一次。
// 注意：UPDATE_NODES_V2 动作中的 dispatch 在这里没有处理，因此需要像以前一样使用 setTimeout 进行异步操作。
export function nestDispatchHandlerGenerator() {
  let depth = 0;
  let seq = 0;
  const queue: PriorityQueue<{ action: CompAction; depth: number; seq: number }> =
    new PriorityQueue((a, b) => {
      // Depth first, the same depth is FIFO
      const diff = a.depth - b.depth;
      return diff !== 0 ? diff : b.seq - a.seq;
    });
  return (action: CompAction, reduceFn: (action: CompAction) => void) => {
    // Find the nested dispatch action, add it to the queue, and execute reduce later
    if (depth > 0) {
      ++seq;
      queue.enqueue({ action, depth, seq });
      return;
    }
    // start reduce the top-level action
    depth = 1;
    reduceFn(action);
    let next;
    while ((next = queue.dequeue())) {
      // Take the deepest action processing from the queue to ensure the original call order
      depth = next.depth + 1;
      reduceFn(next.action);
    }
    depth = 0;
    seq = 0;
  };
}

type CompContainerChangeHandler = (actions?: CompAction[]) => void;

export interface GetContainerParams<T extends CompConstructor> {
  Comp: T;
  initialValue?: JSONValue;
  reduceContext?: PartialReduceContext;
  isReady?: boolean;
  initHandler?: (comp: InstanceType<T>) => Promise<InstanceType<T>>;

  /**
 * 预处理所有动作
 * 如果返回 false，动作将被丢弃
 */
  actionPreInterceptor?: (action: CompAction) => boolean;
}

/**
 * 通过组件类获取一个组件实例，因为实例会变化，所以将其放在一个容器中。
 * 注意：保持此代码不被 React 管理
 */
export function getCompContainer<T extends CompConstructor>(params: GetContainerParams<T>) {
  const {
    Comp,
    initialValue,
    initHandler,
    actionPreInterceptor,
    reduceContext,
    isReady = true,
  } = params;

  if (!initialValue || !isReady) {
    return null;
  }
  const [actionHandler, stats] = actionHandlerGenerator();
  const nestDispatchHandler = nestDispatchHandlerGenerator();

  class CompContainer {
    comp: InstanceType<T>;
    /**
    * 变更处理器 将在容器初始化后才会被触发
    */
    initialized: boolean = false;
    initializing: boolean = false;

    private changeListeners: CompContainerChangeHandler[] = [];

    constructor() {
      this.dispatch = this.dispatch.bind(this);
      this.comp = new Comp({
        dispatch: this.dispatch,
        value: initialValue,
      }) as InstanceType<T>;
    }

    async init() {
      if (this.initialized || this.initializing) {
        return this.comp;
      }
      this.initializing = true;
      let comp = this.comp;
      if (initHandler) {
        comp = await initHandler(comp);
      }
      this.comp = evalAndReduceWithExposing(comp);
      this.initialized = true;
      this.initializing = false;
      return this.comp;
    }

    clearQueueTimerHandle = 0;
    appCalmDownTimerHandle = 0;
    appCalmDowned = false;

    dispatch(action?: CompAction) {
      if (!this.initialized) {
        throw new Error("comp container is not initialized");
      }
      if (action && actionPreInterceptor?.(action) === false) {
        log.info("action dropped because of preInterceptor return false", action);
        return;
      }
      // action 不一定是 JSON 对象，无法序列化，稍后再查看如何记录日志。
      if (action?.type !== CompActionTypes.UPDATE_NODES_V2) {
        // 打印日志
        // log.log(`receive action ${action?.type} ${JSON.stringify(action, null, 2)}`);
      }
      let tmpComp = this.comp;
      // 所有由这个 dispatch 处理的动作
      const actions: CompAction[] = [];
      // 1ms
      const reduceFn = wrapWithPromiseHandling((act: CompAction) => {
        let action = act;
        if (reduceContext && reduceContext.readOnly && action.editDSL) {
          log.error("editDSL should be false in view mode, action: ", action);
          action = { ...action, editDSL: false };
        }

        // console.info("~~ action: ", action);
        tmpComp = reduceContext
          ? reduceInContext(reduceContext, () => tmpComp.reduce(action))
          : tmpComp.reduce(action);
        actions.push(action);

        // record the time of the first calm down
        if (!this.appCalmDowned) {
          window.clearTimeout(this.appCalmDownTimerHandle);
          this.appCalmDownTimerHandle = window.setTimeout(() => {
            this.appCalmDowned = true;
            perfMark(MarkAppCalmDown);
            perfMeasure(MeasureCalmDown, MarkAppDSLLoaded, MarkAppCalmDown, stats());
            log.info("~~~ CALM DOWN ~~~");
          }, CALM_DOWN_TIMEOUT);
        }
      });

      // 嵌套处理在异步处理之后进行，嵌套的异步操作也可以按正确的顺序处理
      const size = actionHandler({ action, reduceFn: (act) => nestDispatchHandler(act, reduceFn) })
      // const size = showCost("actionHandler", () =>
      //   actionHandler({ action, reduceFn: (act) => nestDispatchHandler(act, reduceFn) })
      // );

      // if (!_.isEmpty(actions)) console.info("~~ actions: ", actions);
      this.setComp(tmpComp, actions);

      if (size > 0) {
        cancelIdleCallback(this.clearQueueTimerHandle);
        this.clearQueueTimerHandle = requestIdleCallback(() => this.dispatch(), {
          timeout: CLEAR_ACTION_QUEUE_TIMEOUT,
        });
      }
    }

    /**
     * Add comp change event listener
     */
    addChangeListener(handler: CompContainerChangeHandler) {
      this.changeListeners.push(handler);
    }

    /**
     * Remove comp change event listener
     */
    removeChangeListener(handler: CompContainerChangeHandler) {
      this.changeListeners = this.changeListeners.filter((i) => i !== handler);
    }

    /**
     * 设置一个新的 comp 实例
     */
    setComp(tmpComp: InstanceType<T>, actions?: CompAction[]) {
      if (!this.initialized) {
        throw new Error("comp container is not initialized, setComp of container can't be called");
      }
      if (tmpComp === this.comp) {
        return;
      }
      // 1ms
      const evaluatedComp = evalAndReduceWithExposing(tmpComp);
      // const evaluatedComp = showCost("eval", () => evalAndReduceWithExposing(tmpComp));
      if (evaluatedComp === this.comp) {
        return;
      }
      this.comp = evaluatedComp;
      // FIXME: Check it out, why does it take 30ms to change the code editor, and only 1ms for others
      this.changeListeners.forEach((x) => x(actions))
      // showCost("setComp", () => this.changeListeners.forEach((x) => x(actions)));
    }
  }

  return new CompContainer();
}

export type CompContainer = ReturnType<typeof getCompContainer>;

/**
 * 需要记忆化，不是每次渲染都会改变引用
 */
export function useCompInstance<T extends CompConstructor>(
  params: GetContainerParams<T>,
  handlers?: CompContainerChangeHandler[]
) {
  const [comp, setComp] = useState<InstanceType<T> | null>(null);
  const container = useMemo(() => getCompContainer(params), [params]);

  if (container && !container.initialized) {
    container.init().then(setComp);
  }

  useEffect(() => {
    if (!container) {
      return () => { };
    }

    const finalHandlers = [...(handlers || []), () => setComp(container.comp)];
    finalHandlers.forEach((handler) => container.addChangeListener(handler));
    return () => {
      finalHandlers.forEach((handler) => container.removeChangeListener(handler));
    };
  }, [container, handlers]);

  return [comp, container] as const;
}