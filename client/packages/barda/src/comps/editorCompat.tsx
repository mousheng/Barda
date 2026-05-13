import React, { type ReactNode, useEffect, useReducer, useRef } from "react";
import _ from "lodash";
import { useEditorStore } from "./editorStore";
import {
  ESRootCompType,
  CompInfo,
  SelectSourceType,
} from "./editorState";
import type { OptionalComp } from "barda-core";
import type { PositionParams } from "layout";
import type { LayoutModeType } from "@barda/layout/gridLayoutPropTypes";
import type { UiLayoutType } from "./comps/uiComp";
import type { CompItemType } from "./comps/containerBase";
import type { GridItemComp } from "./comps/gridItemComp";
import { isContainer } from "./comps/containerBase";
import { hookCompCategory, isHookComp } from "./hooks/hookCompTypes";
import { BottomResComp, BottomResListComp, BottomResTypeEnum } from "types/bottomRes";
import { NameGenerator } from "./utils";
import type { NameAndExposingInfo } from "./utils/exposingTypes";
import { trans } from "i18n";
import { checkName } from "./utils/rename";
import { renameAction } from "barda-core";

/**
 * Compatibility layer: provides an object that mimics the old EditorState API,
 * backed by the zustand store. This allows all existing consumers using
 * useContext(EditorContext) to continue working during phased migration.
 */
export function createEditorStateCompat(): EditorStateCompat {
  const store = useEditorStore.getState();
  return new EditorStateCompat(store);
}

/**
 * React hook that returns a compat object which updates when the store changes.
 * Uses useRef + useEffect + useReducer instead of useSyncExternalStore to avoid
 * React 18 "getSnapshot should be cached" warnings when zustand fires subscribers
 * on every set() call (including empty partials).
 *
 * The compat ref is synced with the store on every render so it never returns
 * a stale snapshot — even when the store was updated between initial mount
 * and the subscription effect registering.
 */
export function useEditorStateCompat(): EditorStateCompat {
  const [, forceUpdate] = useReducer((x: number) => x + 1, 0);
  const compatRef = useRef<EditorStateCompat>(
    new EditorStateCompat(useEditorStore.getState())
  );

  // Always sync with the latest store state during render.
  // The subscription below drives re-renders; this line guarantees the
  // returned object is never stale when a re-render happens (regardless
  // of whether the subscription callback has caught up yet).
  compatRef.current = new EditorStateCompat(useEditorStore.getState());

  useEffect(() => {
    const unsub = useEditorStore.subscribe(() => {
      forceUpdate();
    });
    return unsub;
  }, []);

  return compatRef.current;
}

// Re-export these types for convenience
export type { ESRootCompType, CompInfo, SelectSourceType };

export class EditorStateCompat {
  // Public fields mirroring EditorState's readonly properties
  readonly rootComp: ESRootCompType;
  readonly showPropertyPane: boolean;
  readonly selectedCompNames: Set<string>;
  readonly isDragging: boolean;
  readonly draggingCompType: string;
  readonly forceShowGrid: boolean;
  readonly disableInteract: boolean;
  readonly selectedBottomResName: string;
  readonly selectedBottomResType?: BottomResTypeEnum;
  readonly showResultCompName: string;
  readonly selectSource?: SelectSourceType;
  readonly isPasting: boolean;
  readonly isCodeEditorPanelOpen: boolean;

  constructor(state: ReturnType<typeof useEditorStore.getState>) {
    this.rootComp = state.rootComp as ESRootCompType;
    this.showPropertyPane = state.showPropertyPane;
    this.selectedCompNames = state.selectedCompNames;
    this.isDragging = state.isDragging;
    this.draggingCompType = state.draggingCompType;
    this.forceShowGrid = state.forceShowGrid;
    this.disableInteract = state.disableInteract;
    this.selectedBottomResName = state.selectedBottomResName;
    this.selectedBottomResType = state.selectedBottomResType;
    this.showResultCompName = state.showResultCompName;
    this.selectSource = state.selectSource;
    this.isPasting = state.isPasting;
    this.isCodeEditorPanelOpen = state.isCodeEditorPanelOpen;
  }

  // ==================== Setters (delegate to zustand store) ====================

  setDragging(dragging: boolean) {
    useEditorStore.getState().setDragging(dragging);
  }

  setDraggingCompType(draggingComp: string) {
    useEditorStore.getState().setDraggingCompType(draggingComp);
  }

  setForceShowGrid(forceShowGrid: boolean) {
    useEditorStore.getState().setForceShowGrid(forceShowGrid);
  }

  setDisableInteract(disableInteract: boolean) {
    useEditorStore.getState().setDisableInteract(disableInteract);
  }

  setShowPropertyPane(showPropertyPane: boolean) {
    useEditorStore.getState().setShowPropertyPane(showPropertyPane);
  }

  setComp(compFn: (comp: ESRootCompType) => ESRootCompType) {
    const store = useEditorStore.getState();
    const comp = store.rootComp;
    if (comp) {
      // Reset pasting flag when comp changes
      useEditorStore.setState({ rootComp: compFn(comp), isPasting: false });
    }
  }

  setSelectedCompNames(selectedCompNames: Set<string>, selectSource?: SelectSourceType) {
    useEditorStore.getState().setSelectedCompNames(selectedCompNames, selectSource);
  }

  setSelectedBottomRes(name: string, type?: BottomResTypeEnum) {
    useEditorStore.getState().setSelectedBottomRes(name, type);
  }

  setShowResultCompName(showResultCompName: string | undefined) {
    useEditorStore.getState().setShowResultCompName(showResultCompName ?? "");
  }

  setIsPasting(isPasting: boolean) {
    useEditorStore.getState().setIsPasting(isPasting);
  }

  setCodeEditorPanelOpen(isOpen: boolean) {
    useEditorStore.getState().setCodeEditorPanelOpen(isOpen);
  }

  // ==================== Core accessors ====================

  getUIComp() {
    return this.rootComp?.children.ui;
  }

  getModuleLayoutComp() {
    return this.getUIComp()?.getModuleLayoutComp();
  }

  isModule() {
    return !!this.getModuleLayoutComp();
  }

  getHooksComp() {
    return this.rootComp?.children.hooks;
  }

  getQueriesComp() {
    return this.rootComp?.children.queries;
  }

  getTempStatesComp() {
    return this.rootComp?.children.tempStates;
  }

  getTransformersComp() {
    return this.rootComp?.children.transformers;
  }

  getDataRespondersComp() {
    return this.rootComp?.children.dataResponders;
  }

  getFoldersComp() {
    return this.rootComp?.children.folders;
  }

  getAppSettingsComp() {
    return this.rootComp?.children.settings;
  }

  getBottomResListComp(type: BottomResTypeEnum): BottomResListComp {
    switch (type) {
      case BottomResTypeEnum.Query:
        return this.getQueriesComp()!;
      case BottomResTypeEnum.TempState:
        return this.getTempStatesComp()!;
      case BottomResTypeEnum.Transformer:
        return this.getTransformersComp()!;
      case BottomResTypeEnum.DateResponder:
        return this.getDataRespondersComp()!;
      case BottomResTypeEnum.Folder:
        return this.getFoldersComp()!;
    }
  }

  getBottomResComp(name: string): BottomResComp | undefined {
    const bottomResComps = Object.values(BottomResTypeEnum).reduce<BottomResComp[]>((a, b) => {
      const items = this.getBottomResListComp(b).items();
      return a.concat(items);
    }, []);
    return bottomResComps.find((i) => i.name() === name);
  }

  // ==================== Derived data ====================

  private getAllHooksCompMap() {
    return this.getHooksComp()?.getAllCompItems() ?? {};
  }

  getAllCompMap() {
    return { ...this.getAllHooksCompMap(), ...this.getUIComp()?.getAllCompItems() ?? {} };
  }

  getAllUICompMap() {
    const ret = { ...(this.getUIComp()?.getAllCompItems() ?? {}) };
    Object.entries(this.getAllHooksCompMap()).forEach(([key, item]) => {
      const type = item.children.compType.getView();
      if (!isHookComp(type) || hookCompCategory(type) === "ui") {
        ret[key] = item;
      }
    });
    return ret;
  }

  getUICompByName(name: string) {
    const compMap = this.getAllUICompMap();
    return Object.values(compMap).find((item) => item.children.name.getView() === name);
  }

  nameAndExposingInfo(): NameAndExposingInfo {
    return this.rootComp?.nameAndExposingInfo() ?? {};
  }

  getNameGenerator() {
    const nameGenerator = new NameGenerator();
    const exposingInfo = this.nameAndExposingInfo();
    nameGenerator.init(Object.keys(exposingInfo));
    return nameGenerator;
  }

  uiCompInfoList(filter?: (item: CompItemType) => boolean): CompInfo[] {
    const compMap = this.getAllUICompMap();
    const filteredMap = filter
      ? _.filter(Object.values(compMap), filter)
      : Object.values(compMap);
    return filteredMap.map((item) => ({
      name: item.children.name.getView(),
      type: item.children.compType.getView(),
      data: item.children.comp.exposingValues,
      dataDesc: item.children.comp.exposingInfo().propertyDesc,
    }));
  }

  bottomResComInfoList(): CompInfo[] {
    const queryComInfoList = this.queryCompInfoList();
    const stateComInfoList = this.getTempStateCompInfoList();
    const transformerComInfoList = this.getTransformerCompInfoList();
    const dataResponderInfoList = this.getDataResponderInfoList();
    return [...queryComInfoList, ...stateComInfoList, ...transformerComInfoList, ...dataResponderInfoList];
  }

  queryCompInfoList(): CompInfo[] {
    const exposingInfo = this.getQueriesComp()?.nameAndExposingInfo() ?? {};
    return this.getQueriesComp()?.getView().map((item) =>
      this.getCompInfo(exposingInfo, item.children.name.getView(), BottomResTypeEnum.Query)
    ) ?? [];
  }

  getTempStateCompInfoList(): CompInfo[] {
    const listComp = this.getTempStatesComp();
    const exposingInfo = listComp?.nameAndExposingInfo() ?? {};
    return listComp?.getView().map((item) =>
      this.getCompInfo(exposingInfo, item.children.name.getView(), BottomResTypeEnum.TempState)
    ) ?? [];
  }

  getTransformerCompInfoList(): CompInfo[] {
    const listComp = this.getTransformersComp();
    const exposingInfo = listComp?.nameAndExposingInfo() ?? {};
    return listComp?.getView().map((item) =>
      this.getCompInfo(exposingInfo, item.children.name.getView(), BottomResTypeEnum.Transformer)
    ) ?? [];
  }

  getDataResponderInfoList(): CompInfo[] {
    const listComp = this.getDataRespondersComp();
    const exposingInfo = listComp?.nameAndExposingInfo() ?? {};
    return listComp?.getView().map((item) =>
      this.getCompInfo(exposingInfo, item.children.name.getView(), BottomResTypeEnum.DateResponder)
    ) ?? [];
  }

  hooksCompInfoList(): CompInfo[] {
    return Object.values(this.getAllHooksCompMap()).map((item) => ({
      name: item.children.name.getView(),
      type: item.children.compType.getView(),
      data: item.children.comp.exposingValues,
      dataDesc: item.children.comp.exposingInfo().propertyDesc,
    }));
  }

  getCompInfo(
    nameAndExposingInfo: NameAndExposingInfo,
    name: string,
    type: string
  ): CompInfo {
    return {
      name,
      type,
      data: (nameAndExposingInfo[name]?.propertyValue ?? {}) as Record<string, any>,
      dataDesc: nameAndExposingInfo[name]?.propertyDesc ?? {},
    };
  }

  getBottomResItemFromList(listComp: BottomResListComp) {
    return listComp.items().find((i) => i.id() === this.selectedBottomResName);
  }

  // ==================== Selection queries ====================

  selectedComp(): OptionalComp {
    const selectedComps = Object.values(this.selectedComps());
    return selectedComps.length === 1 ? selectedComps[0] : undefined;
  }

  selectedComps() {
    const compMap = this.getAllCompMap();
    return _.pickBy(compMap, (item) =>
      this.selectedCompNames.has(item.children.name.getView())
    );
  }

  selectedContainer() {
    const selectedComps = this.selectedComps();
    if (_.size(selectedComps) === 0) {
      return this.getUIComp()?.getComp();
    }
    const [key, comp] = _.toPairs(selectedComps)[0];
    if (_.size(selectedComps) === 1 && isContainer((comp as GridItemComp)?.children?.comp)) {
      return comp.children.comp;
    }
    return this.findContainer(key) ?? this.getUIComp()?.getComp();
  }

  findContainer(compKey: string) {
    return (
      this.getUIComp()?.getComp()?.findContainer?.(compKey) ||
      this.getHooksComp()?.findContainer(compKey)
    );
  }

  findUIParentContainer(compName: string, containerCompType?: string) {
    return (
      this.getUIComp()?.findParentContainer(compName, containerCompType) ||
      this.getHooksComp()?.findParentContainer(compName, containerCompType)
    );
  }

  isCompSelected(compName: string): OptionalComp {
    const compMap = this.getAllCompMap();
    return Object.values(compMap).find(
      (item) =>
        item.children.name.getView() === compName && this.selectedCompNames.has(compName)
    );
  }

  selectedBottomResComp(): BottomResComp | undefined {
    return this.getBottomResComp(this.selectedBottomResName);
  }

  selectedQueryComp() {
    if (
      this.selectedBottomResType !== BottomResTypeEnum.Query ||
      !this.selectedBottomResName
    ) {
      return undefined;
    }
    return this.getQueriesComp()?.getView().find(
      (queryComp) => queryComp.children.name.getView() === this.selectedBottomResName
    );
  }

  selectedOrFirstQueryComp() {
    const selectedQueryComp = this.selectedQueryComp();
    if (selectedQueryComp) {
      return selectedQueryComp;
    }
    return this.getQueriesComp()?.getView()[0];
  }

  showResultComp(): BottomResComp | undefined {
    const bottomResComps = Object.values(BottomResTypeEnum).reduce<BottomResComp[]>(
      (a, b) => {
        const items = this.getBottomResListComp(b).items();
        return a.concat(items);
      },
      []
    );
    return bottomResComps.find((i) => i.name() === this.showResultCompName);
  }

  // ==================== App settings ====================

  getAppSettings() {
    return this.getAppSettingsComp()?.getView();
  }

  getAppType(): UiLayoutType | undefined {
    return this.getUIComp()?.children.compType.getView();
  }

  getLayoutMode(): LayoutModeType | undefined {
    return this.getAppSettings()?.layoutMode;
  }

  // ==================== Grid / canvas ====================

  canvasPositionParams(): PositionParams | undefined {
    return this.getUIComp()?.getComp()?.getPositionParams();
  }

  showGridLines() {
    return this.isDragging || this.forceShowGrid;
  }

  // ==================== Rename ====================

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
      this.rootComp?.dispatch(renameAction(oldName, name));
    }
    return true;
  }
}

/**
 * Wrapper component that provides the compat object via render props.
 * Used as a drop-in replacement for <EditorContext.Consumer> during migration.
 */
export function EditorStateView({ children }: { children: (es: EditorStateCompat) => ReactNode }) {
  const es = useEditorStateCompat();
  return <>{children(es)}</>;
}

/**
 * Kept for backward compatibility during migration.
 * Once all consumers are migrated, this context can be removed.
 */
export const EditorContext = React.createContext<EditorStateCompat>(undefined as any);

export const CompNameContext = React.createContext<string>(undefined as any);
