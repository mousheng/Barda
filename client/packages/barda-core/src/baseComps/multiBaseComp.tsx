import {
  CompAction,
  CompActionTypes,
  isChildAction,
  unwrapChildAction,
  updateNodesV2Action,
  wrapChildAction,
} from "actions";
import { fromRecord, Node } from "eval";
import _ from "lodash";
import log from "loglevel";
import { CACHE_PREFIX } from "util/cacheUtils";
import { JSONValue } from "util/jsonTypes";
import { containFields, setFieldsNoTypeCheck, shallowEqual } from "util/objectUtils";
import {
  AbstractComp,
  Comp,
  CompParams,
  ConstructorToDataType,
  DispatchType,
  OptionalNodeType,
} from "./comp";

/**
 * 多功能基础组件构造函数，其中包含实现了抽象函数
 */
export type MultiCompConstructor = new (params: CompParams<any>) => MultiBaseComp<any, any, any> &
  Comp<any, any, any>;

/**
 * 将调度程序包装为子调度程序
 *
 * @param dispatch 输入调度程序
 * @param childName 子调度程序的键
 * @returns 包含子调度程序的包装调度程序
 */
export function wrapDispatch(dispatch: DispatchType | undefined, childName: string): DispatchType {
  return (action: CompAction): void => {
    if (dispatch) {
      dispatch(wrapChildAction(childName, action));
    }
  };
}

export type ExtraNodeType = {
  node: Record<string, Node<any>>;
  updateNodeFields: (value: any) => Record<string, any>;
};

/**
 * 多功能的核心类，用于构建 comps 的树形结构
 * @remarks
 * 如果需要，可以对函数进行缓存
 **/
export abstract class MultiBaseComp<
  ChildrenType extends Record<string, Comp<unknown>> = Record<string, Comp<unknown>>,
  DataType extends JSONValue = JSONValue,
  NodeType extends OptionalNodeType = OptionalNodeType
> extends AbstractComp<any, DataType, NodeType> {
  readonly children: ChildrenType;

  constructor(params: CompParams<DataType>) {
    super(params);
    this.children = this.parseChildrenFromValue(params);
  }

  abstract parseChildrenFromValue(params: CompParams<DataType>): ChildrenType;

  override reduce(action: CompAction): this {
    const comp = this.reduceOrUndefined(action);
    if (!comp) {
      console.warn(
        "不支持的操作，不应该发生，操作：",
        action,
        "\n当前组件：",
        this
      );

      return this;
    }
    return comp;
  }

  // 如果基类无法处理此操作，则返回 undefined
  protected reduceOrUndefined(action: CompAction): this | undefined {
    // log.debug("reduceOrUndefined. action: ", action, " this: ", this);
    // 必须处理父级中的 DELETE 操作
    if (action.type === CompActionTypes.DELETE_COMP && action.path.length === 1) {
      return this.setChildren(_.omit(this.children, action.path[0]));
    }
    if (action.type === CompActionTypes.REPLACE_COMP && action.path.length === 1) {
      const NextComp = action.compFactory;
      if (!NextComp) {
        return this;
      }

      const compName = action.path[0];
      const currentComp = this.children[compName];
      const value = currentComp.toJsonValue();
      const nextComp = new NextComp({
        value,
        dispatch: wrapDispatch(this.dispatch, compName),
      });
      return this.setChildren({
        ...this.children,
        [compName]: nextComp,
      });
    }
    if (isChildAction(action)) {
      const [childName, childAction] = unwrapChildAction(action);
      const child = this.children[childName];
      if (!child) {
        log.error("找到了无效的操作路径 ", childName, ", children:", this.children);
        return this;
      }
      const newChild = child.reduce(childAction);
      return this.setChild(childName, newChild);
    }
    // 键值对
    switch (action.type) {
      // 批量更改
      case CompActionTypes.MULTI_CHANGE: {
        const { changes } = action;
        // 处理父级中的 DELETE 操作
        let mcChildren = _.omitBy(this.children, (comp, childName) => {
          const innerAction = changes[childName];
          return (
            innerAction &&
            innerAction.type === CompActionTypes.DELETE_COMP &&
            innerAction.path.length === 0
          );
        });
        // 更改
        mcChildren = _.mapValues(mcChildren, (comp, childName) => {
          const innerAction = changes[childName];
          if (innerAction) {
            return comp.reduce(innerAction);
          }
          return comp;
        });
        return this.setChildren(mcChildren);
      }
      // 更新节点 V2
      case CompActionTypes.UPDATE_NODES_V2: {
        const { value } = action;
        if (value === undefined) {
          return this;
        }
        const cacheKey = CACHE_PREFIX + "REDUCE_UPDATE_NODE";
        // 如果是通过值构建的，则直接返回
        if ((this as any)[cacheKey] === value) {
          // console.info("UPDATE_NODE_V2 缓存命中，操作：", action, "\n值：", value, "\nthis：", this);
          return this;
        }
        const children = _.mapValues(this.children, (comp, childName) => {
          if (value.hasOwnProperty(childName)) {
            return comp.reduce(updateNodesV2Action(value[childName]));
          }
          return comp;
        });
        const extraFields = this.extraNode()?.updateNodeFields(value);
        if (shallowEqual(children, this.children) && containFields(this, extraFields)) {
          return this;
        }
        return setFieldsNoTypeCheck(
          this,
          {
            children: children,
            [cacheKey]: value,
            ...extraFields,
          },
          { keepCacheKeys: ["node"] }
        );
      }
      // 更改值
      case CompActionTypes.CHANGE_VALUE: {
        return this.setChildren(
          this.parseChildrenFromValue({
            dispatch: this.dispatch,
            value: action.value as DataType,
          })
        );
      }
      // 广播
      case CompActionTypes.BROADCAST: {
        return this.setChildren(
          _.mapValues(this.children, (comp) => {
            return comp.reduce(action);
          })
        );
      }
      // 仅求值
      case CompActionTypes.ONLY_EVAL: {
        return this;
      }
    }
  }

  setChild(childName: keyof ChildrenType, newChild: Comp): this {
    if (this.children[childName] === newChild) {
      return this;
    }
    return this.setChildren({
      ...this.children,
      [childName]: newChild,
    });
  }

  protected setChildren(
    children: Record<string, Comp>,
    params?: { keepCacheKeys?: string[] }
  ): this {
    if (shallowEqual(children, this.children)) {
      return this;
    }
    return setFieldsNoTypeCheck(this, { children: children }, params);
  }

  /**
   * 扩展的接口
   *
   * @return 用于添加节点的 node，用于处理 UPDATE_NODE 事件的 updateNodeFields
   * FIXME: 请使类型安全
   */
  protected extraNode(): ExtraNodeType | undefined {
    return undefined;
  }

  protected childrenNode() {
    const result: { [key: string]: Node<unknown> } = {};
    Object.keys(this.children).forEach((key) => {
      const node = this.children[key].node();
      if (node !== undefined) {
        result[key] = node;
      }
    });
    return result;
  }

  override nodeWithoutCache(): NodeType {
    return fromRecord({
      ...this.childrenNode(),
      ...this.extraNode()?.node,
    }) as unknown as NodeType;
  }

  override changeDispatch(dispatch: DispatchType): this {
    const newChildren = _.mapValues(this.children, (comp, childName) => {
      return comp.changeDispatch(wrapDispatch(dispatch, childName));
    });
    return super.changeDispatch(dispatch).setChildren(newChildren, { keepCacheKeys: ["node"] });
  }

  protected ignoreChildDefaultValue() {
    return false;
  }

  readonly IGNORABLE_DEFAULT_VALUE = {};
  override toJsonValue(): DataType {
    const result: Record<string, any> = {};
    const ignore = this.ignoreChildDefaultValue();
    Object.keys(this.children).forEach((key) => {
      const comp = this.children[key];
      // FIXME: 这是一个不太好的实现，更好的做法是选择一个封装的实现
      if (comp.hasOwnProperty("NO_PERSISTENCE")) {
        return;
      }
      const value = comp.toJsonValue();
      if (ignore && _.isEqual(value, (comp as any)["IGNORABLE_DEFAULT_VALUE"])) {
        return;
      }
      result[key] = value;
    });
    return result as DataType;
  }

  // FIXME: autoHeight 应封装在 UIComp/UICompBuilder 中
  autoHeight(): boolean {
    return true;
  }

  changeChildAction(
    childName: string & keyof ChildrenType,
    value: ConstructorToDataType<new (...params: any) => ChildrenType[typeof childName]>
  ) {
    return wrapChildAction(childName, this.children[childName].changeValueAction(value));
  }
}

export function mergeExtra(e1: ExtraNodeType | undefined, e2: ExtraNodeType): ExtraNodeType {
  if (e1 === undefined) {
    return e2;
  }
  return {
    node: {
      ...e1.node,
      ...e2.node,
    },
    updateNodeFields: (value: any) => {
      return {
        ...e1.updateNodeFields(value),
        ...e2.updateNodeFields(value),
      };
    },
  };
}
