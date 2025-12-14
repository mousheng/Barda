import { ConfigToType } from "barda-sdk/dataSource";

export const dataSourceConfig = {
  type: "dataSource",
  params: [
    {
      type: "groupTitle",
      key: "auth",
      label: "认证配置",
    },
    {
      type: "textInput",
      key: "agentId",
      label: "AgentId",
      tooltip: "钉钉应用的 AgentId，用于发送工作通知时指定应用，可在钉钉开放平台获取",
      placeholder: "12345678",
    },
    {
      type: "textInput",
      key: "appKey",
      label: "Client ID",
      tooltip: "原 AppKey 或 SuiteKey，请到钉钉开放平台获取",
      placeholder: "ding*****",
      rules: [{ required: true, message: "请输入 AppKey" }],
    },
    {
      type: "password",
      key: "appSecret",
      label: "Client Secret",
      tooltip: "原 AppSecret 或 SuiteKey，请到钉钉开放平台获取",
      placeholder: "J5ap-*******",
      rules: [{ required: true, message: "请输入 AppSecret" }],
    },
  ],
} as const;

export type DataSourceDataType = ConfigToType<typeof dataSourceConfig>;
