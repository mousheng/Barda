/// <reference types="react" />
import * as react from 'react';
import { ReactNode } from 'react';
import * as react_jsx_runtime from 'react/jsx-runtime';

type EvalMethods = Record<string, Record<string, Function>>;
type CodeType = undefined | "JSON" | "Function" | "PureJSON";
type CodeFunction = (args?: Record<string, unknown>, runInHost?: boolean) => any;

type NodeToValue<NodeT> = NodeT extends Node<infer ValueType> ? ValueType : never;
type FetchInfo = {
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
type RecordOptionalNodeToValue<T> = {
    [K in NonOptionalKeys<T>]: NodeToValue<T[K]>;
};
interface FetchInfoOptions {
    ignoreManualDepReadyStatus?: boolean;
}
/**
 * 评估的基本结构
 */
interface Node<T> {
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
declare abstract class AbstractNode<T> implements Node<T> {
    readonly type: string;
    evalCache: EvalCache<T>;
    constructor();
    /**
     * 对当前节点进行评估，并考虑依赖关系和缓存。
     * @param exposingNodes - 由外部暴露的节点，按节点ID映射。
     * @param methods - 自定义评估方法。
     * @returns 节点的评估结果。
     */
    evaluate(exposingNodes?: Record<string, Node<unknown>>, methods?: EvalMethods): T;
    hasCycle(): boolean;
    abstract getChildren(): Node<unknown>[];
    dependNames(): string[];
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
    isHitEvalCache(exposingNodes?: Record<string, Node<unknown>>): boolean;
    abstract filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;
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
 * 检查两个依赖节点映射是否相等
 * - 使用 "===" 检查 Node 的引用相等性
 * - 使用深度比较检查 string[] 的内容是否相等
 *
 * @param dependingNodeMap1 第一个依赖节点映射
 * @param dependingNodeMap2 第二个依赖节点映射
 * @returns 返回两个依赖节点映射是否相等
 */
declare function dependingNodeMapEquals(dependingNodeMap1: Map<Node<unknown>, Set<string>> | undefined, dependingNodeMap2: Map<Node<unknown>, Set<string>>): boolean;

interface CachedValue<T> {
    value: T;
    isCached: boolean;
}
declare class CachedNode<T> extends AbstractNode<CachedValue<T>> {
    type: string;
    child: AbstractNode<T>;
    constructor(child: AbstractNode<T>);
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): CachedValue<T>;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
}
/**
 * 创建一个新节点，该节点包含两个输入节点。
 * - 如果 mainNode 永远未求值，则 (新节点).evaluate 等于 mainNode.evaluate
 * - 如果 mainNode 已求值，则 (新节点).evaluate 等于 minorNode.evaluate
 *
 * @remarks
 * 封装逻辑：2 个节点 -> CachedNode(mainNode) + minorNode -> RecordNode({main, minor}) -> FunctionNode
 *
 * @warn 不当使用可能导致意外行为，请小心。
 * @param mainNode 主节点
 * @param minorNode 次要节点
 * @returns 新节点
 */
declare function evalNodeOrMinor<T>(mainNode: AbstractNode<T>, minorNode: Node<T>): Node<T>;

/**
 * 返回一个新节点，以输入节点值作为函数的输入，对函数结果进行求值
 */
declare class FunctionNode<T, OutputType> extends AbstractNode<OutputType> {
    readonly child: Node<T>;
    readonly func: (params: T) => OutputType;
    readonly type = "function";
    constructor(child: Node<T>, func: (params: T) => OutputType);
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): OutputType;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>, options?: FetchInfoOptions): FetchInfo;
}
declare function withFunction<T, OutputType>(child: Node<T>, func: (params: T) => OutputType): FunctionNode<T, OutputType>;

type ValueExtra = {
    segments?: {
        value: string;
        success: boolean;
    }[];
};
/**
 * 值和消息的封装类。
 *
 * @template T 值的类型
 */
declare class ValueAndMsg<T> {
    /** 值 */
    value: T;
    /** 消息，如果存在的话 */
    msg?: string;
    /** 额外信息，如果存在的话 */
    extra?: ValueExtra;
    /** 值在求值和转换之前的中间值 */
    midValue?: any;
    /**
     * 构造函数
     *
     * @param value 值
     * @param msg 消息
     * @param extra 额外信息
     * @param midValue 中间值
     */
    constructor(value: T, msg?: string, extra?: ValueExtra, midValue?: any);
    /**
     * 检查是否存在消息
     *
     * @returns 如果存在消息返回 true，否则返回 false
     */
    hasError(): boolean;
    /**
     * 获取消息
     *
     * @param displayValueFn 用于将值转换为可读字符串的函数，默认为 toReadableString
     * @returns 如果存在消息返回消息，否则返回通过 displayValueFn 转换的值
     */
    getMsg(displayValueFn?: (value: T) => string): string;
}

interface CodeNodeOptions {
    codeType?: CodeType;
    evalWithMethods?: boolean;
}
/**
 * 用户输入节点
 *
 * @remarks
 * CodeNode 应解决循环依赖问题
 * 我们可以假设循环依赖仅由 CodeNode 引入
 *
 * FIXME(libin): 区分 Json CodeNode，因为 wrapContext 可能导致问题。
 */
declare class CodeNode extends AbstractNode<ValueAndMsg<unknown>> {
    readonly unevaledValue: string;
    readonly options?: CodeNodeOptions | undefined;
    readonly type = "input";
    private readonly codeType?;
    private readonly evalWithMethods;
    private directDepends;
    constructor(unevaledValue: string, options?: CodeNodeOptions | undefined);
    private convertedValue;
    /**
     * 使用缓存机制过滤节点
     * 当前方法旨在通过缓存来优化节点的过滤过程，避免循环引用导致的问题
     *
     * @param exposingNodes 一个包含字符串键和Node实例值的记录，表示待过滤的节点集合
     * @returns 返回一个Map对象，其中Node实例作为键，Set对象（包含字符串类型的路径）作为值，
     * 表示过滤后的节点及其相关路径
     */
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;
    /**
     * 该方法筛选出当前节点直接依赖的暴露节点
     * 使用 memoized 装饰器缓存方法的结果，以提高性能
     * 当 convertedValue 和 exposingNodes 不变时，避免重复计算
     */
    private filterDirectDepends;
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
    justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): ValueAndMsg<unknown>;
    /**
     * 重写getChildren方法，用于获取当前节点的所有子节点
     * 在这个特定的实现中，子节点实际上是直接依赖的键集合
     *
     * @returns {Node<unknown>[]} 子节点的数组，如果不存在直接依赖，则返回空数组
     */
    getChildren(): Node<unknown>[];
    /**
     * 重写dependValues方法
     * 该方法用于获取当前节点直接依赖的值
     * 通过遍历直接依赖的节点和其对应的路径，从评估缓存中获取值，并收集到一个对象中返回
     *
     * @returns {Record<string, unknown>} 一个键值对对象，键为依赖的路径，值为对应节点的评估缓存值
     */
    dependValues(): Record<string, unknown>;
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
    fetchInfo(exposingNodes: Record<string, Node<unknown>>, options?: FetchInfoOptions): FetchInfo;
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
declare function fromUnevaledValue(unevaledValue: string): FunctionNode<ValueAndMsg<unknown>, unknown>;

/**
 * 评估以获取FetchInfo或获取状态
 */
declare class FetchCheckNode extends AbstractNode<FetchInfo> {
    readonly child: Node<unknown>;
    readonly options?: FetchInfoOptions | undefined;
    readonly type = "fetchCheck";
    constructor(child: Node<unknown>, options?: FetchInfoOptions | undefined);
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;
    justEval(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
}
declare function isFetching(node: Node<unknown>): Node<FetchInfo>;

type RecordNodeToValue<T> = {
    [K in keyof T]: NodeToValue<T[K]>;
};
/**
 * 评估值是由子节点构造的记录
 */
declare class RecordNode<T extends Record<string, Node<unknown>>> extends AbstractNode<RecordNodeToValue<T>> {
    readonly children: T;
    readonly type = "record";
    constructor(children: T);
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): RecordNodeToValue<T>;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>, options?: FetchInfoOptions): {
        isFetching: boolean;
        ready: boolean;
    };
}
declare function fromRecord<T extends Record<string, Node<unknown>>>(record: T): RecordNode<T>;

/**
 * 直接提供数据
 */
declare class SimpleNode<T> extends AbstractNode<T> {
    readonly value: T;
    readonly type = "simple";
    constructor(value: T);
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;
    justEval(exposingNodes: Record<string, Node<unknown>>): T;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): {
        isFetching: boolean;
        ready: boolean;
    };
}
/**
 * 提供简单的值，不需要eval
 */
declare function fromValue<T>(value: T): SimpleNode<T>;
declare function fromValueWithCache<T>(value: T): SimpleNode<T>;

declare class WrapNode<T> extends AbstractNode<T> {
    readonly delegate: Node<T>;
    readonly moduleExposingNodes: Record<string, Node<unknown>>;
    readonly moduleExposingMethods?: EvalMethods | undefined;
    readonly inputNodes?: Record<string, string | Node<unknown>> | undefined;
    readonly type = "wrap";
    constructor(delegate: Node<T>, moduleExposingNodes: Record<string, Node<unknown>>, moduleExposingMethods?: EvalMethods | undefined, inputNodes?: Record<string, string | Node<unknown>> | undefined);
    private wrap;
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods: EvalMethods): T;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
}

type WrapContextFn<T> = (params?: Record<string, unknown>) => T;
declare function wrapContext<T>(node: Node<T>): Node<WrapContextFn<T>>;

/**
 * 通过在子节点中设置新的依赖节点来构建新节点
 */
declare class WrapContextNodeV2<T> extends AbstractNode<T> {
    readonly child: Node<T>;
    readonly paramNodes: Record<string, Node<unknown>>;
    readonly type = "wrapContextV2";
    constructor(child: Node<T>, paramNodes: Record<string, Node<unknown>>);
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, Set<string>>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): T;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
    private wrap;
}

/**
 * 创建一个转换包装器，它将一个函数作为参数，并返回一个新的函数，
 * 该函数将原始函数应用于输入值，并返回一个新的值和消息。
 *
 * @param transformFn - 要应用于输入值的函数。
 * @param defaultValue - 转换函数在发生错误时返回的默认值。
 * @returns 一个新的函数，该函数将原始函数应用于输入值，并返回一个新的值和消息。
 */
declare function transformWrapper<T>(transformFn: (value: unknown) => T, defaultValue?: T): (valueAndMsg: ValueAndMsg<unknown>) => ValueAndMsg<T>;

interface PerfInfo {
    obj: any;
    name: string;
    childrenPerfInfo: PerfInfo[];
    costMs: number;
    depth: number;
    info: Record<string, any>;
}
type Log = (key: string, log: any) => void;
declare class RecursivePerfUtil {
    root: symbol;
    record: PerfInfo;
    stack: number[];
    constructor();
    private initRecord;
    private getRecordByStack;
    log(info: Record<string, any>, key: string, log: any): void;
    perf<T>(obj: any, name: string, fn: (log: Log) => T): T;
    clear: () => void;
    print: (stack: number[], cost_ms_print_thr?: number) => void;
}
declare const evalPerfUtil: RecursivePerfUtil;

/**
 * 该函数将宽松的 JSON 文本字符串转换为严格的 JSON 字符串。
 *
 * @param text 要转换的宽松的 JSON 文本字符串
 * @param compact 布尔值，指示是否返回紧凑的 JSON 字符串。如果为 true，则返回紧凑的 JSON 字符串，否则返回格式化的 JSON 字符串。
 * @returns 严格的 JSON 字符串
 */
declare function relaxedJSONToJSON(text: string, compact: boolean): string;

/**
 * 判断给定的段落是否为动态段落
 *
 * 本函数通过应用正则表达式来检测输入的段落字符串是否符合动态段落的特定模式
 * 动态段落通常包含可以变化的部分，例如占位符或变量，这些可能在运行时被替换或填充
 *
 * @param segment 待检测的段落字符串
 * @returns 如果段落符合动态段落的正则表达式模式，则返回true；否则返回false
 */
declare function isDynamicSegment(segment: string): boolean;
/**
 * 将字符串分割为动态和静态部分
 * 动态部分以{{开头，}}结尾，可能包含嵌套的动态部分
 * 静态部分为动态部分之间的文本
 *
 * @param input 待处理的字符串
 * @returns 分割后的字符串数组，过滤掉空字符串
 */
declare function getDynamicStringSegments(input: string): string[];

declare function clearMockWindow(): void;
type SandboxScope = "function" | "expression";
interface SandBoxOption {
    /**
   * 禁用所有限制，如同在宿主环境中运行
   */
    disableLimit?: boolean;
    /**
   * 该沙箱工作的范围，将使用不同的黑名单
   */
    scope?: SandboxScope;
    /**
   * 当设置全局变量到沙箱时的处理器，仅在范围为函数时被调用
   */
    onSetGlobalVars?: (name: string) => void;
}
declare function evalScript(script: string, context: any, methods?: EvalMethods): any;
declare function evalFunc(functionBody: string, context: any, methods?: EvalMethods, options?: SandBoxOption, isAsync?: boolean): any;

/**
 * 在页面上评估并应用给定ID的样式规则
 *
 * 此函数通过接受一个ID和一个CSS字符串数组，生成相应的样式节点并将其添加到页面中
 * 它处理CSS规则的编译和序列化，跳过空的CSS字符串，使用预定义的中间件处理CSS，
 * 然后将编译后的CSS字符串赋给样式节点，从而在浏览器中生效
 *
 * @param id 样式的ID，用于生成唯一的样式节点ID和选择器
 * @param css CSS规则数组，每个规则作为一个数组元素
 */
declare function evalStyle(id: string, css: string[]): void;
/**
 * 清除内联样式评价函数
 * 该函数用于清除特定的内联样式，如果未指定ID，则清除所有匹配的数据源样式节点
 *
 * @param id 可选参数，指定要清除的样式的ID如果未提供，则清除所有通过eval方式注入的样式
 */
declare function clearStyleEval(id?: string): void;

declare class DefaultParser {
    readonly context: Record<string, unknown>;
    protected readonly segments: string[];
    private readonly valueAndMsgs;
    constructor(unevaledValue: string, context: Record<string, unknown>);
    parse(): ValueAndMsg<unknown>;
    parseObject(): unknown;
    evalDynamicSegment(segment: string): unknown;
}
declare class RelaxedJsonParser extends DefaultParser {
    constructor(unevaledValue: string, context: Record<string, unknown>);
    parseObject(): any;
    parseRelaxedJson(): any;
    evalIndexedObject(obj: any): any;
    evalIndexedStringToObject(indexedString: string): unknown;
    evalIndexedStringToString(indexedString: string): string;
    evalIndexedSnippet(snippet: string): unknown;
}
declare function evalFunctionResult(unevaledValue: string, context: Record<string, unknown>, methods?: EvalMethods): Promise<ValueAndMsg<unknown>>;

declare function nodeIsRecord(node: Node<unknown>): node is RecordNode<Record<string, Node<unknown>>>;

/**
 * 修改依赖项的名称
 *
 * 此函数用于在给定的字符串中重命名某个依赖项的名称它支持两种模式：函数模式和非函数模式
 * 在函数模式下，整个字符串作为函数体进行重命名；在非函数模式下，仅对字符串中的动态段进行重命名
 *
 * @param unevaledValue 未评估的字符串值，可能是函数体或包含动态段的字符串
 * @param oldName 需要被替换的原始依赖项名称
 * @param name 新的依赖项名称
 * @param isFunction 可选参数，指示是否将整个字符串作为函数体进行重命名，默认为false
 * @returns 修改后的字符串如果输入参数无效，则返回原始字符串
 */
declare function changeDependName(unevaledValue: string, oldName: string, name: string, isFunction?: boolean): string;

/**
 * 该函数返回一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后解析为默认值。
 *
 * @param promise - 要进行超时处理的原始 Promise。
 * @param timeout - 超时时间（以毫秒为单位）。
 * @param defaultValue - 超时后返回的默认值。
 * @param timeoutMessage - 超时时返回的错误消息（可选，默认为 "timeout"）。
 * @returns 一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后解析为默认值。
 */
declare function promiseWithDefaultOnTimeout<T>(promise: Promise<T>, timeout: number, defaultValue: T, timeoutMessage?: string): Promise<T>;
/**
 * 该函数返回一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后拒绝。
 *
 * @param promise - 要进行超时处理的原始 Promise。
 * @param timeout - 超时时间（以毫秒为单位）。
 * @param timeoutMessage - 超时时返回的错误消息（可选，默认为 "timeout"）。
 * @returns 一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后拒绝。
 */
declare function promiseWithTimeout<T>(promise: Promise<T>, timeout: number, timeoutMessage?: string): Promise<unknown>;

type JSONValue = string | number | boolean | JSONObject | JSONArray | null;
interface JSONObject {
    [x: string]: JSONValue | undefined;
}
type JSONArray = Array<JSONValue>;

type OptionalNodeType = Node<unknown> | undefined;
type DispatchType = (action: CompAction) => void;
/**
 * 组件接口，定义了组件的通用方法和属性
 *
 * @template ViewReturn 组件的返回值类型
 * @template DataType 组件的数据类型
 * @template NodeType 组件的节点类型
 */
interface Comp<ViewReturn = any, DataType extends JSONValue = JSONValue, NodeType extends OptionalNodeType = OptionalNodeType> {
    dispatch: DispatchType;
    getView(): ViewReturn;
    getPropertyView(): ReactNode;
    reduce(action: CompAction): this;
    node(): NodeType;
    toJsonValue(): DataType;
    /**
     * 更改当前组件的派发函数
     * 用于当组件在树结构中移动时
     *
     * @param dispatch 新的派发函数
     */
    changeDispatch(dispatch: DispatchType): this;
    changeValueAction(value: DataType): ChangeValueAction;
}
declare abstract class AbstractComp<ViewReturn = any, DataType extends JSONValue = JSONValue, NodeType extends OptionalNodeType = OptionalNodeType> implements Comp<ViewReturn, DataType, NodeType> {
    dispatch: DispatchType;
    constructor(params: CompParams);
    abstract getView(): ViewReturn;
    abstract getPropertyView(): ReactNode;
    abstract toJsonValue(): DataType;
    abstract reduce(_action: CompAction): this;
    abstract nodeWithoutCache(): NodeType;
    changeDispatch(dispatch: DispatchType): this;
    /**
   * 调用 `changeValueAction` 并保证类型安全。
   *
   * @param value 要修改的值
   */
    dispatchChangeValueAction(value: DataType): void;
    changeValueAction(value: DataType): ChangeValueAction;
    /**
   * 不要重写函数，重写nodeWithout函数
   * FIXME：如果更改了此对象，则不能更改节点引用
   */
    node(): NodeType;
}
type OptionalComp<T = any> = Comp<T> | undefined;
type CompConstructor<ViewReturn = any, DataType extends JSONValue = any, NodeType extends OptionalNodeType = OptionalNodeType> = new (params: CompParams<DataType>) => Comp<ViewReturn, DataType, NodeType>;
/**
 * 提取构造函数的泛型类型
 */
type ConstructorToView<T> = T extends CompConstructor<infer ViewReturn> ? ViewReturn : never;
type ConstructorToComp<T> = T extends new (params: CompParams<any>) => infer X ? X : never;
type ConstructorToDataType<T> = T extends new (params: CompParams<infer DataType>) => any ? DataType : never;
type ConstructorToNodeType<T> = ConstructorToComp<T> extends Comp<any, any, infer NodeType> ? NodeType : never;
type RecordConstructorToComp<T> = {
    [K in keyof T]: ConstructorToComp<T[K]>;
};
type RecordConstructorToView<T> = {
    [K in keyof T]: ConstructorToView<T[K]>;
};
interface CompParams<DataType extends JSONValue = JSONValue> {
    dispatch?: (action: CompAction) => void;
    value?: DataType;
}

declare enum CompActionTypes {
    CHANGE_VALUE = "CHANGE_VALUE",
    RENAME = "RENAME",
    MULTI_CHANGE = "MULTI_CHANGE",
    DELETE_COMP = "DELETE_COMP",
    REPLACE_COMP = "REPLACE_COMP",
    ONLY_EVAL = "NEED_EVAL",
    UPDATE_NODES_V2 = "UPDATE_NODES_V2",
    EXECUTE_QUERY = "EXECUTE_QUERY",
    TRIGGER_MODULE_EVENT = "TRIGGER_MODULE_EVENT",
    /**
   * 此操作可以通过名称将数据传递给组件
   */
    ROUTE_BY_NAME = "ROUTE_BY_NAME",
    /**
     * 执行带有上下文的操作。例如，表格列中的按钮应将 currentRow 作为上下文。
     * 注意：这是一个广播消息，可以通过继承机制来改进。
     */
    UPDATE_ACTION_CONTEXT = "UPDATE_ACTION_CONTEXT",
    /**
   * comp-specific action 应当不全局地放置。
   * 请统一使用 CUSTOM。
   */
    CUSTOM = "CUSTOM",
    /**
   * 在组件树结构中广播其他操作。
   * 用于封装 MultiBaseComp
   */
    BROADCAST = "BROADCAST"
}
type ExtraActionType = "layout" | "delete" | "add" | "modify" | "rename" | "recover" | "upgrade";
type ActionExtraInfo = {
    compInfos?: {
        compName: string;
        compType: string;
        type: ExtraActionType;
    }[];
};
type ActionPriority = "sync" | "defer";
interface ActionCommon {
    path: Array<string>;
    editDSL: boolean;
    skipHistory?: boolean;
    extraInfo?: ActionExtraInfo;
    priority?: ActionPriority;
}
interface CustomAction<DataType = JSONValue> extends ActionCommon {
    type: CompActionTypes.CUSTOM;
    value: DataType;
}
interface ChangeValueAction<DataType extends JSONValue = JSONValue> extends ActionCommon {
    type: CompActionTypes.CHANGE_VALUE;
    value: DataType;
}
interface ReplaceCompAction extends ActionCommon {
    type: CompActionTypes.REPLACE_COMP;
    compFactory: CompConstructor;
}
interface RenameAction extends ActionCommon {
    type: CompActionTypes.RENAME;
    oldName: string;
    name: string;
}
interface BroadcastAction<Action extends ActionCommon = ActionCommon> extends ActionCommon {
    type: CompActionTypes.BROADCAST;
    action: Action;
}
interface MultiChangeAction extends ActionCommon {
    type: CompActionTypes.MULTI_CHANGE;
    changes: Record<string, CompAction>;
}
interface SimpleCompAction extends ActionCommon {
    type: CompActionTypes.DELETE_COMP | CompActionTypes.ONLY_EVAL;
}
interface ExecuteQueryAction extends ActionCommon {
    type: CompActionTypes.EXECUTE_QUERY;
    queryName?: string;
    args?: Record<string, unknown>;
    afterExecFunc?: () => void;
}
interface TriggerModuleEventAction extends ActionCommon {
    type: CompActionTypes.TRIGGER_MODULE_EVENT;
    name: string;
}
interface RouteByNameAction extends ActionCommon {
    type: CompActionTypes.ROUTE_BY_NAME;
    name: string;
    action: CompAction<any>;
}
interface UpdateNodesV2Action extends ActionCommon {
    type: CompActionTypes.UPDATE_NODES_V2;
    value: any;
}
type ActionContextType = Record<string, unknown>;
interface UpdateActionContextAction extends ActionCommon {
    type: CompActionTypes.UPDATE_ACTION_CONTEXT;
    context: ActionContextType;
}
type CompAction<DataType extends JSONValue = JSONValue> = CustomAction<unknown> | ChangeValueAction<DataType> | BroadcastAction | RenameAction | ReplaceCompAction | MultiChangeAction | SimpleCompAction | ExecuteQueryAction | UpdateActionContextAction | RouteByNameAction | TriggerModuleEventAction | UpdateNodesV2Action;

/**
 * 创建一个自定义操作对象
 *
 * 此函数用于生成一个自定义操作对象，该对象表示一种非标准的、可由前端开发者自定义的UI操作
 * 它包含了执行操作所需的数据以及是否编辑DSL（领域特定语言）的标志
 *
 * @param value - 操作中传递的数据，类型由使用者自定义
 * @param editDSL - 表示操作是否涉及编辑DSL的布尔值
 * @returns 返回一个包含类型、路径、值和是否编辑DSL的自定义操作对象
 */
declare function customAction<DataType>(value: DataType, editDSL: boolean): CustomAction<DataType>;
/**
 * 创建一个更新操作上下文的广播操作
 *
 * @param context - 要更新的操作上下文
 * @returns 返回一个包含类型、路径、操作值和是否编辑DSL的广播操作对象
 *
 * @remarks
 * 该函数用于生成一个广播操作，该操作表示更新操作上下文
 * 它包含了执行操作所需的数据以及是否编辑DSL（领域特定语言）的标志
 *
 */
declare function updateActionContextAction(context: ActionContextType): BroadcastAction<UpdateActionContextAction>;
/**
 * 检查是否为当前的自定义操作
 *
 * 该函数用于检查一个操作是否为自定义操作，并保证类型安全
 * 它使用了泛型来保证类型安全，使用者需要保证传入的类型与 T 相同，否则可能引起 bug
 *
 * @param action - 要检查的操作
 * @param type - 自定义操作的类型
 * @returns 如果操作是自定义操作且类型匹配，则返回 true，否则返回 false
 */
declare function isMyCustomAction<T>(action: CompAction, type: string): action is CustomAction<T>;
/**
 * 检查是否为自定义操作
 *
 * 该函数用于检查一个操作是否为自定义操作，并保证类型安全
 * 它使用了泛型来保证类型安全，使用者需要保证传入的类型与 T 相同，否则可能引起 bug
 *
 * @param action - 要检查的操作
 * @param type - 自定义操作的类型
 * @returns 如果操作是自定义操作且类型匹配，则返回 true，否则返回 false
 */
declare function isCustomAction<T>(action: CompAction, type: string): action is CustomAction<T>;
/**
 * 执行查询的动作。
 * 精确地指向查询的路径路由。
 * 传递queryName时，RootComp将正确更改路径。
 *
 * @param props - 执行查询操作的属性。
 * @param props.args - 查询的参数。
 * @param props.afterExecFunc - 查询执行后要执行的函数。
 * @returns 返回一个ExecuteQueryAction对象。
 */
declare function executeQueryAction(props: {
    args?: Record<string, unknown>;
    afterExecFunc?: () => void;
}): ExecuteQueryAction;
/**
 * 触发模块事件的操作。
 *
 * @param name - 要触发的模块事件的名称。
 * @returns 返回一个TriggerModuleEventAction对象。
 */
declare function triggerModuleEventAction(name: string): TriggerModuleEventAction;
/**
 * 更改值操作,最好使用comp.dispatchChangeValueAction来保证类型安全
 *
 * @param value - 要更改的值。
 * @param editDSL - 一个布尔值，表示是否编辑DSL（领域特定语言）。
 * @returns 返回一个ChangeValueAction对象。
 */
declare function changeValueAction(value: JSONValue, editDSL: boolean): ChangeValueAction;
declare function isBroadcastAction<T extends CompAction>(action: CompAction, type: T["type"]): action is BroadcastAction<T>;
declare function renameAction(oldName: string, name: string): BroadcastAction<RenameAction>;
declare function routeByNameAction(name: string, action: CompAction<any>): RouteByNameAction;
declare function multiChangeAction(changes: Record<string, CompAction>): MultiChangeAction;
declare function deleteCompAction(): SimpleCompAction;
declare function replaceCompAction(compFactory: CompConstructor): ReplaceCompAction;
declare function onlyEvalAction(): SimpleCompAction;
declare function wrapChildAction(childName: string, action: CompAction): CompAction;
declare function isChildAction(action: CompAction): boolean;
declare function unwrapChildAction(action: CompAction): [string, CompAction];
declare function changeChildAction(childName: string, value: JSONValue, editDSL: boolean): CompAction;
declare function updateNodesV2Action(value: any): UpdateNodesV2Action;
/**
 * 包装操作的额外信息。
 *
 * @param action - 要包装的操作。
 * @param extraInfos - 要添加的额外信息。
 * @returns 返回一个包含额外信息的操作。
 */
declare function wrapActionExtraInfo<T extends CompAction>(action: T, extraInfos: ActionExtraInfo): T;
/**
 * 推迟执行操作。
 *
 * @param action - 要推迟的操作。
 * @returns 返回一个推迟执行的操作。
 */
declare function deferAction<T extends CompAction>(action: T): T;
/**
 * 更改操作的编辑DSL标志。
 *
 * @param action - 要更改的操作。
 * @param editDSL - 一个布尔值，表示是否编辑DSL（领域特定语言）。
 * @returns 返回一个更改了编辑DSL标志的操作。
 */
declare function changeEditDSLAction<T extends CompAction>(action: T, editDSL: boolean): T;

/**
 * 多功能基础组件构造函数，其中包含实现了抽象函数
 */
type MultiCompConstructor = new (params: CompParams<any>) => MultiBaseComp<any, any, any> & Comp<any, any, any>;
/**
 * 将调度程序包装为子调度程序
 *
 * @param dispatch 输入调度程序
 * @param childName 子调度程序的键
 * @returns 包含子调度程序的包装调度程序
 */
declare function wrapDispatch(dispatch: DispatchType | undefined, childName: string): DispatchType;
type ExtraNodeType = {
    node: Record<string, Node<any>>;
    updateNodeFields: (value: any) => Record<string, any>;
};
/**
 * 多功能的核心类，用于构建 comps 的树形结构
 * @remarks
 * 如果需要，可以对函数进行缓存
 **/
declare abstract class MultiBaseComp<ChildrenType extends Record<string, Comp<unknown>> = Record<string, Comp<unknown>>, DataType extends JSONValue = JSONValue, NodeType extends OptionalNodeType = OptionalNodeType> extends AbstractComp<any, DataType, NodeType> {
    readonly children: ChildrenType;
    constructor(params: CompParams<DataType>);
    abstract parseChildrenFromValue(params: CompParams<DataType>): ChildrenType;
    reduce(action: CompAction): this;
    protected reduceOrUndefined(action: CompAction): this | undefined;
    setChild(childName: keyof ChildrenType, newChild: Comp): this;
    protected setChildren(children: Record<string, Comp>, params?: {
        keepCacheKeys?: string[];
    }): this;
    /**
     * 扩展的接口
     *
     * @return 用于添加节点的 node，用于处理 UPDATE_NODE 事件的 updateNodeFields
     * FIXME: 请使类型安全
     */
    protected extraNode(): ExtraNodeType | undefined;
    protected childrenNode(): {
        [key: string]: Node<unknown>;
    };
    nodeWithoutCache(): NodeType;
    changeDispatch(dispatch: DispatchType): this;
    protected ignoreChildDefaultValue(): boolean;
    readonly IGNORABLE_DEFAULT_VALUE: {};
    toJsonValue(): DataType;
    autoHeight(): boolean;
    changeChildAction(childName: string & keyof ChildrenType, value: ConstructorToDataType<new (...params: any) => ChildrenType[typeof childName]>): CompAction<JSONValue>;
}
declare function mergeExtra(e1: ExtraNodeType | undefined, e2: ExtraNodeType): ExtraNodeType;

/**
 * 一个简单的抽象组件，用于维护一个JSON值。
 * 它不包含任何特殊的功能，仅用作其他组件的基础。
 */
declare abstract class SimpleAbstractComp<ViewReturn extends JSONValue> extends AbstractComp<any, ViewReturn, Node<ViewReturn>> {
    value: ViewReturn;
    /**
     * 构造函数。
     *
     * @param params - 组件参数。
     */
    constructor(params: CompParams<ViewReturn>);
    /**
     * 获取此组件的默认值。
     * 子类需要实现此方法来提供默认值。
     */
    protected abstract getDefaultValue(): ViewReturn;
    /**
     * 可能重写此方法来实现兼容性。
     *
     * @param value - 旧的值。
     * @returns 新值，如果不需要更改，可以返回undefined。
     */
    protected oldValueToNew(value?: ViewReturn): ViewReturn | undefined;
    /**
     * 重写reduce方法来处理CHANGE_VALUE操作。
     *
     * @param action - 要处理的操作。
     * @returns 如果值没有更改，返回this；否则，返回一个新的组件实例。
     */
    reduce(action: CompAction): this;
    /**
     * 重写nodeWithoutCache方法来返回一个Node实例。
     *
     * @returns 一个Node实例，包含组件的值。
     */
    nodeWithoutCache(): SimpleNode<ViewReturn>;
    /**
     * 暴露一个方法来获取组件的Node实例。
     *
     * @returns 组件的Node实例。
     */
    exposingNode(): Node<ViewReturn>;
    /**
     * 重写toJsonValue方法来返回组件的值。
     * 可以在defaultValue中使用
     *
     * @returns 组件的值。
     */
    toJsonValue(): ViewReturn;
}
declare abstract class SimpleComp<ViewReturn extends JSONValue> extends SimpleAbstractComp<ViewReturn> {
    getView(): ViewReturn;
}

interface LocaleInfo {
    locale: string;
    language: string;
    region?: string;
}
declare const i18n: {
    locale: string;
    language: string;
    region?: string | undefined;
    locales: string[];
};
declare function getValueByLocale<T>(defaultValue: T, func: (info: LocaleInfo) => T | undefined): T;
type AddDot<T extends string> = T extends "" ? "" : `.${T}`;
type ValidKey<T> = Exclude<keyof T, symbol>;
type NestedKey<T> = (T extends object ? {
    [K in ValidKey<T>]: `${K}${AddDot<NestedKey<T[K]>>}`;
}[ValidKey<T>] : "") extends infer D ? Extract<D, string> : never;
type AddPrefix<T, P extends string> = {
    [K in keyof T as K extends string ? `${P}${K}` : never]: T[K];
};
declare const globalMessages: AddPrefix<{}, "@">;
type GlobalMessageKey = NestedKey<typeof globalMessages>;
type VariableValue = string | number | boolean | Date | React.ReactNode;
declare class Translator<Messages extends object> {
    private readonly messages;
    readonly language: string;
    constructor(fileData: object, filterLocales?: string, locales?: string[]);
    trans(key: NestedKey<Messages> | GlobalMessageKey, variables?: Record<string, VariableValue>): string;
    transToNode(key: NestedKey<Messages> | GlobalMessageKey, variables?: Record<string, VariableValue>): string | react.ReactElement<any, string | react.JSXElementConstructor<any>> | Iterable<react.ReactNode> | react_jsx_runtime.JSX.Element[];
    private getMessage;
}
declare function getI18nObjects<I18nObjects>(fileData: object, filterLocales?: string): I18nObjects;

export { AbstractComp, AbstractNode, ActionContextType, ActionExtraInfo, ActionPriority, BroadcastAction, CachedNode, ChangeValueAction, CodeFunction, CodeNode, CodeNodeOptions, CodeType, Comp, CompAction, CompActionTypes, CompConstructor, CompParams, ConstructorToComp, ConstructorToDataType, ConstructorToNodeType, ConstructorToView, CustomAction, DispatchType, EvalMethods, ExecuteQueryAction, ExtraActionType, ExtraNodeType, FetchCheckNode, FetchInfo, FetchInfoOptions, FunctionNode, MultiBaseComp, MultiChangeAction, MultiCompConstructor, Node, NodeToValue, OptionalComp, OptionalNodeType, RecordConstructorToComp, RecordConstructorToView, RecordNode, RecordNodeToValue, RecordOptionalNodeToValue, RelaxedJsonParser, RenameAction, ReplaceCompAction, RouteByNameAction, SimpleAbstractComp, SimpleComp, SimpleCompAction, SimpleNode, Translator, TriggerModuleEventAction, UpdateActionContextAction, UpdateNodesV2Action, ValueAndMsg, WrapContextFn, WrapContextNodeV2, WrapNode, changeChildAction, changeDependName, changeEditDSLAction, changeValueAction, clearMockWindow, clearStyleEval, customAction, deferAction, deleteCompAction, dependingNodeMapEquals, evalFunc, evalFunctionResult, evalNodeOrMinor, evalPerfUtil, evalScript, evalStyle, executeQueryAction, fromRecord, fromUnevaledValue, fromValue, fromValueWithCache, getDynamicStringSegments, getI18nObjects, getValueByLocale, i18n, isBroadcastAction, isChildAction, isCustomAction, isDynamicSegment, isFetching, isMyCustomAction, mergeExtra, multiChangeAction, nodeIsRecord, onlyEvalAction, promiseWithDefaultOnTimeout, promiseWithTimeout, relaxedJSONToJSON, renameAction, replaceCompAction, routeByNameAction, transformWrapper, triggerModuleEventAction, unwrapChildAction, updateActionContextAction, updateNodesV2Action, withFunction, wrapActionExtraInfo, wrapChildAction, wrapContext, wrapDispatch };
