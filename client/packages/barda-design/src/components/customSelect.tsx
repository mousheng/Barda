import { Select as AntdSelect, SelectProps as AntdSelectProps } from "antd";
import { ReactComponent as PackUpIcon } from "icons/icon-Pack-up.svg";
import styled from "styled-components";
import React from "react";
import _ from "lodash";

const SelectWrapper = styled.div<{ $border?: boolean }>`
    .ant-select-suffix {
      margin-right: 0px;
      transform: rotate(180deg);
    }
    .ant-select-open {
      .ant-select-suffix {
      transform: rotate(0deg);
      display: flex;
      align-items: center;
      svg g path {
        fill: #4965f2;
      }
    }
    }
  .ant-select-content {
    margin-right: 0px;
  }
    
  :where(.css-dev-only-do-not-override-1uga22k).ant-select.ant-select-outlined:not(.ant-select-disabled).ant-select-focused {
    box-shadow: none !important;
  }
  .ant-select {
    background-color: transparent;
    color: #8b8fa3;
    border: ${(props) => (props.$border ? "1px solid transparent" : "1px solid #d7d9e0")};
    border-radius: 4px;
    padding: ${(props) => (props.$border ? "0px" : "0 0 0 12px")};
    height: 100%;
    align-items: center;

    .ant-select-selection-item {
      display: flex;
      align-items: center;
    }
  }

  .ant-select-focused.ant-select.ant-select-show-arrow {
    .ant-select-selector {
      border: ${(props) => (props.$border ? "1px solid transparent!important" : "1px solid #3377ff")};
      border-radius: 4px;
      box-shadow: 0 0 0 2px ${(props) => (props.$border ? "transparent!important" : "rgba(51,119,255,0.20)")};
    }
  }

  .ant-select:hover,
  .ant-select-disabled:hover {
    .ant-select-selector {
      border: ${(props) => (props.$border ? "1px solid transparent!important" : "1px solid #8b8fa3")};
      border-radius: 4px;
    }
  }

  .ant-select-arrow {
    width: 20px;
    height: 20px;
    right: 8px;
    top: 0;
    bottom: 0;
    margin: auto;
    transform: rotate(180deg);
  }

  .ant-select-disabled.ant-select {
    .ant-select-selector {
      background: ${(props) => (props.$border ? "#ffffff" : "#fdfdfd")};
      border-radius: 4px;
      color: #b8b9bf;
    }

    .ant-select-arrow svg g path {
      fill: #b8b9bf;
    }
  }

  .ant-select-clear {
    right: 32px;
  }
`;

export type CustomSelectProps = {
  children?: JSX.Element | React.ReactNode;
  innerRef?: React.Ref<HTMLDivElement> | undefined;
  border?: boolean;
};

function CustomSelect(props: CustomSelectProps & AntdSelectProps) {
  const {
    children,
    innerRef,
    className,
    border,
    classNames,
    styles,
    ...restProps
  } = props;
  
  const mergedStyles = _.merge(
    { root: { height: 32 } },
    styles
  ) as any;

  return (
    <SelectWrapper className={className} ref={innerRef} $border={border}>
      <AntdSelect
        popupMatchSelectWidth={false}
        suffixIcon={<PackUpIcon />}
        styles={mergedStyles}
        {...restProps}
      >
        {children}
      </AntdSelect>
    </SelectWrapper>
  );
}

CustomSelect.Option = AntdSelect.Option;
export { CustomSelect };
