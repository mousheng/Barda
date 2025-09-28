import { Dropdown, message, Skeleton, Tooltip } from "antd";
import LayoutHeader from "components/layout/Header";
import { SHARE_TITLE } from "constants/apiConstants";
import { AppTypeEnum } from "constants/applicationConstants";
import { ALL_APPLICATIONS_URL, AUTH_LOGIN_URL, preview } from "constants/routesURL";
import { User } from "constants/userConstants";
import {
  CommonTextLabel,
  CustomModal,
  DropdownMenu,
  EditText,
  Left,
  Middle,
  ModuleIcon,
  PackUpIcon,
  Right,
  TacoButton,
} from "barda-design";
import { trans } from "i18n";
import dayjs from "dayjs";
import { memo, useContext, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { publishApplication, updateAppMetaAction } from "redux/reduxActions/applicationActions";
import { recoverSnapshotAction, setShowAppSnapshot } from "redux/reduxActions/appSnapshotActions";
import { currentApplication } from "redux/selectors/applicationSelector";
import {
  getSelectedAppSnapshot,
  showAppSnapshotSelector,
} from "redux/selectors/appSnapshotSelector";
import { getUser, isFetchingUser } from "redux/selectors/usersSelectors";
import styled, { css } from "styled-components";
import { ExternalEditorContext } from "util/context/ExternalEditorContext";
import history from "util/history";
import { useApplicationId } from "util/hooks";
import { canManageApp } from "util/permissionUtils";
import ProfileDropdown from "./profileDropdown";
import { Logo } from "@barda/assets/images";
import { HeaderStartDropdown } from "./headerStartDropdown";
import { AppPermissionDialog } from "../../components/PermissionDialog/AppPermissionDialog";
import { getBrandingConfig } from "../../redux/selectors/configSelectors";
import { StyledLinkComp } from "./StyledLinkComp";

const IconCss = css<{ $show: boolean }>`
  &:hover {
    background-color: #8b8fa34c;
  }

  & g g {
    stroke: ${(props) => (props.$show ? "#dddddd" : "#dddddd65")};
  }

  &:hover g g {
    stroke: #ffffff;
  }

  cursor: pointer;
`;
const LeftIcon = styled(Left)`
  ${IconCss as any}
`;
const MiddleIcon = styled(Middle) <{ $show: boolean }>`
  ${IconCss}
  & g line {
    stroke: ${(props) => (props.$show ? "#dddddd" : "#dddddd65")};
  }

  &:hover g line {
    stroke: #ffffff;
  }

  & g rect {
    stroke: ${(props) => (props.$show ? "#dddddd" : "#dddddd65")};
  }

  &:hover g rect {
    stroke: #ffffff;
  }
`;

export const PlatformTitle = styled.span`
  font-size: 24px;
  margin-left: 8px;
  font-weight: bold;
  color: #FFFFFF;
`

const RightIcon = styled(Right)`
  ${IconCss as any}
`;
const IconRadius = styled.div<{ $disabled?: boolean }>`
  width: 28px;
  height: 28px;
  border-radius: 4px;
  overflow: hidden;

  ${(props) =>
    props.$disabled &&
    `
    svg {
       pointer-events: none;
       g g {
         stroke: #dddddd30;
       }
    }
    cursor: not-allowed;  
  `}
`;

const PreviewBtn = styled(TacoButton)`
  padding: 4px 12px;
  height: 28px;
  background: #4965f2;
  border-radius: 4px 0 0 4px;
`;

const SnapshotBtnWrapper = styled.div`
  margin-left: 14px;
  display: flex;
  gap: 8px;
`;

const RecoverSnapshotBtn = styled(TacoButton)`
  padding: 4px 7px;
  height: 28px;

  :disabled,
  :disabled:hover {
    background: #4965f2;
    border: 1px solid #4965f2;
    color: #ffffff;
    opacity: 0.35;
  }
`;

const LoginBtn = styled(TacoButton)`
  padding: 4px 7px;
  height: 28px;
  margin-right: 4px;
`;
const GrayBtn = styled(TacoButton)`
  &&&{
    color: #ffffff;
    background-color: #8b8fa34c;
    border: none;
    height: 28px;
    padding: 4px 13px;
    margin-right: 8px;
    cursor: pointer;
    --antd-wave-shadow-color: #8b8fa34c;

    &:hover {
      background: #666666;
      color: #ffffff;
      border: none;
    }

    &:focus {
      background: #666666;
      color: #ffffff;
      border: none;
    }
  }
`;

const attrs = () => ({
  tabIndex: 0,
});

const ViewOnlyLabel = styled.span`
  font-size: 12px;
  color: #b8b9bf;
  line-height: 12px;
  margin-left: -22px;
`;

const PackUpIconStyled = styled(PackUpIcon)`
  transform: rotate(180deg);
  margin-left: 4px;
  min-width: 18px;

  path {
    fill: #ffffff;
  }
`;

const PackUpBtn = styled(TacoButton)`
  padding: 0;
  width: 28px;
  height: 28px;
  border-radius: 0 4px 4px 0;
  margin-right: 24px;
  margin-left: 1px;

  svg {
    transform: rotate(180deg);
    width: 18px;

    path {
      fill: #ffffff;
    }
  }

  &.ant-dropdown-open {
    background-color: #3a51c2;
    border-color: #3a51c2;
  }
`;

const DropdownStyled = styled(Dropdown)`
  &.ant-dropdown-open {
    background-color: #8b8fa34c;
  }
`;

const DropdownMenuStyled = styled(DropdownMenu)`
  .ant-dropdown-menu-item:hover {
    background: #edf4fa;
  }
`;

const Wrapper = styled.div`
  .taco-edit-text-wrapper {
    width: fit-content;
    max-width: 288px;
  }

  input {
    width: 288px;
    border-radius: 4px;
  }
`;

const Prefix = styled.div`
  display: inline-flex;
  align-items: center;
  margin-right: 4px;

  &.module svg {
    visibility: visible;
  }
`;

function HeaderProfile(props: { user: User }) {
  const { user } = props;
  const fetchingUser = useSelector(isFetchingUser);
  if (fetchingUser) {
    return <Skeleton.Avatar shape="circle" size={28} />;
  }
  return (
    <div>
      {user.isAnonymous ? (
        <LoginBtn buttonType="primary" onClick={() => history.push(AUTH_LOGIN_URL)}>
          {trans("userAuth.login")}
        </LoginBtn>
      ) : (
        <ProfileDropdown user={user} profileSide={28} fontSize={12} />
      )}
    </div>
  );
}

export type PanelStatus = { left: boolean; bottom: boolean; right: boolean };
export type TogglePanel = (panel?: keyof PanelStatus) => void;
type HeaderProps = {
  togglePanel: TogglePanel;
  panelStatus: PanelStatus;
};

// header in editor page
const Header = memo((props: HeaderProps) => {
  const { togglePanel } = props;
  const { left, bottom, right } = props.panelStatus;
  const user = useSelector(getUser);
  const application = useSelector(currentApplication);
  const applicationId = useApplicationId();
  const dispatch = useDispatch();
  const showAppSnapshot = useSelector(showAppSnapshotSelector);
  const selectedSnapshot = useSelector(getSelectedAppSnapshot);
  const { appType } = useContext(ExternalEditorContext);
  const [editName, setEditName] = useState(false);
  const [editing, setEditing] = useState(false);
  const [permissionDialogVisible, setPermissionDialogVisible] = useState(false);
  const lastPreviewTab = useRef<WindowProxy | null>(null);

  const isModule = appType === AppTypeEnum.Module;

  const headerStart = (
    <>
      <StyledLinkComp onClick={() => history.push(ALL_APPLICATIONS_URL)} overflow={true} type="editAndPreview" />
      {editName ? (
        <Wrapper>
          <EditText
            prefixIcon={isModule && <ModuleIcon />}
            disabled={showAppSnapshot}
            style={showAppSnapshot ? { maxWidth: "fit-content" } : {}}
            text={application?.name ?? ""}
            editing={editing}
            onFinish={(value) => {
              if (!value.trim()) {
                message.warning(trans("header.nameCheckMessage"));
                return;
              }
              dispatch(updateAppMetaAction({ applicationId: applicationId, name: value }));
              setEditName(false);
            }}
          />
        </Wrapper>
      ) : (
        <HeaderStartDropdown
          setEdit={() => {
            setEditName(true);
            setEditing(true);
          }}
        />
      )}
      {showAppSnapshot && <ViewOnlyLabel>{trans("header.viewOnly")}</ViewOnlyLabel>}
    </>
  );

  const headerMiddle = (
    <>
      <Tooltip title={trans("header.left")}>
        <IconRadius >
          <LeftIcon onClick={() => togglePanel("left")} $show={left} />
        </IconRadius>
      </Tooltip>
      <Tooltip title={trans("header.bottom")}>
        <IconRadius>
          <MiddleIcon onClick={() => togglePanel("bottom")} $show={bottom} />
        </IconRadius>
      </Tooltip>
      <Tooltip title={trans("header.right")}>
        <IconRadius $disabled={showAppSnapshot} >
          <RightIcon onClick={() => togglePanel("right")} $show={right} />
        </IconRadius>
      </Tooltip>
      {showAppSnapshot && (
        <SnapshotBtnWrapper>
          <RecoverSnapshotBtn
            disabled={!selectedSnapshot}
            buttonType="primary"
            onClick={() => {
              if (!application || !selectedSnapshot) return;
              CustomModal.confirm({
                title: trans("header.recoverAppSnapshotTitle"),
                content: trans("header.recoverAppSnapshotContent", {
                  time: dayjs(selectedSnapshot.createTime).format("YYYY-MM-DD HH:mm"),
                }),
                onConfirm: () => {
                  dispatch(
                    recoverSnapshotAction(
                      application.applicationId,
                      selectedSnapshot.snapshotId,
                      selectedSnapshot.createTime
                    )
                  );
                },
              });
            }}
          >
            {trans("header.recoverAppSnapshotMessage")}
          </RecoverSnapshotBtn>
          <GrayBtn onClick={() => dispatch(setShowAppSnapshot(false))}>
            {trans("header.returnEdit")}
          </GrayBtn>
        </SnapshotBtnWrapper>
      )}
    </>
  );

  const headerEnd = showAppSnapshot ? (
    <HeaderProfile user={user} />
  ) : (
    <>
      {applicationId && (
        <AppPermissionDialog
          applicationId={applicationId}
          visible={permissionDialogVisible}
          onVisibleChange={(visible) => !visible && setPermissionDialogVisible(false)}
        />
      )}
      {canManageApp(user, application) && (
        <GrayBtn onClick={() => setPermissionDialogVisible(true)}>{SHARE_TITLE}</GrayBtn>
      )}
      <PreviewBtn buttonType="primary" onClick={() => lastPreviewTab.current = preview(applicationId, lastPreviewTab.current)}>
        {trans("header.preview")}
      </PreviewBtn>

      <Dropdown
        className="cypress-header-dropdown"
        placement="bottomRight"
        trigger={["click"]}
        dropdownRender={() =>
        (<DropdownMenuStyled
          style={{ minWidth: "110px", borderRadius: "4px" }}
          onClick={(e: any) => {
            if (e.key === "deploy") {
              dispatch(publishApplication({ applicationId }));
            } else if (e.key === "snapshot") {
              dispatch(setShowAppSnapshot(true));
            }
          }}
          items={[
            {
              key: "deploy",
              label: <CommonTextLabel>{trans("header.deploy")}</CommonTextLabel>,
            },
            {
              key: "snapshot",
              label: <CommonTextLabel>{trans("header.snapshot")}</CommonTextLabel>,
            },
          ]}
        />)
        }
      >
        <PackUpBtn buttonType="primary">
          <PackUpIcon />
        </PackUpBtn>
      </Dropdown>

      <HeaderProfile user={user} />
    </>
  );

  return (
    <LayoutHeader $headerStart={headerStart} $headerMiddle={headerMiddle} $headerEnd={headerEnd} />
  );
})
export default Header;

// header in manager page
export function AppHeader() {
  const user = useSelector(getUser);
  const brandingConfig = useSelector(getBrandingConfig);
  const headerStart = (
    <StyledLinkComp onClick={() => history.push(ALL_APPLICATIONS_URL)} type="index" />
  );
  const headerEnd = <HeaderProfile user={user} />;
  return (
    <LayoutHeader
      $headerStart={headerStart}
      $headerEnd={headerEnd}
      $style={user.orgDev ? {} : { backgroundColor: brandingConfig?.headerColor }}
    />
  );
}
