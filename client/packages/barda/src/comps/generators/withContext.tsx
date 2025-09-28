import {
  CompAction,
  CompActionTypes,
  customAction,
  isMyCustomAction,
  Node,
  NodeToValue,
  updateActionContextAction,
  updateNodesV2Action,
  wrapContext,
  WrapContextFn,
} from "barda-core";
import { ConstructorToNodeType, ConstructorToView, MultiCompConstructor } from "barda-core";
import React from "react";
import { lastValueIfEqual, setFieldsNoTypeCheck, shallowEqual } from "util/objectUtils";
import _ from "lodash";

type NodeFnType<T> = ConstructorToNodeType<T> extends undefined
  ? undefined
  : Node<WrapContextFn<NodeToValue<ConstructorToNodeType<T>>>>;
export const CompExposingContext = React.createContext<Record<string, unknown> | undefined>(
  undefined
);

/**
 * Convert a Comp<T> to Comp<Func<T>>, the required context is passed in from the function parameter.
 * implemented as proxy mode.
 *
 * @param VariantComp must use getPropertyView(), otherwise the exposing context not works.
 *
 * FIXME: error handling
 */
/**
 * 为组件构造函数添加上下文支持的函数
 * 
 * 此函数用于扩展一个组件构造函数（VariantComp），使其支持上下文数据
 * 它通过创建一个临时类（TMP_CLASS）并继承自VariantComp来实现这一功能
 * 临时类添加了处理上下文数据的能力，以及更新和刷新上下文数据的方法
 * 
 * @param VariantComp 要扩展的组件构造函数
 * @param paramNames 上下文参数的名称数组
 * @returns 返回扩展后的组件构造函数
 */
export function withContext<ParamNames extends readonly string[], T extends MultiCompConstructor>(
  VariantComp: T,
  paramNames: ParamNames
) {
  // 定义上下文数据类型，为参数名称数组中每个元素到任意类型的映射
  type ContextDataType = Record<ParamNames[number], unknown>;
  // 定义更改上下文数据的动作类型
  type ChangeContextDataAction = {
    type: "setContextData";
    data: ContextDataType;
  };

  // 用于扩展VariantComp的临时类
  // @ts-ignore  // 忽略TypeScript类型检查
  class TMP_CLASS extends VariantComp {
    private readonly valueV2?: any;
    private readonly contextData?: ContextDataType;
    private prevContextVal?: string;

    /**
     * 获取父类视图
     * 
     * 通过类型转换获取父类的视图（View），因为父类是类型T
     * @returns 父类的视图
     */
    getSuperView(): ConstructorToView<T> {
      return super.getView() as unknown as ConstructorToView<T>;
    }

    /**
     * 生成视图函数
     * 
     * 此函数返回一个视图函数，该函数接受上下文数据并返回节点
     * 它会更新子节点的上下文数据，并返回更新后的视图
     * @returns 视图函数
     */
    getView() {
      return (input: ContextDataType) => {
        const superNode = super.node() as unknown as ConstructorToNodeType<T>;
        if (superNode === undefined) {
          return this.getSuperView();
        }
        return this.updateChildContextData(input)
          .reduce(updateActionContextAction(input))
          .getSuperView();
      };
    }

    /**
     * 覆盖属性视图
     * 
     * 此方法覆盖父类的getPropertyView方法，为子组件提供上下文数据
     * @returns 包含上下文数据的属性视图
     */
    override getPropertyView() {
      return (
        <CompExposingContext.Provider value={this.contextData}>
          {super.getPropertyView()}
        </CompExposingContext.Provider>
      );
    }

    /**
     * 生成更改上下文数据的动作
     * 
     * 此静态方法创建一个自定义动作，用于更改上下文数据
     * @param contextData 新的上下文数据
     * @returns 更改上下文数据的动作对象
     */
    static changeContextDataAction(contextData: ContextDataType) {
      return customAction(
        {
          type: "setContextData",
          data: contextData,
        },
        false
      );
    }

    /**
     * 获取上下文值
     * 
     * 根据输入的上下文数据，通过valueV2函数计算新的上下文值
     * @param input 上下文数据
     * @returns 计算后的上下文值
     */
    private getContextValue(input: ContextDataType) {
      return this.valueV2(input);
    }

    /**
     * 更新子节点的上下文数据
     * 
     * 此方法通过调用父类的reduce方法和updateNodesV2Action来更新子节点的上下文数据
     * @param input 新的上下文数据
     * @returns 更新后的组件实例
     */
    private updateChildContextData(input: ContextDataType): this {
      return super.reduce(updateNodesV2Action(this.getContextValue(input)));
    }

    /**
     * 刷新子节点的上下文数据
     * 
     * 此方法检查当前上下文数据和之前的数据是否相同，如果不同则更新子节点的上下文数据
     * @returns 更新后的组件实例
     */
    private refreshChildContextData(): this {
      if (this.contextData && this.valueV2) {
        const newValue = this.getContextValue(this.contextData);
        if (!_.isEqual(this.prevContextVal, newValue)) {
          this.prevContextVal = newValue;
          return this.updateChildContextData(this.contextData);
        }
      }
      return this;
    }

    /**
     * 覆盖reduce方法以处理自定义动作
     * 
     * 此方法重写父类的reduce方法，用于处理特定的自定义动作
     * 它可以处理设置上下文数据的动作和更新节点的动作
     * @param action 动作对象
     * @returns 更新后的组件实例
     */
    override reduce(action: CompAction): this {
      if (isMyCustomAction<ChangeContextDataAction>(action, "setContextData")) {
        const data = action.value.data;
        if (this.contextData && shallowEqual(this.contextData, data)) {
          return this;
        }
        return setFieldsNoTypeCheck(this, { contextData: data }).refreshChildContextData();
      }
      if (action.type === CompActionTypes.UPDATE_NODES_V2) {
        if (this.valueV2 !== action.value) {
          return setFieldsNoTypeCheck(this, { valueV2: action.value }).refreshChildContextData();
        }
      }
      const newComp = super.reduce(action);
      return newComp;
    }

    /**
     * 覆盖node方法以缓存节点
     * 
     * 此方法重写父类的node方法，通过缓存机制优化性能
     * 它只有在子节点改变时才改变节点
     * @returns 节点函数或undefined
     */
    override node() {
      return lastValueIfEqual(
        this,
        "with_context",
        [this.children, this.nodeWithContext()],
        (a, b) => {
          return a[0] === b[0];
        }
      )[1];
    }

    /**
     * 生成带上下文的节点
     * 
     * 此方法生成一个包含上下文信息的额外节点
     * 它通过父类的nodeWithoutCache方法获取原始节点，并添加上下文支持
     * @returns 带上下文的节点函数或undefined
     */
    nodeWithContext() {
      const superNode = super.nodeWithoutCache() as unknown as ConstructorToNodeType<T>;
      if (superNode === undefined) {
        return undefined as NodeFnType<T>;
      }
      return wrapContext(superNode) as NodeFnType<T>;
    }
  }

  // 返回扩展后的组件构造函数
  return TMP_CLASS;
}
