import { JSONValue } from "util/jsonTypes";
import { Comp } from "barda-core";
import { NameGenerator } from "comps/utils";
import { SimpleContainerComp } from "./simpleContainerComp";
import { CompTree } from "./utils";
import { SimpleFlowContainerComp } from "./simpleFlowContainerComp";

export interface IContainer extends Comp {
  realSimpleContainer(key?: string): SimpleContainerComp | SimpleFlowContainerComp | undefined;
  getCompTree(): CompTree;
  findContainer(key: string): IContainer | undefined;
  // 复制和粘贴时，重新生成一组唯一的组件
  getPasteValue(nameGenerator: NameGenerator): JSONValue;
}

export function isContainer(comp: Comp | undefined): comp is IContainer {
  return !!comp && !!(comp as any).getCompTree;
}

export function isSimpleFlowContainer(comp: Comp | undefined): comp is SimpleFlowContainerComp {
  return !!comp && (comp as any)["IsSimpleFlowContainer"];
}
