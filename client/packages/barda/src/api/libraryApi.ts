import Api from "./api";
import { AxiosPromise } from "axios";
import { GenericApiResponse } from "./apiResponses";

interface LibraryFile {
  id: string;
  libraryId?: string;
  originalFilename?: string;
  filename: string;
  displayName?: string;
  version?: string;
  fileSize: number;
  type: string;
  description?: string;
  createdAt: number;
}

interface UploadLibraryRequest {
  filename: string;
  content: string;
  displayName?: string;
  version?: string;
  description?: string;
}

class LibraryApi extends Api {
  static baseURL = "/libraries";

  // 上传共享库
  static uploadSharedLibrary(
    request: UploadLibraryRequest
  ): AxiosPromise<GenericApiResponse<LibraryFile>> {
    return Api.post(`${LibraryApi.baseURL}/shared/upload`, request);
  }

  // 上传组织库
  static uploadOrgLibrary(
    request: UploadLibraryRequest
  ): AxiosPromise<GenericApiResponse<LibraryFile>> {
    return Api.post(`${LibraryApi.baseURL}/org/upload`, request);
  }

  // 删除共享库
  static deleteSharedLibrary(filename: string): AxiosPromise<GenericApiResponse<void>> {
    return Api.delete(`${LibraryApi.baseURL}/shared/${filename}`);
  }

  // 删除组织库
  static deleteOrgLibrary(filename: string): AxiosPromise<GenericApiResponse<void>> {
    return Api.delete(`${LibraryApi.baseURL}/org/${filename}`);
  }

  // 获取共享库列表
  static listSharedLibraries(): AxiosPromise<GenericApiResponse<LibraryFile[]>> {
    return Api.get(`${LibraryApi.baseURL}/shared/list`);
  }

  // 获取组织库列表
  static listOrgLibraries(): AxiosPromise<GenericApiResponse<LibraryFile[]>> {
    return Api.get(`${LibraryApi.baseURL}/org/list`);
  }

  // 获取可用库列表
  static listAvailableLibraries(): AxiosPromise<GenericApiResponse<LibraryFile[]>> {
    return Api.get(`${LibraryApi.baseURL}/available`);
  }

  // 检查是否是主要组织管理员
  static isPrimaryOrgAdmin(): AxiosPromise<GenericApiResponse<boolean>> {
    return Api.get("/system/is-primary-org-admin");
  }
}

export default LibraryApi;
