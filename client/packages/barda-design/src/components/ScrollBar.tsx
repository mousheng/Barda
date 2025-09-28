import React, { useMemo } from "react";
import SimpleBar, { Props as SimplebarProps } from 'simplebar-react';
import styled from "styled-components";

const ScrollBarWrapper = styled.div`
  min-height: 0;
  height: 100%;
  width: 100%;

  .simplebar-scrollbar::before {
    width: 4px !important;
    background: #8b8fa333 !important;
    right: 4px !important;
  }

  .simplebar-hover::before {
    background: #8b8fa37f !important;
    right: 4px !important;
    opacity: 0.8 !important;
  }

  .simplebar-content-wrapper {
    height: 100% !important;
    outline: none !important;
    .simplebar-content {
      height: 100% !important;
    }
  }

  .simplebar-offset {
    width: 100% !important;
  }

  .simplebar-track.simplebar-vertical .simplebar-scrollbar:before {
    top: 6px;
    bottom: 6px;
  }
`;

interface IProps extends SimplebarProps {
  children: React.ReactNode;
  className?: string;
  height?: string;
}

export const ScrollBar = (props: IProps) => {
  const { height = "100%", className, children, ...otherProps } = props;

  const style = useMemo(() => ({ height }), [height]);

  return (
    <ScrollBarWrapper className={className}>
      <SimpleBar forceVisible="y" style={style} {...otherProps}>
        {children}
      </SimpleBar>
    </ScrollBarWrapper>
  );
};
