import { CompTree } from "@barda/comps/comps/containerBase/utils";
import { UICompType } from "@barda/comps/uiCompRegistry";
import { ensureUniqueZArray, Layout } from "@barda/layout/utils";
import { getTreeNodeByKey } from "@barda/util/objectUtils";
import { Key } from "antd/es/table/interface";
import { layoutsNodeItem, LeftCommon, ScrollBar, SelectedComps } from "barda-design";
import { EmptyContent } from "components/EmptyContent";
import UIComp, { UiLayoutType } from "comps/comps/uiComp";
import { EditorContext } from "comps/editorState";
import { GridCompOperator } from "comps/utils/gridCompOperator";
import { trans } from "i18n";
import _ from "lodash";
import { ReactNode, useContext } from "react";
import { CompStateIcon } from "../editorConstants";

const ScrollWrapper = (props: { children: ReactNode }) => (
  <ScrollBar>
    <div style={{ paddingBottom: "10px" }}>{props.children}</div>
  </ScrollBar>
);

interface PropertyViewProps {
  uiComp?: InstanceType<typeof UIComp>;
}

/**
 * 递归地获取树形结构的数据和布局信息
 * @param tree 组件树对象，包含组件的层次结构信息
 * @param layouts 布局对象，包含每个组件的布局信息
 * @param result 节点项数组，用于存储处理后的节点信息
 * @param key 可选参数，父节点的键值，用于在递归调用时标识父节点
 * @returns 返回处理后的节点项数组
 */
const getTreeDataAndLayout = (tree: CompTree, layouts: Layout, result: layoutsNodeItem[], key?: string) => {
  const { items, children } = tree;
  const parent = getTreeNodeByKey(result, key ?? "");
  // 获取当前层级的图层数据
  const tempResult = _.mapValues(items, (item, key) => {
    return {
      title: item.children.name.getView(),
      type: item.children.compType.getView() as UICompType,
      key,
      children: [],
      z: _.get(layouts, `${key}.z`) as unknown as number ?? 0,
    } as layoutsNodeItem;
  });
  if (_.size(tempResult)) {
    const uniqueZLayouts = _.reverse(ensureUniqueZArray(tempResult, 'key'));

    // 如果提供了父节点键值，则将当前节点添加到父节点的子节点数组中，否则直接添加到结果数组中
    if (key) {
      parent?.children.push(...uniqueZLayouts);
    } else {
      result.push(...(uniqueZLayouts as layoutsNodeItem[]));
    }
  }

  // 使用 lodash 的 _.forEach 递归处理下一层级的组件树
  if (Object.keys(children).length) {
    _.forEach(children, (childTree, childKey) => {
      getTreeDataAndLayout(childTree, layouts, result, childKey);
    });
  }

  return result;
};

/**
 * 递归筛选树形数据，只返回被选中的组件
 * @param treeData 树形数据
 * @param selectedCompNames 选中的组件名称集合
 * @returns 筛选后的树形数据，只包含被选中的组件
 */
const filterTreeData = (treeData: layoutsNodeItem[], selectedCompNames: Set<string>): layoutsNodeItem[] => {
  return treeData.reduce<layoutsNodeItem[]>((result, node) => {
    // 如果当前节点被选中，添加到结果中
    if (selectedCompNames.has(node.title)) {
      result.push({ ...node, children: [] });
    }

    // 递归处理子节点
    if (node.children.length > 0) {
      result.push(...filterTreeData(node.children, selectedCompNames));
    }

    return result;
  }, []);
};

export default function PropertyView(props: PropertyViewProps) {
  const { uiComp } = props;
  const editorState = useContext(EditorContext);
  const selectedCompNames = editorState.selectedCompNames;
  const selectedComp = editorState.selectedComp();
  const moduleLayoutComp = uiComp?.getModuleLayoutComp();
  const layoutType = uiComp?.children.compType.getView() as UiLayoutType;
  const AllLayouts = editorState.getUIComp().getComp()?.getAllLayouts?.() ?? {};
  const treeLayouts = getTreeDataAndLayout(editorState.getUIComp().getTree(), AllLayouts!, []);
  const treeData: layoutsNodeItem[] = filterTreeData(treeLayouts, selectedCompNames);
  const layoutMode = editorState.getLayoutMode();

  let propertyView;
  if (selectedComp) {
    return <>{selectedComp.getPropertyView()}</>;
  } else if (selectedCompNames.size > 1) {
    propertyView = (
      <SelectedComps
        layoutMode={layoutMode}
        treeData={treeData}
        icon={(props: any) => props.type && (
          <div style={{ margin: '3px 4px 0 -14px' }}>
            {CompStateIcon[props.type as UICompType] || <LeftCommon />}
          </div>
        )}
        onSelect={(selectedKeys: Key[], info) => {
          if (info?.node?.title) {
            editorState.setSelectedCompNames(new Set([info.node.title as string]));
          }
        }}
        delete={() => {
          GridCompOperator.deleteComp(editorState, editorState.selectedComps());
        }}
        onDrop={(info) => {
          const newLayout: any = {}
          const dragPos = info.dragNode.pos.split('-');
          const dsl = editorState.rootComp.toJsonValue();
          const layoutData = dsl?.ui.layout
          const dragPostion = Number(dragPos[dragPos.length - 1])
          if (layoutData) {
            const newTreeData = [...treeData]
            const ZArray = _.reverse(_.map(newTreeData, 'z').sort());
            const [element] = _.pullAt(newTreeData, [dragPostion])
            newTreeData.splice(_.max([info.dropPosition > dragPostion ? info.dropPosition - 1 : info.dropPosition, 0])!, 0, element)
            newTreeData.forEach((item, index) => {
              newLayout[item.key] = {
                ..._.get(layoutData, item.key),
                z: ZArray[index]
              }
            })
            const layout = _.mapValues(layoutData, item => {
              if (newLayout.hasOwnProperty(item.i)) {
                return { ...item, 'z': newLayout[item.i].z }
              } else return item
            })
            editorState.rootComp.children.ui.dispatchChangeValueAction({
              ...dsl.ui,
              layout,
            })
          }
        }}
      />
    );
  } else if (moduleLayoutComp) {
    propertyView = moduleLayoutComp.getPropertyView();
  } else if (uiComp?.getComp() &&  (layoutType  === "nav" || layoutType === "mobileTabLayout")) {
    propertyView = uiComp?.getComp()?.getPropertyView();
  } else {
    propertyView = (
      <EmptyContent style={{ margin: 16 }} text={trans("rightPanel.noSelectedComps")} />
    );
  }

  return <ScrollWrapper>{propertyView}</ScrollWrapper>;
}
