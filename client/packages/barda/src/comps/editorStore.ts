import { create } from "zustand";
import { BottomResTypeEnum } from "types/bottomRes";
import {
  ESRootCompType,
  SelectSourceType,
} from "./editorState";

export type { ESRootCompType, SelectSourceType } from "./editorState";

export interface EditorStore {
  // === Core (low frequency) ===
  rootComp: ESRootCompType | null;

  // === Panel state (low frequency) ===
  showPropertyPane: boolean;
  isCodeEditorPanelOpen: boolean;

  // === Selection (medium frequency) ===
  selectedCompNames: Set<string>;
  selectSource?: SelectSourceType;
  selectedBottomResName: string;
  selectedBottomResType?: BottomResTypeEnum;
  showResultCompName: string;

  // === Interaction (high frequency) ===
  isDragging: boolean;
  draggingCompType: string;
  forceShowGrid: boolean;
  disableInteract: boolean;
  isPasting: boolean;

  // === Actions ===
  setRootComp: (comp: ESRootCompType) => void;
  setShowPropertyPane: (show: boolean) => void;
  setCodeEditorPanelOpen: (isOpen: boolean) => void;
  setSelectedCompNames: (names: Set<string>, selectSource?: SelectSourceType) => void;
  setSelectedBottomRes: (name: string, type?: BottomResTypeEnum) => void;
  setShowResultCompName: (name: string | undefined) => void;
  setDragging: (dragging: boolean) => void;
  setDraggingCompType: (compType: string) => void;
  setForceShowGrid: (show: boolean) => void;
  setDisableInteract: (disable: boolean) => void;
  setIsPasting: (pasting: boolean) => void;
  resetPasting: () => void;
}

function isSameSet<T>(a: Set<T>, b: Set<T>) {
  if (a === b) return true;
  if (a.size !== b.size) return false;
  for (const item of a) {
    if (!b.has(item)) return false;
  }
  return true;
}

export const useEditorStore = create<EditorStore>()((set) => ({
  // === Initial values ===
  rootComp: null,
  showPropertyPane: false,
  isCodeEditorPanelOpen: false,
  selectedCompNames: new Set(),
  selectSource: undefined,
  selectedBottomResName: "",
  selectedBottomResType: undefined,
  showResultCompName: "",
  isDragging: false,
  draggingCompType: "button",
  forceShowGrid: false,
  disableInteract: false,
  isPasting: false,

  // === Actions ===
  setRootComp: (comp) =>
    set((s) => (s.rootComp === comp ? s : { rootComp: comp })),

  setShowPropertyPane: (show) => set({ showPropertyPane: show }),

  setCodeEditorPanelOpen: (isOpen) =>
    set((s) => (s.isCodeEditorPanelOpen !== isOpen ? { isCodeEditorPanelOpen: isOpen } : s)),

  setSelectedCompNames: (names, selectSource) =>
    set((s) => {
      const nextShowPropertyPane = names.size > 0;
      if (
        isSameSet(names, s.selectedCompNames) &&
        s.selectSource === selectSource &&
        s.showPropertyPane === nextShowPropertyPane
      ) {
        return s;
      }
      return {
        selectedCompNames: names,
        showPropertyPane: nextShowPropertyPane,
        selectSource,
      };
    }),

  setSelectedBottomRes: (name, type) =>
    set({ selectedBottomResName: name, selectedBottomResType: type }),

  setShowResultCompName: (name) => set({ showResultCompName: name }),

  setDragging: (dragging) =>
    set((s) => (s.isDragging !== dragging ? { isDragging: dragging } : s)),

  setDraggingCompType: (compType) =>
    set((s) =>
      s.draggingCompType !== compType
        ? { draggingCompType: compType, isDragging: true }
        : s
    ),

  setForceShowGrid: (show) =>
    set((s) => (s.forceShowGrid !== show ? { forceShowGrid: show } : s)),

  setDisableInteract: (disable) =>
    set((s) => (s.disableInteract !== disable ? { disableInteract: disable } : s)),

  setIsPasting: (pasting) =>
    set((s) => (s.isPasting !== pasting ? { isPasting: pasting } : s)),

  resetPasting: () =>
    set((s) => (s.isPasting ? { isPasting: false } : s)),
}));
