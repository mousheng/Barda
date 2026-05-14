import { Dropdown, Tooltip, Space, DropdownProps, Button } from "antd";
import { getThemeDetailName, isThemeColorKey, ThemeDetail } from "api/commonSettingApi";
import { ControlItemCompBuilder } from "comps/generators/controlCompBuilder";
import { childrenToProps, ToConstructor } from "comps/generators/multi";
import { BackgroundColorContext } from "comps/utils/backgroundColorContext";
import { ThemeContext } from "comps/utils/themeContext";
import { trans } from "i18n";
import { isEmpty, get as lodashGet, filter as lodashFilter, mapValues } from "lodash";
import { controlItem, FlokcloseIcon, IconRadius, IconReset, IconResetAll, LeftNumberInput, PlusIcon, TransparentGridBackground } from "barda-design";
import React, { useContext, useState } from "react";
import styled from "styled-components";
import { useIsMobile } from "util/hooks";
import { CssUnitControl, RadiusControl } from "./codeControl";
import { ColorControl } from "./colorControl";
import {
  defaultTheme,
  DepColorConfig,
  DEP_TYPE,
  RadiusConfig,
  SimpleColorConfig,
  SingleColorConfig,
  PaddingOrMarginConfig,
} from "./styleControlConstants";

function isSimpleColorConfig(config: SingleColorConfig): config is SimpleColorConfig {
  return config.hasOwnProperty("color");
}

function isDepColorConfig(config: SingleColorConfig): config is DepColorConfig {
  return config.hasOwnProperty("depName") || config.hasOwnProperty("depTheme");
}

function isRadiusConfig(config: SingleColorConfig): config is RadiusConfig {
  return config.hasOwnProperty("radius");
}

// 判断是否是css可以带单位的输入项
function isUnitConfig(config: SingleColorConfig): config is PaddingOrMarginConfig {
  return config['name'].endsWith('_UNIT');
}

// function styleControl(colorConfig: Array<SingleColorConfig>) {
type Names<T extends readonly SingleColorConfig[]> = T[number]["name"];
export type StyleConfigType<T extends readonly SingleColorConfig[]> = { [K in Names<T>]: string };

// Options[number]["value"]
function isEmptyColor(color: string) {
  return isEmpty(color);
}


/**
 * Calculate the actual used color from the dsl color
 */
function calcColors<ColorMap extends Record<string, string>>(
  props: ColorMap,
  colorConfigs: readonly SingleColorConfig[],
  theme?: ThemeDetail,
  bgColor?: string
) {
  const themeWithDefault = (theme || defaultTheme) as unknown as Record<string, string>;
  // Cover what is not there for the first pass
  let res: Record<string, string> = {};
  colorConfigs.forEach((config) => {
    const name = config.name;
    // 检查是否有效，如无效则返回默认值
    if (isRadiusConfig(config)) {
      res[name] = props[name] === "" ? config.radius : props[name];
    }
    if (isUnitConfig(config)) {
      res[name] = props[name] === "" ? config.default : props[name];
    }
    if (!isEmptyColor(props[name])) {
      if (isThemeColorKey(props[name])) {
        res[name] = themeWithDefault[props[name]];
      } else {
        res[name] = props[name];
      }
      return;
    }
    if (isSimpleColorConfig(config)) {
      res[name] = config.color;
    }
    if (isRadiusConfig(config)) {
      res[name] = themeWithDefault[config.radius];
    }
  });
  // The second pass calculates dep
  colorConfigs.forEach((config) => {
    const name = config.name;
    if (!isEmptyColor(props[name])) {
      return;
    }
    if (isDepColorConfig(config)) {
      if (config.depType && config.depType === DEP_TYPE.CONTRAST_TEXT) {
        // bgColor is the background color of the container component, equivalent to canvas
        let depKey = config.depName ? res[config.depName] : themeWithDefault[config.depTheme!];
        if (bgColor && config.depTheme === "canvas") {
          depKey = bgColor;
        }
        res[name] = config.transformer(
          depKey,
          themeWithDefault.textDark,
          themeWithDefault.textLight
        );
      } else if (config?.depType === DEP_TYPE.SELF && config.depTheme === "canvas" && bgColor) {
        res[name] = bgColor;
      } else {
        const rest = [];
        config.depName && rest.push(res[config.depName]);
        config.depTheme && rest.push(themeWithDefault[config.depTheme]);
        res[name] = config.transformer(rest[0], rest[1]);
      }
    }
  });
  return res as ColorMap;
}

const TitleDiv = styled.div`
  display: flex;
  justify-content: flex-end;
  font-size: 13px;
  line-height: 1;

  span {
    cursor: pointer;
  }
`;

const StyleContent = styled.div`
  border: 1px solid #d7d9e0;
  border-radius: 6px;

  .cm-editor,
  .cm-editor:hover,
  .cm-editor.cm-focused {
    border: none;
    box-shadow: none;
  }

  > div {
    padding: 1px 0 1px 12px;
    border-bottom: 1px solid #d7d9e0;

    &:hover,
    &:focus {
      background: #fafafa;

      .cm-content {
        background: #fafafa;
      }
    }

    > div {
      align-items: center;
      flex-direction: row;
      gap: 0;

      > div:nth-of-type(1) {
        flex: 0 0 96px;

        div {
          line-height: 32px;
        }
      }

      > svg {
        height: 32px;
      }

      > div:nth-of-type(2) {
        flex: 1 1 auto;
      }
    }
  }

  > div:nth-of-type(1) {
    border-radius: 6px 6px 0 0;
  }

  > div:nth-last-of-type(1) {
    border: none;
    border-radius: 0 0 6px 6px;
  }
`;

const RadiusIcon = styled(IconRadius)`
  margin: 0 8px 0 -2px;
`;

const ResetIcon = styled(IconReset)`
  &:hover g g {
    width: 18px;
    height: 18px;
    stroke: #315efb;
  }
`;

const AddIcon = styled(PlusIcon)`
  &:hover g g {
    stroke: #315efb;
  }
`;

const DropdownItemDiv = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  div {
    height: 16px;
    width: 16px;
  }
   span {
    margin-right: 20px;
   }
`
const PreviewColorBackgroundDiv = styled.div`
  background-color: ${props => props.color};
  background: url(${TransparentGridBackground});
`
const PreviewColorFrontDiv = styled.div<{ color: string }>`
  border: 1px solid #cfcfcf;
  background-color: ${props => props.color};
`

const CodeEditorWithCloseButtonWrapper = styled.div < { editorName?: string } > `
    display: flex;
    flex-direction: row;
    width: 100%;
    height: 32px;
    align-items: center;
    .cm-line {
      line-height: 24px!important;
    }
    & > div {
      width: calc(100% - 20px);
    }
    & > svg {
      display: none;
      margin: 0 2px;
    }
    &:hover >svg {
        display: block;
        cursor: pointer;
    }
    & > div > div > div:nth-of-type(1) {
      max-width: 86px;
    }
`

const NoStyleText = styled.div`
  text-align: center;
  color: #8b8fa3;
  user-select: none;
`

type dropdownItemsType = {
  key: number;
  label: React.ReactNode;
  name: string;
  value?: string;
}

export function styleControl<T extends readonly SingleColorConfig[]>(colorConfigs: T) {
  type ColorMap = { [K in Names<T>]: string };
  const childrenMap: any = {};
  var showMap: Record<string, boolean> = {};
  colorConfigs.map((config) => {
    const name: Names<T> = config.name;
    if (name === "radius") {
      childrenMap[name] = RadiusControl;
    } else if (name.endsWith('_UNIT')) {
      childrenMap[name] = CssUnitControl
    }
    else {
      childrenMap[name] = ColorControl;
    }
  });
  // [K in Names<T>]: new (params: CompParams<any>) => ColorControl;
  const label = trans("prop.style");
  return new ControlItemCompBuilder(
    childrenMap as ToConstructor<{ [K in Names<T>]: ColorControl }>,
    (props) => {
      if (!Object.keys(showMap).length) showMap = mapValues(props, v => v === "" ? false : true);
      const theme = useContext(ThemeContext);
      const bgColor = useContext(BackgroundColorContext);
      return calcColors(props as ColorMap, colorConfigs, theme?.theme, bgColor);
    }
  )
    .setControlItemData({ filterText: label, searchChild: true })
    .setPropertyViewFn((children) => {
      const theme = useContext(ThemeContext);
      const bgColor = useContext(BackgroundColorContext);
      const isMobile = useIsMobile();
      const changedProps = childrenToProps(children)

      const props = calcColors(
        changedProps as ColorMap,
        colorConfigs,
        theme?.theme,
        bgColor
      );
      const DropdownItems: dropdownItemsType[] = []
      colorConfigs.map((colorConfig, index) => {
        if (!showMap[colorConfig.name] && changedProps[colorConfig.name as Names<T>] === "") {
          DropdownItems.push({
            key: index,
            label: (
              <DropdownItemDiv onClick={() => {
                const name = colorConfig.name as Names<T>
                showMap[colorConfig.name] = true;
                if (name === "radius" || name.endsWith('_UNIT')) {
                  children[name]?.dispatchChangeValueAction("");
                } else {
                  children[name] &&
                    children[name].dispatch(children[name].changeValueAction(""));
                }
              }}>
                <span>{colorConfig.label}</span>
                {
                  colorConfig.name === "radius" || colorConfig.name.endsWith('_UNIT') ?
                    <LeftNumberInput /> :
                    (<PreviewColorBackgroundDiv  >
                      <PreviewColorFrontDiv color={lodashGet(props, colorConfig.name, "")} />
                    </PreviewColorBackgroundDiv>)
                }
              </DropdownItemDiv>
            ),
            name: colorConfig.label,
          })
        }
      })
      const showReset = Object.values(changedProps).findIndex((item) => item) > -1;
      const conditionDisplay = () => {
        const items = colorConfigs
          .filter(
            (config) =>
              (!config.platform ||
                (isMobile && config.platform === "mobile") ||
                (!isMobile && config.platform === "pc")) &&
              (showMap[config.name] || changedProps[config.name as Names<T>] !== "")
          )
        if (items.length > 0) {
          return items.map((config, index) => {
            const name: Names<T> = config.name;
            let depMsg = (config as SimpleColorConfig)["color"];
            if (isDepColorConfig(config)) {
              if (config.depType === DEP_TYPE.CONTRAST_TEXT) {
                depMsg = trans("style.contrastText");
              } else if (config.depType === DEP_TYPE.SELF && config.depTheme) {
                depMsg = getThemeDetailName(config.depTheme);
              } else {
                depMsg = trans("style.generated");
              }
            }
            return (
              <CodeEditorWithCloseButtonWrapper key={index}>
                {controlItem(
                  { filterText: config.label },
                  <div key={index}>
                    {name === "radius"
                      ? (children[name] as InstanceType<typeof RadiusControl>).propertyView({
                        label: config.label,
                        preInputNode: <RadiusIcon />,
                        placeholder: props[name],
                        layout: "horizontal",
                      })
                      : (name.endsWith('_UNIT') ?
                        (children[name] as InstanceType<typeof CssUnitControl>).propertyView({
                          label: config.label,
                          preInputNode: <RadiusIcon />,
                          placeholder: props[name],
                          layout: "horizontal",
                        })
                        : children[name].propertyView({
                          label: config.label,
                          panelDefaultColor: props[name],
                          // isDep: isDepColorConfig(config),
                          isDep: true,
                          depMsg: depMsg,
                        }))
                    }
                  </div>
                )}
                {
                  changedProps[config.name as Names<T>] !== "" ? <ResetIcon onClick={() => {
                    children[name]?.dispatchChangeValueAction("");
                  }} /> : <FlokcloseIcon onClick={
                    () => {
                      showMap[name] = false;
                      if (name === "radius" || name.endsWith('_UNIT')) {
                        children[name]?.dispatchChangeValueAction("");
                      } else {
                        children[name] &&
                          children[name].dispatch(children[name].changeValueAction(""));
                      }
                    }
                  } />
                }

              </CodeEditorWithCloseButtonWrapper>
            )
          })
        } else {
          return <NoStyleText>{trans("style.noStyleAdded")}</NoStyleText>
        }

      }
      const [open, setOpen] = useState(false)
      const handleOpenChange: DropdownProps['onOpenChange'] = (nextOpen, info) => {
        if (info.source === 'trigger' || nextOpen) {
          setOpen(nextOpen);
        }
      };
      const showAddButton = lodashFilter(showMap, value => !value).length > 0
      return (
        <>
          <TitleDiv>
            <Space.Compact>
              {showReset && (
                <Tooltip placement="leftBottom" title={trans("style.resetTooltip")}>
                  <Button
                    type="text"
                    icon={<IconResetAll />}
                    onClick={() => {
                      colorConfigs.forEach((item) => {
                        const name: Names<T> = item.name;
                        if (name === "radius" || name.endsWith('_UNIT')) {
                          children[name]?.dispatchChangeValueAction("");
                        } else {
                          children[name] &&
                            children[name].dispatch(children[name].changeValueAction(""));
                        }
                      });
                    }}
                  >
                  </Button>
                </Tooltip>
              )}
              {showAddButton && <Dropdown
                menu={{ items: DropdownItems }}
                trigger={['click']}
                placement={"topRight"}
                open={open}
                onOpenChange={handleOpenChange}
              >
                <Button
                  type="text"
                  icon={<AddIcon />}
                />
              </Dropdown>}
            </Space.Compact>
          </TitleDiv>
          <StyleContent>
            {conditionDisplay()}
          </StyleContent>
        </>
      );
    })
    .build();
}

export function useStyle<T extends readonly SingleColorConfig[]>(colorConfigs: T) {
  const theme = useContext(ThemeContext);
  const bgColor = useContext(BackgroundColorContext);
  type ColorMap = { [K in Names<T>]: string };
  const props = {} as ColorMap;
  colorConfigs.forEach((config) => {
    props[config.name as Names<T>] = "";
  });
  return calcColors(props, colorConfigs, theme?.theme, bgColor);
}
