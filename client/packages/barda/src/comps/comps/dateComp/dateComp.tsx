import _, { noop } from "lodash";
import dayjs from "dayjs";
import { RecordConstructorToComp, RecordConstructorToView } from "barda-core";
import {
  BoolCodeControl,
  CustomRuleControl,
  RangeControl,
  StringControl,
} from "../../controls/codeControl";
import { BoolControl } from "../../controls/boolControl";
import {
  blurEvent,
  changeEvent,
  eventHandlerControl,
  focusEvent,
} from "../../controls/eventHandlerControl";
import { LabelControl } from "../../controls/labelControl";
import { stringExposingStateControl } from "../../controls/codeStateControl";
import { UICompBuilder, withDefault } from "../../generators";
import { CommonNameConfig, depsConfig, withExposingConfigs } from "../../generators/withExposing";
import { formDataChildren, FormDataPropertyView } from "../formComp/formDataConstants";
import { styleControl } from "comps/controls/styleControl";
import { DateTimeStyle, DateTimeStyleType } from "comps/controls/styleControlConstants";
import { withMethodExposing } from "../../generators/withMethodExposing";
import {
  disabledPropertyView,
  formatPropertyView,
  hiddenPropertyView,
  hourStepPropertyView,
  maxDatePropertyView,
  maxTimePropertyView,
  minDatePropertyView,
  minTimePropertyView,
  minuteStepPropertyView,
  requiredPropertyView,
  SecondStepPropertyView,
} from "comps/utils/propertyUtils";
import { trans } from "i18n";
import { DATE_FORMAT, DATE_TIME_FORMAT, DateParser, PickerMode, fromStringSafeGetDayjs } from "util/dateTimeUtils";
import { ReactNode, useState } from "react";
import { IconControl } from "comps/controls/iconControl";
import { hasIcon } from "comps/utils";
import { Section, sectionNames } from "components/Section";
import { dateRefMethods, disabledTime, handleDateChange } from "comps/comps/dateComp/dateCompUtil";
import { DateUIView } from "./dateUIView";
import { useIsMobile, useSkipInitialEffect } from "util/hooks";
import { RefControl } from "comps/controls/refControl";
import { DateRangeUIView } from "comps/comps/dateComp/dateRangeUIView";
import { TimePickerProps } from "antd";

const EventOptions = [changeEvent, focusEvent, blurEvent] as const;

export type hourStepType = TimePickerProps['hourStep'];
export type minuteStepType = TimePickerProps['minuteStep'];
export type secondStepType = TimePickerProps['secondStep'];

const validationChildren = {
  required: BoolControl,
  minDate: StringControl,
  maxDate: StringControl,
  minTime: StringControl,
  maxTime: StringControl,
  customRule: CustomRuleControl,
};
const commonChildren = {
  label: LabelControl,
  format: StringControl,
  disabled: BoolCodeControl,
  onEvent: eventHandlerControl(EventOptions),
  showTime: BoolControl,
  use12Hours: BoolControl,
  hourStep: RangeControl.closed(1, 24, 1),
  minuteStep: RangeControl.closed(1, 60, 1),
  secondStep: RangeControl.closed(1, 60, 1),
  style: styleControl(DateTimeStyle),
  suffixIcon: withDefault(IconControl, "/icon:regular/calendar"),
  ...validationChildren,
  viewRef: RefControl<HTMLElement>,
};
type CommonChildrenType = RecordConstructorToComp<typeof commonChildren>;

const datePickerProps = (props: RecordConstructorToView<typeof commonChildren>) =>
  _.pick(props, "format", "showTime", "use12Hours", "hourStep", "minuteStep", "secondStep");

const timeFields = (children: CommonChildrenType, isMobile?: boolean) => [
  children.showTime.propertyView({ label: trans("date.showTime") }),
  !isMobile && children.use12Hours.propertyView({ label: trans("prop.use12Hours") }),
];
const commonAdvanceSection = (children: CommonChildrenType, isDate: boolean = true) => {
  if (isDate && children.showTime.getView()) {
    return (
      <Section name={sectionNames.advanced}>
        {hourStepPropertyView(children)}
        {minuteStepPropertyView(children)}
        {SecondStepPropertyView(children)}
      </Section>
    );
  }
};

const dateValidationFields = (children: CommonChildrenType, dateType: PickerMode = "date") => {
  if (dateType === "date") {
    return [minDatePropertyView(children), maxDatePropertyView(children)];
  }
};

const timeValidationFields = (children: CommonChildrenType, dateType: PickerMode = "date") => {
  if (dateType === "date" && children.showTime.getView()) {
    return [minTimePropertyView(children), maxTimePropertyView(children)];
  }
};

export function validate(
  props: RecordConstructorToView<typeof validationChildren> & {
    value: { start: string, end?: string, range?: boolean, showTime?: boolean };
  }
): {
  validateStatus: "success" | "warning" | "error";
  help?: string;
} {
  if (props.customRule) {
    return { validateStatus: "error", help: props.customRule };
  }

  const startDateTime = dayjs(props.value.start, DATE_TIME_FORMAT);
  const endDateTime = props.value.range ? dayjs(props.value.end, DATE_TIME_FORMAT) : null;

  if (!startDateTime.isValid() || (props.value.range && endDateTime && !endDateTime.isValid())) {
    return { validateStatus: "error", help: props.required ? trans("prop.required") : undefined };
  }

  const minDateValid = props.minDate && dayjs(props.minDate).isValid();
  const maxDateValid = props.maxDate && dayjs(props.maxDate).isValid();

  if (minDateValid && dayjs(props.minDate).isAfter(startDateTime)) {
    return { validateStatus: "error", help: trans("prop.minDateTip") };
  }

  if (maxDateValid && props.value.range && endDateTime && dayjs(props.maxDate).isBefore(endDateTime)) {
    return { validateStatus: "error", help: trans("prop.maxDateTip") };
  }
  if (props.value.showTime) {
    const baseMinTime = dayjs(`1970-01-01 ${props.minTime}`, DATE_TIME_FORMAT, true);
    const baseMaxTime = dayjs(`1970-01-01 ${props.maxTime}`, DATE_TIME_FORMAT, true);
    const baseCurrentStartTime = dayjs(`1970-01-01 ${startDateTime.format('HH:mm:ss')}`, DATE_TIME_FORMAT, true);
    const baseCurrentEndTime = endDateTime ? dayjs(`1970-01-01 ${endDateTime.format('HH:mm:ss')}`, DATE_TIME_FORMAT, true) : null;

    if (
      (baseMinTime.isAfter(baseCurrentStartTime) ||
        (props.value.range && baseCurrentEndTime && baseMinTime.isAfter(baseCurrentEndTime)))
    ) {
      return { validateStatus: "error", help: trans("prop.minTimeTip") };
    }

    if (
      (baseMaxTime.isBefore(baseCurrentStartTime) ||
        (props.value.range && baseCurrentEndTime && baseMaxTime.isBefore(baseCurrentEndTime)))
    ) {
      return { validateStatus: "error", help: trans("prop.maxTimeTip") };
    }
  }
  return { validateStatus: "success" };
}

const childrenMap = {
  value: stringExposingStateControl("value"),
  ...commonChildren,
  ...formDataChildren,
};
export type DateCompViewProps = Pick<
  RecordConstructorToView<typeof childrenMap>,
  | "disabled"
  | "format"
  | "suffixIcon"
  | "showTime"
  | "use12Hours"
  | "viewRef"
> & {
  onFocus: () => void;
  onBlur: () => void;
  $style: DateTimeStyleType;
  disabledTime: () => ReturnType<typeof disabledTime>;
  suffixIcon: ReactNode;
  hourStep: hourStepType;
  minuteStep: minuteStepType;
  secondStep: secondStepType;
  minDate: dayjs.Dayjs | undefined;
  maxDate: dayjs.Dayjs | undefined;
};

export const datePickerControl = new UICompBuilder(childrenMap, (props) => {
  const time = fromStringSafeGetDayjs(props.value.value, DateParser);
  const [validateState, setValidateState] = useState({});

  useSkipInitialEffect(() => {
    setValidateState(validate({ ...props, value: { start: props.value.value, showTime: props.showTime } }));
  }, [props.value.value])

  return props.label({
    required: props.required,
    style: props.style,
    children: (
      <DateUIView
        viewRef={props.viewRef}
        disabledTime={() => disabledTime(props.minTime, props.maxTime)}
        $style={props.style}
        disabled={props.disabled}
        {...datePickerProps(props)}
        minDate={dayjs(props.minDate).isValid() ? dayjs(props.minDate) : dayjs("1970-01-01")}
        maxDate={dayjs(props.maxDate).isValid() ? dayjs(props.maxDate) : dayjs("2200-12-29")}
        hourStep={props.hourStep as hourStepType}
        minuteStep={props.minuteStep as minuteStepType}
        secondStep={props.secondStep as secondStepType}
        value={time.isValid() ? time : null}
        onChange={(time) => {
          handleDateChange(
            time && (time as dayjs.Dayjs).isValid()
              ? (time as dayjs.Dayjs).format(props.showTime ? DATE_TIME_FORMAT : DATE_FORMAT)
              : "",
            props.value.onChange,
            props.onEvent
          );
        }}
        onPanelChange={() => {
          handleDateChange("", props.value.onChange, noop);
        }}
        onFocus={() => props.onEvent("focus")}
        onBlur={() => props.onEvent("blur")}
        suffixIcon={hasIcon(props.suffixIcon) && props.suffixIcon}
      />
    ),
    ...validateState,
  });
})
  .setPropertyViewFn((children) => {
    const isMobile = useIsMobile();
    return (
      <>
        <Section name={sectionNames.basic}>
          {children.value.propertyView({
            label: trans("prop.defaultValue"),
            placeholder: "2022-04-07 21:39:59",
            tooltip: trans("date.formatTip"),
          })}
          {formatPropertyView({ children })}
          {timeFields(children, isMobile)}
        </Section>

        <FormDataPropertyView {...children} />

        {children.label.getPropertyView()}

        <Section name={sectionNames.interaction}>
          {children.onEvent.getPropertyView()}
          {disabledPropertyView(children)}
        </Section>

        <Section name={sectionNames.validation}>
          {requiredPropertyView(children)}
          {dateValidationFields(children)}
          {timeValidationFields(children)}
          {children.customRule.propertyView({})}
        </Section>

        {/*{commonAdvanceSection(children, children.dateType.value === "date")}*/}
        {!isMobile && commonAdvanceSection(children)}

        <Section name={sectionNames.layout}>
          {children.suffixIcon.propertyView({ label: trans("button.suffixIcon") })}
          {hiddenPropertyView(children)}
        </Section>

        <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
      </>
    );
  })
  .setExposeMethodConfigs(dateRefMethods)
  .build();

export const dateRangeControl = (function () {
  const childrenMap = {
    start: stringExposingStateControl("start"),
    end: stringExposingStateControl("end"),
    ...commonChildren,
  };

  return new UICompBuilder(childrenMap, (props) => {
    const start = fromStringSafeGetDayjs(props.start.value, DateParser);
    const end = fromStringSafeGetDayjs(props.end.value, DateParser);
    const [validateState, setValidateState] = useState({});

    useSkipInitialEffect(() => {
      let hh = validate({
        ...props,
        value: { start: props.start.value, end: props.end.value, range: true, showTime: props.showTime }
      })
      setValidateState(hh)
    }, [props.start.value, props.end.value])

    const children = (
      <DateRangeUIView
        viewRef={props.viewRef}
        $style={props.style}
        disabled={props.disabled}
        {...datePickerProps(props)}
        start={start.isValid() ? start : null}
        end={end.isValid() ? end : null}
        minDate={dayjs(props.minDate).isValid() ? dayjs(props.minDate) : dayjs("1970-01-01")}
        maxDate={dayjs(props.maxDate).isValid() ? dayjs(props.maxDate) : dayjs("2200-12-29")}
        disabledTime={() => disabledTime(props.minTime, props.maxTime)}
        onChange={(start, end) => {
          props.start.onChange(
            start && start.isValid()
              ? start.format(props.showTime ? DATE_TIME_FORMAT : DATE_FORMAT)
              : ""
          );
          props.end.onChange(
            end && end.isValid() ? end.format(props.showTime ? DATE_TIME_FORMAT : DATE_FORMAT) : ""
          );
          props.onEvent("change");
        }}
        onPanelChange={(_, mode) => {
          mode[0] !== "date" && handleDateChange("", props.start.onChange, noop);
          mode[1] !== "date" && handleDateChange("", props.end.onChange, noop);
        }}
        onFocus={() => props.onEvent("focus")}
        onBlur={() => props.onEvent("blur")}
        suffixIcon={hasIcon(props.suffixIcon) && props.suffixIcon}
        hourStep={props.hourStep as hourStepType}
        minuteStep={props.minuteStep as minuteStepType}
        secondStep={props.secondStep as secondStepType}
      />
    );

    return props.label({
      required: props.required,
      style: props.style,
      children: children,
      ...validateState,
    });
  })
    .setPropertyViewFn((children) => {
      const isMobile = useIsMobile();
      return (
        <>
          <Section name={sectionNames.basic}>
            {children.start.propertyView({
              label: trans("date.start"),
              placeholder: "2022-04-07 21:39:59",
              tooltip: trans("date.formatTip"),
            })}
            {children.end.propertyView({
              label: trans("date.end"),
              placeholder: "2022-04-07 21:39:59",
              tooltip: trans("date.formatTip"),
            })}
            {formatPropertyView({ children })}
            {timeFields(children, isMobile)}
          </Section>

          {children.label.getPropertyView()}

          <Section name={sectionNames.interaction}>
            {children.onEvent.getPropertyView()}
            {disabledPropertyView(children)}
          </Section>

          <Section name={sectionNames.validation}>
            {requiredPropertyView(children)}
            {dateValidationFields(children)}
            {timeValidationFields(children)}
            {children.customRule.propertyView({})}
          </Section>

          {commonAdvanceSection(children)}

          <Section name={sectionNames.layout}>
            {children.suffixIcon.propertyView({ label: trans("button.suffixIcon") })}
            {hiddenPropertyView(children)}
          </Section>

          <Section name={sectionNames.style}>{children.style.getPropertyView()}</Section>
        </>
      );
    })
    .build();
})();

export const DatePickerComp = withExposingConfigs(datePickerControl, [
  depsConfig({
    name: "value",
    desc: trans("export.datePickerValueDesc"),
    depKeys: ["value", "showTime"],
    func: (input) => {
      const mom = dayjs(input.value, DateParser, true);
      return mom.isValid() ? mom.format(input.showTime ? DATE_TIME_FORMAT : DATE_FORMAT) : "";
    },
  }),
  depsConfig({
    name: "formattedValue",
    desc: trans("export.datePickerFormattedValueDesc"),
    depKeys: ["value", "format"],
    func: (input) => {
      const mom = dayjs(input.value, DateParser, true);
      return mom.isValid() ? mom.format(input.format) : "";
    },
  }),
  depsConfig({
    name: "timestamp",
    desc: trans("export.datePickerTimestampDesc"),
    depKeys: ["value"],
    func: (input) => {
      const mom = dayjs(input.value, DateParser);
      return mom.isValid() ? mom.unix() : "";
    },
  }),
  depsConfig({
    name: "invalid",
    desc: trans("export.invalidDesc"),
    depKeys: ["value", "required", "minTime", "maxTime", "minDate", "maxDate", "customRule"],
    func: (input) =>
      validate({
        ...input,
        value: { value: input.value },
      } as any).validateStatus !== "success",
  }),
  ...CommonNameConfig,
]);

export let DateRangeComp = withExposingConfigs(dateRangeControl, [
  depsConfig({
    name: "start",
    desc: trans("export.dateRangeStartDesc"),
    depKeys: ["start", "showTime"],
    func: (input) => {
      const mom = dayjs(input.start, DateParser);
      return mom.isValid() ? mom.format(input.showTime ? DATE_TIME_FORMAT : DATE_FORMAT) : "";
    },
  }),
  depsConfig({
    name: "end",
    desc: trans("export.dateRangeEndDesc"),
    depKeys: ["end", "showTime"],
    func: (input) => {
      const mom = dayjs(input.end, DateParser);
      return mom.isValid() ? mom.format(input.showTime ? DATE_TIME_FORMAT : DATE_FORMAT) : "";
    },
  }),
  depsConfig({
    name: "startTimestamp",
    desc: trans("export.dateRangeStartTimestampDesc"),
    depKeys: ["start"],
    func: (input) => {
      const mom = dayjs(input.start, DateParser);
      return mom.isValid() ? mom.unix() : "";
    },
  }),
  depsConfig({
    name: "endTimestamp",
    desc: trans("export.dateRangeEndTimestampDesc"),
    depKeys: ["end"],
    func: (input) => {
      const mom = dayjs(input.end, DateParser);
      return mom.isValid() ? mom.unix() : "";
    },
  }),
  depsConfig({
    name: "formattedValue",
    desc: trans("export.dateRangeFormattedValueDesc"),
    depKeys: ["start", "end", "format"],
    func: (input) => {
      const start = dayjs(input.start, DateParser);
      const end = dayjs(input.end, DateParser);
      return [
        start.isValid() && start.format(input.format),
        end.isValid() && end.format(input.format),
      ]
        .filter((item) => item)
        .join(" - ");
    },
  }),
  depsConfig({
    name: "formattedStartValue",
    desc: trans("export.dateRangeFormattedStartValueDesc"),
    depKeys: ["start", "format"],
    func: (input) => {
      const start = dayjs(input.start, DateParser);
      return start.isValid() && start.format(input.format);
    },
  }),
  depsConfig({
    name: "formattedEndValue",
    desc: trans("export.dateRangeFormattedEndValueDesc"),
    depKeys: ["end", "format"],
    func: (input) => {
      const end = dayjs(input.end, DateParser);
      return end.isValid() && end.format(input.format);
    },
  }),
  depsConfig({
    name: "invalid",
    desc: trans("export.invalidDesc"),
    depKeys: ["start", "end", "required", "minTime", "maxTime", "minDate", "maxDate", "customRule", "showTime"],
    func: (input) =>
      validate({
        ...input,
        value: { start: input.start, end: input.end, range: true, showTime: input.showTime }
      }).validateStatus !== "success",
  }),
  ...CommonNameConfig,
]);

DateRangeComp = withMethodExposing(DateRangeComp, [
  ...dateRefMethods,
  {
    method: {
      name: "clearAll",
      description: trans("date.clearAllDesc"),
      params: [],
    },
    execute: (comp) => {
      comp.children.start.getView().onChange("");
      comp.children.end.getView().onChange("");
    },
  },
  {
    method: {
      name: "resetAll",
      description: trans("date.resetAllDesc"),
      params: [],
    },
    execute: (comp) => {
      comp.children.start.getView().reset();
      comp.children.end.getView().reset();
    },
  },
]);
