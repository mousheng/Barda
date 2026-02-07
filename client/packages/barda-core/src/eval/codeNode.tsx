import _ from "lodash";
import { memoized } from "util/memoize";
import { FunctionNode, withFunction } from "./functionNode";
import { AbstractNode, FetchInfo, FetchInfoOptions, Node } from "./node";
import { fromRecord } from "./recordNode";
import { CodeType, EvalMethods } from "./types/evalTypes";
import { ValueAndMsg, ValueExtra } from "./types/valueAndMsg";
import { addDepend, addDepends } from "./utils/dependMap";
import { filterDepends, hasCycle } from "./utils/evaluate";
import { dependsErrorMessage, mergeNodesWithSameName, nodeIsRecord } from "./utils/nodeUtils";
import { string2Fn } from "./utils/string2Fn";

export interface CodeNodeOptions {
  codeType?: CodeType;

  // whether to support comp methods?
  evalWithMethods?: boolean;

  // 函数是否为异步函数
  isAsync?: boolean;
}

const IS_FETCHING_FIELD = "isFetching";
const LATEST_END_TIME_FIELD = "latestEndTime";
const TRIGGER_TYPE_FIELD = "triggerType";

/**
 * 用户输入节点
 *
 * @remarks
 * CodeNode 应解决循环依赖问题
 * 我们可以假设循环依赖仅由 CodeNode 引入
 *
 * FIXME(libin): 区分 Json CodeNode，因为 wrapContext 可能导致问题。
 */
export class CodeNode extends AbstractNode<ValueAndMsg<unknown>> {
  readonly type = "input";

  private readonly codeType?: CodeType;
  private readonly evalWithMethods: boolean;
  private readonly isAsync: boolean;
  private directDepends = new Map<Node<unknown>, Set<string>>();

  constructor(readonly unevaledValue: string, readonly options?: CodeNodeOptions) {
    super();
    this.codeType = options?.codeType;
    this.evalWithMethods = options?.evalWithMethods ?? true;
    this.isAsync = options?.isAsync ?? false;
  }

  // FIXME: optimize later
  private convertedValue(): string {
    if (this.codeType === "Function") {
      return `{{function(){${this.unevaledValue}}}}`;
    }
    return this.unevaledValue;
  }

  /**
   * 使用缓存机制过滤节点
   * 当前方法旨在通过缓存来优化节点的过滤过程，避免循环引用导致的问题
   * 
   * @param exposingNodes 一个包含字符串键和Node实例值的记录，表示待过滤的节点集合
   * @returns 返回一个Map对象，其中Node实例作为键，Set对象（包含字符串类型的路径）作为值，
   * 表示过滤后的节点及其相关路径
   */
  @memoized()
  override filterNodes(exposingNodes: Record<string, Node<unknown>>) {
    // 检查是否存在循环引用，如果是，则直接返回空Map
    if (!!this.evalCache.inFilterNodes) {
      return new Map<Node<unknown>, Set<string>>();
    }
    // 标记当前状态为正在过滤节点
    this.evalCache.inFilterNodes = true;
    try {
      // 首先过滤直接依赖项
      const filteredDepends = this.filterDirectDepends(exposingNodes);
      // log.log("unevaledValue: ", this.unevaledValue, "\nfilteredDepends:", filteredDepends);
      // 初始化结果Map，并添加直接依赖项
      const result = addDepends(new Map(), filteredDepends);
      // 迭代过滤后的依赖项，递归添加节点的过滤结果
      filteredDepends.forEach((paths, node) => {
        addDepends(result, node.filterNodes(exposingNodes));
      });

      // 为FetchCheck添加isFetching和latestEndTime节点
      // 从转换后的值中过滤出顶级依赖项
      const topDepends = filterDepends(this.convertedValue(), exposingNodes, 1);
      // 遍历顶级依赖项，为特定字段添加依赖
      topDepends.forEach((paths, depend) => {
        // 检查依赖项是否为记录类型节点
        if (nodeIsRecord(depend)) {
          // 为每个需要关注的字段生成依赖项
          for (const field of [IS_FETCHING_FIELD, LATEST_END_TIME_FIELD]) {
            const node = depend.children[field];
            // 如果节点存在，则添加到结果中
            if (node) {
              addDepend(
                result,
                node,
                Array.from(paths).map((p) => p + "." + field)
              );
            }
          }
        }
      });
      // 返回最终的过滤结果
      return result;
    } finally {
      // 重置标记，表示不再处于过滤节点的状态
      this.evalCache.inFilterNodes = false;
    }
  }

  /**
   * 该方法筛选出当前节点直接依赖的暴露节点
   * 使用 memoized 装饰器缓存方法的结果，以提高性能
   * 当 convertedValue 和 exposingNodes 不变时，避免重复计算
   */
  @memoized()
  private filterDirectDepends(exposingNodes: Record<string, Node<unknown>>) {
    return filterDepends(this.convertedValue(), exposingNodes);
  }

  /**
   * 评估当前节点的值，同时检测循环依赖并处理。
   * 
   * 此方法主要用于计算当前节点的值，它依赖于其他节点的值。
   * 如果存在循环依赖，会进行特殊处理，返回一个错误信息。
   * 
   * @param exposingNodes - 一个记录，键为节点名，值为节点对象。这些节点是当前评估节点所依赖的节点。
   * @param methods - 可选参数，用于评估的方法对象，如果指定，则使用这些方法进行评估。
   * @returns 返回一个 ValueAndMsg 对象，包含评估结果和可能的错误信息。
   */
  override justEval(
    exposingNodes: Record<string, Node<unknown>>,
    methods?: EvalMethods
  ): ValueAndMsg<unknown> {
    // log.log("justEval: ", this, "\nexposingNodes: ", exposingNodes);
    // 为了避免死循环，检测当前节点是否正在被评估
    if (!!this.evalCache.inEval) {
      // 发现循环评估，设置标记并返回空值
      this.evalCache.cyclic = true;
      return new ValueAndMsg<unknown>("");
    }
    // 标记当前节点正在被评估
    this.evalCache.inEval = true;
    try {
      // 筛选直接依赖的节点
      const dependingNodeMap = this.filterDirectDepends(exposingNodes);
      // 将筛选后的依赖节点赋值给直接依赖属性
      this.directDepends = dependingNodeMap;
      // 合并具有相同名称的节点
      const dependingNodes = mergeNodesWithSameName(dependingNodeMap);
      // 将未评估的值和相关配置转换为函数
      const fn = string2Fn(this.unevaledValue, this.codeType, this.evalWithMethods ? methods : {}, this.isAsync);
      // 使用依赖节点和转换后的函数创建评估节点
      const evalNode = withFunction(fromRecord(dependingNodes), fn);
      // 评估当前节点的值
      let valueAndMsg = evalNode.evaluate(exposingNodes);
      // 如果存在循环依赖，处理返回值和错误信息
      if (this.evalCache.cyclic) {
        valueAndMsg = new ValueAndMsg<unknown>(
          valueAndMsg.value,
          (valueAndMsg.msg ? valueAndMsg.msg + "\n" : "") + dependsErrorMessage(this),
          fixCyclic(valueAndMsg.extra, exposingNodes)
        );
      }
      return valueAndMsg;
    } finally {
      // 评估结束后，重置正在被评估的标记
      this.evalCache.inEval = false;
    }
  }

  /**
   * 重写getChildren方法，用于获取当前节点的所有子节点
   * 在这个特定的实现中，子节点实际上是直接依赖的键集合
   * 
   * @returns {Node<unknown>[]} 子节点的数组，如果不存在直接依赖，则返回空数组
   */
  override getChildren(): Node<unknown>[] {
    // 如果directDepends存在直接依赖，将其键转换为数组并返回
    if (this.directDepends) {
      return Array.from(this.directDepends.keys());
    }
    // 如果没有直接依赖，返回空数组
    return [];
  }

  /**
   * 重写dependValues方法
   * 该方法用于获取当前节点直接依赖的值
   * 通过遍历直接依赖的节点和其对应的路径，从评估缓存中获取值，并收集到一个对象中返回
   * 
   * @returns {Record<string, unknown>} 一个键值对对象，键为依赖的路径，值为对应节点的评估缓存值
   */
  override dependValues(): Record<string, unknown> {
    // 初始化一个空对象，用于收集所有依赖的值
    let ret: Record<string, unknown> = {};
    // 遍历当前节点的直接依赖项，包括节点和对应的路径
    this.directDepends.forEach((paths, node) => {
      // 检查依赖的节点是否为AbstractNode的实例
      if (node instanceof AbstractNode) {
        // 遍历当前节点的所有路径
        paths.forEach((path) => {
          // 将当前节点评估缓存的值，根据路径添加到返回对象中
          ret[path] = node.evalCache.value;
        });
      }
    });
    // 返回收集完成的依赖值对象
    return ret;
  }

  /**
   * 使用memoized装饰器优化的fetchInfo方法重写
   * 本方法旨在获取当前依赖关系中的 fetching 状态和准备状态
   * 它会评估当前依赖关系以及依赖于当前节点的其他节点的状态
   * 
   * @param exposingNodes 一个包含多个Node的记录，节点类型为<unknown>
   *                      这些节点可能影响当前节点的状态
   * @param options 可选的fetchInfo选项，包含一些特殊的忽略规则
   * @returns FetchInfo对象，包含isFetching和ready两个属性
   * 
   * 本方法首先检查是否存在循环依赖 fetching 状态
   * 如果当前节点正在 fetching，则直接返回相应的状态
   * 否则，评估直接依赖项和依赖于当前节点的节点的状态
   * 最终汇总这些状态并返回综合的 fetching 和 ready 状态
   */
  @memoized()
  override fetchInfo(
    exposingNodes: Record<string, Node<unknown>>,
    options?: FetchInfoOptions
  ): FetchInfo {
    // 检查是否存在循环依赖的 fetching 状态
    if (!!this.evalCache.inIsFetching) {
      return {
        isFetching: false,
        ready: true,
      };
    }
    // 标记当前节点为正在 fetching
    this.evalCache.inIsFetching = true;
    try {
      // 获取直接依赖项
      const topDepends = filterDepends(this.convertedValue(), exposingNodes, 1);

      // 初始化 fetching 和 ready 状态
      let isFetching = false;
      let ready = true;

      // 遍历直接依赖项，评估它们的状态
      topDepends.forEach((paths, depend) => {
        const value = depend.evaluate(exposingNodes) as any;
        // 如果根据选项忽略手动触发依赖的 ready 状态，则跳过当前依赖
        if (
          options?.ignoreManualDepReadyStatus &&
          _.has(value, TRIGGER_TYPE_FIELD) &&
          value.triggerType === "manual"
        ) {
          return;
        }

        // 如果依赖项正在 fetching，则更新 fetching 状态
        if (_.has(value, IS_FETCHING_FIELD)) {
          isFetching = isFetching || value.isFetching === true;
        }
        // 如果依赖项的最新结束时间未定义，则更新 ready 状态
        if (_.has(value, LATEST_END_TIME_FIELD)) {
          ready = ready && value.latestEndTime > 0;
        }
      });

      // 获取依赖于当前节点的其他节点
      const dependingNodeMap = this.filterNodes(exposingNodes);
      // 遍历这些节点，评估它们的 fetching 和 ready 状态
      dependingNodeMap.forEach((paths, depend) => {
        const fi = depend.fetchInfo(exposingNodes, options);
        // 综合当前节点和依赖节点的状态
        isFetching = isFetching || fi.isFetching;
        ready = ready && fi.ready;
      });

      // 返回综合的 fetching 和 ready 状态
      return {
        isFetching,
        ready: ready,
      };
    } finally {
      // 重置 fetching 标记
      this.evalCache.inIsFetching = false;
    }
  }
}

/**
 * 将未评估的值转换为FunctionNode对象
 * 
 * 此函数用于接收一个字符串形式的未评估值，并将其转换为一个FunctionNode对象
 * FunctionNode对象包含一个CodeNode，用于表示原始的未评估值，以及一个用于提取值的函数
 * 
 * @param unevaledValue 未评估的值，通常是一个字符串
 * @returns 返回一个FunctionNode对象，该对象包含CodeNode和值提取函数
 */
export function fromUnevaledValue(unevaledValue: string) {
  return new FunctionNode(new CodeNode(unevaledValue), (valueAndMsg) => valueAndMsg.value);
}

/**
 * 修复循环引用问题
 * 
 * 当extra参数中包含可能引起循环引用的值时，本函数将调整这些值以解决循环引用问题
 * 循环引用发生在exposingNodes中的节点互相引用形成一个循环的情况，这可能导致某些算法或逻辑无限循环
 * 该函数通过检查每个段（segment）是否成功来处理这个问题，如果一个段的成功标志为true且存在循环引用，
 * 则将其成功标志设置为false如此，可以避免因循环引用而导致的问题
 * 
 * @param extra 可能包含引起循环引用的数据的额外值信息如果为undefined，则不需要修复
 * @param exposingNodes 一个记录所有可能参与循环引用的节点的集合，通过其键值可以访问对应的节点
 * @returns 返回经过循环引用修复后的extra值如果输入为undefined，则可能返回undefined
 */
function fixCyclic(
  extra: ValueExtra | undefined,
  exposingNodes: Record<string, Node<unknown>>
): ValueExtra | undefined {
  // 遍历extra中所有段（如果存在），检查并调整成功标志以避免循环引用
  extra?.segments?.forEach((segment) => {
    // 根据是否有循环引用，更新段的成功标志
    if (segment.success) {
      segment.success = !hasCycle(segment.value, exposingNodes);
    }
  });
  // 返回经过修复的extra值
  return extra;
}
