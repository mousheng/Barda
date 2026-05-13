import dayjs from "dayjs";
import type { DateCompViewProps } from "./dateComp";
import { disabledDate, getStyle } from "comps/comps/dateComp/dateCompUtil";
import { useUIView } from "../../utils/useUIView";
import { checkIsMobile } from "util/commonUtils";
import React from "react";
import styled from "styled-components";
import type { DateTimeStyleType } from "../../controls/styleControlConstants";
import { useEditorStore } from "comps/editorStore";
import { DatePicker } from "antd";
import { hasIcon } from "comps/utils";
import { omit } from "lodash";

const RangePickerStyled = styled(DatePicker.RangePicker) <{ $style: DateTimeStyleType }>`
  width: 100%;
  ${(props) => props.$style && getStyle(props.$style)}
`;

const DateRangeMobileUIView = React.lazy(() =>
  import("./dateMobileUIView").then((m) => ({ default: m.DateRangeMobileUIView }))
);

export interface DateRangeUIViewProps extends DateCompViewProps {
  start: dayjs.Dayjs | null;
  end: dayjs.Dayjs | null;
  onChange: (start?: dayjs.Dayjs | null, end?: dayjs.Dayjs | null) => void;
  onPanelChange: (value: any, mode: [string, string]) => void;
}

export const DateRangeUIView = (props: DateRangeUIViewProps) => {
  const maxWidth = useEditorStore((s) => s.rootComp?.children.settings.getView().maxWidth);

  return useUIView(
    <DateRangeMobileUIView {...props} />,
    <RangePickerStyled
      {...omit(props, "onChange")}
      ref={props.viewRef as any}
      value={[props.start, props.end]}
      disabledDate={(current: dayjs.Dayjs) => disabledDate(current, props.minDate, props.maxDate)}
      onCalendarChange={(time: any) => {
        props.onChange(time?.[0], time?.[1]);
      }}
      inputReadOnly={checkIsMobile(maxWidth)}
      suffixIcon={hasIcon(props.suffixIcon) && props.suffixIcon}
    />
  );
};
