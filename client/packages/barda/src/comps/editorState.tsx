/**
 * Backward-compatible re-exports from the zustand-based editor state management.
 * All implementation has moved to editorCompat.tsx and editorStore.ts.
 */

import type React from "react";
import UIComp from "./comps/uiComp";
import { RootComp as RootCompTmp } from "./comps/rootComp";
import { QueryListComp } from "./queries/queryComp";
import { TemporaryStateListComp } from "./comps/temporaryStateComp";
import { TransformerListComp } from "./comps/transformerListComp";
import { DataChangeResponderListComp } from "./comps/dataChangeResponderComp";
import { FolderListComp } from "./comps/folderListComp";
import RefTreeComp from "./comps/refTreeComp";
import { HookListComp } from "./hooks/hookListComp";

export type ESRootCompType = InstanceType<typeof RootCompTmp>;
export type ESUICompType = InstanceType<typeof UIComp>;
export type ESQueryCompType = InstanceType<typeof QueryListComp>;
export type ESTempStateCompType = InstanceType<typeof TemporaryStateListComp>;
export type ESTransformerCompType = InstanceType<typeof TransformerListComp>;
export type ESDataResponderCompType = InstanceType<typeof DataChangeResponderListComp>;
export type ESFolderCompType = InstanceType<typeof FolderListComp>;
export type ESRefTreeCompType = InstanceType<typeof RefTreeComp>;
export type ESHookCompType = InstanceType<typeof HookListComp>;

export type CompInfo = {
  name: string;
  type: string;
  data: Record<string, any>;
  dataDesc: Record<string, React.ReactNode>;
};

export type SelectSourceType = "editor" | "leftPanel" | "addComp" | "rightPanel";

// Re-export EditorState compat class and contexts from the zustand-backed implementation
export { EditorStateCompat as EditorState } from "./editorCompat";
export { EditorContext, CompNameContext } from "./editorCompat";
