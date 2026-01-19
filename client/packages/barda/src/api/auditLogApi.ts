import Api from "api/api";
import { AxiosPromise } from "axios";
import { GenericApiResponse } from "./apiResponses";

export interface AuditLogSearchRequest {
  startTime?: string;
  endTime?: string;
  userId?: string;
  eventType?: string;
  appId?: string;
  queryName?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface AuditLog {
  id: string;
  eventType: string;
  orgId: string;
  createTime: number;
  detail?: Record<string, any>;
  user?: {
    id: string;
    name: string;
  };
}

export interface AuditLogPageResponse {
  data: AuditLog[];
  pageNum: number;
  pageSize: number;
  total: number;
}

export interface EventTypeItem {
  event: string;
  desc: string;
}

class AuditLogApi extends Api {
  static auditLogURL = "/audit-logs";
  static searchURL = `${AuditLogApi.auditLogURL}/search`;
  static eventTypesURL = `${AuditLogApi.auditLogURL}/event-types`;

  static search(
    request: AuditLogSearchRequest
  ): AxiosPromise<GenericApiResponse<AuditLogPageResponse>> {
    return Api.post(AuditLogApi.searchURL, request);
  }

  static getEventTypes(): AxiosPromise<GenericApiResponse<EventTypeItem[]>> {
    return Api.get(AuditLogApi.eventTypesURL);
  }
}

export default AuditLogApi;
