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
    set((s) => (s.rootComp === comp ? {} : { rootComp: comp })),

  setShowPropertyPane: (show) => set({ showPropertyPane: show }),

  setCodeEditorPanelOpen: (isOpen) =>
    set((s) => (s.isCodeEditorPanelOpen !== isOpen ? { isCodeEditorPanelOpen: isOpen } : {})),

  setSelectedCompNames: (names, selectSource) =>
    set((s) => {
      if (names.size === 0 && s.selectedCompNames.size === 0) {
        return {};
      }
      return {
        selectedCompNames: names,
        showPropertyPane: names.size > 0,
        selectSource,
      };
    }),

  setSelectedBottomRes: (name, type) =>
    set({ selectedBottomResName: name, selectedBottomResType: type }),

  setShowResultCompName: (name) => set({ showResultCompName: name }),

  setDragging: (dragging) =>
    set((s) => (s.isDragging !== dragging ? { isDragging: dragging } : {})),

  setDraggingCompType: (compType) =>
    set({ draggingCompType: compType, isDragging: true }),

  setForceShowGrid: (show) =>
    set((s) => (s.forceShowGrid !== show ? { forceShowGrid: show } : {})),

  setDisableInteract: (disable) =>
    set((s) => (s.disableInteract !== disable ? { disableInteract: disable } : {})),

  setIsPasting: (pasting) => set({ isPasting: pasting }),

  resetPasting: () => set({ isPasting: false }),
}));
