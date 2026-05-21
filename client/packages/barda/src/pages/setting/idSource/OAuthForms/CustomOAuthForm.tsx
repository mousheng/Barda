import { useState, useCallback } from "react";
import { messageInstance, CloseEyeIcon, CustomSelect } from "barda-design";
import { trans } from "i18n";
import {
  FormStyled,
  PasswordLabel,
  StyledSteps
} from "../styledComponents";
import { default as Form, FormInstance } from "antd/es/form";
import { default as Input } from "antd/es/input";
import { default as AutoComplete } from "antd/es/auto-complete";
import { default as Tooltip } from "antd/es/tooltip";
import IdSourceApi, { ConfigItem } from "api/idSourceApi";
import { validateResponse } from "api/apiUtils";
import { authConfig, AuthType, ItemType, providerPresets } from "../idSourceConstants";
import _ from "lodash";
import { ImageUrlInput } from "./ImageUrlInput";
import Flex from "antd/es/flex";
import Button from "antd/es/button";
import Switch from "antd/es/switch";
import Checkbox from "antd/es/checkbox";
import Alert from "antd/es/alert";
import Collapse from "antd/es/collapse";
import Typography from "antd/es/typography";
import Space from "antd/es/space";
import Spin from "antd/es/spin";
import axios from "axios";
import { CheckCircleFilled, CloseCircleFilled } from "@ant-design/icons";

const URL_RULE = {
  pattern: /^https?:\/\/.+/i,
  message: trans("idSource.invalidUrl"),
};

export const sourceMappingKeys = [
  'uid',
  'email',
  'username',
  'avatar',
];

const steps = [
  {
    title: trans("idSource.genericStep1"),
    description: trans("idSource.genericStep1Desc"),
  },
  {
    title: trans("idSource.genericStep2"),
    description: trans("idSource.genericStep2Desc"),
  },
  {
    title: trans("idSource.genericStep3"),
    description: trans("idSource.genericStep3Desc"),
  },
];

interface OpenIdProvider {
  issuer: string,
  authorization_endpoint: string,
  token_endpoint: string,
  userinfo_endpoint: string,
  jwks_uri?: string,
  scopes_supported: string[],
}

export interface ConfigProvider {
  authType: string,
  source: string,
  sourceName: string,
  sourceIcon?: string,
  issuer: string,
  issuerUri?: string,
  wellKnownEndpoint?: string,
  authorizationEndpoint: string,
  tokenEndpoint: string,
  userInfoEndpoint: string,
  jwksUri?: string,
  scope: string,
  sourceMappings: any,
  userCanSelectAccounts?: boolean,
}

type CustomOAuthFormProp = {
  authType: AuthType,
  onSave: () => void;
  onCancel: () => void;
};

function deriveWellKnownUrl(issuerUri: string): string {
  const trimmed = issuerUri.trim().replace(/\/$/, '');
  if (!trimmed) return '';
  if (trimmed.includes('/.well-known/openid-configuration')) {
    return trimmed;
  }
  return `${trimmed}/.well-known/openid-configuration`;
}

function CustomOAuthForm(props: CustomOAuthFormProp) {
  const {
    authType,
    onSave,
    onCancel
  } = props;

  const [form1] = Form.useForm<ConfigProvider>();

  const [saveLoading, setSaveLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(false);
  const [fetchStatus, setFetchStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [currentStep, setCurrentStep] = useState(0);
  const [issuerDetails, setIssuerDetails] = useState<ConfigProvider | {}>({});

  function saveAuthProvider(values: ConfigItem) {
    setSaveLoading(true);
    const { wellKnownEndpoint, enableRegister, ...restValues } = values as any;
    const config = {
      ...restValues,
      enableRegister: enableRegister ?? true,
    }
    IdSourceApi.saveConfig(config)
      .then((resp) => {
        if (validateResponse(resp)) {
          messageInstance.success(trans("idSource.saveSuccess"));
          onSave();
        }
      })
      .catch((e) => messageInstance.error(e.message))
      .finally(() => {
        setSaveLoading(false);
      });
  }

  const handleFetchWellKnown = useCallback(async () => {
    const wellKnown = form1.getFieldValue('wellKnownEndpoint');
    if (!wellKnown || !wellKnown.trim()) {
      messageInstance.warning(trans("idSource.formPlaceholder", {
        label: trans("idSource.wellKnownEndpoint"),
      }));
      return;
    }
    setFetchLoading(true);
    setFetchStatus('loading');
    try {
      const res = await axios.get<OpenIdProvider>(wellKnown.trim(), { timeout: 10000 });
      const issuerFromResponse = res.data.issuer || form1.getFieldValue('issuerUri') || '';
      form1.setFieldsValue({
        issuerUri: issuerFromResponse,
        authorizationEndpoint: res.data.authorization_endpoint,
        tokenEndpoint: res.data.token_endpoint,
        userInfoEndpoint: res.data.userinfo_endpoint,
        jwksUri: res.data.jwks_uri,
        scope: res.data.scopes_supported?.join(' ') || 'openid',
      });
      setFetchStatus('success');
      messageInstance.success(trans("idSource.issuerFetchSuccess"));
    } catch (e: any) {
      setFetchStatus('error');
      if (e?.code === 'ECONNABORTED') {
        messageInstance.warning(trans("idSource.issuerFetchTimeout"));
      } else {
        messageInstance.warning(trans("idSource.issuerFetchFailed"));
      }
    } finally {
      setFetchLoading(false);
    }
  }, [form1]);

  const handleIssuerUriChange = useCallback(() => {
    const issuerUri = form1.getFieldValue('issuerUri');
    if (issuerUri && issuerUri.trim()) {
      const derived = deriveWellKnownUrl(issuerUri);
      form1.setFieldValue('wellKnownEndpoint', derived);
    }
  }, [form1]);

  const handleProviderSelect = useCallback((value: string) => {
    const preset = providerPresets.find(p => p.value === value);
    if (!preset) return;

    if (preset.supportsOidcDiscovery) {
      // OIDC provider: populate issuer URI + well-known endpoint, user edits domain and clicks Fetch
      if (preset.issuerUri) {
        form1.setFieldValue('issuerUri', preset.issuerUri);
        form1.setFieldValue('wellKnownEndpoint', deriveWellKnownUrl(preset.issuerUri));
      } else if (preset.wellKnownEndpoint) {
        form1.setFieldValue('wellKnownEndpoint', preset.wellKnownEndpoint);
      }
      messageInstance.info(trans("idSource.presetOidcHint"));
    } else {
      // Non-OIDC provider: directly fill all endpoint fields
      if (preset.authorizationEndpoint) form1.setFieldValue('authorizationEndpoint', preset.authorizationEndpoint);
      if (preset.tokenEndpoint) form1.setFieldValue('tokenEndpoint', preset.tokenEndpoint);
      if (preset.userInfoEndpoint) form1.setFieldValue('userInfoEndpoint', preset.userInfoEndpoint);
      if (preset.scope) form1.setFieldValue('scope', preset.scope);
      form1.setFieldValue('issuerUri', '');
      form1.setFieldValue('wellKnownEndpoint', '');
      messageInstance.success(trans("idSource.presetManualHint", { name: preset.label }));
    }
    if (preset.sourceMappings) {
      Object.entries(preset.sourceMappings).forEach(([key, val]) => {
        form1.setFieldValue(key as any, val);
      });
    }
  }, [form1]);

  const handleStep1Save = () => {
    form1.validateFields().then((values) => {
      setIssuerDetails(issuerDetails => ({
        ...issuerDetails,
        ...values,
        authType: AuthType.Generic,
      }))
      setCurrentStep(currentStep => currentStep + 1);
    })
  }

  const handleStep2Save = () => {
    form1.validateFields().then(values => {
      setIssuerDetails(issuerDetails => ({
        ...issuerDetails,
        ...values,
        authType: AuthType.Generic,
      }))
      setCurrentStep(currentStep => currentStep + 1);
    })
  }

  const handleStep3Save = () => {
    form1.validateFields().then(values => {
      setIssuerDetails((issuerDetails: any) => {
        const updatedDetails = {
          ...issuerDetails,
          sourceMappings: {
            ...values,
          },
          authType: AuthType.Generic,
        };
        saveAuthProvider(updatedDetails);
        return updatedDetails;
      });
    })
  }

  const handleSave = () => {
    if (currentStep === 0) {
      return handleStep1Save();
    }
    if (currentStep === 1) {
      return handleStep2Save();
    }
    if (currentStep === 2) {
      return handleStep3Save();
    }
  }

  function handleCancel() {
    if (currentStep === 0) {
      onCancel();
      return form1.resetFields();
    }
    setCurrentStep(currentStep => currentStep - 1);
  }

  const authConfigForm = authConfig[AuthType.Generic].form;

  const renderFetchStatus = () => {
    if (fetchStatus === 'loading') {
      return <Spin size="small" />;
    }
    if (fetchStatus === 'success') {
      return <CheckCircleFilled style={{ color: '#52c41a' }} />;
    }
    if (fetchStatus === 'error') {
      return <CloseCircleFilled style={{ color: '#ff4d4f' }} />;
    }
    return null;
  };

  return (
    <>
      <StyledSteps
        current={currentStep}
        items={steps}
        style={{marginBottom: '16px'}}
        onChange={(current) => setCurrentStep(current)}
      />

      <Collapse
        ghost
        style={{marginBottom: '16px'}}
        items={[
          {
            key: 'help',
            label: <Typography.Text strong>{trans("idSource.genericHelpTitle")}</Typography.Text>,
            children: (
              <Flex vertical gap="12px">
                <Alert
                  type="info"
                  message={trans("idSource.genericHelpAuthingName")}
                  description={
                    <Flex vertical gap="4px">
                      <Typography.Text>{trans("idSource.genericHelpAuthingDesc")}</Typography.Text>
                      <Typography.Text code>{trans("idSource.genericHelpAuthingIssuer")}</Typography.Text>
                      <Typography.Text>{trans("idSource.genericHelpAuthingScope")}</Typography.Text>
                      <Typography.Text>{trans("idSource.genericHelpAuthingMappings")}</Typography.Text>
                    </Flex>
                  }
                />
                <Alert
                  type="info"
                  message={trans("idSource.genericHelpGiteeName")}
                  description={
                    <Flex vertical gap="4px">
                      <Typography.Text>{trans("idSource.genericHelpGiteeDesc")}</Typography.Text>
                      <Typography.Text type="danger">{trans("idSource.genericHelpRequired")}:</Typography.Text>
                      <Typography.Text code>{trans("idSource.genericHelpGiteeAuthEndpoint")}</Typography.Text>
                      <Typography.Text code>{trans("idSource.genericHelpGiteeTokenEndpoint")}</Typography.Text>
                      <Typography.Text code>{trans("idSource.genericHelpGiteeUserInfoEndpoint")}</Typography.Text>
                      <Typography.Text>{trans("idSource.genericHelpGiteeScope")}</Typography.Text>
                      <Typography.Text>{trans("idSource.genericHelpGiteeMappings")}</Typography.Text>
                    </Flex>
                  }
                />
              </Flex>
            ),
          },
        ]}
      />

      <FormStyled
        form={form1 as unknown as FormInstance<unknown>}
        name="generic"
        layout="vertical"
        style={{ maxWidth: '100%' }}
        autoComplete="off"
      >
        {currentStep === 0 && (
          <Flex vertical gap="16px">
            <Form.Item
              name="issuerUri"
              label={trans("idSource.sourceIssuerURI")}
              rules={[{ required: false }, URL_RULE]}
            >
              <Input
                onChange={handleIssuerUriChange}
                placeholder={trans("idSource.formPlaceholder", {
                  label: trans("idSource.sourceIssuerURI"),
                })}
              />
            </Form.Item>
            <Form.Item
              name="wellKnownEndpoint"
              label={
                <Tooltip title={trans("idSource.wellKnownEndpointTip")}>
                  <span className="has-tip">{trans("idSource.wellKnownEndpoint")}</span>
                </Tooltip>
              }
              rules={[{ required: false }, URL_RULE]}
            >
              <Space.Compact style={{ width: '100%' }}>
                <AutoComplete
                  options={providerPresets}
                  onSelect={handleProviderSelect}
                  placeholder={trans("idSource.formPlaceholder", {
                    label: trans("idSource.wellKnownEndpoint"),
                  })}
                  style={{ flex: 1 }}
                />
                <Button
                  type="default"
                  onClick={handleFetchWellKnown}
                  loading={fetchLoading}
                  icon={renderFetchStatus()}
                >
                  {fetchLoading ? trans("idSource.fetchingConfig") : trans("idSource.fetchConfig")}
                </Button>
              </Space.Compact>
            </Form.Item>
            <Typography.Text type="secondary">
              {trans("idSource.issuerOptionalHint")}
            </Typography.Text>
            <Form.Item
              name="enableRegister"
              valuePropName="checked"
              initialValue={true}
            >
              <Checkbox>{trans("idSource.enableRegister")}</Checkbox>
            </Form.Item>
          </Flex>
        )}
        {currentStep === 1 && Object.entries(authConfigForm).filter(([key]) => key !== 'issuerUri').map(([key, value]) => {
          const valueObject = _.isObject(value) ? (value as ItemType) : false;
          let required = (key === "clientId" || key === "clientSecret" || key === "scope");
          required = valueObject ? valueObject.isRequire ?? required : required;
          const label = valueObject ? valueObject.label : value as unknown as string;
          const tip = valueObject && valueObject.tip;
          const isPassword = valueObject && valueObject.isPassword;
          const isIcon = valueObject && valueObject.isIcon;
          const isList = valueObject && valueObject.isList;
          const isSwitch = valueObject && valueObject.isSwitch;
          const isUrl = valueObject && valueObject.isUrl;
          return (
            <div key={key}>
              <Form.Item
                key={key}
                name={key}
                valuePropName={isSwitch ? "checked" : "value"}
                rules={[
                  {
                    required,
                    message: trans("idSource.formPlaceholder", {
                      label,
                    }),
                  },
                  ...(isUrl ? [URL_RULE] : []),
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
                    placeholder={trans("idSource.encryptedServer")}
                    autoComplete={"one-time-code"}
                  />
                ) : isSwitch ? (
                  <Switch />
                ) : isIcon ? (
                  <ImageUrlInput
                    value={form1.getFieldValue("sourceIcon") || ""}
                    onChange={(value) => form1.setFieldValue("sourceIcon", value)}
                  />
                ) : isList ? (
                  <CustomSelect
                    options={(value as ItemType).options}
                    placeholder={trans("idSource.formSelectPlaceholder", {
                      label,
                    })}
                  />
                ) : (
                  <Input
                    placeholder={trans("idSource.formPlaceholder", {
                      label,
                    })}
                  />
                )}
              </Form.Item>
            </div>
          );
        })}
        {currentStep === 2 && sourceMappingKeys.map(sourceKey => (
          <Flex gap="10px" align="start" key={sourceKey} >
            <Input
              readOnly
              disabled
              value={sourceKey}
              style={{flex: 1}}
            />
            <span> &#8594; </span>
            <Form.Item
              name={sourceKey}
              rules={[{ required: true }]}
              style={{flex: 1}}
            >
              <Input
                placeholder={trans("idSource.formPlaceholder", {
                  label: sourceKey,
                })}
              />
            </Form.Item>
          </Flex>
        ))}
        <Flex justify="end" gap={'8px'}>
          <Button
            type="default"
            style={{margin: 0}}
            onClick={handleCancel}
          >
            {trans("cancel")}
          </Button>
          <Button
            type="primary"
            style={{margin: 0}}
            onClick={handleSave}
            loading={saveLoading}
          >
            {trans("idSource.save")}
          </Button>
        </Flex>
      </FormStyled>
    </>
  );
}

export default CustomOAuthForm;
