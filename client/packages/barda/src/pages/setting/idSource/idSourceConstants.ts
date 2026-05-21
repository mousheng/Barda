import { DefaultOptionType } from "antd/lib/select";
import { trans } from "i18n";

export enum AuthType {
  Form = "FORM",
  Google = "GOOGLE",
  Github = "GITHUB",
  Feishu = "FEISHU",
  DingTalk = "DINGTALK",
  Generic = "GENERIC",
}

export const HiddenAuthTypes = [AuthType.Google, AuthType.Github];
export const IdSource = [AuthType.Feishu, AuthType.DingTalk, AuthType.Form, AuthType.Generic];

export const validatorOptions = [];

export const clientIdandSecretConfig = {
  clientId: { label: trans("idSource.clientId"), tip: trans("idSource.clientIdTip") },
  clientSecret: {
    label: trans("idSource.clientSecret"),
    isPassword: true,
    tip: trans("idSource.clientSecretTip"),
  },
};

export const authConfig = {
  [AuthType.Form]: {
    sourceName: trans("idSource.form"),
    sourceValue: AuthType.Form,
    form: {},
  },
  [AuthType.Github]: {
    sourceName: "GitHub",
    sourceValue: AuthType.Github,
    form: clientIdandSecretConfig,
  },
  [AuthType.Google]: {
    sourceName: "Google",
    sourceValue: AuthType.Google,
    form: clientIdandSecretConfig,
  },
  [AuthType.Feishu]: {
    sourceName: trans("idSource.feishu"),
    sourceValue: AuthType.Feishu,
    form: clientIdandSecretConfig,
  },
  [AuthType.DingTalk]: {
    sourceName: trans("idSource.dingtalk"),
    sourceValue: AuthType.DingTalk,
    form: clientIdandSecretConfig,
  },
  [AuthType.Generic]: {
    sourceName: trans("idSource.genericProvider"),
    sourceValue: AuthType.Generic,
    form: {
      source: {
        label: trans("idSource.source"),
        isRequire: true,
        tip: trans("idSource.sourceTip"),
      },
      sourceName: {
        label: trans("idSource.sourceName"),
        isRequire: true,
        tip: trans("idSource.sourceNameTip"),
      },
      sourceIcon: {
        label: trans("idSource.sourceIcon"),
        isIcon: true,
        isRequire: true,
        tip: trans("idSource.sourceIconTip"),
      },
      ...clientIdandSecretConfig,
      issuerUri: {
        label: trans("idSource.sourceIssuerURI"),
        isRequire: false,
        isUrl: true,
        tip: trans("idSource.sourceIssuerURITip"),
      },
      authorizationEndpoint: {
        label: trans("idSource.sourceAuthorizationEndpoint"),
        isRequire: true,
        isUrl: true,
        tip: trans("idSource.sourceAuthorizationEndpointTip"),
      },
      tokenEndpoint: {
        label: trans("idSource.sourceTokenEndpoint"),
        isRequire: true,
        isUrl: true,
        tip: trans("idSource.sourceTokenEndpointTip"),
      },
      userInfoEndpoint: {
        label: trans("idSource.sourceUserInfoEndpoint"),
        isRequire: true,
        isUrl: true,
        tip: trans("idSource.sourceUserInfoEndpointTip"),
      },
      scope: {
        label: trans("idSource.scope"),
        tip: trans("idSource.scopeTip"),
      },
      userCanSelectAccounts: {
        label: trans("idSource.userCanSelectAccounts"),
        isSwitch: true,
        isRequire: false,
        tip: trans("idSource.userCanSelectAccountsTip"),
      },
    },
  },
} as unknown as { [key: string]: { sourceName: string; sourceValue: AuthType; form: Record<string, ItemType> } };

export interface ProviderPreset {
  label: string;
  value: string;
  issuerUri?: string;
  wellKnownEndpoint?: string;
  supportsOidcDiscovery: boolean;
  authorizationEndpoint?: string;
  tokenEndpoint?: string;
  userInfoEndpoint?: string;
  scope?: string;
  sourceMappings?: Record<string, string>;
}

export const providerPresets: ProviderPreset[] = [
  {
    label: trans("idSource.presetAuthing"),
    value: "authing",
    issuerUri: "https://{your-app}.authing.cn/oidc",
    supportsOidcDiscovery: true,
    sourceMappings: {
      uid: "sub",
      email: "email",
      username: "email",
      avatar: "picture",
    },
  },
  {
    label: trans("idSource.presetGitee"),
    value: "gitee",
    supportsOidcDiscovery: false,
    authorizationEndpoint: "https://gitee.com/oauth/authorize",
    tokenEndpoint: "https://gitee.com/oauth/token",
    userInfoEndpoint: "https://gitee.com/api/v5/user",
    scope: "user_info",
    sourceMappings: {
      uid: "id",
      email: "email",
      username: "login",
      avatar: "avatar_url",
    },
  },
];

export const FreeTypes = [AuthType.Feishu, AuthType.DingTalk, AuthType.Form, AuthType.Generic];

export const authTypeDisabled = (type: AuthType) => {
  return !FreeTypes.includes(type);
};

export const ManualSyncTypes: Array<AuthType> = [];

export type ListForm = {
  template?: FormItemType;
  ldapsearch?: FormItemType;
}

export type ItemType = {
  label: string;
  options?: DefaultOptionType[];
  isList?: boolean;
  isRequire?: boolean;
  isPassword?: boolean;
  isIcon?: boolean;
  isSwitch?: boolean;
  isUrl?: boolean;
  hasLock?: boolean;
  tip?: string;
}

export type FormItemType = {
  clientId?: ItemType;
  clientSecret?: ItemType;
  loginUri?: string;
  prefixUri?: string;
  source?: ItemType;
  sourceName?: ItemType;
  validator?: ItemType;
  url?: string;
  subType?: ItemType;
  distinguishedNameTemplate?: ItemType;
  searchBase?: ItemType;
  filter?: ItemType;
  bindDn?: ItemType;
  password?: ItemType;
  idAttribute?: ItemType;
  domainPrefix?: string;
  authServerId?: string;
  publicKey?: ItemType;
  domain?: string;
  realm?: string;
  scope?: string;
};
