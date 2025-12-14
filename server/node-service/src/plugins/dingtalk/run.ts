import { DataSourceDataType } from "./dataSourceConfig";
import { ActionDataType } from "./queryConfig";
import queryConfig from "./queryConfig";
import { fetch } from "../../common/fetch";
import { ServiceError } from "../../common/error";

/**
 * 验证数据源配置
 * @param dataSourceConfig 数据源配置
 */
export async function validateDataSourceConfig(dataSourceConfig: DataSourceDataType) {
  const { appKey, appSecret } = dataSourceConfig;

  if (!appKey || !appSecret) {
    return {
      success: false,
      message: "AppKey 和 AppSecret 不能为空",
    };
  }

  // 尝试获取 access_token 来验证配置是否正确
  try {
    await getAccessToken(appKey, appSecret);
    return {
      success: true,
    };
  } catch (e: any) {
    return {
      success: false,
      message: e.message || "AppKey 或 AppSecret 无效",
    };
  }
}

// Token 缓存，key 为 appKey，value 为 { token: string, expiresAt: number }
const tokenCache = new Map<string, { token: string; expiresAt: number }>();

/**
 * 获取 access_token
 * @param appKey 应用 Key
 * @param appSecret 应用 Secret
 * @param serverUrl 服务器地址
 * @returns access_token
 */
async function getAccessToken(appKey: string, appSecret: string): Promise<string> {
  // 检查缓存
  const cached = tokenCache.get(appKey);
  if (cached && cached.expiresAt > Date.now()) {
    return cached.token;
  }

  // 获取新 token
  // 钉钉新版 OAuth2 API: https://api.dingtalk.com/v1.0/oauth2/accessToken
  // 注意：需要确保 appKey 和 appSecret 不为空且已去除首尾空格
  const trimmedAppKey = (appKey || "").trim();
  const trimmedAppSecret = (appSecret || "").trim();

  if (!trimmedAppKey || !trimmedAppSecret) {
    throw new ServiceError(`获取 access_token 失败: AppKey 或 AppSecret 为空`, 400);
  }

  // 使用新版 OAuth2 API 端点
  const url = "https://api.dingtalk.com/v1.0/oauth2/accessToken";

  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      appKey: trimmedAppKey,
      appSecret: trimmedAppSecret,
    }),
  });

  if (!response.ok) {
    const errorText = await response.text().catch(() => "无法读取错误信息");
    throw new ServiceError(
      `获取 access_token 失败: HTTP ${response.status} ${response.statusText}. ${errorText}`,
      400
    );
  }

  const result = await response.json();

  // 新版 API 可能使用不同的错误格式
  if (result.code && result.code !== 0) {
    const errorMsg = result.message || result.msg || `错误码: ${result.code}`;
    throw new ServiceError(`获取 access_token 失败: ${errorMsg}`, 400);
  }

  // 兼容新旧两种响应格式
  const token = result.accessToken || result.access_token;
  if (!token) {
    throw new ServiceError(
      `获取 access_token 失败: 响应中未包含 accessToken 或 access_token 字段`,
      400
    );
  }

  // 缓存 token，提前 5 分钟过期（钉钉 token 有效期通常为 7200 秒）
  const expiresIn = (result.expireIn || result.expires_in || 7200) * 1000;
  tokenCache.set(appKey, {
    token,
    expiresAt: Date.now() + expiresIn - 5 * 60 * 1000,
  });

  return token;
}

/**
 * 调用钉钉 API
 * @param apiPath API 路径
 * @param method 请求方法
 * @param params 请求参数
 * @param accessToken access_token
 * @param isNewApi 是否使用新版 API
 * @returns API 响应结果
 */
async function callDingTalkApi(
  apiPath: string,
  method: string,
  params: any,
  accessToken: string,
  isNewApi: boolean = true
): Promise<any> {
  // 根据 API 版本选择不同的 host 和认证方式
  const baseUrl = isNewApi ? "https://api.dingtalk.com" : "https://oapi.dingtalk.com";

  const options: any = {
    method,
    headers: {
      "Content-Type": "application/json",
    },
  };

  let finalUrl: string;

  if (isNewApi) {
    // 新版 API: 在 header 中携带 token
    options.headers["x-acs-dingtalk-access-token"] = accessToken;
    finalUrl = `${baseUrl}${apiPath}`;
  } else {
    // 旧版 API: 在 query 参数中携带 token
    finalUrl = `${baseUrl}${apiPath}?access_token=${accessToken}`;
  }

  if (method === "GET" && params) {
    // GET 请求将参数拼接到 URL
    const queryString = new URLSearchParams(
      Object.entries(params).reduce((acc, [key, value]) => {
        if (value !== null && value !== undefined) {
          acc[key] = String(value);
        }
        return acc;
      }, {} as Record<string, string>)
    ).toString();
    if (queryString) {
      const separator = isNewApi ? "?" : "&";
      finalUrl = `${finalUrl}${separator}${queryString}`;
    }
  } else if (method === "POST" && params) {
    options.body = JSON.stringify(params);
  }

  const response = await fetch(finalUrl, options);
  const result = await response.json();

  // 兼容新旧两种错误格式
  const errorCode = result.errcode || result.code;
  if (errorCode !== undefined && errorCode !== 0) {
    const errorMsg = result.errmsg || result.message || result.msg || errorCode;
    throw new ServiceError(`API 调用失败: ${errorMsg}`, 400);
  }

  return result;
}

export default async function run(action: ActionDataType, dataSourceConfig: DataSourceDataType) {
  const { appKey, appSecret, agentId } = dataSourceConfig;

  if (!appKey || !appSecret) {
    throw new ServiceError("AppKey 和 AppSecret 不能为空", 400);
  }

  try {
    let isNewApi: boolean;
    const actionIsNewApi = (action as any)?.isNewApi;

    if (actionIsNewApi !== undefined) {
      isNewApi = actionIsNewApi === true || actionIsNewApi === "true";
    } else {
      const actionConfig = queryConfig.actions.find((a) => a.actionName === action.actionName);
      if (actionConfig && (actionConfig as any)?.isNewApi === false) {
        isNewApi = false;
      } else {
        isNewApi = true;
      }
    }
    const accessToken = await getAccessToken(appKey, appSecret);

    switch (action.actionName) {
      case "getAccessToken":
        return { access_token: accessToken };

      case "getUserInfo": {
        const { userid } = action;
        if (!userid) {
          throw new ServiceError("用户ID不能为空", 400);
        }
        return await callDingTalkApi(
          "/topapi/v2/user/get",
          "POST",
          { userid },
          accessToken,
          isNewApi
        );
      }

      case "searchUserByMobile": {
        const { mobile } = action;
        if (!mobile) {
          throw new ServiceError("手机号不能为空", 400);
        }
        return await callDingTalkApi(
          "/topapi/v2/user/getbymobile",
          "POST",
          { mobile },
          accessToken,
          isNewApi
        );
      }

      case "searchUserByName": {
        const { queryWord, offset = 0, size = 10, fullMatchField } = action;
        if (!queryWord) {
          throw new ServiceError("姓名不能为空", 400);
        }
        const params: any = {
          queryWord,
          offset,
          size: size == 0 ? 10 : size,
        };
        if (fullMatchField === "1") {
          params.fullMatchField = 1;
        }
        return await callDingTalkApi(
          "/v1.0/contact/users/search",
          "POST",
          params,
          accessToken,
          isNewApi
        );
      }

      case "getDepartmentList": {
        const { dept_id = 1 } = action;
        return await callDingTalkApi(
          "/topapi/v2/department/listsub",
          "POST",
          { dept_id },
          accessToken,
          isNewApi
        );
      }

      case "getDepartmentInfo": {
        const { dept_id } = action;
        if (!dept_id) {
          throw new ServiceError("部门ID不能为空", 400);
        }
        return await callDingTalkApi(
          "/topapi/v2/department/get",
          "POST",
          { dept_id },
          accessToken,
          isNewApi
        );
      }

      case "getDepartmentUserInfo": {
        const { department_id, offset = 0, size = 100 } = action;
        if (!department_id) {
          throw new ServiceError("部门ID不能为空", 400);
        }
        return await callDingTalkApi(
          "/topapi/v2/user/list",
          "POST",
          {
            dept_id: department_id,
            cursor: offset,
            size,
          },
          accessToken,
          isNewApi
        );
      }

      case "sendMessage": {
        const { userid_list, msg } = action as any;
        if (!userid_list) {
          throw new ServiceError("接收人用户ID列表不能为空", 400);
        }
        if (!msg) {
          throw new ServiceError("消息内容不能为空", 400);
        }

        let msgObj;
        try {
          msgObj = typeof msg === "string" ? JSON.parse(msg) : msg;
        } catch (e) {
          throw new ServiceError("消息内容格式错误，必须是有效的 JSON", 400);
        }
        return await callDingTalkApi(
          "/topapi/message/corpconversation/asyncsend_v2",
          "POST",
          {
            agent_id: msgObj.agent_id || agentId || appKey, // 优先级：消息中的 agent_id > 数据源配置的 agentId > appKey
            userid_list: userid_list,
            msg,
          },
          accessToken,
          isNewApi
        );
      }

      case "customApi": {
        const { apiPath, method = "POST", params } = action;
        if (!apiPath) {
          throw new ServiceError("API 路径不能为空", 400);
        }

        let paramsObj = {};
        if (params) {
          try {
            paramsObj = typeof params === "string" ? JSON.parse(params) : params;
          } catch (e) {
            throw new ServiceError("请求参数格式错误，必须是有效的 JSON", 400);
          }
        }

        return await callDingTalkApi(apiPath, method, paramsObj, accessToken, isNewApi);
      }

      default: {
        const unknownAction = action as any;
        throw new ServiceError(`未知的操作: ${unknownAction.actionName || "unknown"}`, 400);
      }
    }
  } catch (e: any) {
    if (e instanceof ServiceError) {
      throw e;
    }
    throw new ServiceError(`执行失败: ${e.message || String(e)}`, e.status || 500);
  }
}
