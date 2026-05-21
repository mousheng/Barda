import React, { useCallback, useEffect, useState } from "react";
import { trans, transToNode } from "i18n";
import IdSourceApi, { ConfigItem } from "api/idSourceApi";
import { DetailContainer } from "pages/setting/theme/styledComponents";
import { HeaderBack } from "pages/setting/permission/styledComponents";
import { ArrowIcon, CustomModal, CustomSelect, LockIcon, UnLockIcon, CloseEyeIcon } from "barda-design";
import history from "util/history";
import { IDSOURCE_SETTING } from "constants/routesURL";
import {
  authConfig,
  AuthType,
  ManualSyncTypes,
  providerPresets,
} from "@barda/pages/setting/idSource/idSourceConstants";
import { Manual } from "pages/setting/idSource/detail/manual";
import { DeleteConfig } from "pages/setting/idSource/detail/deleteConfig";
import { Divider, Form, Input, Tooltip, AutoComplete } from "antd";
import { SaveButton, FormStyled, PasswordLabel, Content, Header } from "pages/setting/idSource/styledComponents";
import { validateResponse } from "api/apiUtils";
import { ItemType } from "pages/setting/idSource/idSourceConstants";
import { useForm } from "antd/es/form/Form";
import _ from "lodash";
import { messageInstance } from "barda-design";
import { ImageUrlInput } from "../OAuthForms/ImageUrlInput";
import Switch from "antd/es/switch";
import Checkbox from "antd/es/checkbox";
import Title from "antd/es/typography/Title";
import { sourceMappingKeys } from "../OAuthForms/CustomOAuthForm";
import Flex from "antd/es/flex";
import Space from "antd/es/space";
import Spin from "antd/es/spin";
import Button from "antd/es/button";
import axios from "axios";
import { CheckCircleFilled, CloseCircleFilled } from "@ant-design/icons";

interface OpenIdProvider {
  issuer: string;
  authorization_endpoint: string;
  token_endpoint: string;
  userinfo_endpoint: string;
  jwks_uri?: string;
  scopes_supported: string[];
}

function deriveWellKnownUrl(issuerUri: string): string {
  const trimmed = issuerUri.trim().replace(/\/$/, "");
  if (!trimmed) return "";
  if (trimmed.includes("/.well-known/openid-configuration")) {
    return trimmed;
  }
  return `${trimmed}/.well-known/openid-configuration`;
}

type IdSourceDetailProps = {
  location: Location & { state: { config: ConfigItem; totalEnabledConfigs: number; allConfigs?: ConfigItem[] } };
};

export const IdSourceDetail = (props: IdSourceDetailProps) => {
  const { config: configDetail, totalEnabledConfigs, allConfigs } = props.location.state;
  // totalEnabledConfigs now = count of OTHER enabled configs (excluding current)
  const isLastEnabledConfig = totalEnabledConfigs === 0 && !!configDetail.enable;
  const [form] = useForm();
  const [lock, setLock] = useState(() => {
    const { config } = props.location.state;
    return !config.ifLocal;
  });
  const [saveLoading, setSaveLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(false);
  const [fetchStatus, setFetchStatus] = useState<"idle" | "loading" | "success" | "error">("idle");
  const [saveDisable, setSaveDisable] = useState(() => {
    const { config } = props.location.state;
    if ((config.authType === AuthType.Form && !config.enable) || (!config.ifLocal && !config.enable)) {
      return false;
    } else {
      return true;
    }
  });

  useEffect(() => {
    if (configDetail.authType === AuthType.Generic) {
      sourceMappingKeys.forEach((sourceKey) => {
        form.setFieldValue(sourceKey, (configDetail as any).sourceMappings?.[sourceKey]);
      });
    }
  }, [configDetail, form]);

  const goList = () => {
    history.push(IDSOURCE_SETTING);
  };
  if (!configDetail) {
    goList();
  }
  const handleSuccess = (values: any) => {
    // Check for duplicate source among GENERIC configs
    if (configDetail.authType === AuthType.Generic && allConfigs) {
      const sourceVal = values.source || configDetail.source;
      const dup = allConfigs.find(
        (c) => c.authType === AuthType.Generic && c.id !== configDetail.id && c.id !== "GENERIC_new" && c.source === sourceVal
      );
      if (dup) {
        messageInstance.error(trans("idSource.duplicateSource"));
        return;
      }
    }

    setSaveLoading(true);
    const { wellKnownEndpoint, enableRegister, ...cleanValues } = values;
    // strip placeholder clientSecret so backend keeps existing value
    if (cleanValues.clientSecret === "********") {
      delete cleanValues.clientSecret;
    }
    const id = configDetail.id === "GENERIC_new"
      ? `GENERIC_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 8)}`
      : configDetail.id;
    let params = {
      id,
      authType: configDetail.authType,
      enableRegister: enableRegister ?? configDetail.enableRegister,
    };

    if (configDetail.authType === AuthType.Generic) {
      const { uid, email, avatar, username, ...newValues } = cleanValues;
      params = {
        ...newValues,
        sourceMappings: {
          uid,
          email,
          avatar,
          username,
        },
        ...params,
      };
    } else {
      params = {
        ...cleanValues,
        ...params,
      };
    }
    IdSourceApi.saveConfig(params)
      .then((resp) => {
        if (validateResponse(resp)) {
          messageInstance.success(trans("idSource.saveSuccess"), 0.8, goList);
        }
      })
      .catch((e) => messageInstance.error(e.message))
      .finally(() => setSaveLoading(false));
  };

  const handleChange = (allValues: { [key: string]: string }) => {
    let ifChange = false;
    let ifError = false;
    for (const key in allValues) {
      const item = configDetail[key as keyof ConfigItem];
      const val = allValues[key];
      // treat placeholder as equivalent to the original secret
      const normalizedVal = (key === "clientSecret" && val === "********") ? undefined : val;
      const normalizedItem = (key === "clientSecret" && item === undefined) ? undefined : item;
      if (normalizedVal !== normalizedItem && (normalizedVal || normalizedItem)) {
        ifChange = true;
      }
    }
    const requiredValues = {} as { [key: string]: string };
    Object.entries(authConfig[configDetail.authType].form).forEach(([key, value]) => {
      if (typeof value === "string" || value.isRequire !== false) {
        key in allValues && (requiredValues[key] = allValues[key]);
      }
    });
    if (configDetail.ifLocal) {
      ifError = Object.entries(requiredValues).findIndex(([key, item]) => {
        if (key === "clientSecret" && item === "********") return false;
        return item === "" || item === undefined;
      }) >= 0;
    } else {
      for (const key in requiredValues) {
        const value = requiredValues[key as string];
        if (key !== "clientSecret" && key !== "publicKey" && (value === "" || value === undefined || value === null)) {
          ifError = true;
        }
      }
    }
    if (
      (configDetail.authType === AuthType.Form && !configDetail.enable) ||
      (!configDetail.ifLocal && !configDetail.enable && !ifError) ||
      (ifChange && !ifError)
    ) {
      setSaveDisable(false);
    } else {
      setSaveDisable(true);
    }
  };

  const handleLockClick = () => {
    CustomModal.confirm({
      title: trans("idSource.disableTip"),
      content: trans("idSource.lockModalContent"),
      onConfirm: () => setLock(false),
    });
  };

  const handleFetchWellKnown = useCallback(async () => {
    const wellKnown = form.getFieldValue("wellKnownEndpoint");
    if (!wellKnown || !wellKnown.trim()) {
      messageInstance.warning(
        trans("idSource.formPlaceholder", {
          label: trans("idSource.wellKnownEndpoint"),
        })
      );
      return;
    }
    setFetchLoading(true);
    setFetchStatus("loading");
    try {
      const res = await axios.get<OpenIdProvider>(wellKnown.trim(), { timeout: 10000 });
      form.setFieldsValue({
        issuerUri: res.data.issuer || form.getFieldValue("issuerUri") || "",
        authorizationEndpoint: res.data.authorization_endpoint,
        tokenEndpoint: res.data.token_endpoint,
        userInfoEndpoint: res.data.userinfo_endpoint,
        scope: res.data.scopes_supported?.join(" ") || "openid",
      });
      setFetchStatus("success");
      messageInstance.success(trans("idSource.issuerFetchSuccess"));
    } catch (e: any) {
      setFetchStatus("error");
      if (e?.code === "ECONNABORTED") {
        messageInstance.warning(trans("idSource.issuerFetchTimeout"));
      } else {
        messageInstance.warning(trans("idSource.issuerFetchFailed"));
      }
    } finally {
      setFetchLoading(false);
    }
  }, [form]);

  const handleProviderSelect = useCallback(
    (value: string) => {
      const preset = providerPresets.find((p) => p.value === value);
      if (!preset) return;

      if (preset.supportsOidcDiscovery) {
        if (preset.issuerUri) {
          form.setFieldValue("issuerUri", preset.issuerUri);
          form.setFieldValue("wellKnownEndpoint", deriveWellKnownUrl(preset.issuerUri));
        } else if (preset.wellKnownEndpoint) {
          form.setFieldValue("wellKnownEndpoint", preset.wellKnownEndpoint);
        }
        messageInstance.info(trans("idSource.presetOidcHint"));
      } else {
        if (preset.authorizationEndpoint) form.setFieldValue("authorizationEndpoint", preset.authorizationEndpoint);
        if (preset.tokenEndpoint) form.setFieldValue("tokenEndpoint", preset.tokenEndpoint);
        if (preset.userInfoEndpoint) form.setFieldValue("userInfoEndpoint", preset.userInfoEndpoint);
        if (preset.scope) form.setFieldValue("scope", preset.scope);
        form.setFieldValue("issuerUri", "");
        form.setFieldValue("wellKnownEndpoint", "");
        messageInstance.success(trans("idSource.presetManualHint", { name: preset.label }));
      }
      if (preset.sourceMappings) {
        Object.entries(preset.sourceMappings).forEach(([key, val]) => {
          form.setFieldValue(key, val);
        });
      }
    },
    [form]
  );

  const renderFetchStatus = () => {
    if (fetchStatus === "loading") return <Spin size="small" />;
    if (fetchStatus === "success") return <CheckCircleFilled style={{ color: "#52c41a" }} />;
    if (fetchStatus === "error") return <CloseCircleFilled style={{ color: "#ff4d4f" }} />;
    return null;
  };

  const renderField = (key: string, value: any) => {
    const valueObject = _.isObject(value) ? (value as ItemType) : false;
    let required = key === "clientId" || key === "clientSecret" || key === "scope";
    required = valueObject ? valueObject.isRequire ?? required : required;
    const hasLock = valueObject && valueObject?.hasLock;
    const tip = valueObject && valueObject.tip;
    const label = valueObject ? valueObject.label : (value as unknown as string);
    const isList = valueObject && valueObject.isList;
    const isPassword = valueObject && valueObject.isPassword;
    const isIcon = valueObject && valueObject.isIcon;
    const isSwitch = valueObject && valueObject.isSwitch;
    const isUrl = valueObject && valueObject.isUrl;
    const urlRule = { pattern: /^https?:\/\/.+/i, message: trans("idSource.invalidUrl") };
    return (
      <div key={key}>
        <Form.Item
          key={key}
          name={key}
          valuePropName={isSwitch ? "checked" : "value"}
          className={hasLock && lock ? "lock" : ""}
          rules={[
            {
              required,
              message: isList
                ? trans("idSource.formSelectPlaceholder", { label })
                : trans("idSource.formPlaceholder", { label }),
            },
            ...(isUrl ? [urlRule] : []),
          ]}
          label={
            isPassword ? (
              <PasswordLabel>
                <span>{label}:</span>
                <CloseEyeIcon />
              </PasswordLabel>
            ) : (
              <Tooltip title={tip}>
                <span className={tip ? "has-tip" : ""}>{label}</span>:
              </Tooltip>
            )
          }
        >
          {isPassword ? (
            <Input
              type={"password"}
              placeholder={
                configDetail.ifLocal ? trans("idSource.formPlaceholder", { label }) : trans("idSource.encryptedServer")
              }
              autoComplete={"one-time-code"}
            />
          ) : isSwitch ? (
            <Switch />
          ) : isIcon ? (
            <ImageUrlInput
              value={form.getFieldValue("sourceIcon") || ""}
              onChange={(val: string) => form.setFieldValue("sourceIcon", val)}
            />
          ) : isList ? (
            <CustomSelect
              options={(value as ItemType).options}
              placeholder={trans("idSource.formSelectPlaceholder", { label })}
            />
          ) : (
            <Input
              placeholder={trans("idSource.formPlaceholder", { label })}
              disabled={hasLock && lock}
              prefix={hasLock && (lock ? <LockIcon onClick={() => handleLockClick()} /> : <UnLockIcon />)}
            />
          )}
        </Form.Item>
        {hasLock && lock && <span className="lock-tip">{transToNode("idSource.lockTip", { icon: <LockIcon /> })}</span>}
      </div>
    );
  };

  const isGeneric = configDetail.authType === AuthType.Generic;
  const formConfig = authConfig[configDetail.authType].form;
  const watchedIssuerUri = Form.useWatch("issuerUri", form);

  useEffect(() => {
    if (isGeneric && watchedIssuerUri && watchedIssuerUri.trim()) {
      form.setFieldValue("wellKnownEndpoint", deriveWellKnownUrl(watchedIssuerUri));
    }
  }, [watchedIssuerUri, isGeneric, form]);

  return (
    <DetailContainer>
      <Header>
        <HeaderBack>
          <span onClick={() => goList()}>{trans("idSource.title")}</span>
          <ArrowIcon />
          <span>{authConfig[configDetail.authType].sourceName}</span>
        </HeaderBack>
      </Header>
      <Content>
        <FormStyled
          form={form}
          name="basic"
          layout="vertical"
          style={{ maxWidth: 440 }}
          initialValues={{
            ...configDetail,
            // pre-derive wellKnownEndpoint for Generic type
            ...(isGeneric ? { wellKnownEndpoint: deriveWellKnownUrl((configDetail as any).issuerUri || "") } : {}),
            // set placeholder for clientSecret when not returned from server
            ...(!configDetail.clientSecret ? { clientSecret: "********" } : {}),
          }}
          onFinish={(values: unknown) => handleSuccess(values as ConfigItem)}
          autoComplete="off"
          onValuesChange={(changedValues: any, allValues: unknown) =>
            handleChange(allValues as { [key: string]: string })
          }
        >
          {/* Well-Known Discovery Section — only for Generic */}
          {isGeneric && (
            <>
              <Title level={5} style={{ marginBottom: 16 }}>
                {trans("idSource.wellKnownEndpoint")}
              </Title>
              <Flex gap="8px" align="flex-start">
                <Form.Item name="wellKnownEndpoint" rules={[{ required: false }]} style={{ flex: 1, marginBottom: 0 }}>
                  <AutoComplete
                    options={providerPresets}
                    onSelect={handleProviderSelect}
                    placeholder={trans("idSource.formPlaceholder", {
                      label: trans("idSource.wellKnownEndpoint"),
                    })}
                  />
                </Form.Item>
                <Button
                  type="default"
                  onClick={handleFetchWellKnown}
                  loading={fetchLoading}
                  icon={fetchStatus === "idle" ? undefined : renderFetchStatus()}
                  style={{ marginTop: 0 }}
                >
                  {fetchLoading ? trans("idSource.fetchingConfig") : trans("idSource.fetchConfig")}
                </Button>
              </Flex>
              <Divider style={{ margin: "24px 0" }} />
            </>
          )}

          {/* Form fields — for Generic, grouped with section headers */}
          {isGeneric ? (
            <>
              {/* Basic Info */}
              <Title level={5} style={{ marginBottom: 16 }}>
                {trans("idSource.sectionBasicInfo")}
              </Title>
              {["source", "sourceName", "sourceIcon"]
                .filter((k) => k in formConfig)
                .map((k) => renderField(k, formConfig[k]))}

              <Divider />

              {/* Endpoints */}
              <Title level={5} style={{ marginBottom: 16 }}>
                {trans("idSource.sectionEndpoints")}
              </Title>
              {["issuerUri", "authorizationEndpoint", "tokenEndpoint", "userInfoEndpoint"]
                .filter((k) => k in formConfig)
                .map((k) => renderField(k, formConfig[k]))}

              <Divider />

              {/* Credentials */}
              <Title level={5} style={{ marginBottom: 16 }}>
                {trans("idSource.sectionCredentials")}
              </Title>
              {["clientId", "clientSecret"].filter((k) => k in formConfig).map((k) => renderField(k, formConfig[k]))}

              <Divider />

              {/* Settings */}
              <Title level={5} style={{ marginBottom: 16 }}>
                {trans("idSource.sectionSettings")}
              </Title>
              {["scope", "userCanSelectAccounts"]
                .filter((k) => k in formConfig)
                .map((k) => renderField(k, formConfig[k]))}
            </>
          ) : (
            /* Non-Generic: flat list */
            Object.entries(formConfig).map(([key, value]) => renderField(key, value))
          )}

          {/* Source Mappings — only for Generic */}
          {isGeneric && (
            <>
              <Divider />
              <Title level={5}>{trans("idSource.sourceMappings")}</Title>
              {sourceMappingKeys.map((sourceKey) => (
                <Flex gap="10px" align="start" key={sourceKey}>
                  <Input readOnly disabled value={sourceKey} style={{ flex: 1 }} />
                  <span> &#8594; </span>
                  <Form.Item name={sourceKey} rules={[{ required: true }]} style={{ flex: 1 }}>
                    <Input placeholder={trans("idSource.formPlaceholder", { label: sourceKey })} />
                  </Form.Item>
                </Flex>
              ))}
            </>
          )}

          <Divider />

          <Form.Item name="enableRegister" valuePropName="checked">
            <Checkbox>{trans("idSource.enableRegister")}</Checkbox>
          </Form.Item>

          <SaveButton loading={saveLoading} disabled={saveDisable} htmlType="submit">
            {configDetail.enable ? trans("idSource.save") : trans("idSource.saveBtn")}
          </SaveButton>
        </FormStyled>
        {ManualSyncTypes.includes(configDetail.authType) && (
          <>
            <Divider />
            <Manual type={configDetail.authType} />
          </>
        )}
        {configDetail.id !== "GENERIC_new" && (
          <>
            <Divider />
            <DeleteConfig
              id={configDetail.id}
              allowDisable={configDetail.enable}
              isLastEnabledConfig={isLastEnabledConfig}
              authType={configDetail.authType}
            />
          </>
        )}
      </Content>
    </DetailContainer>
  );
};
