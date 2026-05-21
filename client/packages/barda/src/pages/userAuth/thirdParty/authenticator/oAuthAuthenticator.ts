import { AbstractAuthenticator } from "./abstractAuthenticator";
import { AxiosPromise } from "axios";
import UserApi from "api/userApi";
import { ApiResponse } from "api/apiResponses";

export class OAuthAuthenticator extends AbstractAuthenticator {
  bind() {
    const { authParams, urlParam, redirectUrl, orgId } = this;
    const reLoginOnBindFail = authParams.authGoal !== "innerBind";
    return UserApi.bindThirdParty({
      state: urlParam.state!,
      code: urlParam.code!,
      source: authParams.sourceType,
      authId: authParams.authId,
      redirectUrl: redirectUrl,
      reLoginOnBindFail: reLoginOnBindFail,
      ...(orgId && { orgId }),
    });
  }

  login(): AxiosPromise<ApiResponse> {
    const { urlParam, authParams, redirectUrl, orgId } = this;
    return UserApi.thirdPartyLogin({
      state: urlParam.state!,
      code: urlParam.code!,
      source: authParams.sourceType,
      authId: authParams.authId,
      redirectUrl: redirectUrl,
      ...(authParams.invitationId && { invitationId: authParams.invitationId }),
      ...(orgId && { orgId }),
    });
  }
}
