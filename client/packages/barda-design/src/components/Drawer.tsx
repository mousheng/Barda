import { Drawer as AntdDrawer, DrawerProps as AntdDrawerProps } from "antd";
import { useMemo, useState, useEffect, useRef } from "react";
import styled from "styled-components";

const StyledDrawer = styled(AntdDrawer)`
  .ant-drawer-body {
    padding: 0px;
    overflow: hidden;
  }
`;

export const parseSize = (size: "default" | "large" | number | undefined) => {
  if (size === "default" || typeof size === undefined) return 378;
  else if (size === "large") return 736;
  else return size as number;
};

type DrawerProps = {
  resizable?: boolean;
  autoHeight?: boolean;
  onResizeStop?: (size: number) => void;
} & AntdDrawerProps;

export function Drawer(props: DrawerProps) {
  const { resizable, children, onResizeStop, size, ...otherProps } = props;
  const resizeing = useRef(false);
  const [drawerSize, setDrawerSize] = useState<number>(parseSize(props.size ?? 378));
  const isTopBom = ["top", "bottom"].includes(props.placement ?? "");

  const style = {
    section: {
      height: resizeing.current
        ? isTopBom
          ? drawerSize + "px"
          : "100%"
        : isTopBom
        ? props.autoHeight
          ? "auto"
          : props.size + "px"
        : "100%",
    },
  };
  return (
    <StyledDrawer
      {...otherProps}
      placement={props.placement}
      resizable={
        resizable
          ? {
              onResizeStart: () => {
                resizeing.current = true;
              },
              onResize: (newSize) => setDrawerSize(newSize),
              onResizeEnd: () => {
                props.onResizeStop?.(drawerSize);
                resizeing.current = false;
              },
            }
          : false
      }
      styles={style}
    >
      {children}
    </StyledDrawer>
  );
}
