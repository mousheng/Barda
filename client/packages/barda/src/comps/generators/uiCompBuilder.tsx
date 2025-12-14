import { BoolCodeControl } from "comps/controls/codeControl";
import React, { ReactNode, useContext } from "react";
import { ExternalEditorContext } from "util/context/ExternalEditorContext";
import { Comp, CompParams, MultiBaseComp } from "barda-core";
import {
  childrenToProps,
  parseChildrenFromValueAndChildrenMap,
  PropertyView,
  PropertyViewFnTypeForComp,
  ToConstructor,
  ToDataType,
  ToNodeType,
  ViewFnTypeForComp,
} from "./multi";
import { ChildrenToComp, ExposingConfig, withExposingConfigs } from "./withExposing";
import {
  ExposeMethodCompConstructor,
  MethodConfigsType,
  withMethodExposing,
} from "./withMethodExposing";

export type NewChildren<ChildrenCompMap extends Record<string, Comp<unknown>>> = ChildrenCompMap & {
  hidden: InstanceType<typeof BoolCodeControl>;
};

export function HidableView(props: { children: JSX.Element | React.ReactNode; hidden: boolean }) {
  const { readOnly } = useContext(ExternalEditorContext);
  if (readOnly) {
    return <>{!props.hidden && props.children}</>;
  } else {
    return (
      <>
        {props.hidden ? (
          <div style={{ opacity: "50%", width: "100%", height: "100%" }}>{props.children}</div>
        ) : (
          <>{props.children}</>
        )}
      </>
    );
  }
}

/**
 * 为新组件添加一个 hidden 属性的构造器函数
 * 
 * @param childrenMap 子组件的构造器映射
 * @returns 扩展后的子组件构造器，包括所有传入的子组件和一个隐藏属性
 */
export function uiChildren<ChildrenCompMap extends Record<string, Comp<unknown>>>(
  childrenMap: ToConstructor<ChildrenCompMap>
): ToConstructor<NewChildren<ChildrenCompMap>> {
  // 扩展子组件映射，添加隐藏属性构造器
  return { ...childrenMap, hidden: BoolCodeControl } as any;
}

type ViewReturn = ReactNode;

/**
 * UI组件构建器类，用于构建具有子组件的UI组件
 * 通过泛型 ChildrenCompMap 来定义子组件的映射类型，并提供隐藏的接口
 */
export class UICompBuilder<ChildrenCompMap extends Record<string, Comp<unknown>>> {
  // 子组件映射的构造函数类型
  private childrenMap: ToConstructor<ChildrenCompMap>;
  // 视图函数，用于渲染组件
  private viewFn: ViewFnTypeForComp<ViewReturn, NewChildren<ChildrenCompMap>>;
  // 视图属性函数，用于渲染组件的属性面板，默认为空函数
  private propertyViewFn: PropertyViewFnTypeForComp<NewChildren<ChildrenCompMap>> = () => null;
  // 暴露的状态配置数组
  private stateConfigs: ExposingConfig<ChildrenToComp<ChildrenCompMap>>[] = [];
  // 暴露的方法配置数组
  private methodConfigs: MethodConfigsType<ExposeMethodCompConstructor<any>> = [];

  /**
   * 构造函数，初始化childrenMap和viewFn
   * @param childrenMap 子组件的构造函数映射
   * @param viewFn 组件的视图函数
   */
  constructor(
    childrenMap: ToConstructor<ChildrenCompMap>,
    viewFn: ViewFnTypeForComp<ViewReturn, NewChildren<ChildrenCompMap>>
  ) {
    this.childrenMap = childrenMap;
    this.viewFn = viewFn;
  }

  /**
   * 设置右侧属性面板的渲染函数
   * @param propertyViewFn 组件的属性视图函数
   * @returns 返回UICompBuilder实例，支持链式调用
   */
  setPropertyViewFn(propertyViewFn: PropertyViewFnTypeForComp<NewChildren<ChildrenCompMap>>) {
    this.propertyViewFn = propertyViewFn;
    return this;
  }

  /**
   * 设置暴露的状态配置
   * @param configs 状态配置数组
   * @returns 返回UICompBuilder实例，支持链式调用
   */
  setExposeStateConfigs(configs: ExposingConfig<ChildrenToComp<ChildrenCompMap>>[]) {
    this.stateConfigs = configs;
    return this;
  }

  /**
   * 设置暴露的方法配置
   * @param configs 方法配置数组
   * @returns 返回UICompBuilder实例，支持链式调用
   */
  setExposeMethodConfigs(
    configs: MethodConfigsType<ExposeMethodCompConstructor<MultiBaseComp<ChildrenCompMap>>>
  ) {
    this.methodConfigs = configs;
    return this;
  }

  /**
   * 构建UI组件
   * @returns 返回配置好的多子组件构造函数
   */
  build() {
    // 检查childrenMap中是否含有hidden属性，避免重复定义
    if (this.childrenMap.hasOwnProperty("hidden")) {
      throw new Error("already has hidden");
    }
    // 创建新的子组件映射
    const newChildrenMap = uiChildren(this.childrenMap);
    // 保存当前UICompBuilder实例，以便在内部使用
    const builder = this;

    // MultiTempComp类，继承自MultiBaseComp，用于构建最终的UI组件
    class MultiTempComp extends MultiBaseComp<
      NewChildren<ChildrenCompMap>,
      ToDataType<NewChildren<ChildrenCompMap>>,
      ToNodeType<NewChildren<ChildrenCompMap>>
    > {
      /**
       * 重写parseChildrenFromValue方法，根据参数和子组件映射解析子组件
       * @param params 组件参数
       * @returns 解析后的子组件
       */
      override parseChildrenFromValue(
        params: CompParams<ToDataType<NewChildren<ChildrenCompMap>>>
      ): NewChildren<ChildrenCompMap> {
        return parseChildrenFromValueAndChildrenMap(params, newChildrenMap);
      }

      /**
       * 重写ignoreChildDefaultValue方法，忽略子组件的默认值
       * @returns 返回true，表示忽略子组件的默认值
       */
      protected override ignoreChildDefaultValue() {
        return true;
      }

      /**
       * 重写getView方法，返回组件的视图
       * @returns 组件的视图
       */
      override getView(): ViewReturn {
        // 使用UICompBuilder实例的viewFn渲染视图
        return <UIView comp={this} viewFn={builder.viewFn} />;
      }

      /**
       * 重写getPropertyView方法，返回组件的属性视图
       * @returns 组件的属性视图
       */
      override getPropertyView(): ReactNode {
        // 使用UICompBuilder实例的propertyViewFn渲染属性视图
        return <PropertyView comp={this} propertyViewFn={builder.propertyViewFn} />;
      }
    }

    // 应用状态和方法暴露配置到MultiTempComp组件上，并返回最终的组件构造函数
    return withExposingConfigs(
      withMethodExposing(
        MultiTempComp,
        this.methodConfigs as MethodConfigsType<ExposeMethodCompConstructor<MultiTempComp>>
      ) as typeof MultiTempComp,
      this.stateConfigs
    );
  }
}

export const DisabledContext = React.createContext<boolean>(false);

/**
 * Guaranteed to be in a react component, so that react hooks can be used internally
 */

/**
 * 保证位于React组件中，以确保可以在内部使用React Hooks
 * @param props - 包含要渲染的组件和视图函数的对象
 * @param props.comp - 要渲染的组件
 * @param props.viewFn - 视图函数，用于生成UI
 */
function UIView(props: { comp: any; viewFn: any }) {
  // 从props中提取组件
  const comp = props.comp;
  // 将组件的子节点转换为属性
  const childrenProps = childrenToProps(comp.children);
  // 使用DisabledContext上下文获取父组件的禁用状态
  const parentDisabled = useContext(DisabledContext);
  // 从子节点属性中提取禁用状态
  const disabled = childrenProps["disabled"];
  // 如果禁用状态存在且为布尔值，则合并子节点和父节点的禁用状态
  if (disabled !== undefined && typeof disabled === "boolean") {
    childrenProps["disabled"] = disabled || parentDisabled;
  }
  // 渲染组件，使用HidableView组件包装视图函数的返回值
  // 传递 comp 对象作为第三个参数，以便访问原始的 children
  return (
    <HidableView hidden={childrenProps.hidden as boolean}>
      {props.viewFn(childrenProps, comp.dispatch, comp)}
    </HidableView>
  );
}
