import styled from "styled-components";
import { ReactComponent as Edit } from "icons/icon-text-edit.svg";
import { CSSProperties, ReactNode, useEffect, useRef, useState } from "react";
import { Input } from "../components/Input";
import { InputProps, InputRef } from "antd";

const Wrapper = styled.div`
  position: relative;
`;

const Prefix = styled.div`
  position: absolute;
  left: 8px;
  top: 6px;
`;

export const EditTextWrapper = styled.div<{ $disabled?: boolean; $hasPrefix?: boolean }>`
  font-weight: 500;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px 0 4px;
  padding-left: ${(props) => (props.$hasPrefix ? "28px" : "4px")};
  border-radius: 4px;
  width: 220px;
  height: 28px;
  line-height: 28px;
  color: #ffffff;
  font-size: 14px;
  cursor: ${(props) => !props.$disabled && "pointer"};

  &:hover {
    background-color: ${(props) => !props.$disabled && "#e1e3eb4c"};
  }

  &:hover svg {
    visibility: ${(props) => (props.$disabled ? "hidden" : "visible")};
    &:hover g {
      fill: #315efb;
    }
  }
`;
export const TextWrapper = styled.div`
  width: 100%;
  height: 100%;
  font-size: inherit;
  color: inherit;
  font-style: normal;
  font-weight: 500;
  line-height: inherit;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
`;
const EditIcon = styled(Edit)`
  visibility: hidden;
  margin-left: 8px;
  flex-shrink: 0;
`;
const TextInput = styled(Input) <InputProps & { $hasPrefix?: boolean }>`
  font-weight: 500;
  margin: 0;
  border-radius: 4px;
  width: 220px;
  height: 28px;
  min-height: 0;
  background-color: #8b8fa34c!important;
  border: none;
  padding: 0 8px 0 4px;
  padding-left: ${(props) => (props.$hasPrefix ? "28px" : "4px")};
  color: #ffffff;
  line-height: 28px;
  font-size: 14px;

  &:focus {
    box-shadow: none;
  }
`;

export interface EditTextProps {
  prefixIcon?: ReactNode;
  text: string;
  disabled?: boolean;
  forceClickIcon?: boolean;
  onFinish: (value: string) => void;
  onChange?: (value: string) => void;
  onEditStateChange?: (editing: boolean) => void;
  style?: CSSProperties;
  editing?: boolean;
  disableHoverIcon?: boolean; // 禁用悬停显示编辑图标
}

export const EditText = (props: EditTextProps) => {
  const [isHover, setIsHover] = useState(false);
  const [showHoverIcon, setShowHoverIcon] = useState(false);  // 是否实际显示编辑图标
  const [editing, setEditing] = useState(props.editing || false);
  const inputRef = useRef<InputRef>(null);

  useEffect(() => {
    inputRef.current?.focus({
      cursor: "all",
    });
    props.onEditStateChange?.(editing);
  }, [editing]);

  useEffect(() => {
    if (!props.disableHoverIcon) {
      setShowHoverIcon(isHover);
    }
  }, [isHover, props.disableHoverIcon])

  return (
    <Wrapper>
      {editing ? (
        <TextInput
          className="taco-edit-text-input"
          $hasPrefix={!!props.prefixIcon}
          ref={inputRef}
          spellCheck={false}
          defaultValue={props.text}
          onClick={(e: React.MouseEvent<HTMLInputElement>) => e.stopPropagation()}
          onChange={(e: React.ChangeEvent<HTMLInputElement>) => props.onChange?.((e.target as HTMLInputElement).value)}
          onBlur={(e: React.FocusEvent<HTMLInputElement>) => {
            props.onFinish(e.target.value);
            setEditing(false);
          }}
          onPressEnter={(e: React.KeyboardEvent) => {
            props.onFinish((e.target as HTMLInputElement).value);
            setEditing(false);
          }}
        />
      ) : (
        <EditTextWrapper
          style={props.style}
          $disabled={props.disabled}
          $hasPrefix={!!props.prefixIcon}
          className="taco-edit-text-wrapper"
          onClick={() => !props.disabled && !props.forceClickIcon && setEditing(true)}
          onMouseEnter={() => setIsHover(true)}
          onMouseLeave={() => setIsHover(false)}
        >
          <TextWrapper title={props.text}>
            {props.text}
          </TextWrapper>
          {showHoverIcon && <EditIcon
            onClick={(e: React.MouseEvent) => {
              e.stopPropagation();
              !props.disabled && setEditing(true);
            }}
          />}
        </EditTextWrapper>
      )}
      {props.prefixIcon && <Prefix>{props.prefixIcon}</Prefix>}
    </Wrapper>
  );
};
