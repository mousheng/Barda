import { JSONValue } from "util/jsonTypes";
import { changeValueAction, CustomAction, multiChangeAction } from "barda-core";
import { ConstructorToDataType } from "barda-core";
import { sameTypeMap, stateComp, valueComp } from "comps/generators";
import { MultiCompBuilder } from "comps/generators/multi";
import { addMapChildAction, multiMapAction } from "comps/generators/sameTypeMap";
import { NameGenerator } from "comps/utils";
import { genRandomKey } from "comps/utils/idGenerator";
import { DEFAULT_POSITION_PARAMS, Layout, LayoutItem, PositionParams } from "layout";
import _ from "lodash";
import { GridItemComp, GridItemDataType } from "../gridItemComp";
import { IContainer, isContainer } from "./iContainer";
import { CompTree, getCompTree } from "./utils";

const children = {
  layout: valueComp<Layout>({}),
  items: sameTypeMap(GridItemComp),
  positionParams: stateComp<PositionParams>(DEFAULT_POSITION_PARAMS),
};

const SimpleContainerTmpComp = new MultiCompBuilder(children, (props, dispatch) => {
  return {
    ...props,
    dispatch: dispatch,
  };
})
  .setPropertyViewFn(() => <></>)
  .build();

export class SimpleContainerComp extends SimpleContainerTmpComp implements IContainer {
  /**
   * 根据可选的键值参数获取一个简单的容器组件
   * 此方法旨在从子组件映射中检索与给定键关联的简单容器组件
   * 如果键未定义或在子组件映射中找到该键，则返回当前实例，否则返回undefined
   * 
   * @param key - 用于检索的键值，可能未定义
   * @returns 返回检索到的简单容器组件，如果没有找到或键未定义，则返回当前实例或undefined
   */
  realSimpleContainer(key?: string): SimpleContainerComp | undefined {
    const compMap = this.children.items.children;
    if (_.isNil(key) || compMap.hasOwnProperty(key)) {
      return this;
    }
  }

  /**
   * 获取组件树
   * 
   * @returns {CompTree} 返回当前组件的树状结构
   */
  getCompTree(): CompTree {
    const compMap = this.children.items.children;
    return getCompTree(compMap);
  }

  getAllLayouts(): Layout {
    const compMap = this.children.items.children;
    // 使用 flatMap 来过滤掉空对象，并直接合并布局
    const layoutsFromItems = _.flatMap(compMap, (item: GridItemComp) =>
      (item.children?.comp?.children?.container || item.children?.comp?.children?.containers) && (item.children?.comp as any)?.getAllLayouts
        ? [(item.children.comp as any).getAllLayouts()]
        : []
    );
    // 直接合并所有布局，包括当前容器的布局
    return _.merge({}, ...layoutsFromItems, this.children.layout.value);
  }

  /**
   * 通过键值查找容器对象
   * 
   * @param key 要查找的键值
   * @returns 如果找到匹配的容器，则返回该容器对象；否则返回 undefined
   */
  findContainer(key: string): IContainer | undefined {
    const compMap = this.children.items.children;
    if (compMap.hasOwnProperty(key)) {
      return this;
    }
    for (const childComp of Object.values(compMap)) {
      if (isContainer(childComp.children.comp)) {
        const childResult = childComp.children.comp?.findContainer?.(key);
        if (childResult) {
          return childResult;
        }
      }
    }
    return undefined;
  }

  /**
   * 根据提供的名称生成器，生成并返回一个包含当前组件及其子组件信息的JSON值
   * 该值可用于粘贴操作，如复制到剪贴板或保存为模板
   * 
   * @param nameGenerator 一个NameGenerator实例，用于生成组件的名称
   * @returns 返回一个JSONValue类型的对象，表示当前组件及其子组件的状态和布局信息
   */
  getPasteValue(nameGenerator: NameGenerator): JSONValue {
    // 存储子组件的映射
    let compMap = this.children.items.children;

    // 为每个子组件生成一个随机键，并创建键与子组件的映射关系
    const keyMap = _.mapValues(compMap, () => genRandomKey());

    // 获取当前组件的布局视图，并根据随机键重新排序
    let layout = this.children.layout.getView();
    layout = _.mapKeys(layout, (_, key) => keyMap[key]);

    // 为布局中的每个子组件添加随机键作为其标识符
    layout = _.mapValues(layout, (item, key) => ({ ...item, i: key }));

    // 使用随机键重新排序子组件映射
    compMap = _.mapKeys(compMap, (_, key) => keyMap[key]);

    // 遍历每个子组件，根据其类型生成粘贴值
    const newJSONValue = _.mapValues(compMap, (comp) => {
      // 如果子组件是一个容器，则递归调用getPasteValue方法
      // 否则，直接将子组件转换为JSON值
      const compValue = isContainer(comp.children.comp)
        ? (comp.children.comp as any).getPasteValue(nameGenerator)
        : comp.children.comp.toJsonValue();

      // 为子组件生成友好的名称
      const name = nameGenerator.genItemName(comp.children.compType.getView());

      // 返回子组件的JSON表示，并包含其名称和组件值
      return { ...comp.toJsonValue(), name, comp: compValue };
    });

    // 将当前组件转换为JSON表示，并合并子组件的粘贴值和布局信息
    const finalValue = {
      ...this.toJsonValue(),
      items: newJSONValue,
      layout,
    };

    // log.debug("getPasteValue. origin: ", this.toJsonValue(), " result: ", finalValue);
    return finalValue;
  }
}

/**
 * 将网格项布局和数据转换为简易容器组件的数据格式
 * 
 * 此函数用于将包含网格项数据和布局信息的数组转换为一个对象，
 * 该对象包含两个属性：layout（布局信息）和items（网格项数据）。
 * 每个网格项在布局中用一个唯一标识符（key）表示，该标识符在函数中是随机生成的。
 * 
 * @param infos 包含网格项数据和布局信息的数组每个元素包含两个属性：
 *              item（网格项的数据）和layoutItem（网格项的布局信息）
 * @returns 返回一个对象，包含两个属性：
 *          layout（布局信息对象）和items（网格项数据对象）
 */
export function toSimpleContainerData(
  infos: {
    item: GridItemDataType;
    layoutItem: LayoutItem;
  }[]
): ConstructorToDataType<typeof SimpleContainerComp> {
  // 初始化布局信息对象
  const layout: Layout = {};
  // 初始化网格项数据对象
  const items: Record<string, GridItemDataType> = {};

  // 遍历infos数组，处理每个网格项的数据和布局信息
  infos.forEach((info) => {
    // 为当前网格项生成一个唯一的标识符（key）
    const key = genRandomKey();
    // 将布局信息添加到layout对象中，使用生成的key作为标识
    layout[key] = { ...info.layoutItem, i: key };
    // 将网格项的数据添加到items对象中，使用生成的key作为标识
    items[key] = info.item;
  });

  // 返回包含布局信息和网格项数据的对象
  return {
    layout: layout,
    items: items,
  };
}

/**
 * 简单容器添加操作
 * 
 * 此函数用于将一组新的网格项信息添加到当前布局中它首先将项信息转换为简化容器数据格式，
 * 然后通过多个变更操作来更新布局和项信息这些变更操作包括更新布局数据和添加新的项到布局中
 * 
 * @param currentLayout 当前的布局对象，包含了当前的布局信息
 * @param infos 一个包含待添加项的信息的数组，每个元素包含了一个网格项的数据类型和布局项的信息
 * @returns 返回一个包含布局和项变更的操作对象，用于一次性应用所有的变更
 */
export function simpleContainerAddAction(
  currentLayout: Layout,
  infos: {
    item: GridItemDataType;
    layoutItem: LayoutItem;
  }[]
) {
  // 将传入的项信息转换为简化容器数据格式
  const data = toSimpleContainerData(infos);

  // 返回一个包含布局和项变更的操作对象
  return multiChangeAction({
    // 更新布局数据，合并当前布局和新的布局数据
    layout: changeValueAction(
      {
        ...currentLayout,
        ...data.layout,
      },
      true
    ),
    // 对于每个新的项，创建一个添加操作，并将其转换为自定义操作
    items: multiMapAction(
      Object.entries(data.items ?? {}).map(
        ([key, value]) => addMapChildAction(key, value) as CustomAction
      )
    ),
  });
}
