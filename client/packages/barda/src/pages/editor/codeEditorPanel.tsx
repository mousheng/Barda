import { PushpinFilled, PushpinOutlined } from "@ant-design/icons";
import { CodeEditorCloseIcon, CodeEditorOpenIcon, DragIcon } from "barda-design";
import { isEmpty } from "lodash";
import Trigger from "rc-trigger";
import { ReactNode, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import Draggable from "react-draggable";
import { Resizable, ResizeCallbackData } from "react-resizable";
import { useWindowSize } from "react-use";
import styled from "styled-components";
import { CompNameContext } from "../../comps/editorState";
import { useEditorStore } from "../../comps/editorStore";
import { Layers } from "../../constants/Layers";
import Handle from "../../layout/handler";
import { getPanelStyle, savePanelStyle } from "../../util/localStorageUtil";

const Wrapper = styled.div`
  max-width: 60vw;
  max-height: 70vh;
  position: fixed;
  z-index: ${Layers.codeEditorPanel};
  top: 35%;
  left: 50%;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.3);
  border: 1px solid #b6b6b6;
`;
const HeaderWrapper = styled.div`
  height: 40px;
  cursor: move;
  padding: 0 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
`;
const TitleWrapper = styled.div`
  display: flex;
  align-items: center;
  font-weight: 500;
  font-size: 16px;
  line-height: 16px;
  color: #222222;
`;
const StyledDragIcon = styled(DragIcon)`
  margin-right: 8px;
`;
const BodyWrapper = styled.div`
  height: calc(100% - 45px);
  border: 1px solid #e5e5e5;
  margin: 0 5px 5px 5px;
  border-radius: 4px;
`;

const OpenButton = styled.div`
  position: absolute;
  height: 16px;
  width: 16px;
  border: 1px solid #d7d9e0;
  border-radius: 4px;
  z-index: 1;
  bottom: 8px;
  right: 8px;
  cursor: pointer;
  display: none;
  background-color: #ffffff;

  svg {
    height: 100%;
    display: block;
    margin: auto;
  }

  &:hover {
    svg g g {
      stroke: #222222;
    }
  }
`;
export const CloseButton = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 22px;
  background: #ffffff;
  border-radius: 4px;
  font-size: 14px;
  color: #bfbfbf;
  padding: 3px 5px 3px 5px;
  cursor: pointer;

  &:hover {
    background: #eeeeee;
    border-radius: 4px;

    svg g g {
      width: 20px;
      height: 20px;
      stroke: #222222;
    }
  }
`;

const ButtonWrapper = styled.div`
  display: flex;
`;

export const CodeEditorPanel = (props: {
  editor: ReactNode;
  breadcrumb?: ReactNode[];
  onVisibleChange: (visible: boolean) => void;
}) => {
  const { width, height } = useWindowSize();
  const draggableRef = useRef<HTMLDivElement>(null);
  const [unDraggable, setUnDraggable] = useState(true);
  const [bounds, setBounds] = useState({
    left: 0,
    top: 0,
    bottom: 0,
    right: 0,
  });

  const panelStyle = useMemo(() => getPanelStyle(), [props.editor]);
  const [size, setSize] = useState({ w: panelStyle.codeEditor.w, h: panelStyle.codeEditor.h });

  const [visible, setVisible] = useState(false);
  const [pinned, setpinned] = useState(false);

  const compName = useContext(CompNameContext);
  const setCodeEditorPanelOpen = useEditorStore((s) => s.setCodeEditorPanelOpen);

  const updateEditorState = useCallback(() => {
    setCodeEditorPanelOpen(visible);
  }, [visible, setCodeEditorPanelOpen]);

  useEffect(() => {
    updateEditorState();
  }, [updateEditorState]);

  return (
    <Trigger
      popupVisible={visible}
      action={["click"]}
      zIndex={Layers.codeEditorPanel}
      popupStyle={{ opacity: 1, display: visible ? "block" : "none" }}
      maskClosable={!pinned}
      mask={true}
      onPopupVisibleChange={(visible) => setVisible(visible)}
      afterPopupVisibleChange={(visible) => props.onVisibleChange(visible)}
      popup={() => (
        <Draggable
          nodeRef={draggableRef}
          positionOffset={{ x: "-50%", y: "-50%" }}
          disabled={unDraggable}
          bounds={bounds}
          onStart={(event, uiData) => {
            const targetRect = draggableRef.current?.getBoundingClientRect();
            if (!targetRect) {
              return;
            }
            setBounds({
              left: -targetRect.left + uiData.x,
              right: width - (targetRect.right - uiData.x),
              top: -targetRect.top + uiData.y,
              bottom: height - (targetRect.bottom - uiData.y),
            });
          }}
        >
          <Resizable
            width={size.w}
            height={size.h}
            onResize={(event, { size }) => setSize({ w: size.width, h: size.height })}
            onResizeStop={(e: React.SyntheticEvent, data: ResizeCallbackData) => {
              const targetRect = draggableRef.current?.getBoundingClientRect();
              const newHeight =
                targetRect && targetRect?.top < 0 ? targetRect.bottom + targetRect?.top : size.h;
              setSize({ w: size.w, h: newHeight });
              savePanelStyle({ ...panelStyle, codeEditor: { w: size.w, h: newHeight } });
            }}
            handle={Handle}
            resizeHandles={["s", "n", "w", "e", "sw", "nw", "se", "ne"]}
            minConstraints={[480, 360]}
          >
            <Wrapper ref={draggableRef} style={{ width: size.w + "px", height: size.h + "px" }}>
              <HeaderWrapper
                onMouseOver={() => setUnDraggable(false)}
                onMouseOut={() => setUnDraggable(true)}
              >
                <TitleWrapper>
                  <StyledDragIcon />
                  {[compName, ...(props.breadcrumb ?? [])].filter((t) => !isEmpty(t)).join(" / ")}
                </TitleWrapper>
                <ButtonWrapper>
                  <CloseButton onClick={() => setpinned(!pinned)}>
                    {pinned ? <PushpinFilled rotate={-45} /> : <PushpinOutlined />}
                  </CloseButton>
                  <CloseButton
                    onClick={() => {
                      setVisible(false);
                      setpinned(false);
                    }}
                  >
                    <CodeEditorCloseIcon />
                  </CloseButton>
                </ButtonWrapper>
              </HeaderWrapper>

              <BodyWrapper>{props.editor}</BodyWrapper>
            </Wrapper>
          </Resizable>
        </Draggable>
      )}
    >
      <OpenButton className={"code-editor-panel-open-button"} onClick={() => setVisible(true)}>
        <CodeEditorOpenIcon />
      </OpenButton>
    </Trigger>
  );
};
