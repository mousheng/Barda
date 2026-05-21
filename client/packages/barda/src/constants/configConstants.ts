import { ThirdPartyConfigType } from "constants/authConstants";
import { QR_CODE_OAUTH_URL } from "constants/routesURL";
import { UserConnectionSource } from "@barda/constants/userConstants";
import { GeneralLoginIcon } from "assets/icons";
import {
  isRouteLink,
  ServerAuthType,
  ServerAuthTypeInfo,
} from "@barda/constants/authConstants";

interface FeatureFlag {
  enableCustomBranding: boolean;
  enableEnterpriseLogin: boolean;
  enableAuditLog: boolean;
}

export interface BrandingConfig {
  logo?: string;
  favicon?: string;
  brandName?: string;
  headerColor?: string;
  logoContainsName?: boolean;
}

export type ConfigBaseInfo = {
  selfDomain: boolean;
  cloudHosting: boolean;
  workspaceMode: "SAAS" | "ENTERPRISE";
  warning?: string;
  featureFlag: FeatureFlag;
  branding?: BrandingConfig;
  orgName?: string;
};

type OAuthConfig = {
  authorizeUrl: string;
  authType: ServerAuthType;
  source: string;
  sourceName: string;
  agentId?: string;
  clientId?: string;
  id?: string;
  sourceIcon?: string;
};

export type FormConfig = {
  enableRegister?: boolean;
  enable?: boolean;
  authType: ServerAuthType;
  source: string;
  sourceName: string;
  id?: string;
  enableRSA?: boolean;
  publicKey?: string;
};

export type AuthConfigType = OAuthConfig | FormConfig;

export type ConfigResponseData = {
  authConfigs?: AuthConfigType[];
} & ConfigBaseInfo;

function isOAuthConfig(config: AuthConfigType): config is OAuthConfig {
  return !!ServerAuthTypeInfo[config.authType]?.isOAuth2;
}

export type SystemConfig = {
  form: {
    enableRegister: boolean;
    enableLogin: boolean;
    id?: string;
    type: "EMAIL" | "PHONE";
    enableRSA?: boolean;
    publicKey?: string;
  };
  authConfigs: ThirdPartyConfigType[];
} & ConfigBaseInfo;

export const transToSystemConfig = (responseData: ConfigResponseData): SystemConfig => {
  const thirdPartyAuthConfigs: ThirdPartyConfigType[] = [];
  responseData.authConfigs?.forEach((authConfig) => {
    const defaultLogo = ServerAuthTypeInfo[authConfig.authType]?.logo || GeneralLoginIcon;
    if (isOAuthConfig(authConfig)) {
      // 优先使用管理员配置的 sourceIcon（Generic 类型），否则使用默认图标
      // sourceIcon 可能是图标名称（/icon:xxx）或图片 URL，统一传递，由渲染端处理
      const logo = authConfig.sourceIcon || defaultLogo;
      const routeLinkConf: Partial<ThirdPartyConfigType> = isRouteLink(authConfig.authType)
        ? {
          url: QR_CODE_OAUTH_URL,
          routeLink: true,
        }
        : {};
      thirdPartyAuthConfigs.push({
        logo: logo,
        name: authConfig.sourceName,
        url: authConfig.authorizeUrl,
        sourceType: authConfig.source,
        authType: "OAUTH2",
        clientId: authConfig.clientId,
        agentId: authConfig.agentId,
        id: authConfig.id,
        ...routeLinkConf,
      });
    }
  });
  const emailConfig = responseData.authConfigs?.find(
    (c) => c.source === UserConnectionSource.email
  ) as FormConfig | undefined;

  return {
    ...responseData,
    form: {
      enableRegister: !!emailConfig?.enableRegister,
      enableLogin: !!emailConfig?.enable,
      id: emailConfig?.id,
      type: "EMAIL",
      enableRSA: !!emailConfig?.enableRSA,
      publicKey: emailConfig?.publicKey,
    },
    authConfigs: thirdPartyAuthConfigs,
  };
};
