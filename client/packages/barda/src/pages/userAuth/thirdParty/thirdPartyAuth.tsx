import {
  AuthSearchParams,
  OAuthLocationState,
  ThirdPartyAuthGoal,
  ThirdPartyConfigType,
} from "constants/authConstants";
import { WhiteLoading, useIcon, removeQuote, iconPrefix } from "barda-design";
import { useLocation } from "react-router-dom";
import history from "util/history";
import { LoginIconWrapper, LoginLogoStyle, StyledLoginButton } from "pages/userAuth/authComponents";
import { messageInstance } from "barda-design";
import { trans } from "i18n";
import { geneAuthStateAndSaveParam, getAuthUrl, getRedirectUrl, AuthContext } from "pages/userAuth/authUtils";
import { useContext } from "react";

function ThirdPartyLoginButton(props: {
  config: ThirdPartyConfigType;
  invitationId?: string;
  autoJump?: boolean;
  authGoal: ThirdPartyAuthGoal;
  label: string;
}) {
  const { config, label } = props;
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const loginRedirectUrl = queryParams.get(AuthSearchParams.redirectUrl);
  const { orgId } = useContext(AuthContext);
  const redirectUrl = getRedirectUrl(config.authType);
  const onLoginClick = () => {
    const state = geneAuthStateAndSaveParam(
      props.authGoal,
      config,
      loginRedirectUrl,
      props.invitationId,
      orgId
    );
    if (config.authType === "LDAP") {
      history.push({
        pathname: config.url,
        search: loginRedirectUrl ? `?redirectUrl=${loginRedirectUrl}` : "",
        state: {
          autoJump: props.autoJump,
        },
      });
    } else if (config.routeLink) {
      if (!config?.clientId) {
        messageInstance.error(trans("userAuth.invalidThirdPartyParam"));
        return;
      }
      const routeState: OAuthLocationState = {
        sourceType: config.sourceType,
        appId: config.clientId,
        redirectUri: redirectUrl,
        state: state,
        agentId: config.agentId,
        authGoal: props.authGoal,
        autoJump: props.autoJump,
      };
      history.push({
        pathname: config.url,
        state: routeState,
      });
    } else {
      window.location.href = getAuthUrl(config, redirectUrl, state);
    }
  };

  const isIconName = config.logo && removeQuote(config.logo).startsWith(iconPrefix);
  const icon = useIcon(isIconName && config.logo ? config.logo : undefined);
  // 非图标名称（URL）才用 <img>，图标名称加载中时不做任何渲染以避免破损图标
  const renderAsImg = !isIconName;

  if (props.autoJump) {
    onLoginClick();
    return <WhiteLoading size={18} />;
  }

  return (
    <StyledLoginButton onClick={onLoginClick} title={label}>
      {icon ? (
        <LoginIconWrapper>{icon.getView()}</LoginIconWrapper>
      ) : renderAsImg ? (
        <LoginIconWrapper>
          <LoginLogoStyle alt={config.name} src={config.logo} title={config.name} />
        </LoginIconWrapper>
      ) : null}
    </StyledLoginButton>
  );
}

export function ThirdPartyAuth(props: {
  invitationId?: string;
  autoJumpSource?: string;
  authGoal: ThirdPartyAuthGoal;
  labelFormatter?: (name: string) => string;
}) {
  const { systemConfig } = useContext(AuthContext);
  if (!systemConfig) {
    return null;
  }
  const configs = systemConfig.authConfigs;
  // auto redirect when only one login method
  const socialLoginButtons = configs.map((config) => {
    if (props.autoJumpSource && config.sourceType !== props.autoJumpSource) {
      return null;
    }
    return (
      <ThirdPartyLoginButton
        authGoal={props.authGoal}
        autoJump={config.sourceType === props.autoJumpSource}
        key={config.name}
        config={config}
        invitationId={props.invitationId}
        label={props.labelFormatter ? props.labelFormatter(config.name) : config.name}
      />
    );
  });
  return <>{socialLoginButtons}</>;
}
