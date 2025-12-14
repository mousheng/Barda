import { ConfigToType } from "barda-sdk/dataSource";

const queryConfig = {
  type: "query",
  label: "操作",
  actions: [
    {
      actionName: "getAccessToken",
      label: "获取 Access Token",
      description: "获取钉钉访问令牌",
      params: [],
      isNewApi: true, 
    },
    {
      actionName: "getUserInfo",
      label: "获取用户信息",
      description: "根据用户ID获取用户详细信息",
      params: [
        {
          label: "用户ID",
          key: "userid",
          type: "textInput",
          rules: [{ required: true, message: "请输入用户ID" }],
        },
      ],
      isNewApi: false,
    },
    {
      actionName: "searchUserByMobile",
      label: "根据手机号搜索用户ID",
      description: "根据手机号搜索用户ID",
      params: [
        {
          label: "手机号",
          key: "mobile",
          type: "textInput",
          rules: [{ required: true, message: "请输入手机号" }],
          tooltip: "用户的手机号码",
        },
      ],
      isNewApi: false,
    },
    {
      actionName: "searchUserByName",
      label: "根据姓名搜索用户ID",
      description: "根据用户姓名搜索用户ID",
      params: [
        {
          label: "姓名",
          key: "queryWord",
          type: "textInput",
          rules: [{ required: true, message: "请输入姓名" }],
          tooltip: "用户的姓名，支持模糊搜索",
          placeholder: "张三"
        },
        {
          label: "分页页码",
          key: "offset",
          type: "numberInput",
          placeholder: "0"
        },
        {
          label: "分页大小",
          key: "size",
          type: "numberInput",
          placeholder: "10",
          tooltip: "每页返回的数量",
        },
        {
          label: "匹配方式",
          key: "fullMatchField",
          type: "select",
          options: [
            { label: "精确匹配", value: "1" },
            { label: "模糊匹配", value: "" },
          ],
          defaultValue: "1",
          tooltip: "精确匹配：完全匹配用户名称；模糊匹配：部分匹配用户名称",
        },
      ],
      isNewApi: true,
    },
    {
      actionName: "getDepartmentList",
      label: "获取部门列表",
      description: "获取下一级部门基础信息",
      params: [
        {
          label: "部门ID",
          key: "dept_id",
          type: "textInput",
          tooltip: "可选，不填则获取根部门列表",
        },
      ],
      isNewApi: false, 
    },
    {
      actionName: "getDepartmentInfo",
      label: "获取部门信息",
      description: "根据部门ID获取部门详细信息",
      params: [
        {
          label: "部门ID",
          key: "dept_id",
          type: "numberInput",
          rules: [{ required: true, message: "请输入部门ID" }],
        },
      ],
      isNewApi: false, 
    },
    {
      actionName: "getDepartmentUserInfo",
      label: "获取部门用户详情",
      description: "获取指定部门中的用户详细信息",
      params: [
        {
          label: "部门ID",
          key: "department_id",
          type: "numberInput",
          rules: [{ required: true, message: "请输入部门ID" }],
          defaultValue: 1,
        },
        {
          label: "分页页码",
          key: "offset",
          type: "numberInput",
          placeholder: "0"
        },
        {
          label: "分页大小",
          key: "size",
          type: "numberInput",
          placeholder: "10",
          tooltip: "每页返回的数量",
        },
      ],
      isNewApi: false, 
    },
    {
      actionName: "sendMessage",
      label: "发送工作通知",
      description: "发送工作通知消息",
      params: [
        {
          label: "接收人用户ID列表",
          key: "userid_list",
          type: "textInput",
          tooltip: "多个用户ID用逗号分隔",
          rules: [{ required: true, message: "请输入接收人用户ID列表" }],
          placeholder: "123456,8765432"
        },
        {
          label: "消息内容",
          key: "msg",
          type: "jsonInput",
          tooltip: "消息内容，JSON格式",
          rules: [{ required: true, message: "请输入消息内容" }],
          defaultValue: '{\n  "msgtype": "text",\n  "text": {\n    "content": "消息内容"\n  }\n}',
        },
      ],
      isNewApi: false, 
    },
    {
      actionName: "customApi",
      label: "自定义 API 调用",
      description: "调用钉钉其他 API 接口",
      params: [
        {
          label: "API 版本",
          key: "isNewApi",
          type: "select",
          options: [
            { label: "新版 API (推荐)", value: "true" },
            { label: "旧版 API", value: "false" },
          ],
          defaultValue: "true",
          tooltip: "新版 API 使用 https://api.dingtalk.com，旧版使用 https://oapi.dingtalk.com",
        },
        {
          label: "API 路径",
          key: "apiPath",
          type: "textInput",
          tooltip: "例如：/topapi/v2/user/get",
          rules: [{ required: true, message: "请输入 API 路径" }],
        },
        {
          label: "请求方法",
          key: "method",
          type: "select",
          options: [
            { label: "GET", value: "GET" },
            { label: "POST", value: "POST" },
          ],
          defaultValue: "POST",
        },
        {
          label: "请求参数",
          key: "params",
          type: "jsonInput",
          tooltip: "请求参数，JSON格式",
        },
      ],
    },
  ],
} as const;

export type ActionDataType = ConfigToType<typeof queryConfig>;

export default queryConfig;
