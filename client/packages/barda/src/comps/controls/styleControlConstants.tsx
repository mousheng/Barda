import { ThemeDetail } from "api/commonSettingApi";
import { darkenColor, isDarkColor, lightenColor, toHex } from "barda-design";
import { trans } from "i18n";
import { StyleConfigType } from "./styleControl";

type SupportPlatform = "pc" | "mobile";

type CommonColorConfig = {
  readonly name: string;
  readonly label: string;
  readonly platform?: SupportPlatform; // support all if undefined
};
export type SimpleColorConfig = CommonColorConfig & {
  readonly color: string;
};
export type RadiusConfig = CommonColorConfig & {
  readonly radius: string;
};
export type PaddingConfig = CommonColorConfig & {
  readonly padding: string;
};
export type PaddingOrMarginConfig = CommonColorConfig & {
  readonly default: string;
};
export type DepColorConfig = CommonColorConfig & {
  readonly depName?: string;
  readonly depTheme?: keyof ThemeDetail;
  readonly depType?: DEP_TYPE;
  transformer: (color: string, ...rest: string[]) => string;
};
export type SingleColorConfig = SimpleColorConfig | DepColorConfig | RadiusConfig | PaddingOrMarginConfig;

export const defaultTheme: ThemeDetail = {
  primary: "#3377FF",
  textDark: "#222222",
  textLight: "#FFFFFF",
  canvas: "#F5F5F6",
  primarySurface: "#FFFFFF",
  borderRadius: "4px",
};

export const SURFACE_COLOR = "#FFFFFF";
const SECOND_SURFACE_COLOR = "#D7D9E0";
const ERROR_COLOR = "#F5222D";
const SUCCESS_COLOR = "#079968";

export enum DEP_TYPE {
  CONTRAST_TEXT = "contrastText",
  SELF = "toSelf",
}

export function contrastText(color: string, textDark: string, textLight: string) {
  return isDarkColor(color) ? textLight : textDark;
}

// return similar background color
export function contrastBackground(color: string, amount: number = 0.05) {
  if (isDarkColor(color)) {
    return lightenColor(color, amount);
  } else {
    return darkenColor(color, amount);
  }
}

// return contrast color
export function contrastColor(color: string) {
  if (isDarkColor(color)) {
    return lightenColor(color, 0.2);
  } else {
    return darkenColor(color, 0.1);
  }
}

// return dependent color
function toSelf(color: string) {
  return color;
}

// Background color generates border. To be optimized
export function backgroundToBorder(color: string) {
  if (toHex(color) === SURFACE_COLOR) {
    return SECOND_SURFACE_COLOR;
  }
  return darkenColor(color, 0.03);
}

// calendar background color to boder
export function calendarBackgroundToBorder(color: string) {
  if (toHex(color) === SURFACE_COLOR) {
    return SECOND_SURFACE_COLOR;
  }
  return darkenColor(color, 0.12);
}

// return switch unchecked color
function handleToUnchecked(color: string) {
  if (toHex(color) === SURFACE_COLOR) {
    return SECOND_SURFACE_COLOR;
  }
  return contrastBackground(color);
}

// return segmented background
function handleToSegmentBackground(color: string) {
  if (toHex(color) === SURFACE_COLOR) {
    return "#E1E3EB";
  }
  return contrastBackground(color);
}

// return table hover row background color
export function handleToHoverRow(color: string) {
  if (isDarkColor(color)) {
    return "#FFFFFF23";
  } else {
    return "#00000007";
  }
}

// return table select row background color
export function handleToSelectedRow(color: string, primary: string = defaultTheme.primary) {
  if (toHex(color) === SURFACE_COLOR) {
    return `${toHex(primary)?.substring(0, 7)}16`;
  } else if (isDarkColor(color)) {
    return "#FFFFFF33";
  } else {
    return "#00000011";
  }
}

// return table header background color
export function handleToHeadBg(color: string) {
  if (toHex(color) === SURFACE_COLOR) {
    return "#FAFAFA";
  }
  if (isDarkColor(color)) {
    return darkenColor(color, 0.06);
  } else {
    return lightenColor(color, 0.015);
  }
}

// return divider text color
function handleToDividerText(color: string) {
  return darkenColor(color, 0.4);
}

// return calendar select background color
function handleCalendarSelectColor(color: string) {
  return lightenColor(color, 0.3) + "4C";
}

// return lighten color
function handlelightenColor(color: string) {
  return lightenColor(color, 0.1);
}

// return calendar head button select background
export function handleToCalendarHeadSelectBg(color: string) {
  if (toHex(color) === SURFACE_COLOR) {
    return "#E1E3EB";
  }
  return contrastBackground(color, 0.15);
}

// return calendar today background
export function handleToCalendarToday(color: string) {
  if (isDarkColor(color)) {
    return "#FFFFFF33";
  } else {
    return "#0000000c";
  }
}

// return calendar text
function handleCalendarText(color: string, textDark: string, textLight: string) {
  return isDarkColor(color) ? textLight : lightenColor(textDark, 0.1);
}

const TEXT = {
  name: "text",
  label: trans("text"),
  depName: "background",
  depType: DEP_TYPE.CONTRAST_TEXT,
  transformer: contrastText,
} as const;

const STATIC_TEXT = {
  name: "staticText",
  label: trans("style.staticText"),
  depTheme: "canvas",
  depType: DEP_TYPE.CONTRAST_TEXT,
  transformer: contrastText,
} as const;

const LABEL = {
  name: "label",
  label: trans("label"),
  depTheme: "canvas",
  depType: DEP_TYPE.CONTRAST_TEXT,
  transformer: contrastText,
} as const;

const ACCENT = {
  name: "accent",
  label: trans("style.accent"),
  depTheme: "primary",
  depType: DEP_TYPE.SELF,
  transformer: toSelf,
  platform: "pc",
} as const;

const VALIDATE = {
  name: "validate",
  label: trans("style.validate"),
  color: ERROR_COLOR,
} as const;

const ACCENT_VALIDATE = [ACCENT, VALIDATE] as const;

const BORDER = {
  name: "border",
  label: trans("style.border"),
  depName: "background",
  transformer: backgroundToBorder,
} as const;

const getRadius = (label: string = trans("style.borderRadius")) => ({
  name: "radius",
  label,
  radius: "borderRadius",
}) as const;

const getMargin = (defaultVal: string = "", label: string = trans("style.compMargin")) => ({
  name: "margin_UNIT",
  label,
  default: defaultVal,
}) as const;

const getPadding = (defaultVal: string = "4px", label: string = trans("style.compPadding")) => ({
  name: "padding_UNIT",
  label,
  default: defaultVal,
}) as const;

const getStaticBorder = (color: string = SECOND_SURFACE_COLOR, label: string = trans("style.border")) =>
({
  name: "border",
  label,
  color,
} as const);

const HEADER_BACKGROUND = {
  name: "headerBackground",
  label: trans("style.headerBackground"),
  depName: "background",
  depType: DEP_TYPE.SELF,
  transformer: toSelf,
} as const;

const BG_STATIC_BORDER_RADIUS = [getBackground(), getStaticBorder(), getRadius()] as const;

const FILL = {
  name: "fill",
  label: trans("style.fill"),
  depTheme: "primary",
  depType: DEP_TYPE.SELF,
  transformer: toSelf,
} as const;

const TRACK = {
  name: "track",
  label: trans("style.track"),
  color: SECOND_SURFACE_COLOR,
} as const;

const SUCCESS = {
  name: "success",
  label: trans("success"),
  color: SUCCESS_COLOR,
} as const;

function getStaticBgBorderRadiusByBg(background: string, platform?: SupportPlatform) {
  return [
    getStaticBackground(background),
    platform ? { ...BORDER, platform } : BORDER,
    platform ? { ...getRadius(), platform } : getRadius(),
  ] as const;
}

function getBgBorderRadiusByBg(background: keyof ThemeDetail = "primarySurface") {
  return [getBackground(background), BORDER, getRadius()] as const;
}

function getBackground(depTheme: keyof ThemeDetail = "primarySurface", label: string = trans("style.background")) {
  return {
    name: "background",
    label,
    depTheme: depTheme,
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  } as const;
}

function getStaticBackground(color: string) {
  return {
    name: "background",
    label: trans("style.background"),
    color,
  } as const;
}

export const ButtonStyle = [...getBgBorderRadiusByBg("primary"), TEXT] as const;

export const dropdownStyle = [getBackground("primary"), getStaticBorder("#4096ff"), getRadius(), TEXT] as const;

export const ToggleButtonStyle = [
  getBackground("canvas"),
  TEXT,
  {
    name: "border",
    label: trans("style.border"),
    depName: "text",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
  getRadius(),
] as const;

export const TextStyle = [
  {
    name: "background",
    label: trans("style.background"),
    depTheme: "canvas",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
  TEXT,
  {
    name: "links",
    label: trans("style.links"),
    depTheme: "primary",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
] as const;

export const ContainerStyle = [
  ...BG_STATIC_BORDER_RADIUS,
  HEADER_BACKGROUND,
  {
    name: "footerBackground",
    label: trans("style.footerBackground"),
    depName: "background",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
  getMargin(),
  {
    name: "headPadding_UNIT",
    label: trans("style.headPadding"),
    default: '3 19',
  },
  {
    name: "bodyPadding_UNIT",
    label: trans("style.bodyPadding"),
    default: '11 19',
  },
  {
    name: "footerPadding_UNIT",
    label: trans("style.footerPadding"),
    default: '3 19',
  }
] as const;

export const SliderStyle = [
  LABEL,
  FILL,
  {
    name: "thumbBoder",
    label: trans("style.thumbBorder"),
    depName: "fill",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
  {
    name: "thumb",
    label: trans("style.thumb"),
    color: SURFACE_COLOR,
  },
  TRACK,
] as const;

export const InputLikeStyle = [
  LABEL,
  ...getStaticBgBorderRadiusByBg(SURFACE_COLOR),
  TEXT,
  ...ACCENT_VALIDATE,
] as const;

export const RatingStyle = [
  LABEL,
  {
    name: "checked",
    label: trans("style.checked"),
    color: "#FFD400",
  },
  {
    name: "unchecked",
    label: trans("style.unchecked"),
    color: SECOND_SURFACE_COLOR,
  },
] as const;

export const SwitchStyle = [
  LABEL,
  {
    name: "handle",
    label: trans("style.handle"),
    color: SURFACE_COLOR,
  },
  {
    name: "unchecked",
    label: trans("style.unchecked"),
    depName: "handle",
    transformer: handleToUnchecked,
  },
  {
    name: "checked",
    label: trans("style.checked"),
    depTheme: "primary",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
] as const;

export const SelectStyle = [
  LABEL,
  ...getStaticBgBorderRadiusByBg(SURFACE_COLOR, "pc"),
  TEXT,
  ...ACCENT_VALIDATE,
] as const;

const multiSelectCommon = [
  LABEL,
  ...getStaticBgBorderRadiusByBg(SURFACE_COLOR, "pc"),
  TEXT,
  {
    name: "tags",
    label: trans("style.tags"),
    color: "#F5F5F6",
    platform: "pc",
  },
  {
    name: "tagsText",
    label: trans("style.tagsText"),
    depName: "tags",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
    platform: "pc",
  },
] as const;

export const MultiSelectStyle = [
  ...multiSelectCommon,
  {
    name: "multiIcon",
    label: trans("style.multiIcon"),
    depTheme: "primary",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
    platform: "pc",
  },
  ...ACCENT_VALIDATE,
] as const;

export const TabContainerStyle = [
  ...BG_STATIC_BORDER_RADIUS,
  HEADER_BACKGROUND,
  {
    name: "tabText",
    label: trans("style.tabText"),
    depName: "headerBackground",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
  {
    name: "accent",
    label: trans("style.tabAccent"),
    depTheme: "primary",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
] as const;

export const ModalStyle = getBgBorderRadiusByBg();

export const CascaderStyle = [
  LABEL,
  ...getStaticBgBorderRadiusByBg(SURFACE_COLOR, "pc"),
  TEXT,
  ACCENT,
] as const;

function checkAndUncheck() {
  return [
    {
      name: "checkedBackground",
      label: trans("style.checkedBackground"),
      depTheme: "primary",
      depType: DEP_TYPE.SELF,
      transformer: toSelf,
    },
    {
      name: "uncheckedBackground",
      label: trans("style.uncheckedBackground"),
      color: SURFACE_COLOR,
    },
    {
      name: "uncheckedBorder",
      label: trans("style.uncheckedBorder"),
      depName: "uncheckedBackground",
      transformer: backgroundToBorder,
    },
  ] as const;
}

export const CheckboxStyle = [
  LABEL,
  ...checkAndUncheck(),
  {
    name: "checked",
    label: trans("style.checked"),
    depName: "checkedBackground",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
  getRadius(),
  STATIC_TEXT,
  VALIDATE,
] as const;

export const RadioStyle = [
  LABEL,
  ...checkAndUncheck(),
  {
    name: "checked",
    label: trans("style.checked"),
    depName: "uncheckedBackground",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
  STATIC_TEXT,
  VALIDATE,
] as const;

export const SegmentStyle = [
  LABEL,
  {
    name: "indicatorBackground",
    label: trans("style.indicatorBackground"),
    color: SURFACE_COLOR,
  },
  {
    name: "background",
    label: trans("style.background"),
    depName: "indicatorBackground",
    transformer: handleToSegmentBackground,
  },
  {
    name: "text",
    label: trans("text"),
    depName: "indicatorBackground",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
  getRadius(),
  VALIDATE,
] as const;

export const TableStyle = [
  ...BG_STATIC_BORDER_RADIUS,
  {
    name: "cellText",
    label: trans("style.tableCellText"),
    depName: "background",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
  {
    name: "selectedRowBackground",
    label: trans("style.selectedRowBackground"),
    depName: "background",
    depTheme: "primary",
    transformer: handleToSelectedRow,
  },
  {
    name: "hoverRowBackground",
    label: trans("style.hoverRowBackground"),
    depName: "background",
    transformer: handleToHoverRow,
  },
  {
    name: "alternateBackground",
    label: trans("style.alternateRowBackground"),
    depName: "background",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
  {
    name: "headerBackground",
    label: trans("style.tableHeaderBackground"),
    depName: "background",
    transformer: handleToHeadBg,
  },
  {
    name: "headerText",
    label: trans("style.tableHeaderText"),
    depName: "headerBackground",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
  {
    name: "toolbarBackground",
    label: trans("style.toolbarBackground"),
    depName: "background",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
  {
    name: "toolbarText",
    label: trans("style.toolbarText"),
    depName: "toolbarBackground",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
] as const;

export const FileStyle = [...getStaticBgBorderRadiusByBg(SURFACE_COLOR), TEXT, ACCENT] as const;

export const FileViewerStyle = [
  getStaticBackground("#FFFFFF"),
  getStaticBorder("#00000000"),
  getRadius(),
] as const;

export const IframeStyle = [getBackground(), getStaticBorder("#00000000"), getRadius()] as const;

export const DateTimeStyle = [
  LABEL,
  ...getStaticBgBorderRadiusByBg(SURFACE_COLOR),
  TEXT,
  ...ACCENT_VALIDATE,
] as const;

export const LinkStyle = [
  {
    name: "text",
    label: trans("text"),
    depTheme: "primary",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
] as const;

export const DividerStyle = [
  {
    name: "color",
    label: trans("color"),
    color: lightenColor(SECOND_SURFACE_COLOR, 0.05),
  },
  {
    name: "text",
    label: trans("text"),
    depName: "color",
    transformer: handleToDividerText,
  },
] as const;

export const ProgressStyle = [
  {
    name: "text",
    label: trans("text"),
    depTheme: "canvas",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
  TRACK,
  FILL,
  SUCCESS,
] as const;

export const NavigationStyle = [
  {
    name: "text",
    label: trans("text"),
    depName: "background",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
  ACCENT,
  getStaticBackground("#FFFFFF00"),
  getStaticBorder("#FFFFFF00"),
] as const;

export const ImageStyle = [getStaticBorder("#00000000"), getRadius(), getMargin()] as const;

export const ListViewStyle = [
  getBackground(undefined, trans("style.compBackground")),
  getStaticBorder(undefined, trans("style.compBorder")),
  getRadius(trans("style.compRadius")),
  getPadding("10px"),
];

export const JsonSchemaFormStyle = BG_STATIC_BORDER_RADIUS;

export const QRCodeStyle = [
  getBackground(),
  {
    name: "color",
    label: trans("color"),
    color: "#000000",
  },
] as const;

export const TimeLineStyle = [
  getBackground(),
  {
    name: "titleColor",
    label: trans("timeLine.titleColor"),
    color: "#000000",
  },
  {
    name: "lableColor",
    label: trans("timeLine.lableColor"),
    color: "#000000",
  },
  {
    name: "subTitleColor",
    label: trans("timeLine.subTitleColor"),
    color: "#848484",
  },
  getRadius(),
  getMargin(),
  getPadding(),
] as const;

export const CommentStyle = [
  {
    name: "background",
    label: trans("style.background"),
    depTheme: "canvas",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
  {
    name: "commentHoverBackground",
    label: trans("comment.commentHoverBackground"),
    depTheme: "textLight",
    depType: DEP_TYPE.SELF,
    transformer: toSelf,
  },
  getStaticBorder(),
  getRadius(),
  getMargin(),
  getPadding(),
] as const

export const TreeStyle = [
  LABEL,
  ...getStaticBgBorderRadiusByBg(SURFACE_COLOR),
  TEXT,
  VALIDATE,
] as const;

export const TreeSelectStyle = [...multiSelectCommon, ...ACCENT_VALIDATE] as const;

export const DrawerStyle = [getBackground()] as const;

export const JsonEditorStyle = [LABEL] as const;

export const JsonExplorerStyle = [getStaticBorder("#d7d9e0"), getRadius(), getBackground()] as const;

export const CalendarStyle = [
  getBackground("primarySurface"),
  {
    name: "border",
    label: trans("style.border"),
    depName: "background",
    transformer: calendarBackgroundToBorder,
  },
  getRadius(),
  {
    name: "text",
    label: trans("text"),
    depName: "background",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: handleCalendarText,
  },
  {
    name: "headerBtnBackground",
    label: trans("calendar.headerBtnBackground"),
    depName: "background",
    transformer: handlelightenColor,
  },
  {
    name: "btnText",
    label: trans("calendar.btnText"),
    depName: "headerBtnBackground",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
  {
    name: "title",
    label: trans("calendar.title"),
    depName: "background",
    depType: DEP_TYPE.CONTRAST_TEXT,
    transformer: contrastText,
  },
  {
    name: "selectBackground",
    label: trans("calendar.selectBackground"),
    depTheme: "primary",
    transformer: handleCalendarSelectColor,
  },
] as const;

export const SignatureStyle = [
  LABEL,
  ...getBgBorderRadiusByBg(),
  {
    name: "pen",
    label: trans("style.pen"),
    color: "#000000",
  },
  {
    name: "tips",
    label: trans("style.tips"),
    color: "#B8B9BF",
  },
  {
    name: "footerIcon",
    label: trans("style.footerIcon"),
    color: "#222222",
  },
] as const;

export const CarouselStyle = [getBackground("canvas")] as const;

export const RichTextEditorStyle = [getStaticBorder(), getRadius()] as const;

export type InputLikeStyleType = StyleConfigType<typeof InputLikeStyle>;
export type ButtonStyleType = StyleConfigType<typeof ButtonStyle>;
export type ToggleButtonStyleType = StyleConfigType<typeof ToggleButtonStyle>;
export type TextStyleType = StyleConfigType<typeof TextStyle>;
export type ContainerStyleType = StyleConfigType<typeof ContainerStyle>;
export type SliderStyleType = StyleConfigType<typeof SliderStyle>;
export type RatingStyleType = StyleConfigType<typeof RatingStyle>;
export type SwitchStyleType = StyleConfigType<typeof SwitchStyle>;
export type SelectStyleType = StyleConfigType<typeof SelectStyle>;
export type MultiSelectStyleType = StyleConfigType<typeof MultiSelectStyle>;
export type TabContainerStyleType = StyleConfigType<typeof TabContainerStyle>;
export type ModalStyleType = StyleConfigType<typeof ModalStyle>;
export type CascaderStyleType = StyleConfigType<typeof CascaderStyle>;
export type CheckboxStyleType = StyleConfigType<typeof CheckboxStyle>;
export type RadioStyleType = StyleConfigType<typeof RadioStyle>;
export type SegmentStyleType = StyleConfigType<typeof SegmentStyle>;
export type TableStyleType = StyleConfigType<typeof TableStyle>;
export type FileStyleType = StyleConfigType<typeof FileStyle>;
export type FileViewerStyleType = StyleConfigType<typeof FileViewerStyle>;
export type IframeStyleType = StyleConfigType<typeof IframeStyle>;
export type DateTimeStyleType = StyleConfigType<typeof DateTimeStyle>;
export type LinkStyleType = StyleConfigType<typeof LinkStyle>;
export type DividerStyleType = StyleConfigType<typeof DividerStyle>;
export type ProgressStyleType = StyleConfigType<typeof ProgressStyle>;
export type NavigationStyleType = StyleConfigType<typeof NavigationStyle>;
export type ImageStyleType = StyleConfigType<typeof ImageStyle>;
export type ListViewStyleType = StyleConfigType<typeof ListViewStyle>;
export type JsonSchemaFormStyleType = StyleConfigType<typeof JsonSchemaFormStyle>;
export type TreeSelectStyleType = StyleConfigType<typeof TreeSelectStyle>;
export type DrawerStyleType = StyleConfigType<typeof DrawerStyle>;
export type JsonEditorStyleType = StyleConfigType<typeof JsonEditorStyle>;
export type CalendarStyleType = StyleConfigType<typeof CalendarStyle>;
export type SignatureStyleType = StyleConfigType<typeof SignatureStyle>;
export type CarouselStyleType = StyleConfigType<typeof CarouselStyle>;
export type RichTextEditorStyleType = StyleConfigType<typeof RichTextEditorStyle>;
export type StandardBoxMargin = [number, number, number, number];
export type TimeLineStyleType = StyleConfigType<typeof TimeLineStyle>;
export type CommentStyleType = StyleConfigType<typeof CommentStyle>;

/**
 * 将给定的文本解析成盒子模型标准四个值的数组
 * @param cssValues - CSS值
 * @param defaultValue - 默认值
 * @param withUnit - 是否返回带单位的值
 * @returns 返回一个包含四个值的数组，分别表示上、右、下、左的值
 */
export const parseBoxValues = (
  cssValues: string,
  defaultValue: StandardBoxMargin = [0, 0, 0, 0],
  withUnit: boolean = false,
) => {
  if (typeof cssValues !== "string" || cssValues === "") return withUnit ? defaultValue.map(v => `${v}px`) : defaultValue;
  const parts = cssValues.trim().split(/\s+/);
  // 提取数值和单位
  const extractValue = (str: string) => {
    const value = parseFloat(str.replace(/[^\d.-]/g, "")) || 0;
    const unitMatch = str.match(/[a-z%]+$/i);
    const unit = unitMatch ? unitMatch[0] : "px"; // 默认单位为 px
    return withUnit ? `${value}${unit}` : value;
  };
  const values = parts.map(extractValue);
  // 补全为 4 项，遵循 CSS 展开规则
  switch (values.length) {
    case 1:
      return [values[0], values[0], values[0], values[0]];
    case 2:
      return [values[0], values[1], values[0], values[1]];
    case 3:
      return [values[0], values[1], values[2], values[1]];
    case 4:
      return values;
    default:
      return withUnit ? defaultValue.map(v => `${v}px`) : defaultValue;
  }
};

/**
 * 返回标准盒子模型四个值的文本
 * @param value - 输入值，如 "3px"
 * @returns 返回标准盒子模型四个值的文本，如 "3px 3px 3px 3px"
 */
export const formatBoxValuesWithUnit = (value: string, defaultValue: StandardBoxMargin = [0, 0, 0, 0]): string => {
  return parseBoxValues(value, defaultValue, true).join(' ');
};

/**
 * 根据margin计算剩余高度
 * @param margin - margin值文本
 * @param defaultValue - 默认margin值
 * @returns CSS calc表达式，如 "calc(100% - 20px)"
 */
export const calculateRemainingHeight = (margin: string, defaultValue: StandardBoxMargin = [0, 0, 0, 0]): string => {
  const marginValues = parseBoxValues(margin, defaultValue, true);
  const topMargin = marginValues[0];
  const bottomMargin = marginValues[2];
  return `calc(100% - ${topMargin} - ${bottomMargin})`;
};

/**
 * 根据margin计算剩余宽度
 * @param margin - margin值文本
 * @param defaultValue - 默认margin值
 * @returns CSS calc表达式，如 "calc(100% - 20px)"
 */
export const calculateRemainingWidth = (margin: string, defaultValue: StandardBoxMargin = [0, 0, 0, 0]): string => {
  const marginValues = parseBoxValues(margin, defaultValue, true);
  const leftMargin = marginValues[3];
  const rightMargin = marginValues[1];
  return `calc(100% - ${leftMargin} - ${rightMargin})`;
};
