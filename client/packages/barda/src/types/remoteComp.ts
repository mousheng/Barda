import { UICompManifest } from "comps/uiCompRegistry";
import { CompConstructor } from "barda-core";

export type RemoteCompSource = "npm" | "bundle";
export interface BardaCompMeta extends Omit<UICompManifest, "comp" | "icon"> {
  icon?: string;
}

export interface BardaMeta {
  entry: string;
  description: string;
  comps: Record<string, BardaCompMeta>;
}

export interface NpmVersionMeta {
  name: string;
  version: string;
  barda: BardaMeta;
}

export interface NpmPackageMeta {
  name: string;
  versions: Record<string, NpmVersionMeta>;
  "dist-tags": {
    latest: string;
  };
}

export interface RemoteCompInfo {
  source: RemoteCompSource;
  compName: string;
  isRemote: true;
  packageName: string;
  packageVersion?: string;
}

export type RemoteCompLoader<T = RemoteCompInfo> = (info: T) => Promise<CompConstructor | null>;
