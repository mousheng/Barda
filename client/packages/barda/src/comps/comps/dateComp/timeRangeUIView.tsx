import styled from "styled-components";
import { TimePicker } from "antd";
import { DateTimeStyleType } from "../../controls/styleControlConstants";
import { getStyle } from "comps/comps/dateComp/dateCompUtil";
import { useUIView } from "../../utils/useUIView";
import { checkIsMobile } from "util/commonUtils";
import React from "react";
import type { TimeCompViewProps } from "./timeComp";
import { useEditorStore } from "comps/editorStore";
import dayjs from "dayjs";
import { hasIcon } from "comps/utils";
import { omit } from "lodash";

const RangePickerStyled = styled(TimePicker.RangePicker) <{ $style: DateTimeStyleType }>`
  width: 100%;
  ${(props) => props.$style && getStyle(props.$style)}
`;

const TimeRangeMobileUIView = React.lazy(() =>
  import("./timeMobileUIView").then((m) => ({ default: m.TimeRangeMobileUIView }))
);

export interface TimeRangeUIViewProps extends TimeCompViewProps {
  start: dayjs.Dayjs | null;
  end: dayjs.Dayjs | null;
  onChange: (start?: dayjs.Dayjs | null, end?: dayjs.Dayjs | null) => void;
}

export const TimeRangeUIView = (props: TimeRangeUIViewProps) => {
  const maxWidth = useEditorStore((s) => s.rootComp?.children.settings.getView().maxWidth);

  return useUIView(
    <TimeRangeMobileUIView {...props} />,
    <RangePickerStyled
      {...omit(props, "onChange")}
      value={[props.start, props.end]}
      order={true}
      hideDisabledOptions
      onCalendarChange={(time: any) => {
        props.onChange(time?.[0], time?.[1]);
      }}
      inputReadOnly={checkIsMobile(maxWidth)}
      suffixIcon={hasIcon(props.suffixIcon) && props.suffixIcon}
    />
  );
};
