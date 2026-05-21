import { ORG_AUTH_LOGIN_URL, ORG_AUTH_URL } from "constants/routesURL";
import { Redirect, Route, Switch, useLocation, useParams } from "react-router-dom";
import React, { useEffect, useState } from "react";
import { AuthContext } from "pages/userAuth/authUtils";
import { OrgAuthRoutes } from "@barda/constants/authConstants";
import { AuthLocationState } from "constants/authConstants";
import { ProductLoading } from "components/ProductLoading";
import ConfigApi from "api/configApi";
import { transToSystemConfig, SystemConfig } from "@barda/constants/configConstants";

export default function OrgUserAuth() {
  const { orgId } = useParams<{ orgId: string }>();
  const location = useLocation<AuthLocationState>();
  const [systemConfig, setSystemConfig] = useState<SystemConfig | undefined>(undefined);

  useEffect(() => {
    ConfigApi.fetchConfig(orgId).then((response) => {
      if (response.data?.data) {
        setSystemConfig(transToSystemConfig(response.data.data));
      }
    });
  }, [orgId]);

  if (!systemConfig) {
    return <ProductLoading hideHeader />;
  }

  return (
    <AuthContext.Provider
      value={{
        systemConfig: systemConfig,
        inviteInfo: location.state?.inviteInfo,
        thirdPartyAuthError: location.state?.thirdPartyAuthError,
        orgId: orgId,
      }}
    >
      <Switch location={location}>
        <Route
          exact
          path={ORG_AUTH_URL}
          render={() => <Redirect to={`/org/${orgId}/auth/login`} />}
        />
        {OrgAuthRoutes.map((route) => (
          <Route key={route.path} exact path={route.path} component={route.component} />
        ))}
      </Switch>
    </AuthContext.Provider>
  );
}
