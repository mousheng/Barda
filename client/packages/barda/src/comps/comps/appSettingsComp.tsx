import { anchorItemKey } from "@barda/pages/editor/left/settingsPanel";
import { Divider, Typography } from "antd";
import { ThemeDetail, ThemeType } from "api/commonSettingApi";
import { RecordConstructorToComp } from "barda-core";
import { AddIcon, CustomModal, Dropdown } from "barda-design";
import { dropdownInputSimpleControl } from "comps/controls/dropdownInputSimpleControl";
import { MultiCompBuilder, valueComp } from "comps/generators";
import { DEFAULT_THEMEID } from "comps/utils/themeUtil";
import { THEME_SETTING } from "constants/routesURL";
import { GreyTextColor } from "constants/style";
import { trans } from "i18n";
import { EllipsisSpan } from "pages/setting/theme/styledComponents";
import { useEffect } from "react";
import { useSelector } from "react-redux";
import { getDefaultTheme, getThemeList } from "redux/selectors/commonSettingSelectors";
import styled, { css } from "styled-components";
import { dropdownControl } from "../controls/dropdownControl";
import { CustomShortcutsComp } from "./customShortcutsComp";

const TitleText = trans("appSetting.pageSettings");
const USER_DEFINE = "__USER_DEFINE";
const { Text } = Typography;

const ItemSpan = styled.span`
  display: inline-flex;
  align-items: center;
  margin: 0 1px;
  max-width: 218px;
`;

const getTagStyle = ($theme: ThemeDetail) => {
  return css`
    background-color: ${$theme.canvas};
    padding: 3px 4px;
    .left,
    .right {
      width: 21px;
      border: 1px solid rgba(0, 0, 0, 0.1);
    }
    .left {
      background-color: ${$theme.primary};
      border-radius: 2px 0 0 2px;
    }
    .right {
      background-color: ${$theme.primarySurface};
      border-radius: 0 2px 2px 0;
    }
  `;
};

export const TagDesc = styled.span < { $theme: ThemeDetail | undefined } > `
  display: inline-flex;
  margin-right: 8px;
  height: 22px;
  width: 36px;
  border-radius: 2px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  font-size: 13px;
  ${(props) => props.$theme && getTagStyle(props.$theme)}
`;

export const DefaultSpan = styled.span`
  border: 1px solid #d6e4ff;
  border-radius: 8px;
  color: #4965f2;
  font-size: 12px;
  margin-left: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 16px;
  padding: 0 5px;
`;

const OPTIONS = [
  { label: trans("appSetting.450"), value: "450" },
  { label: trans("appSetting.800"), value: "800" },
  { label: trans("appSetting.1440"), value: "1440" },
  { label: trans("appSetting.1920"), value: "1920" },
  { label: trans("appSetting.3200"), value: "3200" },
  { label: trans("appSetting.autofill"), value: "Infinity" },
  { label: trans("appSetting.userDefined"), value: USER_DEFINE },
];

const SettingsStyled = styled.div`
  padding: 16px 16px 0 16px;
`;

const DivStyled = styled.div`
  margin: 8px 0;

`;

const StyledAddIcon = styled(AddIcon)`
  height: 16px;
  width: 16px;
  margin-right: 4px;
  color: ${GreyTextColor};
`;

const CreateDiv = styled.div`
  font-size: 13px;
  color: #333333;
  text-align: left;
  line-height: 13px;
  height: 32px;
  margin-right: 8px;
  display: flex;
  align-items: center;
  padding: 12px 0 12px 8px;
  border-radius: 4px;
  cursor: pointer;

  &:hover,
  &:focus {
    background-color: #f2f7fc;
    ${StyledAddIcon} path {
      fill: #315efb;
    }
  }
`;

const DividerStyled = styled(Divider)`
  margin: 6px 16px 6px 8px;
  width: auto;
  min-width: auto;
  border-color: #e1e3eb;
`;

const headerOptions = [
  {
    label: trans("appSetting.normal"),
    value: 'showHeader',
  },
  {
    label: trans("appSetting.embedding"),
    value: 'hiddenHeader',
  },
] as const;

const layoutModeOptions = [
  {
    label: trans("appSetting.gridMode"),
    value: 'grid',
  },
  {
    label: trans("appSetting.freeMode"),
    value: 'free',
  },
] as const;

const childrenMap = {
  maxWidth: dropdownInputSimpleControl(OPTIONS, USER_DEFINE, "1920"),
  hiddenHeader: dropdownControl(headerOptions, "showHeader"),
  layoutMode: dropdownControl(layoutModeOptions, "grid"),
  themeId: valueComp<string>(DEFAULT_THEMEID),
  customShortcuts: CustomShortcutsComp,
};
type ChildrenInstance = RecordConstructorToComp<typeof childrenMap> & {
  themeList: ThemeType[];
  defaultTheme: string;
};

function AppSettingsModal(props: ChildrenInstance) {
  const { themeList, defaultTheme, themeId, maxWidth, hiddenHeader, layoutMode } = props;
  const THEME_OPTIONS = themeList?.map((theme) => ({
    label: theme.name,
    value: theme.id + "",
  }));
  const themeWithDefault = (
    themeId.getView() === DEFAULT_THEMEID ||
      (!!themeId.getView() &&
        THEME_OPTIONS.findIndex((item) => item.value === themeId.getView()) === -1)
      ? DEFAULT_THEMEID
      : themeId.getView()
  ) as string;

  useEffect(() => {
    if (themeWithDefault === DEFAULT_THEMEID) {
      themeId.dispatchChangeValueAction(themeWithDefault);
    }
  }, [themeWithDefault]);

  const DropdownItem = (params: { value: string }) => {
    const themeItem = themeList.find((theme) => theme.id === params.value);
    return (
      <ItemSpan>
        <TagDesc $theme={themeItem?.theme}>
          <div className="left" />
          <div className="right" />
        </TagDesc>
        <EllipsisSpan style={{ maxWidth: "238px" }}>{themeItem?.name}</EllipsisSpan>
        {themeItem?.id === defaultTheme && <DefaultSpan>{trans("appSetting.default")}</DefaultSpan>}
      </ItemSpan>
    );
  };
  return (
    <SettingsStyled>
      <Text strong >{TitleText}</Text>
      <DivStyled>
        {maxWidth.propertyView({
          dropdownLabel: trans("appSetting.canvasMaxWidth"),
          inputLabel: trans("appSetting.userDefinedMaxWidth"),
          inputPlaceholder: trans("appSetting.inputUserDefinedPxValue"),
          placement: "right",
          min: 350,
          lastNode: <span>{trans("appSetting.maxWidthTip")}</span>,
          labelStyle: { marginBottom: "8px", color: "#8b8fa3" },
          dropdownStyle: { marginBottom: "12px" },
          inputStyle: { marginBottom: "6px", color: "#8b8fa3" }
        })}
        {hiddenHeader.propertyView({
          label: trans("appSetting.embeddingMode"),
          radioButton: true,
          tooltip: trans("appSetting.embeddingModeDes"),
          labelStyle: { margin: "5px 0", color: "#8b8fa3" },
        })}
        {layoutMode.propertyView({
          label: trans("appSetting.layoutMode"),
          radioButton: true,
          tooltip: trans("appSetting.layoutModeDes"),
          labelStyle: { margin: "5px 0", color: "#8b8fa3" },
          onChange: (value) => {
            if (value === "grid") {
              CustomModal.confirm({
                title: trans("appSetting.changeLayoutMode"),
                content: trans("appSetting.changeLayoutModePrompt"),
                onConfirm: () => {
                  layoutMode.dispatchChangeValueAction("grid");
                },
                confirmBtnType: "delete",
                okText: trans("appSetting.confirmText"),
              });
            } else {
              layoutMode.dispatchChangeValueAction("free");
            }
          }
        })}
      </DivStyled>
      <Text id={anchorItemKey.Shortcuts} strong>{trans("customShortcut.shortcut")}</Text>
      {props.customShortcuts.getPropertyView()}
      <Divider />
      <Text id={anchorItemKey.Theme} strong>{trans("settings.theme")}</Text>
      <Dropdown
        defaultValue={
          themeWithDefault === ""
            ? undefined
            : themeWithDefault === DEFAULT_THEMEID
              ? defaultTheme || undefined
              : themeWithDefault
        }
        placeholder={trans("appSetting.themeSettingDefault")}
        options={THEME_OPTIONS}
        label={trans("appSetting.themeSetting")}
        placement="right"
        labelStyle={{ margin: "12px 0", color: "#8b8fa3" }}
        dropdownStyle={{ marginBottom: "12px" }}
        itemNode={(value) => <DropdownItem value={value} />}
        preNode={() => (
          <>
            <CreateDiv onClick={() => window.open(THEME_SETTING)}>
              <StyledAddIcon />
              {trans("appSetting.themeCreate")}
            </CreateDiv>
            <DividerStyled />
          </>
        )}
        allowClear
        onChange={(value) => {
          themeId.dispatchChangeValueAction(
            value === defaultTheme ? DEFAULT_THEMEID : value || ""
          );
        }}
      />
    </SettingsStyled>
  );
}

export const AppSettingsComp = new MultiCompBuilder(childrenMap, (props) => {
  return {
    ...props,
    maxWidth: Number(props.maxWidth),
  };
})
  .setPropertyViewFn((children) => {
    const themeList = useSelector(getThemeList) || [];
    const defaultTheme = (useSelector(getDefaultTheme) || "").toString();
    return <AppSettingsModal {...children} themeList={themeList} defaultTheme={defaultTheme} />;
  })
  .build();
