export const ReduxActionTypes = {
  /* 用户认证相关 */
  LOGOUT_USER_INIT: "LOGOUT_USER_INIT",                               //注销
  LOGOUT_USER_SUCCESS: "LOGOUT_USER_SUCCESS",                         //注销成功
  FETCH_USER_INIT: "FETCH_USER_INIT",                                 //获取用户初始化信息
  FETCH_USER_DETAILS_SUCCESS: "FETCH_USER_DETAILS_SUCCESS",           //获取用户信息成功
  FETCH_CURRENT_USER_SUCCESS: "FETCH_CURRENT_USER_SUCCESS",           //获取当前用户信息成功
  FETCH_RAW_CURRENT_USER: "FETCH_RAW_CURRENT_USER",                   //获取原始的当前用户信息
  FETCH_RAW_CURRENT_USER_SUCCESS: "FETCH_RAW_CURRENT_USER_SUCCESS",   //获取原始的当前用户信息成功

  /* 插件相关 */
  FETCH_DATA_SOURCE_TYPES: "FETCH_DATA_SOURCE_TYPES",                   // 获取数据源类型
  FETCH_DATA_SOURCE_TYPES_SUCCESS: "FETCH_DATA_SOURCE_TYPES_SUCCESS",   // 获取数据源类型成功
  /* << 插件相关 */

  /* 数据源相关 */
  FETCH_DATASOURCE_INIT: "FETCH_DATASOURCE_INIT",                       // 初始化获取数据源
  FETCH_DATASOURCE_BY_APP_INIT: "FETCH_DATASOURCE_BY_APP_INIT",         // 按应用初始化获取数据源
  FETCH_DATASOURCE_SUCCESS: "FETCH_DATASOURCE_SUCCESS",                 // 获取数据源成功
  FETCH_DATASOURCE_STRUCTURE_INIT: "FETCH_DATASOURCE_STRUCTURE_INIT",   // 初始化获取数据源结构
  FETCH_DATASOURCE_STRUCTURE_SUCCESS: "FETCH_DATASOURCE_STRUCTURE_SUCCESS", // 获取数据源结构成功
  CREATE_DATASOURCE_INIT: "CREATE_DATASOURCE_INIT",                     // 初始化创建数据源
  CREATE_DATASOURCE_SUCCESS: "CREATE_DATASOURCE_SUCCESS",               // 创建数据源成功
  UPDATE_DATASOURCE_INIT: "UPDATE_DATASOURCE_INIT",                     // 初始化更新数据源
  UPDATE_DATASOURCE_SUCCESS: "UPDATE_DATASOURCE_SUCCESS",               // 更新数据源成功
  DELETE_DATASOURCE_INIT: "DELETE_DATASOURCE_INIT",                     // 初始化删除数据源
  DELETE_DATASOURCE_SUCCESS: "DELETE_DATASOURCE_SUCCESS",               // 删除数据源成功
  FETCH_DATASOURCE_PERMISSION_INIT: "FETCH_DATASOURCE_PERMISSION_INIT", // 初始化获取数据源权限
  FETCH_DATASOURCE_PERMISSION_SUCCESS: "FETCH_DATASOURCE_PERMISSION_SUCCESS", // 获取数据源权限成功
  GRANT_DATASOURCE_PERMISSION_INIT: "GRANT_DATASOURCE_PERMISSION_INIT", // 初始化授予数据源权限
  UPDATE_DATASOURCE_PERMISSION_INIT: "UPDATE_DATASOURCE_PERMISSION_INIT", // 初始化更新数据源权限
  UPDATE_DATASOURCE_PERMISSION_SUCCESS: "UPDATE_DATASOURCE_PERMISSION_SUCCESS", // 更新数据源权限成功
  DELETE_DATASOURCE_PERMISSION_INIT: "DELETE_DATASOURCE_PERMISSION_INIT", // 初始化删除数据源权限
  DELETE_DATASOURCE_PERMISSION_SUCCESS: "DELETE_DATASOURCE_PERMISSION_SUCCESS", // 删除数据源权限成功
  /* << 数据源相关 */

  /* 查询相关 */
  FETCH_QUERY_LIBRARY_BY_ORG_INIT: "FETCH_QUERY_LIBRARY_BY_ORG_INIT",                   // 初始化获取组织的查询库
  FETCH_QUERY_LIBRARY_BY_ORG_SUCCESS: "FETCH_QUERY_LIBRARY_BY_ORG_SUCCESS",             // 获取组织的查询库成功
  FETCH_QUERY_LIBRARY_DROPDOWN_INIT: "FETCH_QUERY_LIBRARY_DROPDOWN_INIT",               // 初始化获取查询库下拉列表
  FETCH_QUERY_LIBRARY_DROPDOWN_SUCCESS: "FETCH_QUERY_LIBRARY_DROPDOWN_SUCCESS",         // 获取查询库下拉列表成功
  CREATE_QUERY_LIBRARY_INIT: "CREATE_QUERY_LIBRARY_INIT",                               // 初始化创建查询库
  CREATE_QUERY_LIBRARY_SUCCESS: "CREATE_QUERY_LIBRARY_SUCCESS",                         // 创建查询库成功
  UPDATE_QUERY_LIBRARY_INIT: "UPDATE_QUERY_LIBRARY_INIT",                               // 初始化更新查询库
  UPDATE_QUERY_LIBRARY_SUCCESS: "UPDATE_QUERY_LIBRARY_SUCCESS",                         // 更新查询库成功
  DELETE_QUERY_LIBRARY_INIT: "DELETE_QUERY_LIBRARY_INIT",                               // 初始化删除查询库
  DELETE_QUERY_LIBRARY_SUCCESS: "DELETE_QUERY_LIBRARY_SUCCESS",                         // 删除查询库成功
  FETCH_QUERY_LIBRARY_RECORD_DSL_INIT: "FETCH_QUERY_LIBRARY_RECORD_DSL_INIT",           // 初始化获取查询库记录DSL
  FETCH_QUERY_LIBRARY_RECORD_DSL_SUCCESS: "FETCH_QUERY_LIBRARY_RECORD_DSL_SUCCESS",     // 获取查询库记录DSL成功
  FETCH_QUERY_LIBRARY_RECORD: "FETCH_QUERY_LIBRARY_RECORD",                             // 获取查询库记录
  FETCH_QUERY_LIBRARY_RECORD_SUCCESS: "FETCH_QUERY_LIBRARY_RECORD_SUCCESS",             // 获取查询库记录成功
  CREATE_QUERY_LIBRARY_RECORD: "CREATE_QUERY_LIBRARY_RECORD",                           // 创建查询库记录
  CREATE_QUERY_LIBRARY_RECORD_SUCCESS: "CREATE_QUERY_LIBRARY_RECORD_SUCCESS",           // 创建查询库记录成功
  /* << 查询相关 */

  /* 文件夹相关 */
  CREATE_FOLDER_INIT: "CREATE_FOLDER_INIT",                                             // 初始化创建文件夹
  CREATE_FOLDER_SUCCESS: "CREATE_FOLDER_SUCCESS",                                       // 创建文件夹成功
  UPDATE_FOLDER_INIT: "UPDATE_FOLDER_INIT",                                             // 初始化更新文件夹
  UPDATE_FOLDER_SUCCESS: "UPDATE_FOLDER_SUCCESS",                                       // 更新文件夹成功
  DELETE_FOLDER_INIT: "DELETE_FOLDER_INIT",                                             // 初始化删除文件夹
  DELETE_FOLDER_SUCCESS: "DELETE_FOLDER_SUCCESS",                                       // 删除文件夹成功
  MOVE_TO_FOLDER_INIT: "MOVE_TO_FOLDER_INIT",                                           // 初始化移动到文件夹
  MOVE_TO_FOLDER_SUCCESS: "MOVE_TO_FOLDER_SUCCESS",                                     // 移动到文件夹成功
  FETCH_FOLDER_ELEMENTS_INIT: "FETCH_FOLDER_ELEMENTS_INIT",                             // 初始化获取文件夹元素
  FETCH_FOLDER_ELEMENTS_SUCCESS: "FETCH_FOLDER_ELEMENTS_SUCCESS",                       // 获取文件夹元素成功
  FETCH_ALL_FOLDERS_INIT: "FETCH_ALL_FOLDERS_INIT",                                     // 初始化获取所有文件夹
  FETCH_ALL_FOLDERS_SUCCESS: "FETCH_ALL_FOLDERS_SUCCESS",                               // 获取所有文件夹成功
  /* << 文件夹相关 */

  /* 组织、组、用户相关 */
  UPDATE_GROUP_INFO: "UPDATE_GROUP_INFO",                                               // 更新组信息
  FETCH_ORG_GROUPS: "FETCH_ORG_GROUPS",                                                 // 获取组织的组列表
  FETCH_ORG_GROUPS_SUCCESS: "FETCH_ORG_GROUPS_SUCCESS",                                 // 获取组织的组列表成功
  UPDATE_USER_ORG_ROLE: "UPDATE_USER_ORG_ROLE",                                         // 更新用户在组织中的角色
  UPDATE_USER_GROUP_ROLE: "UPDATE_USER_GROUP_ROLE",                                     // 更新用户在组中的角色
  FETCH_ORG_ALL_USERS: "FETCH_ORG_ALL_USERS",                                           // 获取组织中的所有用户
  FETCH_ORG_ALL_USERS_SUCCESS: "FETCH_ORG_ALL_USERS_SUCCESS",                           // 获取组织中的所有用户成功
  FETCH_GROUP_USERS: "FETCH_GROUP_USERS",                                               // 获取组中的用户
  FETCH_GROUP_USERS_SUCCESS: "FETCH_GROUP_USERS_SUCCESS",                               // 获取组中的用户成功
  DELETE_GROUP_USER: "DELETE_GROUP_USER",                                               // 从组中删除用户
  DELETE_GROUP_USER_SUCCESS: "DELETE_GROUP_USER_SUCCESS",                               // 从组中删除用户成功
  ADD_GROUP_USER: "ADD_GROUP_USER",                                                     // 添加用户到组
  ADD_GROUP_USER_SUCCESS: "ADD_GROUP_USER_SUCCESS",                                     // 添加用户到组成功
  DELETE_ORG_USER: "DELETE_ORG_USER",                                                   // 删除组织中的用户
  DELETE_ORG_USER_SUCCESS: "DELETE_ORG_USER_SUCCESS",                                   // 删除组织中的用户成功
  QUIT_GROUP: "QUIT_GROUP",                                                             // 退出组
  QUIT_GROUP_SUCCESS: "QUIT_GROUP_SUCCESS",                                             // 退出组成功
  QUIT_ORG: "QUIT_ORG",                                                                 // 退出组织
  SWITCH_ORG: "SWITCH_ORG",                                                             // 切换组织
  CREATE_ORG: "CREATE_ORG",                                                             // 创建组织
  CREATE_ORG_SUCCESS: "CREATE_ORG_SUCCESS",                                             // 创建组织成功
  DELETE_ORG: "DELETE_ORG",                                                             // 删除组织
  DELETE_ORG_SUCCESS: "DELETE_ORG_SUCCESS",                                             // 删除组织成功
  UPDATE_ORG: "UPDATE_ORG",                                                             // 更新组织
  UPDATE_ORG_SUCCESS: "UPDATE_ORG_SUCCESS",                                             // 更新组织成功
  UPDATE_USER_PROFILE: "UPDATE_USER_PROFILE",                                           // 更新用户资料
  UPDATE_USER_PROFILE_SUCCESS: "UPDATE_USER_PROFILE_SUCCESS",                           // 更新用户资料成功
  UPLOAD_USER_HEAD_SUCCESS: "UPLOAD_USER_HEAD_SUCCESS",                                 // 更新用户头像成功
  MARK_USER_STATUS: "MARK_USER_STATUS",                                                 // 标记用户状态
  /* << 组织、组、用户相关 */

  /* 首页数据 */
  FETCH_HOME_DATA: "FETCH_HOME_DATA",                           // 获取首页数据
  FETCH_HOME_DATA_SUCCESS: "FETCH_HOME_DATA_SUCCESS",           // 获取首页数据成功

  /* 应用相关 */
  CREATE_APPLICATION_INIT: "CREATE_APPLICATION_INIT",           // 初始化创建应用
  CREATE_APPLICATION_SUCCESS: "CREATE_APPLICATION_SUCCESS",     // 创建应用成功
  DELETE_APPLICATION_INIT: "DELETE_APPLICATION_INIT",           // 初始化删除应用
  DELETE_APPLICATION_SUCCESS: "DELETE_APPLICATION_SUCCESS",     // 删除应用成功
  UPDATE_APPLICATION: "UPDATE_APPLICATION",                     // 更新应用
  UPDATE_APPLICATION_META: "UPDATE_APPLICATION_META",           // 更新应用元数据
  PUBLISH_APPLICATION: "PUBLISH_APPLICATION",                   // 发布应用
  PUBLISH_APPLICATION_SUCCESS: "PUBLISH_APPLICATION_SUCCESS",   // 发布应用成功
  UPDATE_APPLICATION_META_SUCCESS: "UPDATE_APPLICATION_META_SUCCESS", // 更新应用元数据成功
  FETCH_APPLICATION_DETAIL: "FETCH_APPLICATION_DETAIL",         // 获取应用详细信息
  FETCH_APP_PUBLISH_DETAIL_SUCCESS: "FETCH_APP_PUBLISH_DETAIL_SUCCESS", // 获取发布版本详细信息成功
  FETCH_APP_EDITING_DETAIL_SUCCESS: "FETCH_APP_EDITING_DETAIL_SUCCESS", // 获取编辑版本详细信息成功
  FETCH_APP_PERMISSIONS: "FETCH_APPLICATION_PERMISSIONS",       // 获取应用权限
  FETCH_APP_PERMISSIONS_SUCCESS: "FETCH_APP_PERMISSIONS_SUCCESS", // 获取应用权限成功
  UPDATE_APP_PERMISSION: "UPDATE_APP_PERMISSION",               // 更新应用权限
  UPDATE_APP_PERMISSION_SUCCESS: "UPDATE_APP_PERMISSION_SUCCESS", // 更新应用权限成功
  DELETE_APP_PERMISSION: "DELETE_APP_PERMISSION",               // 删除应用权限
  DELETE_APP_PERMISSION_SUCCESS: "DELETE_APP_PERMISSION_SUCCESS", // 删除应用权限成功
  UPDATE_APP_PERMISSION_INFO: "UPDATE_APP_PERMISSION_INFO",     // 更新应用权限信息
  RECYCLE_APPLICATION_INIT: "RECYCLE_APPLICATION_INIT",         // 初始化回收应用
  RECYCLE_APPLICATION_SUCCESS: "RECYCLE_APPLICATION_SUCCESS",   // 回收应用成功
  RESTORE_APPLICATION_INIT: "RESTORE_APPLICATION_INIT",         // 初始化恢复应用
  RESTORE_APPLICATION_SUCCESS: "RESTORE_APPLICATION_SUCCESS",   // 恢复应用成功
  FETCH_APPLICATION_RECYCLE_LIST_INIT: "FETCH_APPLICATION_RECYCLE_LIST_INIT", // 初始化获取应用回收列表
  FETCH_APPLICATION_RECYCLE_LIST_SUCCESS: "FETCH_APPLICATION_RECYCLE_LIST_SUCCESS", // 获取应用回收列表成功
  FETCH_ALL_APPLICATIONS_INIT: "FETCH_ALL_APPLICATIONS_INIT",   // 初始化获取所有应用
  FETCH_ALL_APPLICATIONS_SUCCESS: "FETCH_ALL_APPLICATIONS_SUCCESS", // 获取所有应用成功
  FETCH_ALL_MODULES_INIT: "FETCH_ALL_MODULES_INIT",             // 初始化获取所有模块
  FETCH_ALL_MODULES_SUCCESS: "FETCH_ALL_MODULES_SUCCESS",       // 获取所有模块成功

  /* 用户资料 */
  SET_USER_PROFILE_SETTING_MODAL_VISIBLE: "SET_USER_PROFILE_SETTING_MODAL_VISIBLE", // 设置用户资料设置模态框可见

  /* 系统配置 */
  FETCH_SYS_CONFIG_INIT: "FETCH_SYS_CONFIG_INIT",               // 初始化获取系统配置
  FETCH_SYS_CONFIG_SUCCESS: "FETCH_SYS_CONFIG_SUCCESS",         // 获取系统配置成功
  SET_EDITOR_EXTERNAL_STATE: "SET_EDITOR_EXTERNAL_STATE",       // 设置编辑器外部状态
  UPDATE_SYS_CINFIG_BRANDING: "UPDATE_SYS_CINFIG_BRANDING",     // 更新品牌信息

  /* 审计日志 */
  FETCH_AUDIT_EVENT: "FETCH_AUDIT_EVENT",                       // 获取审计事件
  FETCH_AUDIT_EVENT_SUCCESS: "FETCH_AUDIT_EVENT_SUCCESS",       // 获取审计事件成功
  FETCH_AUDIT_LOG: "FETCH_AUDIT_LOG",                           // 获取审计日志
  FETCH_AUDIT_LOG_SUCCESS: "FETCH_AUDIT_LOG_SUCCESS",           // 获取审计日志成功

  /* 应用快照 */
  FETCH_APP_SNAPSHOTS: "FETCH_APP_SNAPSHOTS",                   // 获取应用快照
  FETCH_APP_SNAPSHOTS_SUCCESS: "FETCH_APP_SNAPSHOTS_SUCCESS",   // 获取应用快照成功
  FETCH_APP_SNAPSHOT_DSL: "FETCH_APP_SNAPSHOT_DSL",             // 获取应用快照DSL
  FETCH_APP_SNAPSHOT_DSL_SUCCESS: "FETCH_APP_SNAPSHOT_DSL_SUCCESS", // 获取应用快照DSL成功
  RECOVER_APP_SNAPSHOT: "RECOVER_APP_SNAPSHOT",                 // 恢复应用快照
  CREATE_APP_SNAPSHOT: "CREATE_APP_SNAPSHOT",                   // 创建应用快照
  CREATE_APP_SNAPSHOT_SUCCESS: "CREATE_APP_SNAPSHOT_SUCCESS",   // 创建应用快照成功
  SET_SHOW_APP_SNAPSHOT: "SET_SHOW_APP_SNAPSHOT",               // 设置显示应用快照
  SET_SELECT_SNAPSHOT_ID: "SET_SELECT_SNAPSHOT_ID",             // 设置选择的快照ID

  /* 通用设置 */
  FETCH_COMMON_SETTING: "FETCH_COMMON_SETTING",                 // 获取通用设置
  FETCH_COMMON_SETTING_SUCCESS: "FETCH_COMMON_SETTING_SUCCESS", // 获取通用设置成功
  SET_COMMON_SETTING: "SET_COMMON_SETTING",                     // 设置通用设置
  SET_COMMON_SETTING_SUCCESS: "SET_COMMON_SETTING_SUCCESS",     // 设置通用设置成功

  /* npm 插件 */
  PACKAGE_META_READY: "PACKAGE_META_READY",                     // 包元数据准备好
  SELECT_PACKAGE_VERSION: "SELECT_PACKAGE_VERSION",             // 选择包版本

  /* js 库 */
  FETCH_JS_LIB_METAS: "FETCH_JS_LIB_METAS",                     // 获取JS库元数据
  FETCH_JS_LIB_METAS_SUCCESS: "FETCH_JS_LIB_METAS_SUCCESS",     // 获取JS库元数据成功
  FETCH_JS_LIB_RECOMMENDS: "FETCH_JS_LIB_RECOMMENDS",           // 获取JS库推荐
  FETCH_JS_LIB_RECOMMENDS_SUCCESS: "FETCH_JS_LIB_RECOMMENDS_SUCCESS", // 获取JS库推荐成功
};

export const ReduxActionErrorTypes = {
  API_ERROR: "API_ERROR",                                                // API错误

  /* 用户相关 */
  FETCH_USER_DETAILS_ERROR: "FETCH_USER_DETAILS_ERROR",                 // 获取用户详细信息错误
  FETCH_CURRENT_USER_ERROR: "FETCH_CURRENT_USER_ERROR",                 // 获取当前用户错误
  UPDATE_USER_PROFILE_ERROR: "UPDATE_USER_PROFILE_ERROR",               // 更新用户资料错误

  /* 组织、组、用户相关 */
  CREATE_ORG_ERROR: "CREATE_ORG_ERROR",                                 // 创建组织错误
  FETCH_ORG_GROUPS_ERROR: "FETCH_ORG_GROUPS_ERROR",                     // 获取组织的组列表错误

  /* 查询相关 */
  FETCH_QUERIES_ERROR: "FETCH_QUERIES_ERROR",                           // 获取查询列表失败
  /* << 查询相关 */

  /* 应用相关 */
  CREATE_APPLICATION_ERROR: "CREATE_APPLICATION_ERROR",                 // 创建应用错误
  DELETE_APPLICATION_ERROR: "DELETE_APPLICATION_ERROR",                 // 删除应用错误
  FETCH_APPLICATION_DETAIL_ERROR: "FETCH_APPLICATION_DETAIL_ERROR",     // 获取应用详细信息错误
  PUBLISH_APPLICATION_ERROR: "PUBLISH_APPLICATION_ERROR",               // 发布应用错误
  FETCH_HOME_DATA_ERROR: "FETCH_HOME_DATA_ERROR",                       // 获取首页数据错误
  FETCH_FOLDER_ELEMENTS_ERROR: "FETCH_FOLDER_ELEMENTS_ERROR",           // 获取文件夹元素错误

  /* 系统配置 */
  FETCH_SYS_CONFIG_ERROR: "FETCH_SYS_CONFIG_ERROR",                     // 获取系统配置错误

  /* 应用快照 */
  CREATE_APP_SNAPSHOT_ERROR: "CREATE_APP_SNAPSHOT_ERROR",               // 创建应用快照错误
  FETCH_APP_SNAPSHOTS_ERROR: "FETCH_APP_SNAPSHOTS_ERROR",               // 获取应用快照错误
  FETCH_APP_SNAPSHOT_DSL_ERROR: "FETCH_APP_SNAPSHOT_DSL_ERROR",         // 获取应用快照DSL错误

};

export type ReduxActionType = typeof ReduxActionTypes[keyof typeof ReduxActionTypes];
export type ReduxActionErrorType = typeof ReduxActionErrorTypes[keyof typeof ReduxActionErrorTypes];

export interface ReduxAction<T> {
  type: ReduxActionType | ReduxActionErrorType;
  payload: T;
}

export type ReduxActionWithoutPayload = Pick<ReduxAction<undefined>, "type">;

export interface EvaluationReduxAction<T> extends ReduxAction<T> {
  postEvalActions?: Array<ReduxAction<any> | ReduxActionWithoutPayload>;
}

export interface ReduxActionWithCallbacks<T, S, E> extends ReduxAction<T> {
  onSuccess?: ReduxAction<S>;
  onError?: ReduxAction<E>;
  onSuccessCallback?: (response: S) => void;
  onErrorCallback?: (error: E) => void;
}

export interface PromisePayload {
  reject: any;
  resolve: any;
}

export interface ReduxActionWithPromise<T> extends ReduxAction<T> {
  payload: T & PromisePayload;
}
