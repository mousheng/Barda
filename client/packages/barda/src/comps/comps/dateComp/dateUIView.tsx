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

const DatePickerStyled = styled(DatePicker) <{ $style: DateTimeStyleType }>`
  width: 100%;
  ${(props) => props.$style && getStyle(props.$style)}
`;

export interface DataUIViewProps extends DateCompViewProps {
  value: dayjs.Dayjs | null;
  onChange: (date: unknown, dateString: string | string[] | null) => void;
  onPanelChange: () => void;
}

const DateMobileUIView = React.lazy(() =>
  import("./dateMobileUIView").then((m) => ({ default: m.DateMobileUIView }))
);
export const DateUIView = (props: DataUIViewProps) => {
  const maxWidth = useEditorStore((s) => s.rootComp?.children.settings.getView().maxWidth);
  return useUIView(
    <DateMobileUIView {...props} />,
    <DatePickerStyled
      {...props}
      ref={props.viewRef as any}
      disabledDate={(current: dayjs.Dayjs) => disabledDate(current, props.minDate, props.maxDate)}
      picker={"date"}
      inputReadOnly={checkIsMobile(maxWidth)}
    />
  );
};
