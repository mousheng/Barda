import { useMemo } from "react";
import _ from "lodash";
import { useEditorStore } from "./editorStore";
import type { CompInfo } from "./editorState";
import { BottomResComp, BottomResListComp, BottomResTypeEnum } from "types/bottomRes";
import { NameGenerator } from "./utils";
import { NameAndExposingInfo } from "./utils/exposingTypes";
import { CompItemType, isContainer } from "./comps/containerBase";
import type { GridItemComp } from "./comps/gridItemComp";
import { hookCompCategory, isHookComp } from "./hooks/hookCompTypes";
import type { UiLayoutType } from "./comps/uiComp";
import { OptionalComp } from "barda-core";
import type { PositionParams } from "layout";
import type { LayoutModeType } from "@barda/layout/gridLayoutPropTypes";

// ==================== Core accessors ====================

export function useRootComp() {
  return useEditorStore((s) => s.rootComp);
}

export function useUIComp() {
  return useEditorStore((s) => s.rootComp?.children.ui);
}

export function useHooksComp() {
  return useEditorStore((s) => s.rootComp?.children.hooks);
}

export function useQueriesComp() {
  return useEditorStore((s) => s.rootComp?.children.queries);
}

export function useTempStatesComp() {
  return useEditorStore((s) => s.rootComp?.children.tempStates);
}

export function useTransformersComp() {
  return useEditorStore((s) => s.rootComp?.children.transformers);
}

export function useDataRespondersComp() {
  return useEditorStore((s) => s.rootComp?.children.dataResponders);
}

export function useFoldersComp() {
  return useEditorStore((s) => s.rootComp?.children.folders);
}

export function useAppSettingsComp() {
  return useEditorStore((s) => s.rootComp?.children.settings);
}

export function useModuleLayoutComp() {
  const uiComp = useUIComp();
  return useMemo(() => uiComp?.getModuleLayoutComp(), [uiComp]);
}

// ==================== Selection state ====================

export function useSelectedCompNames() {
  return useEditorStore((s) => s.selectedCompNames);
}

export function useIsDragging() {
  return useEditorStore((s) => s.isDragging);
}

export function useShowPropertyPane() {
  return useEditorStore((s) => s.showPropertyPane);
}

export function useSelectedBottomResName() {
  return useEditorStore((s) => s.selectedBottomResName);
}

export function useSelectedBottomResType() {
  return useEditorStore((s) => s.selectedBottomResType);
}

export function useSelectSource() {
  return useEditorStore((s) => s.selectSource);
}

export function useShowGridLines() {
  const isDragging = useEditorStore((s) => s.isDragging);
  const forceShowGrid = useEditorStore((s) => s.forceShowGrid);
  return isDragging || forceShowGrid;
}

// ==================== Derived: component maps ====================

export function useAllHooksCompMap() {
  const hooksComp = useHooksComp();
  return useMemo(() => hooksComp?.getAllCompItems() ?? {}, [hooksComp]);
}

export function useAllUICompMap() {
  const uiComp = useUIComp();
  const allHooksCompMap = useAllHooksCompMap();

  return useMemo(() => {
    const ret = { ...(uiComp?.getAllCompItems() ?? {}) };
    Object.entries(allHooksCompMap).forEach(([key, item]) => {
      const type = item.children.compType.getView();
      if (!isHookComp(type) || hookCompCategory(type) === "ui") {
        ret[key] = item;
      }
    });
    return ret;
  }, [uiComp, allHooksCompMap]);
}

export function useAllCompMap() {
  const allHooksCompMap = useAllHooksCompMap();
  const allUICompMap = useAllUICompMap();
  return useMemo(() => ({ ...allHooksCompMap, ...allUICompMap }), [allHooksCompMap, allUICompMap]);
}

// ==================== Derived: name & exposing ====================

export function useNameAndExposingInfo(): NameAndExposingInfo {
  const rootComp = useRootComp();
  return useMemo(() => rootComp?.nameAndExposingInfo() ?? {}, [rootComp]);
}

export function useNameGenerator() {
  const exposingInfo = useNameAndExposingInfo();
  return useMemo(() => {
    const nameGenerator = new NameGenerator();
    nameGenerator.init(Object.keys(exposingInfo));
    return nameGenerator;
  }, [exposingInfo]);
}

// ==================== Derived: selected comps ====================

export function useSelectedComps() {
  const compMap = useAllCompMap();
  const selectedCompNames = useSelectedCompNames();
  return useMemo(
    () => _.pickBy(compMap, (item) => selectedCompNames.has(item.children.name.getView())),
    [compMap, selectedCompNames]
  );
}

export function useSelectedComp(): OptionalComp {
  const selectedComps = useSelectedComps();
  return useMemo(() => {
    const values = Object.values(selectedComps);
    return values.length === 1 ? values[0] : undefined;
  }, [selectedComps]);
}

export function useSelectedContainer() {
  const selectedComps = useSelectedComps();
  const uiComp = useUIComp();
  const hooksComp = useHooksComp();

  return useMemo(() => {
    if (_.size(selectedComps) === 0) {
      return uiComp?.getComp();
    }
    const [key, comp] = _.toPairs(selectedComps)[0];
    if (_.size(selectedComps) === 1 && isContainer((comp as GridItemComp)?.children?.comp)) {
      return comp.children.comp;
    }
    return (
      (uiComp?.getComp()?.findContainer?.(key) ?? hooksComp?.findContainer(key)) ??
      uiComp?.getComp()
    );
  }, [selectedComps, uiComp, hooksComp]);
}

export function useFindContainer(compKey: string) {
  const uiComp = useUIComp();
  const hooksComp = useHooksComp();
  return useMemo(
    () => uiComp?.getComp()?.findContainer?.(compKey) || hooksComp?.findContainer(compKey),
    [uiComp, hooksComp, compKey]
  );
}

export function useFindUIParentContainer(compName: string, containerCompType?: string) {
  const uiComp = useUIComp();
  const hooksComp = useHooksComp();
  return useMemo(
    () =>
      uiComp?.findParentContainer(compName, containerCompType) ||
      hooksComp?.findParentContainer(compName, containerCompType),
    [uiComp, hooksComp, compName, containerCompType]
  );
}

export function useIsCompSelected(compName: string): OptionalComp {
  const compMap = useAllCompMap();
  const selectedCompNames = useSelectedCompNames();
  return useMemo(
    () =>
      Object.values(compMap).find(
        (item) =>
          item.children.name.getView() === compName && selectedCompNames.has(compName)
      ),
    [compMap, selectedCompNames, compName]
  );
}

// ==================== Derived: bottom res ====================

export function useBottomResListComp(type: BottomResTypeEnum): BottomResListComp | undefined {
  const queriesComp = useQueriesComp();
  const tempStatesComp = useTempStatesComp();
  const transformersComp = useTransformersComp();
  const dataRespondersComp = useDataRespondersComp();
  const foldersComp = useFoldersComp();

  return useMemo(() => {
    switch (type) {
      case BottomResTypeEnum.Query:
        return queriesComp;
      case BottomResTypeEnum.TempState:
        return tempStatesComp;
      case BottomResTypeEnum.Transformer:
        return transformersComp;
      case BottomResTypeEnum.DateResponder:
        return dataRespondersComp;
      case BottomResTypeEnum.Folder:
        return foldersComp;
    }
  }, [queriesComp, tempStatesComp, transformersComp, dataRespondersComp, foldersComp, type]);
}

export function useBottomResComp(name: string): BottomResComp | undefined {
  const queriesComp = useQueriesComp();
  const tempStatesComp = useTempStatesComp();
  const transformersComp = useTransformersComp();
  const dataRespondersComp = useDataRespondersComp();
  const foldersComp = useFoldersComp();

  return useMemo(() => {
    const allComps = [
      ...(queriesComp?.getView() ?? []),
      ...(tempStatesComp?.getView() ?? []),
      ...(transformersComp?.getView() ?? []),
      ...(dataRespondersComp?.getView() ?? []),
      ...(foldersComp?.getView() ?? []),
    ] as BottomResComp[];
    return allComps.find((i) => i.name() === name);
  }, [queriesComp, tempStatesComp, transformersComp, dataRespondersComp, foldersComp, name]);
}

export function useSelectedBottomResComp(): BottomResComp | undefined {
  const selectedBottomResName = useSelectedBottomResName();
  return useBottomResComp(selectedBottomResName);
}

export function useSelectedQueryComp() {
  const selectedBottomResType = useSelectedBottomResType();
  const selectedBottomResName = useSelectedBottomResName();
  const queriesComp = useQueriesComp();

  return useMemo(() => {
    if (selectedBottomResType !== BottomResTypeEnum.Query || !selectedBottomResName) {
      return undefined;
    }
    return queriesComp?.getView().find(
      (queryComp) => queryComp.children.name.getView() === selectedBottomResName
    );
  }, [selectedBottomResType, selectedBottomResName, queriesComp]);
}

export function useSelectedOrFirstQueryComp() {
  const selectedQueryComp = useSelectedQueryComp();
  const queriesComp = useQueriesComp();
  return useMemo(
    () => selectedQueryComp ?? queriesComp?.getView()[0],
    [selectedQueryComp, queriesComp]
  );
}

export function useShowResultComp(): BottomResComp | undefined {
  const showResultCompName = useEditorStore((s) => s.showResultCompName);
  const queriesComp = useQueriesComp();
  const tempStatesComp = useTempStatesComp();
  const transformersComp = useTransformersComp();
  const dataRespondersComp = useDataRespondersComp();
  const foldersComp = useFoldersComp();

  return useMemo(() => {
    const allComps = [
      ...(queriesComp?.getView() ?? []),
      ...(tempStatesComp?.getView() ?? []),
      ...(transformersComp?.getView() ?? []),
      ...(dataRespondersComp?.getView() ?? []),
      ...(foldersComp?.getView() ?? []),
    ] as BottomResComp[];
    return allComps.find((i) => i.name() === showResultCompName);
  }, [showResultCompName, queriesComp, tempStatesComp, transformersComp, dataRespondersComp, foldersComp]);
}

export function useGetBottomResItemFromList(listComp: BottomResListComp | undefined) {
  const selectedBottomResName = useSelectedBottomResName();
  return useMemo(
    () => listComp?.items().find((i) => i.id() === selectedBottomResName),
    [listComp, selectedBottomResName]
  );
}

// ==================== Derived: info lists ====================

function buildCompInfo(nameAndExposingInfo: NameAndExposingInfo, name: string, type: string): CompInfo {
  return {
    name,
    type,
    data: (nameAndExposingInfo[name]?.propertyValue ?? {}) as Record<string, any>,
    dataDesc: nameAndExposingInfo[name]?.propertyDesc ?? {},
  };
}

export function useQueryCompInfoList(): CompInfo[] {
  const queriesComp = useQueriesComp();
  const nameAndExposingInfo = useNameAndExposingInfo();
  return useMemo(
    () =>
      queriesComp?.getView().map((item) =>
        buildCompInfo(nameAndExposingInfo, item.children.name.getView(), BottomResTypeEnum.Query)
      ) ?? [],
    [queriesComp, nameAndExposingInfo]
  );
}

export function useTempStateCompInfoList(): CompInfo[] {
  const tempStatesComp = useTempStatesComp();
  const exposingInfo = useMemo(() => tempStatesComp?.nameAndExposingInfo() ?? {}, [tempStatesComp]);
  return useMemo(
    () =>
      tempStatesComp?.getView().map((item) =>
        buildCompInfo(exposingInfo, item.children.name.getView(), BottomResTypeEnum.TempState)
      ) ?? [],
    [tempStatesComp, exposingInfo]
  );
}

export function useTransformerCompInfoList(): CompInfo[] {
  const transformersComp = useTransformersComp();
  const exposingInfo = useMemo(() => transformersComp?.nameAndExposingInfo() ?? {}, [transformersComp]);
  return useMemo(
    () =>
      transformersComp?.getView().map((item) =>
        buildCompInfo(exposingInfo, item.children.name.getView(), BottomResTypeEnum.Transformer)
      ) ?? [],
    [transformersComp, exposingInfo]
  );
}

export function useDataResponderInfoList(): CompInfo[] {
  const dataRespondersComp = useDataRespondersComp();
  const exposingInfo = useMemo(() => dataRespondersComp?.nameAndExposingInfo() ?? {}, [dataRespondersComp]);
  return useMemo(
    () =>
      dataRespondersComp?.getView().map((item) =>
        buildCompInfo(exposingInfo, item.children.name.getView(), BottomResTypeEnum.DateResponder)
      ) ?? [],
    [dataRespondersComp, exposingInfo]
  );
}

export function useBottomResComInfoList(): CompInfo[] {
  const queryInfo = useQueryCompInfoList();
  const stateInfo = useTempStateCompInfoList();
  const transformerInfo = useTransformerCompInfoList();
  const dataResponderInfo = useDataResponderInfoList();
  return useMemo(
    () => [...queryInfo, ...stateInfo, ...transformerInfo, ...dataResponderInfo],
    [queryInfo, stateInfo, transformerInfo, dataResponderInfo]
  );
}

export function useUICompInfoList(filter?: (item: CompItemType) => boolean): CompInfo[] {
  const compMap = useAllUICompMap();
  return useMemo(() => {
    const values = filter ? _.filter(Object.values(compMap), filter) : Object.values(compMap);
    return values.map((item) => ({
      name: item.children.name.getView(),
      type: item.children.compType.getView(),
      data: item.children.comp.exposingValues,
      dataDesc: item.children.comp.exposingInfo().propertyDesc,
    }));
  }, [compMap, filter]);
}

export function useHooksCompInfoList(): CompInfo[] {
  const allHooksCompMap = useAllHooksCompMap();
  return useMemo(
    () =>
      Object.values(allHooksCompMap).map((item) => ({
        name: item.children.name.getView(),
        type: item.children.compType.getView(),
        data: item.children.comp.exposingValues,
        dataDesc: item.children.comp.exposingInfo().propertyDesc,
      })),
    [allHooksCompMap]
  );
}

// ==================== Derived: app settings ====================

export function useAppSettings() {
  const appSettingsComp = useAppSettingsComp();
  return useMemo(() => appSettingsComp?.getView(), [appSettingsComp]);
}

export function useAppType(): UiLayoutType | undefined {
  const uiComp = useUIComp();
  return useMemo(() => uiComp?.children.compType.getView(), [uiComp]);
}

export function useLayoutMode(): LayoutModeType | undefined {
  const appSettings = useAppSettings();
  return appSettings?.layoutMode;
}

export function useIsModule() {
  const moduleLayoutComp = useModuleLayoutComp();
  return !!moduleLayoutComp;
}

export function useCanvasPositionParams(): PositionParams | undefined {
  const uiComp = useUIComp();
  return useMemo(() => uiComp?.getComp()?.getPositionParams(), [uiComp]);
}

// ==================== Derived: get UI comp by name ====================

export function useUICompByName(name: string) {
  const allUICompMap = useAllUICompMap();
  return useMemo(() => {
    if (!name) return undefined;
    return Object.values(allUICompMap).find((item) => item.children.name.getView() === name);
  }, [allUICompMap, name]);
}

// ==================== Pure function exports (for non-React usage) ====================

export function getStoreForNonReact() {
  return useEditorStore.getState();
}
