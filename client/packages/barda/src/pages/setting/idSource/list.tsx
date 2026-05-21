import React, { useEffect, useState } from "react";
import { trans } from "i18n";
import {
  Level1SettingPageContentWithList,
  Level1SettingPageTitleWithBtn,
} from "pages/setting/styled";
import Column from "antd/lib/table/Column";
import { useSelector } from "react-redux";
import { getUser } from "redux/selectors/usersSelectors";
import IdSourceApi, { ConfigItem } from "api/idSourceApi";
import {
  authConfig,
  AuthType,
  IdSource,
} from "@barda/pages/setting/idSource/idSourceConstants";
import { SpanStyled, StatusSpan, TableStyled } from "pages/setting/idSource/styledComponents";
import FreeLimitTag from "pages/common/freeLimitTag";
import history from "util/history";
import { IDSOURCE_DETAIL } from "constants/routesURL";
import { selectSystemConfig } from "redux/selectors/configSelectors";
import { Badge } from "antd";
import { validateResponse } from "api/apiUtils";
import { ServerAuthTypeInfo } from "@barda/constants/authConstants";
import { GeneralLoginIcon } from "assets/icons";
import { FreeTypes } from "pages/setting/idSource/idSourceConstants";
import { messageInstance } from "barda-design";
import { FileTextOutlined } from "@ant-design/icons";
import { IconControlView } from "comps/controls/iconControl";

export const IdSourceList = () => {
  const user = useSelector(getUser);
  const { currentOrgId } = user;
  const [configs, setConfigs] = useState<ConfigItem[]>([]);
  const [enabledConfigs, setEnabledConfigs] = useState<ConfigItem[]>([]);
  const [fetching, setFetching] = useState(false);
  const enableEnterpriseLogin = useSelector(selectSystemConfig)?.featureFlag?.enableEnterpriseLogin;

  useEffect(() => {
    if (!currentOrgId) {
      return;
    }
    getConfigs();
  }, [currentOrgId]);

  if (!currentOrgId) {
    return null;
  }

  const getConfigs = () => {
    setFetching(true);
    IdSourceApi.getConfigs()
      .then((resp) => {
        if (validateResponse(resp)) {
          const apiConfigs: ConfigItem[] = resp.data.data.filter((item: ConfigItem) =>
            IdSource.includes(item.authType)
          );
          // Build a map from authType to API config (GENERIC allows multiple entries)
          const apiConfigMap: Record<string, ConfigItem> = {};
          const genericConfigs: ConfigItem[] = [];
          apiConfigs.forEach((item: ConfigItem) => {
            if (item.authType === AuthType.Generic) {
              genericConfigs.push(item);
            } else if (!apiConfigMap[item.authType] || item.enable) {
              apiConfigMap[item.authType] = item;
            }
          });
          // Build list from fixed types (exclude GENERIC, handled separately)
          const fixedTypes = IdSource.filter(t => t !== AuthType.Generic);
          const fullList: ConfigItem[] = fixedTypes.map((authType) => {
            if (apiConfigMap[authType]) {
              return apiConfigMap[authType];
            }
            return {
              id: authType,
              authType,
              enable: false,
              enableRegister: true,
              source: authType,
              sourceName: authConfig[authType]?.sourceName || authType,
            } as ConfigItem;
          });
          // Append all GENERIC instances
          fullList.push(...genericConfigs);
          // Append a GENERIC placeholder row for adding new providers
          fullList.push({
            id: "GENERIC_new",
            authType: AuthType.Generic,
            enable: false,
            enableRegister: true,
            source: AuthType.Generic,
            sourceName: trans("idSource.addGenericProvider"),
          } as ConfigItem);
          const enabledRes = fullList.filter(item => item.enable);
          setConfigs(fullList);
          setEnabledConfigs(enabledRes);
        }
      })
      .catch((e) => {
        messageInstance.error(e.message);
      })
      .finally(() => {
        setFetching(false);
      });
  };

  return (
    <>
      <Level1SettingPageContentWithList>
        <Level1SettingPageTitleWithBtn>
          {trans("idSource.title")}
        </Level1SettingPageTitleWithBtn>
        <TableStyled
          tableLayout={"auto"}
          scroll={{ x: "100%" }}
          pagination={false}
          rowKey="id"
          loading={fetching}
          dataSource={configs}
          onRow={(record) => ({
            onClick: () => {
              const otherEnabledCount = enabledConfigs.filter(c => c.id !== (record as ConfigItem).id).length;
              history.push({
                pathname: IDSOURCE_DETAIL,
                state: { config: record, totalEnabledConfigs: otherEnabledCount, allConfigs: configs },
              });
            },
          })}
        >
          <Column
            title={trans("idSource.loginType")}
            dataIndex="authType"
            key="authType"
            render={(value: AuthType, record: ConfigItem) => (
              <SpanStyled $disabled={!record.enable}>
                {
                  (record as any).sourceIcon
                    ? <span className="sourceIcon"><IconControlView value={(record as any).sourceIcon} /></span>
                    : <img
                      src={(value !== AuthType.Generic && ServerAuthTypeInfo[value as keyof typeof ServerAuthTypeInfo]?.logo) || GeneralLoginIcon}
                      alt={value}
                    />
                }
                <span>
                  {value === AuthType.Generic
                    ? record.sourceName
                    : authConfig[value as AuthType].sourceName
                  }
                </span>
                {!FreeTypes.includes(value) && (
                  <FreeLimitTag
                    text={
                      enableEnterpriseLogin ? trans("idSource.payUserTag") : trans("idSource.pay")
                    }
                  />
                )}
              </SpanStyled>
            )}
          />
          <Column
            title={trans("idSource.status")}
            dataIndex="enable"
            key="enable"
            render={(value, record: ConfigItem) => (
              <StatusSpan>
                {value ? (
                  <Badge status="success" text={trans("idSource.enable")} />
                ) : (
                  <Badge status="default" text={trans("idSource.unEnable")} />
                )}
              </StatusSpan>
            )}
          />
        </TableStyled>

        <a
          href="https://docs.barda.com.cn/#/identity-source/README"
          target="_blank"
          rel="noopener noreferrer"
          style={{ display: "inline-flex", alignItems: "center", marginTop: 24, marginLeft: 12, fontSize: 13, color: "#8c8c8c" }}
        >
          <FileTextOutlined style={{ marginRight: 4 }} />
          {trans("idSource.aboutIdSource")}
        </a>
      </Level1SettingPageContentWithList>
    </>
  );
};
