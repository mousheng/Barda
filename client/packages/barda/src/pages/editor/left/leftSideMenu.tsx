import { Menu, MenuProps } from "antd";
import Sider from "antd/lib/layout/Sider";
import { memo } from "react";
import styled from "styled-components";
import { Layers } from "constants/Layers";
import { HelpDropdown } from "pages/common/help";
import { BarsOutlined, SettingOutlined } from "@ant-design/icons";

const SiderWrapper = styled.div`
  .ant-menu {
    background-color: #393b47;
    height: calc(100vh - 48px);

    .ant-menu-item {
      padding: 0 7px !important;
      width: 40px;
      height: 26px;
      margin: 12px 0 0 0;

      svg {
        height: 26px;
        width: 26px;
        padding: 5px;
      }

      &.ant-menu-item-selected,
      &:hover,
      &:active {
        background-color: #393b47;

        svg {
          background: #8b8fa37f;
          border-radius: 4px;
        }
      }
    }
  }

  z-index: ${Layers.leftToolbar};
`;

const HelpDiv = styled.div`
  > div {
    left: 6px;
    right: auto;
    height: 28px;
    bottom: 36px;

    > div.shortcutList {
      left: 42px;
      bottom: 2px;
    }
  }
`;

export enum SiderKey {
    State = "state",
    Setting = "setting",
}

const items = [
    {
        key: SiderKey.State,
        icon: <BarsOutlined />,
    },
    {
        key: SiderKey.Setting,
        icon: <SettingOutlined />,
    }
];

const MemoizedMenu = memo(({ items, selectedKeys, onClick, disabled, defaultSelectedKeys }: MenuProps) => {
    return (
        <Menu
            theme="dark"
            mode="inline"
            defaultSelectedKeys={defaultSelectedKeys}
            selectedKeys={selectedKeys}
            items={items}
            disabled={disabled}
            onClick={onClick}
        />
    );
});

MemoizedMenu.displayName = "MemoizedMenu";

interface LeftSideMenuProps {
    selectedKeys: string[];
    disabled: boolean;
    onClick: (params: { key: string }) => void;
    defaultSelectedKeys: string[];
    showShortcutList: boolean;
    setShowShortcutList: (show: boolean) => void;
}

export function LeftSideMenu({
    selectedKeys,
    disabled,
    onClick,
    defaultSelectedKeys,
    showShortcutList,
    setShowShortcutList
}: LeftSideMenuProps) {
    return (
        <SiderWrapper>
            <Sider width={40}>
                <MemoizedMenu
                    items={items}
                    selectedKeys={selectedKeys}
                    disabled={disabled}
                    onClick={onClick}
                    defaultSelectedKeys={defaultSelectedKeys}
                />
                {!disabled && (
                    <HelpDiv>
                        <HelpDropdown
                            showShortcutList={showShortcutList}
                            setShowShortcutList={setShowShortcutList}
                            isEdit={true}
                        />
                    </HelpDiv>
                )}
            </Sider>
        </SiderWrapper>
    );
}