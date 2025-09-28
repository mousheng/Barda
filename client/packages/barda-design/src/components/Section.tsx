import { trans } from "i18n/design";
import { ReactComponent as Packup } from "icons/icon-Pack-up.svg";
import React, { memo, ReactNode, useCallback, useRef, useState } from "react";
import styled from "styled-components";
import { labelCss } from "./Label";
import { controlItem, ControlNode } from "./control";

const SectionItem = styled.div<{ $width?: number }>`
  width: ${(props) => (props.$width ? props.$width : 312)}px;
  border-bottom: 1px solid #e1e3eb;

  &:last-child {
    border-bottom: none;
  }
`;

const SectionLabel = styled.div`
  ${labelCss};
  flex-grow: 1;
  font-size: 14px;
  color: #8b8fa3;
  line-height: 46px;
  font-weight: 600;
`;

interface Irotate {
  $deg: string;
}

const PackupIcon = styled(Packup) <Irotate>`
  height: 12px;
  width: 12px;
  float: right;
  transform: ${(props) => props.$deg};

  &:hover {
    display: block;
    cursor: pointer;
  }
`;

const SectionLabelDiv = memo(styled.div`
  display: flex;
  align-items: center;
  height: 42px;
  margin-left: 10px;

  &:hover {
    cursor: pointer;
  }

  &:hover ${SectionLabel} {
    color: #222222;
  }

  &:hover ${PackupIcon} path {
    display: block;
    fill: #222222;
  }
`);

const ShowChildren = styled.div<{ $show?: string; $noMargin?: boolean }>`
  display: ${(props) => props.$show || "none"};
  flex-direction: column;
  gap: 8px;
  transition: all 3s;
  margin-left: ${(props) => (props.$noMargin ? 0 : 16)}px;
  padding-bottom: 16px;
  padding-right: ${(props) => (props.$noMargin ? 0 : "16px")};
`;

interface ISectionConfig<T> {
  name?: string;
  width?: number;
  noMargin?: boolean;
  style?: React.CSSProperties;
  children: T;
  additionalButton?: React.ReactNode;
}

export interface PropertySectionState {
  [compName: string]: {
    [sectionName: string]: boolean;
  };
}

export const BaseSection = (props: ISectionConfig<ReactNode>) => {
  const { name } = props;
  const [opened, setOpened] = useState(true);
  const divStyle = useRef({ display: "flex" })
  const toggle = useCallback(() => {
    setOpened(!opened)
  }, [opened])

  return (
    <SectionItem $width={props.width} style={props.style}>
      {name && (
        <SectionLabelDiv onClick={toggle} className={"section-header"}>
          <PackupIcon $deg={opened ? "rotate(180deg)" : "rotate(90deg)"} />
          <SectionLabel>{name}</SectionLabel>
          <div style={divStyle.current}>
            {opened && props.additionalButton}

          </div>
        </SectionLabelDiv>
      )}
      <ShowChildren $show={opened ? "flex" : "none"} $noMargin={props.noMargin}>
        {props.children}
      </ShowChildren>
    </SectionItem>
  );
};

export function Section(props: ISectionConfig<ControlNode>) {
  return controlItem({ filterText: props.name, searchChild: true }, <BaseSection {...props} />);
}

// common section names
export const sectionNames = {
  basic: trans("prop.basic"),
  interaction: trans("prop.interaction"),
  advanced: trans("prop.advanced"),
  validation: trans("prop.validation"),
  layout: trans("prop.layout"),
  style: trans("prop.style"),
  badge: trans("prop.badge"),
};
