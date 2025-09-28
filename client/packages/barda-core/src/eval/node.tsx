import { EvalMethods } from "./types/evalTypes";
import { evalPerfUtil } from "./utils/perfUtils";
import { WrapNode } from "./wrapNode";

export type NodeToValue<NodeT> = NodeT extends Node<infer ValueType> ? ValueType : never;
export type FetchInfo = {
  /**
   * 是否有任何依赖关系的节点正在执行查询
   */
  isFetching: boolean;

  /**
   * 是否所有依赖项的查询都执行过一次
   */
  ready: boolean;
};

/**
 * 不带可选键的keyof
 */
type NonOptionalKeys<T> = {
  [k in keyof T]-?: undefined extends T[k] ? never : k;
}[keyof T];

/**
 * T extends {[key: string]: Node<any> | undefined}
 */
export type RecordOptionalNodeToValue<T> = {
  [K in NonOptionalKeys<T>]: NodeToValue<T[K]>;
};

export interface FetchInfoOptions {
  ignoreManualDepReadyStatus?: boolean; // 忽略检查手动查询deps就绪状态
}

/**
 * 评估的基本结构
 */
export interface Node<T> {
  readonly type: string;

  /**
 * 计算评估结果
 * @param exposingNodes 其他依赖的节点
 */
  evaluate(exposingNodes?: Record<string, Node<unknown>>, methods?: EvalMethods): T;

  /**
 * 当前节点或其依赖项是否存在循环依赖
 * 此函数仅在调用 evaluate() 之后可用
 */
  hasCycle(): boolean;

  /**
 * 仅在调用 evaluate 之后可用
 */
  dependNames(): string[];

  dependValues(): Record<string, unknown>;

  /**
 * 过滤真实的依赖项，以提高评估效率
 * @warn
 * 结果包括直接依赖项和依赖项的依赖项。
 * 由于输入节点的依赖项不属于模块特性中的模块，节点名称可能会重复。
 *
 * FIXME: 这应该是一个受保护的方法。
 */
  filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;

  fetchInfo(exposingNodes: Record<string, Node<unknown>>, options?: FetchInfoOptions): FetchInfo;
}

export abstract class AbstractNode<T> implements Node<T> {
  readonly type: string = "abstract";
  evalCache: EvalCache<T> = {};

  constructor() { }

  /**
   * 对当前节点进行评估，并考虑依赖关系和缓存。
   * @param exposingNodes - 由外部暴露的节点，按节点ID映射。
   * @param methods - 自定义评估方法。
   * @returns 节点的评估结果。
   */
  evaluate(exposingNodes?: Record<string, Node<unknown>>, methods?: EvalMethods): T {
    return evalPerfUtil.perf(this, "eval", () => {
      exposingNodes = exposingNodes ?? {};
      const dependingNodeMap = this.filterNodes(exposingNodes);
      // 当前依赖节点映射与上次相同则使用缓存
      if (dependingNodeMapEquals(this.evalCache.dependingNodeMap, dependingNodeMap)) {
        return this.evalCache.value as T;
      }
      // 初始化循环检测字段
      this.evalCache.cyclic = false;
      const result = this.justEval(exposingNodes, methods);

      // 更新缓存
      this.evalCache.dependingNodeMap = dependingNodeMap;
      this.evalCache.value = result;
      // 检查子节点是否存在循环依赖
      if (!this.evalCache.cyclic) {
        this.evalCache.cyclic = this.getChildren().some((node) => node.hasCycle());
      }
      return result;
    });
  }

  hasCycle(): boolean {
    return this.evalCache.cyclic ?? false;
  }

  abstract getChildren(): Node<unknown>[];

  dependNames(): string[] {
    return Object.keys(this.dependValues());
  }

  abstract dependValues(): Record<string, unknown>;

  /**
   * 判断当前评估是否命中缓存
   * 
   * 该方法用于确定当前评估是否可以命中缓存，从而避免重复计算
   * 它通过比较当前评估所依赖的节点映射与缓存中的依赖节点映射是否一致来实现
   * 
   * @param exposingNodes 可选参数，暴露的节点映射，默认为空对象
   * @returns 返回一个布尔值，表示是否命中缓存
   */
  isHitEvalCache(exposingNodes?: Record<string, Node<unknown>>): boolean {
    // 如果没有提供暴露的节点映射，则默认为空对象
    exposingNodes = exposingNodes ?? {};
    // 使用提供的暴露的节点映射来过滤出依赖的节点映射
    const dependingNodeMap = this.filterNodes(exposingNodes);
    // 比较当前评估的依赖节点映射与缓存中的依赖节点映射是否相等，从而判断是否命中缓存
    return dependingNodeMapEquals(this.evalCache.dependingNodeMap, dependingNodeMap);
  }

  abstract filterNodes(
    exposingNodes: Record<string, Node<unknown>>
  ): Map<Node<unknown>, Set<string>>;

  /**
   * 不使用缓存进行评估
   */
  abstract justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): T;

  abstract fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
}

interface EvalCache<T> {
  dependingNodeMap?: Map<Node<unknown>, Set<string>>;
  value?: T;

  inEval?: boolean;
  cyclic?: boolean;
  inIsFetching?: boolean;
  inFilterNodes?: boolean;
}

/**
 * 将依赖节点映射中的 WrapNode 转换为实际节点。
 * 由于 WrapNode 在评估过程中动态构建，其引用始终在变化。
 */
function unWrapDependingNodeMap(depMap: Map<Node<unknown>, Set<string>>) {
  const nextMap = new Map<Node<unknown>, Set<string>>();
  depMap.forEach((p, n) => {
    if (n.type === "wrap") {
      nextMap.set((n as InstanceType<typeof WrapNode>).delegate, p);
    } else {
      nextMap.set(n, p);
    }
  });
  return nextMap;
}

function setEquals(s1: Set<string>, s2?: Set<string>) {
  return s2 !== undefined && s1.size === s2.size && Array.from(s2).every((v) => s1.has(v));
}

/**
 * 检查两个依赖节点映射是否相等
 * - 使用 "===" 检查 Node 的引用相等性
 * - 使用深度比较检查 string[] 的内容是否相等
 * 
 * @param dependingNodeMap1 第一个依赖节点映射
 * @param dependingNodeMap2 第二个依赖节点映射
 * @returns 返回两个依赖节点映射是否相等
 */
export function dependingNodeMapEquals(
  dependingNodeMap1: Map<Node<unknown>, Set<string>> | undefined,
  dependingNodeMap2: Map<Node<unknown>, Set<string>>
): boolean {
  if (!dependingNodeMap1 || dependingNodeMap1.size !== dependingNodeMap2.size) {
    return false;
  }
  const map1 = unWrapDependingNodeMap(dependingNodeMap1);
  const map2 = unWrapDependingNodeMap(dependingNodeMap2);
  let result = true;
  map2.forEach((paths, node) => {
    result = result && setEquals(paths, map1.get(node));
  });
  return result;
}
