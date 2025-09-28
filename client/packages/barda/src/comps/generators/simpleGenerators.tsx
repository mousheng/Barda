import { ControlParams } from "comps/controls/controlParams";
import { getReduceContext } from "comps/utils/reduceContext";
import {
  changeValueAction,
  ChangeValueAction,
  Comp,
  CompAction,
  CompConstructor,
  CompParams,
  ConstructorToDataType,
  SimpleComp,
} from "barda-core";
import { ControlPropertyViewWrapper } from "barda-design";
import { ReactNode } from "react";
import { JSONValue } from "util/jsonTypes";

/**
 * 创建一个仅包含属性查看功能的组件类
 * 
 * 该函数用于生成一个React组件类，该组件类继承自SimpleComp，
 * 并且仅提供属性查看的功能，不包含任何子组件或复杂的渲染逻辑
 * 
 * @param propertyViewFn 一个函数，用于定义属性查看的渲染逻辑
 * @param defaultValue 该组件的默认值，用于初始化组件的状态
 * @returns 返回一个React组件类，该类具有所定义的属性查看功能
 * 
 * @template ViewReturn 泛型，指定组件返回的值的类型
 */
export function propertyOnlyComp<ViewReturn extends JSONValue>(
  propertyViewFn: (value: JSONValue, dispatch: (action: CompAction) => void) => ReactNode,
  defaultValue: ViewReturn
) {
  // 定义一个名为PropertyOnlyComp的类，该类继承自SimpleComp<ViewReturn>
  class PropertyOnlyComp extends SimpleComp<ViewReturn> {
    /**
     * 覆盖父类方法以提供该组件的默认值
     * 
     * @returns 返回组件的默认值
     */
    override getDefaultValue() {
      return defaultValue;
    }

    /**
     * 覆盖父类方法以提供该组件的属性查看逻辑
     * 
     * @returns 返回属性查看的React节点
     */
    override getPropertyView() {
      return propertyViewFn(this.value, this.dispatch);
    }

    /**
     * 渲染属性查看的React组件
     * 
     * 该方法使用来自父类的props来渲染属性查看部分，
     * 具体的渲染逻辑由getPropertyView方法定义
     * 
     * @param params 控制属性查看的参数，由React组件的props提供
     * @returns 返回渲染后的React组件
     */
    propertyView(params: ControlParams) {
      return (
        <ControlPropertyViewWrapper {...params}>
          {this.getPropertyView()}
        </ControlPropertyViewWrapper>
      );
    }
  }
  return PropertyOnlyComp;
}

/**
 * 一个连属性查看都没有的组件，纯粹的数据提供者
 **/
export function valueComp<ViewReturn extends JSONValue>(defaultValue: ViewReturn) {
  return propertyOnlyComp(() => <></>, defaultValue);
}

/**
 * stateComp 与 valueComp 相同，唯一的区别是数据不会持久化。
 * 名称来源于 React 的 props 和 state 两种状态，其中 state 是组件自身的状态，不会被外部持久化。
 */
export function stateComp<ViewReturn extends JSONValue>(defaultValue: ViewReturn) {
  const VariantComp = valueComp<ViewReturn>(defaultValue);
  class StateComp extends VariantComp {
    /**
    * 不持久化标签，multiComp.toJsonValue 中将有此字段的组件
    */
    readonly NO_PERSISTENCE = true;
    override toJsonValue() {
      return defaultValue;
    }
    override reduce(action: CompAction): this {
      const reduceContext = getReduceContext();
      if (reduceContext.disableUpdateState) {
        return this;
      }
      return super.reduce(action);
    }
    override changeValueAction(value: ViewReturn): ChangeValueAction {
      return changeValueAction(value, false);
    }
  }
  return StateComp;
}

// return an instance of Comp, pure data
export function valueInstance<T extends JSONValue>(defaultValue: T) {
  const ValueComp = valueComp<T>(defaultValue);
  return new ValueComp({});
}

export function stateInstance<T extends JSONValue>(defaultValue: T) {
  const StateComp = stateComp<T>(defaultValue);
  return new StateComp({});
}

/**
 * Used to identify the comp type and default value, which can be initialized
 */
export function withDefault<T extends CompConstructor>(
  VariantComp: T,
  defaultValue: ConstructorToDataType<T>
): T {
  // https://stackoverflow.com/questions/64396668/why-do-typescript-mixins-require-a-constructor-with-a-single-rest-parameter-any
  // It's an anti-pattern for mixins to override constructors, but that's it for now
  // XXX(lijiaqi): As long as the parameter form is not changed, it is not an anti-pattern, right?
  class TEMP_CLASS extends (VariantComp as any) {
    constructor(params: CompParams) {
      const newParams = { value: defaultValue, ...params };
      super(newParams);
    }
    readonly IGNORABLE_DEFAULT_VALUE = undefined;
  }
  return TEMP_CLASS as unknown as T;
}

/**
 * 为组件添加自定义视图函数的高阶函数，可以使用react钩子
 * 
 * 此函数用于扩展一个组件类，允许使用自定义的视图函数来渲染组件的视图
 * 它首先创建一个新类，该类继承自传入的组件类，并重写getView方法，使用提供的视图函数来渲染视图
 * 然后返回这个新类，使得它可以像普通组件一样被使用，但使用的是自定义的视图逻辑
 * 
 * @param VariantComp 要扩展的组件类，它必须扩展自Comp<ReactNode>
 * @param viewFn 渲染视图的函数，它接收组件实例作为参数，并返回一个ReactNode
 * @returns 返回一个扩展了ViewFn的新组件类
 */
export function withViewFn<T extends new (...args: any) => Comp<ReactNode>>(
  VariantComp: T,
  viewFn: (comp: InstanceType<T>) => ReactNode
) {
  // 定义一个新的View组件，用于内部渲染视图
  function View(props: { comp: InstanceType<T> }) {
    // 使用viewFn函数渲染组件的视图
    return <>{viewFn(props.comp)}</>;
  }

  // 创建一个继承自VariantComp的新类
  const WithViewFnComp = class extends VariantComp {
    // 重写getView方法，使用View组件和当前组件实例来渲染视图
    override getView() {
      return <View comp={this as InstanceType<T>} />;
    }
  };

  // 返回扩展了自定义视图函数的新组件类
  return WithViewFnComp;
}

export function withPropertyViewFn<T extends new (...args: any) => Comp>(
  VariantComp: T,
  propertyViewFn: (comp: InstanceType<T>) => ReactNode
) {
  function View(props: { comp: InstanceType<T> }) {
    return <>{propertyViewFn(props.comp)}</>;
  }

  return class extends VariantComp {
    override getPropertyView() {
      return <View comp={this as InstanceType<T>} />;
    }
  };
}

/**
 * Compatible with historical data
 */
export function migrateOldData<T extends new (...args: any) => Comp>(
  VariantComp: T,
  dataTransformer: (oldData: JSONValue) => ConstructorToDataType<T>
): T {
  return class extends VariantComp {
    constructor(...params: any) {
      const newParams = [...params];
      newParams[0] = {
        ...params[0],
        value: dataTransformer(params[0]["value"]),
      };
      super(...newParams);
    }
  };
}
