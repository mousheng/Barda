import { CompConstructor } from "baseComps/comp";
import _ from "lodash";
import { JSONValue } from "util/jsonTypes";
import {
  ActionContextType,
  ActionExtraInfo,
  BroadcastAction,
  ChangeValueAction,
  CompAction,
  CompActionTypes,
  CustomAction,
  ExecuteQueryAction,
  MultiChangeAction,
  RenameAction,
  ReplaceCompAction,
  RouteByNameAction,
  SimpleCompAction,
  TriggerModuleEventAction,
  UpdateActionContextAction,
  UpdateNodesV2Action,
} from "./actionTypes";

/**
 * 创建一个自定义操作对象
 * 
 * 此函数用于生成一个自定义操作对象，该对象表示一种非标准的、可由前端开发者自定义的UI操作
 * 它包含了执行操作所需的数据以及是否编辑DSL（领域特定语言）的标志
 * 
 * @param value - 操作中传递的数据，类型由使用者自定义
 * @param editDSL - 表示操作是否涉及编辑DSL的布尔值
 * @returns 返回一个包含类型、路径、值和是否编辑DSL的自定义操作对象
 */
export function customAction<DataType>(value: DataType, editDSL: boolean): CustomAction<DataType> {
  return {
    type: CompActionTypes.CUSTOM, // 操作的类型，这里表示自定义操作
    path: [], // 操作执行的路径，在此情况下不适用，因此为空数组
    value: value, // 操作中传递的自定义数据
    editDSL, // 标记操作是否涉及编辑DSL
  };
}

/**
 * 创建一个更新操作上下文的广播操作
 * 
 * @param context - 要更新的操作上下文
 * @returns 返回一个包含类型、路径、操作值和是否编辑DSL的广播操作对象
 * 
 * @remarks
 * 该函数用于生成一个广播操作，该操作表示更新操作上下文
 * 它包含了执行操作所需的数据以及是否编辑DSL（领域特定语言）的标志
 * 
 */
export function updateActionContextAction(
  context: ActionContextType
): BroadcastAction<UpdateActionContextAction> {
  const value: UpdateActionContextAction = {
    type: CompActionTypes.UPDATE_ACTION_CONTEXT,
    path: [],
    editDSL: false,
    context: context,
  };
  return {
    type: CompActionTypes.BROADCAST,
    path: [],
    editDSL: false,
    action: value,
  };
}

/**
 * 检查是否为当前的自定义操作
 * 
 * 该函数用于检查一个操作是否为自定义操作，并保证类型安全
 * 它使用了泛型来保证类型安全，使用者需要保证传入的类型与 T 相同，否则可能引起 bug
 * 
 * @param action - 要检查的操作
 * @param type - 自定义操作的类型
 * @returns 如果操作是自定义操作且类型匹配，则返回 true，否则返回 false
 */
export function isMyCustomAction<T>(action: CompAction, type: string): action is CustomAction<T> {
  return !isChildAction(action) && isCustomAction(action, type);
}

/**
 * 检查是否为自定义操作
 * 
 * 该函数用于检查一个操作是否为自定义操作，并保证类型安全
 * 它使用了泛型来保证类型安全，使用者需要保证传入的类型与 T 相同，否则可能引起 bug
 * 
 * @param action - 要检查的操作
 * @param type - 自定义操作的类型
 * @returns 如果操作是自定义操作且类型匹配，则返回 true，否则返回 false
 */
export function isCustomAction<T>(action: CompAction, type: string): action is CustomAction<T> {
  return action.type === CompActionTypes.CUSTOM && _.get(action.value, "type") === type;
}

/**
 * 执行查询的动作。
 * 精确地指向查询的路径路由。
 * 传递queryName时，RootComp将正确更改路径。
 *
 * @param props - 执行查询操作的属性。
 * @param props.args - 查询的参数。
 * @param props.afterExecFunc - 查询执行后要执行的函数。
 * @returns 返回一个ExecuteQueryAction对象。
 */
export function executeQueryAction(props: {
  args?: Record<string, unknown>;
  afterExecFunc?: () => void;
}): ExecuteQueryAction {
  return {
    type: CompActionTypes.EXECUTE_QUERY,
    path: [],
    editDSL: false,
    ...props,
  };
}

/**
 * 触发模块事件的操作。
 *
 * @param name - 要触发的模块事件的名称。
 * @returns 返回一个TriggerModuleEventAction对象。
 */
export function triggerModuleEventAction(name: string): TriggerModuleEventAction {
  return {
    type: CompActionTypes.TRIGGER_MODULE_EVENT,
    path: [],
    editDSL: false,
    name,
  };
}

/**
 * 更改值操作,最好使用comp.dispatchChangeValueAction来保证类型安全
 *
 * @param value - 要更改的值。
 * @param editDSL - 一个布尔值，表示是否编辑DSL（领域特定语言）。
 * @returns 返回一个ChangeValueAction对象。
 */
export function changeValueAction(value: JSONValue, editDSL: boolean): ChangeValueAction {
  return {
    type: CompActionTypes.CHANGE_VALUE,
    path: [],
    editDSL,
    value: value,
  };
}

export function isBroadcastAction<T extends CompAction>(
  action: CompAction,
  type: T["type"]
): action is BroadcastAction<T> {
  return action.type === CompActionTypes.BROADCAST && _.get(action.action, "type") === type;
}

export function renameAction(oldName: string, name: string): BroadcastAction<RenameAction> {
  const value: RenameAction = {
    type: CompActionTypes.RENAME,
    path: [],
    editDSL: true,
    oldName: oldName,
    name: name,
  };
  return {
    type: CompActionTypes.BROADCAST,
    path: [],
    editDSL: true,
    action: value,
  };
}

export function routeByNameAction(name: string, action: CompAction<any>): RouteByNameAction {
  return {
    type: CompActionTypes.ROUTE_BY_NAME,
    path: [],
    name: name,
    editDSL: action.editDSL,
    action: action,
  };
}

export function multiChangeAction(changes: Record<string, CompAction>): MultiChangeAction {
  const editDSL = Object.values(changes).some((action) => !!action.editDSL);
  console.assert(
    Object.values(changes).every(
      (action) => !_.isNil(action.editDSL) && action.editDSL === editDSL
    ),
    `multiChangeAction should wrap actions with the same editDSL value in property. editDSL: ${editDSL}\nchanges:`,
    changes
  );
  return {
    type: CompActionTypes.MULTI_CHANGE,
    path: [],
    editDSL,
    changes: changes,
  };
}

export function deleteCompAction(): SimpleCompAction {
  return {
    type: CompActionTypes.DELETE_COMP,
    path: [],
    editDSL: true,
  };
}

export function replaceCompAction(compFactory: CompConstructor): ReplaceCompAction {
  return {
    type: CompActionTypes.REPLACE_COMP,
    path: [],
    editDSL: false,
    compFactory: compFactory,
  };
}

export function onlyEvalAction(): SimpleCompAction {
  return {
    type: CompActionTypes.ONLY_EVAL,
    path: [],
    editDSL: false,
  };
}

export function wrapChildAction(childName: string, action: CompAction): CompAction {
  return {
    ...action,
    path: [childName, ...action.path],
  };
}

export function isChildAction(action: CompAction): boolean {
  return (action?.path?.length ?? 0) > 0;
}

export function unwrapChildAction(action: CompAction): [string, CompAction] {
  return [action.path[0], { ...action, path: action.path.slice(1) }];
}

export function changeChildAction(
  childName: string,
  value: JSONValue,
  editDSL: boolean
): CompAction {
  return wrapChildAction(childName, changeValueAction(value, editDSL));
}

export function updateNodesV2Action(value: any): UpdateNodesV2Action {
  return {
    type: CompActionTypes.UPDATE_NODES_V2,
    path: [],
    editDSL: false,
    value: value,
  };
}

/**
 * 包装操作的额外信息。
 *
 * @param action - 要包装的操作。
 * @param extraInfos - 要添加的额外信息。
 * @returns 返回一个包含额外信息的操作。
 */
export function wrapActionExtraInfo<T extends CompAction>(
  action: T,
  extraInfos: ActionExtraInfo
): T {
  return { ...action, extraInfo: { ...action.extraInfo, ...extraInfos } };
}

/**
 * 推迟执行操作。
 *
 * @param action - 要推迟的操作。
 * @returns 返回一个推迟执行的操作。
 */
export function deferAction<T extends CompAction>(action: T): T {
  return { ...action, priority: "defer" };
}

/**
 * 更改操作的编辑DSL标志。
 *
 * @param action - 要更改的操作。
 * @param editDSL - 一个布尔值，表示是否编辑DSL（领域特定语言）。
 * @returns 返回一个更改了编辑DSL标志的操作。
 */
export function changeEditDSLAction<T extends CompAction>(action: T, editDSL: boolean): T {
  return { ...action, editDSL };
}
