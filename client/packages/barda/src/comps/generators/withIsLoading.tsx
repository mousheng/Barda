import { Spin } from "antd";
import {
  FetchCheckNode,
  FetchInfo,
  fromRecord,
  mergeExtra,
  MultiCompConstructor,
  CompAction,
  CompActionTypes,
} from "barda-core";
import styled from "styled-components";
import { codeControl } from "comps/controls/codeControl";
import { setFieldsNoTypeCheck } from "util/objectUtils";

const Wrapper = styled.div`
  &,
  .ant-spin-nested-loading,
  .ant-spin-container {
    width: 100%;
    height: 100%;
  }
`;

const __WITH_IS_LOADING = "__WITH_IS_LOADING";

/**
 * 统一增加是加载效果
 */
export function withIsLoading<T extends MultiCompConstructor>(VariantComp: T): T {
  // @ts-ignore
  class IS_LOADING_CLASS extends VariantComp {
    readonly isLoading: boolean = false;

    override extraNode() {
      return mergeExtra(super.extraNode(), {
        node: {
          [__WITH_IS_LOADING]: new FetchCheckNode(fromRecord(this.childrenNode())),
        },
        updateNodeFields: (value: any) => {
          const fetchInfo = value[__WITH_IS_LOADING] as FetchInfo;
          return { isLoading: fetchInfo.isFetching };
        },
      });
    }

    override getView() {
      return (
        <Wrapper>
          <Spin spinning={this.isLoading}>{super.getView()}</Spin>
        </Wrapper>
      );
    }
  }

  return IS_LOADING_CLASS;
}

export const __SUPER_NODE_KEY = "__LOADING_SUPER_NODE";

/**
 * 为组件添加isLoading方法以获取当前的加载状态
 * 此函数是一个高阶函数，它接收一个组件构造函数，并返回一个新组件，该新组件具有isLoading方法
 * 
 * @param VariantComp 要增强的组件构造函数，应为codeControl函数的返回值类型
 * @returns 返回一个带有isLoading方法的新组件构造函数
 */
export function withIsLoadingMethod<T extends ReturnType<typeof codeControl>>(VariantComp: T) {
  // @ts-ignore
  class IS_LOADING_CLASS extends VariantComp {
    private loading: boolean = false;

    /**
     * 获取当前组件的加载状态
     * @returns 返回当前的加载状态，true表示正在加载，false表示加载完成
     */
    isLoading() {
      return this.loading;
    }

    /**
     * 覆盖reduce方法以处理特定的加载状态更新
     * 此方法通过检查action.type来响应特定的更新节点操作，并根据新的加载状态更新组件
     * 
     * @param action 表示一个状态变更的动作，用于触发组件状态更新
     * @returns 返回更新后的组件状态
     */
    override reduce(action: CompAction) {
      if (action.type === CompActionTypes.UPDATE_NODES_V2) {
        const value = action.value;
        const superValue = value[__SUPER_NODE_KEY];
        const fetchInfo = value[__WITH_IS_LOADING] as FetchInfo;
        const comp = super.reduce({
          ...action,
          value: superValue,
        });
        if (fetchInfo.isFetching !== this.loading) {
          return setFieldsNoTypeCheck(comp, {
            loading: fetchInfo.isFetching,
          });
        } else {
          return comp;
        }
      }
      return super.reduce(action);
    }

    /**
     * 覆盖nodeWithoutCache方法以在新节点中添加加载状态检查
     * 此方法创建一个新的节点，并在节点中包含一个超级节点和一个用于检查加载状态的FetchCheckNode
     * 
     * @returns 返回一个新的节点，包含原始超级节点和加载状态检查节点
     */
    override nodeWithoutCache() {
      const superNode = super.nodeWithoutCache();
      return fromRecord({
        [__SUPER_NODE_KEY]: superNode,
        [__WITH_IS_LOADING]: new FetchCheckNode(superNode),
      }) as any;
    }
  }

  return IS_LOADING_CLASS;
}
