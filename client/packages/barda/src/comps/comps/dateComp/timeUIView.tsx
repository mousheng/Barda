import styled from "styled-components";
import { TimePicker } from "antd";
import { DateTimeStyleType } from "../../controls/styleControlConstants";
import { getStyle } from "comps/comps/dateComp/dateCompUtil";
import { useUIView } from "../../utils/useUIView";
import { checkIsMobile } from "util/commonUtils";
import React from "react";
import type { TimeCompViewProps } from "./timeComp";
import { useEditorStore } from "comps/editorStore";
import dayjs from "dayjs"

const TimePickerStyled = styled(TimePicker) <{ $style: DateTimeStyleType }>`
  width: 100%;
  ${(props) => props.$style && getStyle(props.$style)}
`;

const TimeMobileUIView = React.lazy(() =>
  import("./timeMobileUIView").then((m) => ({ default: m.TimeMobileUIView }))
);

export interface TimeUIViewProps extends TimeCompViewProps {
  value: dayjs.Dayjs | null;
  onChange: (value: dayjs.Dayjs | null) => void;
}

export const TimeUIView = (props: TimeUIViewProps) => {
  const maxWidth = useEditorStore((s) => s.rootComp?.children.settings.getView().maxWidth);

  return useUIView(
    <TimeMobileUIView {...props} />,
    <TimePickerStyled
      {...props}
      ref={props.viewRef as any}
      hideDisabledOptions
      inputReadOnly={checkIsMobile(maxWidth)}
    />
  );
};
