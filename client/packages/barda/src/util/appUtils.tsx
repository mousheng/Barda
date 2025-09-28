import { KeyValue } from "../types/common";
import { matchPath } from "react-router";
import { AppPathParams } from "../constants/applicationConstants";
import { APP_EDITOR_URL, APPLICATION_VIEW_URL } from "../constants/routesURL";
import history from "./history";
import { isEmpty } from "lodash";
import { UiLayoutType } from "comps/comps/uiComp";
import { AppSummaryInfo } from "@barda/redux/reduxActions/applicationActions";
import _ from "lodash";

export function keyValueListToSearchStr(kvs: KeyValue[]) {
  const searchParams = new URLSearchParams();
  kvs.forEach((i) => {
    const { key, value } = i;
    if (!key) {
      return;
    }
    searchParams.set(key as string, value as string);
  });
  return searchParams.toString();
}

export function recordToSearchStr(params: Record<string, string>) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    searchParams.set(k, v);
  });
  return searchParams.toString().replaceAll("+", "%20");
}

export function openApp(props: {
  applicationId: string;
  queryParams?: string;
  hashParams?: string;
  newTab?: boolean;
}) {
  const m = matchPath<AppPathParams>(window.location.pathname, APP_EDITOR_URL);
  if (!m || !props.applicationId) {
    return;
  }
  let targetURL = APPLICATION_VIEW_URL(props.applicationId, m.params.viewMode);
  // query
  if (props.queryParams && !isEmpty(props.queryParams)) {
    targetURL += `?${props.queryParams}`;
  }

  // hash
  if (props.hashParams && !isEmpty(props.hashParams)) {
    targetURL += `#${props.hashParams}`;
  }

  if (props.newTab) {
    targetURL = new URL(targetURL, window.location.href).toString();
    window.open(targetURL, "_blank");
  } else {
    history.push(targetURL);
  }
}

export function isAggregationApp(appType: UiLayoutType) {
  return appType === "nav" || appType === "mobileTabLayout";
}
/**
 * 历史原因hooks列表中有已废弃的hook，直接删除将导致页面无法载入，因此，必须在页面载入前删除或替换为新hook
 * @param info app dsl
 * @returns 返回修改后的info
 */
export function handlinghooks(info: AppSummaryInfo) {
  if (!!(info.dsl as any)?.hooks) {
    let hooks = _.cloneDeep((info.dsl as any)?.hooks)
    let hooksByName = _.keyBy(hooks, "name")
    if (Object.hasOwn(hooksByName, "moment") && Object.hasOwn(hooksByName, "dayjs")) { hooksByName = _.omit(hooksByName, "moment") }
    if (Object.hasOwn(hooksByName, "moment")) { _.set(hooksByName, 'moment.name', 'dayjs'); _.set(hooksByName, 'moment.compType', 'dayJsLib'); }
    (info.dsl as any).hooks = _.values(hooksByName);
  }
  return info;
}
