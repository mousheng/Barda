import { LayoutModeType } from "@barda/layout/gridLayoutPropTypes";
import { OptionalComp, renameAction } from "barda-core";
import { CompItemType, isContainer } from "comps/comps/containerBase";
import { RootComp as RootCompTmp } from "comps/comps/rootComp";
import { trans } from "i18n";
import { PositionParams } from "layout";
import _ from "lodash";
import React, { ReactNode } from "react";
import { BottomResComp, BottomResListComp, BottomResTypeEnum } from "types/bottomRes";
import { setFields } from "util/objectUtils";
import { DataChangeResponderListComp } from "./comps/dataChangeResponderComp";
import { FolderListComp } from "./comps/folderListComp";
import { GridItemComp } from "./comps/gridItemComp";
import RefTreeComp from "./comps/refTreeComp";
import { TemporaryStateListComp } from "./comps/temporaryStateComp";
import { TransformerListComp } from "./comps/transformerListComp";
import UIComp, { UiLayoutType } from "./comps/uiComp";
import { hookCompCategory, isHookComp } from "./hooks/hookCompTypes";
import { HookListComp } from "./hooks/hookListComp";
import { QueryListComp } from "./queries/queryComp";
import { NameGenerator } from "./utils";
import { NameAndExposingInfo } from "./utils/exposingTypes";
import { checkName } from "./utils/rename";

export type ESRootCompType = InstanceType<typeof RootCompTmp>;
export type ESUICompType = InstanceType<typeof UIComp>;
export type ESQueryCompType = InstanceType<typeof QueryListComp>;
export type ESTempStateCompType = InstanceType<typeof TemporaryStateListComp>;
export type ESTransformerCompType = InstanceType<typeof TransformerListComp>;
export type ESDataResponderCompType = InstanceType<typeof DataChangeResponderListComp>;
export type ESFolderCompType = InstanceType<typeof FolderListComp>;
export type ESRefTreeCompType = InstanceType<typeof RefTreeComp>;
export type ESHookCompType = InstanceType<typeof HookListComp>;

/**
 * Typescript 无法获取私有变量，因此在此手动添加
 */
type ChangeableProps = Partial<EditorState>;

export type CompInfo = {
  name: string;
  type: string;
  data: Record<string, any>;
  dataDesc: Record<string, ReactNode>;
};

export type SelectSourceType = "editor" | "leftPanel" | "addComp" | "rightPanel";

/**
 * *所有编辑器状态都放置在此处，并且仍然是不可变的。
 *
 * 注意:
 * 1.需要持久化的状态由comp维护，这里的状态不是持久化的。
 * 2.所有setter都不会更改当前editorState实例，而是生成新实例。
 */
/**
 * EditorState 类封装了编辑器的状态管理，包括组件状态、属性面板显示状态、拖拽状态等。
 * 它提供了一系列方法来获取和操作这些状态，以支持编辑器界面的交互需求。
 */
export class EditorState {
  readonly rootComp: ESRootCompType;
  readonly showPropertyPane: boolean = false; //显示属性面板
  readonly selectedCompNames: Set<string> = new Set(); //已选择的的组件名
  readonly isDragging: boolean = false; //正在拖动标识
  readonly draggingCompType: string = "button"; //正在拖动的组件类型
  readonly forceShowGrid: boolean = false; // 强制显示网格线
  readonly disableInteract: boolean = false; // 禁用comp的交互（如点击按钮事件）
  readonly selectedBottomResName: string = ""; //选中的底部查询名
  readonly selectedBottomResType?: BottomResTypeEnum; //选中的底部查询类型
  readonly showResultCompName: string = "";
  readonly selectSource?: SelectSourceType; // the source of select type
  readonly isPasting: boolean = false; // 是否正在粘贴组件
  readonly isCodeEditorPanelOpen: boolean = false; // CodeEditorPanel是否打开

  private __nameAndExposingInfoCache__: NameAndExposingInfo = {}; // 组件名称和暴露信息缓存
  private __uiCompByNameCache__: Record<string, any> = {}; // 按名称缓存的UI组件

  private readonly setEditorState: (fn: (editorState: EditorState) => EditorState) => void;

  /**
   * 构造函数
   * @param rootComp 根组件
   * @param setEditorState 更新编辑器状态的函数
   */
  constructor(
    rootComp: ESRootCompType,
    setEditorState: (fn: (editorState: EditorState) => EditorState) => void
  ) {
    this.rootComp = rootComp;
    this.setEditorState = setEditorState;
  }

  /**
   * 大多数时候使用changeState，您可以使用此方法来获取最新的editorState。（类似于react的setState方法）
   */
  private changeStateFn(fn: (editorState: EditorState) => ChangeableProps) {
    this.setEditorState((oldState) => {
      const stateChanges = fn(oldState);
      return setFields(oldState, stateChanges);
    });
  }

  /**
   * 简便方法来改变状态
   * @param params 要改变的状态属性
   */
  private changeState(params: ChangeableProps) {
    this.changeStateFn(() => params);
  }

  /**
   * 获取所有组件的映射，包括UI组件和Hook组件
   * @returns 所有组件的映射
   */
  getAllCompMap() {
    return { ...this.getAllHooksCompMap(), ...this.getUIComp().getAllCompItems() };
  }

  /**
   * 获取所有UI组件的映射，包括在Hook组件中的UI子组件
   * @returns 所有UI组件的映射
   */
  getAllUICompMap() {
    const ret = this.rootComp.children.ui.getAllCompItems();
    // Include UI sub comps in HookComp
    Object.entries(this.getAllHooksCompMap()).forEach(([key, item]) => {
      const type = item.children.compType.getView();
      if (!isHookComp(type) || hookCompCategory(type) === "ui") {
        ret[key] = item;
      }
    });
    return ret;
  }

  /**
   * 获取所有Hook组件的映射
   * @returns 所有Hook组件的映射
   */
  private getAllHooksCompMap() {
    return this.getHooksComp().getAllCompItems();
  }

  /**
   * 通过名称获取组件变量
   * FIXME: 目前只能获取 UI 组件，将来应该获取所有组件
   * @param name 组件名称
   * @returns 找到的组件变量
   */
  getUICompByName(name: string) {
    if (this.__uiCompByNameCache__[name]) {
      return this.__uiCompByNameCache__[name];
    }
    const compMap = this.getAllUICompMap();
    const result = Object.values(compMap).find((item) => item.children.name.getView() === name);
    if (result) {
      this.__uiCompByNameCache__[name] = result;
    }
    return result;
  }

  /**
   * 获取名称生成器，用于生成唯一的组件名称
   * @returns 名称生成器实例
   */
  getNameGenerator() {
    const nameGenerator = new NameGenerator();
    const exposingInfo = this.nameAndExposingInfo();
    nameGenerator.init(Object.keys(exposingInfo));
    return nameGenerator;
  }

  /**
   * 获取所有组件的名称和暴露信息
   * @returns 组件的名称和暴露信息的映射
   */
  nameAndExposingInfo() {
    if (!_.isEmpty(this.__nameAndExposingInfoCache__)) {
      return this.__nameAndExposingInfoCache__;
    }
    this.__nameAndExposingInfoCache__ = this.rootComp.nameAndExposingInfo();
    return this.__nameAndExposingInfoCache__;
  }

  /**
   * 获取所有或指定UI组件的信息列表
   * @param filter 可选的过滤函数，用于筛选组件
   * @returns UI组件信息列表
   */
  uiCompInfoList(filter?: (item: CompItemType) => boolean): Array<CompInfo> {
    const compMap = this.getAllUICompMap();
    const filteredMap = filter ? _.filter(Object.values(compMap), filter) : Object.values(compMap);
    return filteredMap.map((item) => ({
      name: item.children.name.getView(),
      type: item.children.compType.getView(),
      data: item.children.comp.exposingValues,
      dataDesc: item.children.comp.exposingInfo().propertyDesc,
    }));
  }

  /**
   * 根据组件名称和暴露信息获取组件信息
   * @param nameAndExposingInfo 组件的名称和暴露信息
   * @param name 组件名称
   * @param type 组件类型
   * @returns 组件信息
   */
  getCompInfo(nameAndExposingInfo: NameAndExposingInfo, name: string, type: string): CompInfo {
    return {
      name,
      type,
      data: nameAndExposingInfo[name].propertyValue as Record<string, any>,
      dataDesc: nameAndExposingInfo[name].propertyDesc,
    };
  }

  /**
   * 获取底部资源组件的信息列表，包括查询、状态、转换器和数据响应器
   * @returns 底部资源组件信息列表
   */
  bottomResComInfoList(): Array<CompInfo> {
    const queryComInfoList = this.queryCompInfoList();
    const stateComInfoList = this.getTempStateCompInfoList();
    const transformerComInfoList = this.getTransformerCompInfoList();
    const dataResponderInfoList = this.getDataResponderInfoList();
    return [...queryComInfoList, ...stateComInfoList, ...transformerComInfoList, ...dataResponderInfoList];
  }

  /**
   * 获取数据响应器的信息列表
   * @returns 数据响应器信息列表
   */
  getDataResponderInfoList(): Array<CompInfo> {
    const listComp = this.getDataRespondersComp();
    const exposingInfo = listComp.nameAndExposingInfo();
    return listComp.getView().map((item) => {
      const name = item.children.name.getView();
      return this.getCompInfo(exposingInfo, name, BottomResTypeEnum.DateResponder);
    });
  }

  /**
   * 获取临时状态组件的信息列表
   * @returns 临时状态组件信息列表
   */
  getTempStateCompInfoList(): Array<CompInfo> {
    const listComp = this.getTempStatesComp();
    const exposingInfo = listComp.nameAndExposingInfo();
    return listComp.getView().map((item) => {
      const name = item.children.name.getView();
      return this.getCompInfo(exposingInfo, name, BottomResTypeEnum.TempState);
    });
  }

  /**
   * 获取转换器组件的信息列表
   * @returns 转换器组件信息列表
   */
  getTransformerCompInfoList(): Array<CompInfo> {
    const listComp = this.getTransformersComp();
    const exposingInfo = listComp.nameAndExposingInfo();
    return listComp.getView().map((item) => {
      const name = item.children.name.getView();
      return this.getCompInfo(exposingInfo, name, BottomResTypeEnum.Transformer);
    });
  }

  /**
   * 获取所有查询组件的信息列表
   * @returns 查询组件信息列表
   */
  queryCompInfoList(): Array<CompInfo> {
    const exposingInfo = this.getQueriesComp().nameAndExposingInfo();
    return this.getQueriesComp()
      .getView()
      .map((item) => {
        const name = item.children.name.getView();
        return this.getCompInfo(exposingInfo, name, BottomResTypeEnum.Query);
      });
  }

  /**
   * 获取所有Hook组件的信息列表，包括子组件
   * @returns Hook组件信息列表
   */
  hooksCompInfoList(): Array<CompInfo> {
    // get all hookComps, including sub comps if hookComp is a container, and sub comps may not be hookComp
    return Object.values(this.getAllHooksCompMap()).map((item) => {
      return {
        name: item.children.name.getView(),
        type: item.children.compType.getView(),
        data: item.children.comp.exposingValues,
        dataDesc: item.children.comp.exposingInfo().propertyDesc,
      };
    });
  }

  /**
   * 从列表组件中获取选中的底部资源项
   * @param listComp 底部资源列表组件
   * @returns 找到底部资源项
   */
  getBottomResItemFromList(listComp: BottomResListComp) {
    return listComp.items().find((i) => {
      return i.id() === this.selectedBottomResName;
    });
  }

  /**
   * 获取选中的底部资源组件
   * @returns 选中的底部资源组件
   */
  selectedBottomResComp(): BottomResComp | undefined {
    const { selectedBottomResName } = this;
    return this.getBottomResComp(selectedBottomResName);
  }

  /**
   * 获取选中的或第一个查询组件
   * @returns 选中的或第一个查询组件
   */
  selectedOrFirstQueryComp() {
    const selectedQueryComp = this.selectedQueryComp();
    if (selectedQueryComp) {
      return selectedQueryComp;
    }
    return this.getQueriesComp().getView()[0];
  }

  /**
   * 获取选中的查询组件
   * @returns 选中的查询组件
   */
  selectedQueryComp() {
    if (this.selectedBottomResType !== BottomResTypeEnum.Query || !this.selectedBottomResName) {
      return undefined;
    }
    return this.getQueriesComp()
      .getView()
      .find((queryComp) => {
        return queryComp.children.name.getView() === this.selectedBottomResName;
      });
  }

  /**
   * 获取显示结果的组件
   * @returns 显示结果的组件
   */
  showResultComp(): BottomResComp | undefined {
    const bottomResComps = Object.values(BottomResTypeEnum).reduce<BottomResComp[]>((a, b) => {
      const items = this.getBottomResListComp(b).items();
      return a.concat(items);
    }, []);

    return bottomResComps.find((i) => i.name() === this.showResultCompName);
  }

  /**
   * 获取选中的组件,如果选中多个或没有选中，则返回undefined
   * 如果要获取多个组件，请使用selectedComps
   * @returns 选中的组件
   */
  selectedComp(): OptionalComp {
    const selectedComps = Object.values(this.selectedComps());
    return selectedComps.length === 1 ? selectedComps[0] : undefined;
  }

  /**
   * 获取全局画布的位置参数
   * @returns 画布位置参数
   */
  canvasPositionParams(): PositionParams | undefined {
    return this.getUIComp().getComp()?.getPositionParams();
  }

  /**
   * 获取选定的组件
   *
   * 此方法用于从所有组件中筛选出选定的组件具体步骤如下：
   * 1. 获取所有组件的映射
   * 2. 使用lodash的pickBy方法筛选组件映射中符合选定条件的组件
   * 3. 筛选条件是组件的子元素名称在选定的组件名称列表中
   *
   * @returns 返回一个对象，包含所有选定的组件
   */
  selectedComps() {
    const compMap = this.getAllCompMap();
    const selectedComps = _.pickBy(compMap, (item) =>
      this.selectedCompNames.has(item.children.name.getView())
    );
    return selectedComps;
  }

  /**
   * 选择容器组件
   *
   * 此函数旨在确定当前选中的容器组件如果选中的组件为空，则返回当前UI组件的根组件
   * 如果选中的组件是容器类型，则返回该容器类型的子组件否则，返回第一个选中的组件或根组件
   *
   * @returns {UIComponent} 当前选中的容器组件或根组件
   */
  selectedContainer() {
    const selectedComps = this.selectedComps();
    // log.debug("selectedContainer. selectedComps: ", selectedComps);
    if (_.size(selectedComps) === 0) {
      return this.getUIComp().getComp();
    }
    const [key, comp] = _.toPairs(selectedComps)[0];
    if (_.size(selectedComps) === 1 && isContainer((comp as GridItemComp)?.children?.comp)) {
      return comp.children.comp;
    }

    return this.findContainer(key) ?? this.getUIComp().getComp();
  }

  /**
   * 根据组件键查找容器
   *
   * 此方法旨在通过组件键（compKey）查找对应的容器，首先尝试在UI组件中查找，
   * 如果未找到，则继续在hooks组件中查找这种方式确保了查找过程的灵活性和高效性
   *
   * @param compKey 组件的唯一键，用于标识特定的组件
   * @returns 返回找到的容器，如果没有找到则可能返回undefined或特定的默认值
   */
  findContainer(compKey: string) {
    return this.getUIComp().getComp()?.findContainer?.(compKey) || this.getHooksComp().findContainer(compKey);
  }

  /**
   * 查找UI父容器组件
   *
   * 此方法旨在寻找指定组件的父容器组件它首先通过`getUIComp()`方法获取UI组件实例，
   * 并尝试在其上执行`findParentContainer`方法如果未找到，则通过`getHooksComp()`方法
   * 获取Hooks组件实例，并在其上执行相同方法这种冗余检查确保了在两个可能的组件实例中
   * 寻找到合适的父容器组件
   *
   * @param compName 组件名称，用于标识需要查找父容器的目标组件
   * @param containerCompType 可选参数，指定父容器组件的类型如果不传入此参数，
   * 将返回任何类型的父容器组件
   * @returns 返回找到的父容器组件，如果没有找到，则返回null或undefined
   */
  findUIParentContainer(compName: string, containerCompType?: string) {
    return (
      this.getUIComp().findParentContainer(compName, containerCompType) ||
      this.getHooksComp().findParentContainer(compName, containerCompType)
    );
  }

  /**
   * 确定组件是否被选中，无论其是否处于多选状态。
   *
   * 该方法主要基于组件名称检查当前状态下组件是否被选中。它首先通过 `getAllCompMap` 获取所有组件的映射，然后遍历该映射以查找名称匹配的组件。如果找到该组件且其已被选中（即名称存在于 `selectedCompNames` 中），则返回该组件。这允许快速检查组件的选中状态，而不受其他选中组件的影响。
   *
   * @param compName 要检查的组件名称。
   * @returns 返回与当前组件名称对应的组件，无论其是否处于多选状态。
   */
  isCompSelected(compName: string): OptionalComp {
    const compMap = this.getAllCompMap();
    return Object.values(compMap).find(
      (item) => item.children.name.getView() === compName && this.selectedCompNames.has(compName)
    );
  }

  getAppSettings() {
    return this.getAppSettingsComp().getView();
  }

  setDragging(dragging: boolean) {
    if (this.isDragging === dragging) {
      return;
    }
    this.changeState({ isDragging: dragging });
  }

  setDraggingCompType(draggingComp: string) {
    this.changeState({ draggingCompType: draggingComp, isDragging: true });
  }

  setForceShowGrid(forceShowGrid: boolean) {
    if (this.forceShowGrid !== forceShowGrid) {
      this.changeState({ forceShowGrid });
    }
  }

  showGridLines() {
    return this.isDragging || this.forceShowGrid;
  }

  setDisableInteract(disableInteract: boolean) {
    if (this.disableInteract !== disableInteract) {
      this.changeState({ disableInteract });
    }
  }

  setShowPropertyPane(showPropertyPane: boolean) {
    this.changeState({ showPropertyPane: showPropertyPane });
  }

  /**
   * 清除所有缓存
   */
  private clearCache() {
    this.__nameAndExposingInfoCache__ = {};
    this.__uiCompByNameCache__ = {};
  }

  setComp(compFn: (comp: ESRootCompType) => ESRootCompType) {
    this.clearCache();
    this.changeStateFn((editorState) => {
      return {
        rootComp: compFn(editorState.rootComp),
        isPasting: false, // 重置粘贴状态
      };
    });
  }

  setSelectedCompNames(selectedCompNames: Set<string>, selectSource?: SelectSourceType) {
    if (selectedCompNames.size === 0 && this.selectedCompNames.size === 0) {
      return;
    }
    this.changeState({
      selectedCompNames: selectedCompNames,
      showPropertyPane: selectedCompNames.size > 0,
      selectSource: selectSource,
    });
  }

  setSelectedBottomRes(name: string, type?: BottomResTypeEnum) {
    this.changeState({
      selectedBottomResName: name,
      selectedBottomResType: type,
    });
  }

  setShowResultCompName(showResultCompName: string | undefined) {
    this.changeState({ showResultCompName });
  }

  setIsPasting(isPasting: boolean) {
    this.changeState({ isPasting });
  }

  setCodeEditorPanelOpen(isOpen: boolean) {
    if (this.isCodeEditorPanelOpen !== isOpen) {
      this.changeState({ isCodeEditorPanelOpen: isOpen });
    }
  }

  getUIComp() {
    return this.rootComp.children.ui;
  }

  getModuleLayoutComp() {
    return this.getUIComp().getModuleLayoutComp();
  }

  isModule() {
    return !!this.getModuleLayoutComp();
  }

  getBottomResListComp(type: BottomResTypeEnum): BottomResListComp {
    switch (type) {
      case BottomResTypeEnum.Query:
        return this.getQueriesComp();
      case BottomResTypeEnum.TempState:
        return this.getTempStatesComp();
      case BottomResTypeEnum.Transformer:
        return this.getTransformersComp();
      case BottomResTypeEnum.DateResponder:
        return this.getDataRespondersComp();
      case BottomResTypeEnum.Folder:
        return this.getFoldersComp();
    }
  }

  getBottomResComp(name: string): BottomResComp | undefined {
    const bottomResComps = Object.values(BottomResTypeEnum).reduce<BottomResComp[]>((a, b) => {
      const items = this.getBottomResListComp(b).items();
      return a.concat(items);
    }, []);

    return bottomResComps.find((i) => i.name() === name);
  }

  getQueriesComp() {
    return this.rootComp.children.queries;
  }

  getTempStatesComp() {
    return this.rootComp.children.tempStates;
  }

  getTransformersComp() {
    return this.rootComp.children.transformers;
  }

  getDataRespondersComp() {
    return this.rootComp.children.dataResponders;
  }

  getFoldersComp() {
    return this.rootComp.children.folders;
  }

  getHooksComp() {
    return this.rootComp.children.hooks;
  }

  getAppSettingsComp() {
    return this.rootComp.children.settings;
  }

  checkRename(oldName: string, name: string): string {
    const error = checkName(name);
    if (error) {
      return error;
    }
    if (name !== oldName && this.nameAndExposingInfo().hasOwnProperty(name)) {
      return trans("comp.nameExists", { name: name });
    }
    return "";
  }

  rename(oldName: string, name: string): boolean {
    const error = this.checkRename(oldName, name);
    if (error) {
      return false;
    }
    if (name !== oldName) {
      this.rootComp.dispatch(renameAction(oldName, name));
    }
    return true;
  }

  getAppType(): UiLayoutType {
    return this.getUIComp().children.compType.getView();
  }

  getLayoutMode(): LayoutModeType {
    return this.getAppSettings().layoutMode;
  }
}

export const EditorContext = React.createContext<EditorState>(undefined as any);

// 当前的组件名称
export const CompNameContext = React.createContext<string>(undefined as any);
