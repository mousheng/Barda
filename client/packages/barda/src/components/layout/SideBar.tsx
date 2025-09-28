import { Layout, SiderProps } from "antd";
import { TopHeaderHeight } from "constants/style";
import styled from "styled-components";

const Sider = styled(Layout.Sider)`
  &&{
    height: calc(100vh - ${TopHeaderHeight});
    background: transparent;

    .sidebar-item:hover {
        background: #eef0f3;
      }
  }
`;

export default function SideBar(props: SiderProps) {
  const { children, ...otherProps } = props;
  return (
    <Sider theme="light" width={244} {...otherProps}>
      {props.children}
    </Sider>
  );
}
