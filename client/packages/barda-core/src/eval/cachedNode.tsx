import { memoized } from "util/memoize";
import { FunctionNode, withFunction } from "./functionNode";
import { AbstractNode, Node } from "./node";
import { RecordNode } from "./recordNode";
import { EvalMethods } from "./types/evalTypes";

interface CachedValue<T> {
  value: T;
  isCached: boolean;
}

export class CachedNode<T> extends AbstractNode<CachedValue<T>> {
  type: string = "cached";
  child: AbstractNode<T>;
  constructor(child: AbstractNode<T>) {
    super();
    this.child = withEvalCache(child);
  }
  @memoized()
  override filterNodes(exposingNodes: Record<string, Node<unknown>>) {
    return this.child.filterNodes(exposingNodes);
  }
  override justEval(
    exposingNodes: Record<string, Node<unknown>>,
    methods?: EvalMethods
  ): CachedValue<T> {
    const isCached = this.child.isHitEvalCache(exposingNodes); // isCached must be set before evaluate() call
    const value = this.child.evaluate(exposingNodes, methods);
    return { value, isCached };
  }

  override getChildren(): Node<unknown>[] {
    return [this.child];
  }
  override dependValues(): Record<string, unknown> {
    return this.child.dependValues();
  }
  override fetchInfo(exposingNodes: Record<string, Node<unknown>>) {
    return this.child.fetchInfo(exposingNodes);
  }
}

function withEvalCache<T>(node: AbstractNode<T>): FunctionNode<T, T> {
  const newNode = withFunction(node, (x) => x);
  newNode.evalCache = { ...node.evalCache };
  return newNode;
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
export function evalNodeOrMinor<T>(mainNode: AbstractNode<T>, minorNode: Node<T>): Node<T> {
  const nodeRecord = { main: new CachedNode(mainNode), minor: minorNode };
  return new FunctionNode(new RecordNode(nodeRecord), (record) => {
    const mainCachedValue = record.main;
    if (!mainCachedValue.isCached) {
      return mainCachedValue.value;
    }
    return record.minor;
  });
}
