import { JSONValue } from "util/jsonTypes";
import { fromValue, Node } from "eval";
import { setFieldsNoTypeCheck } from "util/objectUtils";
import { CompAction, CompActionTypes } from "actions";
import { AbstractComp, CompParams } from "./comp";

/**
 * 一个简单的抽象组件，用于维护一个JSON值。
 * 它不包含任何特殊的功能，仅用作其他组件的基础。
 */
export abstract class SimpleAbstractComp<ViewReturn extends JSONValue> extends AbstractComp<
  any,
  ViewReturn,
  Node<ViewReturn>
> {
  value: ViewReturn;

  /**
   * 构造函数。
   *
   * @param params - 组件参数。
   */
  constructor(params: CompParams<ViewReturn>) {
    super(params);
    this.value = this.oldValueToNew(params.value) ?? this.getDefaultValue();
  }

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
  protected oldValueToNew(value?: ViewReturn): ViewReturn | undefined {
    return value;
  }

  /**
   * 重写reduce方法来处理CHANGE_VALUE操作。
   *
   * @param action - 要处理的操作。
   * @returns 如果值没有更改，返回this；否则，返回一个新的组件实例。
   */
  override reduce(action: CompAction): this {
    if (action.type === CompActionTypes.CHANGE_VALUE) {
      if (this.value === action.value) {
        return this;
      }
      return setFieldsNoTypeCheck(this, { value: action.value });
    }
    return this;
  }

  /**
   * 重写nodeWithoutCache方法来返回一个Node实例。
   *
   * @returns 一个Node实例，包含组件的值。
   */
  override nodeWithoutCache() {
    return fromValue(this.value);
  }

  /**
   * 暴露一个方法来获取组件的Node实例。
   *
   * @returns 组件的Node实例。
   */
  exposingNode() {
    return this.node();
  }

  /**
   * 重写toJsonValue方法来返回组件的值。
   * 可以在defaultValue中使用
   *
   * @returns 组件的值。
   */
  override toJsonValue(): ViewReturn {
    return this.value;
  }
}

export abstract class SimpleComp<
  ViewReturn extends JSONValue
> extends SimpleAbstractComp<ViewReturn> {
  override getView(): ViewReturn {
    return this.value;
  }
}
