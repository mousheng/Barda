import { JSONObject } from "util/jsonTypes";
import { NodeToValue, RecordNode } from "barda-core";
import _ from "lodash";
import React, { ReactNode } from "react";
import { CompAction } from "barda-core";
import { Comp, CompParams, MultiBaseComp, wrapDispatch } from "barda-core";

type ViewFnType<ViewReturn, ChildrenType> = (
  childrenType: ChildrenType,
  dispatch: (action: CompAction) => void
) => ViewReturn;

export type ToViewReturn<T> = {
  [K in keyof T]: T[K] extends Comp<infer X> ? X : unknown;
};
export type ToConstructor<T> = {
  [K in keyof T]: T[K] extends Comp<unknown, infer X> ? new (params: CompParams<X>) => T[K] : never;
};
export type ToInstanceType<T> = {
  [K in keyof T]: T[K] extends abstract new (...args: any) => any ? InstanceType<T[K]> : never;
};
export type ToDataType<T> = {
  [K in keyof T]?: T[K] extends Comp<unknown, infer X> ? X : never;
};

type CompToNodeType<T> = T extends Comp<unknown, any, infer X> ? X : never;

type NonOptionalKeys<T> = {
  [K in keyof T]-?: undefined extends CompToNodeType<T[K]> ? never : K;
}[keyof T];
type ToNodeTypeInner<T> = {
  [K in NonOptionalKeys<T>]: CompToNodeType<T[K]>;
};

export type ToNodeType<T extends Record<string, Comp<unknown>>> = NonOptionalKeys<T> extends never
  ? undefined
  : RecordNode<T extends Record<string, Comp<unknown>> ? ToNodeTypeInner<T> : never>;

type ToNodeValueTypeInner<T> = {
  [K in NonOptionalKeys<T>]: NodeToValue<CompToNodeType<T[K]>>;
};
export type ToChildrenNodeValueType<T extends Record<string, Comp<unknown>>> =
  NonOptionalKeys<T> extends never
    ? undefined
    : T extends Record<string, Comp<unknown>>
    ? ToNodeValueTypeInner<T>
    : never;

// Not very readable, but does not affect the running logic
export type ViewFnTypeForComp<ViewReturn, ChildrenCompMap> = ViewFnType<
  ViewReturn,
  ToViewReturn<ChildrenCompMap>
>;
export type PropertyViewFnTypeForComp<ChildrenCompMap> = (
  children: ChildrenCompMap,
  dispatch: (action: CompAction) => void
) => ReactNode;

/**
 * 从值和子组件映射中解析出子组件实例
 * 
 * 此函数用于根据提供的值和子组件的构造函数映射，动态地生成一组子组件实例
 * 它通过检查提供的值中哪些属性对应于子组件，为每个可能的子组件创建参数，并用这些参数实例化相应的子组件
 * 
 * @param params 组件参数，包括可能用于子组件的值和调度函数
 * @param childrenMap 子组件的构造函数映射，键为子组件名，值为子组件的构造函数
 * @returns 返回一个与 childrenMap 类型相同的对象，其属性值为已实例化的子组件
 */
export function parseChildrenFromValueAndChildrenMap<
  ChildrenCompMap extends Record<string, Comp<unknown>>
>(
  params: CompParams<ToDataType<ChildrenCompMap>>,
  childrenMap: ToConstructor<ChildrenCompMap>
): ChildrenCompMap {
  // 初始化派发函数，如果没有提供则使用一个空的调度函数
  const dispatch = params.dispatch ?? ((_action: CompAction) => {});
  // 获取组件的值
  const value = params.value;

  // 生成函数，用于创建子组件的实例
  const genFn = (
    VariantComp: new (params: CompParams) => Comp<unknown>,
    childName: string
  ): Comp<unknown> => {
    // 为当前子组件初始化参数
    let newParams: CompParams = {
      dispatch: wrapDispatch(dispatch, childName),
    };
    // 如果值对象中存在当前子组件的属性，则为其设置值
    if ((value as JSONObject)?.hasOwnProperty(childName)) {
      newParams.value = (value as JSONObject)?.[childName];
    }
    // 使用初始化的参数创建并返回子组件实例
    return new VariantComp(newParams);
  };

  // 遍历子组件映射，为每个子组件调用生成函数，并返回包含所有子组件实例的对象
  return _.mapValues(childrenMap, genFn) as ChildrenCompMap;
}

/**
 * 以这种方式构建组件可以利用 TypeScript 的类型推断能力。
 * 使用 ChildrenCompMap 作为泛型是为了保留每个类的信息，例如不希望 StringControl 退化为 Comp<string>。
 */
export class MultiCompBuilder<ViewReturn, ChildrenCompMap extends Record<string, Comp<unknown>>> {
  private childrenMap: ToConstructor<ChildrenCompMap>;
  private viewFn: ViewFnTypeForComp<ViewReturn, ChildrenCompMap>;
  private propertyViewFn?: PropertyViewFnTypeForComp<ChildrenCompMap>;

  /**
   * 如果 viewFn 不放在构造函数中，那么无法推断出 ViewReturn 的类型
   */
  constructor(
    childrenMap: ToConstructor<ChildrenCompMap>,
    viewFn: ViewFnTypeForComp<ViewReturn, ChildrenCompMap>
  ) {
    this.childrenMap = childrenMap;
    this.viewFn = viewFn;
  }

  setPropertyViewFn(propertyViewFn: PropertyViewFnTypeForComp<ChildrenCompMap>) {
    this.propertyViewFn = propertyViewFn;
    return this;
  }

  build() {
    const builder = this;

    class MultiTempComp extends MultiBaseComp<
      ChildrenCompMap,
      ToDataType<ChildrenCompMap>,
      ToNodeType<ChildrenCompMap>
    > {
      override parseChildrenFromValue(
        params: CompParams<ToDataType<ChildrenCompMap>>
      ): ChildrenCompMap {
        return parseChildrenFromValueAndChildrenMap(params, builder.childrenMap);
      }

      protected override ignoreChildDefaultValue() {
        return true;
      }

      override getView(): ViewReturn {
        return builder.viewFn(
          childrenToProps(this.children) as ToViewReturn<ChildrenCompMap>,
          this.dispatch
        );
      }

      override getPropertyView(): ReactNode {
        return <PropertyView comp={this} propertyViewFn={builder.propertyViewFn} />;
      }
    }

    return MultiTempComp;
  }
}

/**
 * Guaranteed to be in a react component, so that react hooks can be used internally
 */
export function PropertyView(props: { comp: any; propertyViewFn: any }) {
  const comp = props.comp;
  if (!props.propertyViewFn) {
    return null;
  }
  return props.propertyViewFn(comp.children, comp.dispatch);
}

export function childrenToProps<ChildrenCompMap extends Record<string, Comp<unknown>>>(
  children: ChildrenCompMap
) {
  return _.mapValues(children, (comp) => comp.getView());
}

export function simpleMultiComp<ChildrenCompMap extends Record<string, Comp<unknown>>>(
  childrenMap: ToConstructor<ChildrenCompMap>
) {
  return new MultiCompBuilder(childrenMap, () => null as any)
    .setPropertyViewFn(() => <></>)
    .build();
}
