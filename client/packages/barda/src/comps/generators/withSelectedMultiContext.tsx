import _ from "lodash";
import {
  CompAction,
  customAction,
  isMyCustomAction,
  MultiCompConstructor,
  wrapChildAction,
  wrapDispatch,
} from "barda-core";
import { ReactNode } from "react";
import { lastValueIfEqual, setFieldsNoTypeCheck } from "util/objectUtils";
import { COMP_KEY, MAP_KEY, withMultiContext } from "./withMultiContext";
import { paramsEqual } from "./withParams";

const SELECTED_KEY = "SELECTED";

/**
 * 为多上下文组件添加选定上下文功能的高阶函数
 * 
 * 提供一个升级版的 withContext 工具来生成多个可交互的组件或视图。
 * 当有动作在该组件上触发时才懒加载存储这些可交互的组件。
 * 
 * @param VariantCompCtor - 扩展了MultiCompConstructor的构造函数，用于创建具有多上下文能力的组件
 * @returns 返回一个新的构造函数，其对应的类具有处理选定上下文的能力
 */
export function withSelectedMultiContext<TCtor extends MultiCompConstructor>(
  VariantCompCtor: TCtor
) {
  // 使用withMultiContext增强VariantCompCtor，以支持多上下文功能
  const WithMultiContextComp = withMultiContext(VariantCompCtor);

  /**
   * 具有选定上下文功能的组件类
   * 
   * 该类继承自WithMultiContextComp，实现了对多上下文组件的选定上下文功能的支持
   */
  class WithSelectedMultiContextComp extends WithMultiContextComp {
    // 定义选定上下文的键
    private readonly selection: string = "0";

    /**
     * 覆盖getComp方法，为选定的上下文添加或更改分发函数
     * 
     * @param key - 上下文的键
     * @returns 返回获取或更新后的上下文组件
     */
    protected override getComp(key: string) {
      let comp = super.getComp(key);
      if (!_.isNil(comp) && key === this.selection) {
        // 根据当前上下文和分发函数，生成新的分发函数
        const dispatch = lastValueIfEqual(
          this,
          "selectedDispatch",
          [
            wrapDispatch(wrapDispatch(this.dispatch, MAP_KEY), SELECTED_KEY),
            this.dispatch,
          ] as const,
          (a, b) => a[1] === b[1]
        )[0];
        if (dispatch !== comp.dispatch) {
          // 使用新的分发函数替换原有组件的分发函数
          comp = comp.changeDispatch(dispatch);
        }
      }
      return comp;
    }

    /**
     * 覆盖getPropertyView方法，返回选定上下文组件的属性视图
     * 
     * @returns 返回选定上下文组件的属性视图
     */
    override getPropertyView(): ReactNode {
      return this.getSelectedComp().getPropertyView();
    }

    /**
     * 对组件应用操作并返回新的组件状态
     * 
     * @param action - 要应用的操作
     * @returns 返回应用操作后的新组件状态
     */
    override reduce(action: CompAction): this {
      // console.info("enter withSelectedMultiContext reduce. action: ", action, "\nthis: ", this);
      let comp = this;
      // 处理自定义的设置选定上下文操作
      if (isMyCustomAction<SetSelectionAction>(action, "setSelection")) {
        const { selection, params } = action.value;
        const selectedComp = this.getSelectedComp();
        // 设置新的选定上下文
        if (selection !== this.selection) {
          comp = setFieldsNoTypeCheck(comp, { selection });
        }
        // 如果参数有变化，更新选定上下文的参数
        if (!_.isNil(params) && !paramsEqual(params, selectedComp.getParams())) {
          comp = comp.reduce(WithMultiContextComp.setCacheParamsAction({ [selection]: params }));
        }
        // 同步选定上下文和原始组件的参数
        if (!_.isNil(comp.cacheParamsMap.get(selection))) {
          comp = comp.setChild(
            COMP_KEY,
            comp.getOriginalComp().setParams(comp.cacheParamsMap.get(selection)!)
          );
        }
      } else if (!action.editDSL || action.path[0] !== MAP_KEY || _.isNil(action.path[1])) {
        // 对选定上下文之外的操作进行处理
        if (action.path[0] === MAP_KEY && action.path[1] === SELECTED_KEY) {
          action.path[1] = this.selection;
        }
        comp = super.reduce(action);
      } else if (action.editDSL && action.path[1] === SELECTED_KEY) {
        // 广播操作到所有上下文
        const newAction = {
          ...action,
          path: action.path.slice(2),
        };
        comp = comp.reduce(WithMultiContextComp.forEachAction(newAction));
        // 对原始组件应用操作
        comp = comp.reduce(wrapChildAction(COMP_KEY, newAction));
      }

      // console.info("exit withSelectedMultiContext reduce. action: ", action, "\nthis:", this, "\ncomp:", comp);
      return comp;
    }

    /**
     * 获取当前选定的上下文键
     * 
     * @returns 返回选定的上下文键
     */
    getSelection() {
      return this.selection;
    }

    /**
     * 获取选定的上下文组件
     * 
     * @returns 返回选定的上下文组件，如果不存在则返回原始组件
     */
    getSelectedComp() {
      return this.getComp(this.selection) ?? this.getOriginalComp();
    }

    /**
     * 创建一个设置选定上下文的操作
     * 
     * @param selection - 要设置为选定的上下文键
     * @param params - 上下文的参数（可选）
     * @returns 返回一个设置了选定上下文的操作
     */
    static setSelectionAction(selection: string, params?: Record<string, unknown>) {
      return customAction<SetSelectionAction>(
        {
          type: "setSelection",
          selection,
          params,
        },
        false
      );
    }
  }

  // 返回具有选定上下文功能的新组件类
  return WithSelectedMultiContextComp;

  // 定义设置选定上下文的操作类型
  type SetSelectionAction = {
    type: "setSelection";
    selection: string;
    params?: Record<string, unknown>;
  };
}
