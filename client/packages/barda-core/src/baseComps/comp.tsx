import { changeValueAction, ChangeValueAction, CompAction, CompActionTypes } from "actions";
import { Node } from "eval";
import { ReactNode } from "react";
import { memo } from "util/cacheUtils";
import { JSONValue } from "util/jsonTypes";
import { setFieldsNoTypeCheck } from "util/objectUtils";

export type OptionalNodeType = Node<unknown> | undefined;
export type DispatchType = (action: CompAction) => void;

/**
 * 组件接口，定义了组件的通用方法和属性
 *
 * @template ViewReturn 组件的返回值类型
 * @template DataType 组件的数据类型
 * @template NodeType 组件的节点类型
 */
export interface Comp<
  ViewReturn = any,
  DataType extends JSONValue = JSONValue,
  NodeType extends OptionalNodeType = OptionalNodeType
> {
  dispatch: DispatchType; // 组件的派发函数

  getView(): ViewReturn; // 获取组件的视图

  getPropertyView(): ReactNode; // 获取组件的属性视图

  reduce(action: CompAction): this; // 处理组件的动作

  node(): NodeType; // 获取组件的节点

  toJsonValue(): DataType; // 获取组件的数据的 JSON 值

  /**
   * 更改当前组件的派发函数
   * 用于当组件在树结构中移动时
   *
   * @param dispatch 新的派发函数
   */
  changeDispatch(dispatch: DispatchType): this;

  changeValueAction(value: DataType): ChangeValueAction; // 创建一个修改组件值得 action
}

export abstract class AbstractComp<
  ViewReturn = any,
  DataType extends JSONValue = JSONValue,
  NodeType extends OptionalNodeType = OptionalNodeType
> implements Comp<ViewReturn, DataType, NodeType> {
  dispatch: DispatchType;

  constructor(params: CompParams) {
    this.dispatch = params.dispatch ?? ((_action: CompAction) => { });
  }

  abstract getView(): ViewReturn;

  abstract getPropertyView(): ReactNode;

  abstract toJsonValue(): DataType;

  abstract reduce(_action: CompAction): this;

  abstract nodeWithoutCache(): NodeType;

  changeDispatch(dispatch: DispatchType): this {
    return setFieldsNoTypeCheck(this, { dispatch: dispatch }, { keepCacheKeys: ["node"] });
  }

  /**
 * 调用 `changeValueAction` 并保证类型安全。
 * 
 * @param value 要修改的值
 */
  dispatchChangeValueAction(value: DataType) {
    this.dispatch(this.changeValueAction(value));
  }

  changeValueAction(value: DataType): ChangeValueAction {
    return changeValueAction(value, true);
  }

  /**
 * 不要重写函数，重写nodeWithout函数
 * FIXME：如果更改了此对象，则不能更改节点引用
 */
  @memo
  node(): NodeType {
    return this.nodeWithoutCache();
  }
}

export type OptionalComp<T = any> = Comp<T> | undefined;

export type CompConstructor<
  ViewReturn = any,
  DataType extends JSONValue = any,
  NodeType extends OptionalNodeType = OptionalNodeType
> = new (params: CompParams<DataType>) => Comp<ViewReturn, DataType, NodeType>;

/**
 * 提取构造函数的泛型类型
 */
export type ConstructorToView<T> = T extends CompConstructor<infer ViewReturn> ? ViewReturn : never;
export type ConstructorToComp<T> = T extends new (params: CompParams<any>) => infer X ? X : never;
export type ConstructorToDataType<T> = T extends new (params: CompParams<infer DataType>) => any
  ? DataType
  : never;
export type ConstructorToNodeType<T> = ConstructorToComp<T> extends Comp<any, any, infer NodeType>
  ? NodeType
  : never;

export type RecordConstructorToComp<T> = {
  [K in keyof T]: ConstructorToComp<T[K]>;
};
export type RecordConstructorToView<T> = {
  [K in keyof T]: ConstructorToView<T[K]>;
};

export interface CompParams<DataType extends JSONValue = JSONValue> {
  dispatch?: (action: CompAction) => void;
  value?: DataType;
}
