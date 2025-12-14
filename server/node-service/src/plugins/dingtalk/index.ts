import { PluginContext } from "barda-sdk/dataSource";
import queryConfig, { ActionDataType } from "./queryConfig";
import { dataSourceConfig, DataSourceDataType } from "./dataSourceConfig";
import run, { validateDataSourceConfig } from "./run";

const dingtalkPlugin = {
  id: "dingtalk",
  name: "钉钉",
  icon: "dingTalk.svg",
  category: "api",
  dataSourceConfig,
  queryConfig: queryConfig,

  validateDataSourceConfig: async (dataSourceConfig: DataSourceDataType) => {
    return validateDataSourceConfig(dataSourceConfig);
  },

  run: async (
    action: ActionDataType,
    dataSourceConfig: DataSourceDataType,
    ctx: PluginContext
  ) => {
    try {
      return await run(action, dataSourceConfig);
    } catch (e) {
      throw e;
    }
  },
};

export default dingtalkPlugin;
