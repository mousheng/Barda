// React相关
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

// 第三方库
import { CloseOutlined, CopyOutlined, SendOutlined } from "@ant-design/icons";
import { Avatar, Mentions, message, Tooltip } from "antd";
import copy from "copy-to-clipboard";
import dayjs from "dayjs";
import "dayjs/locale/zh-cn";
import relativeTime from "dayjs/plugin/relativeTime";
import _ from "lodash";
import VirtualList from "rc-virtual-list";
import styled from "styled-components";

// 核心库
import { changeChildAction, CompAction, RecordConstructorToView } from "barda-core";
import { Section, sectionNames } from "barda-design";

// 组件和控件
import { AutoHeightControl } from "comps/controls/autoHeightControl";
import { BoolControl } from "comps/controls/boolControl";
import { jsonControl, StringControl } from "comps/controls/codeControl";
import { jsonValueExposingStateControl } from "comps/controls/codeStateControl";
import { clickEvent, deleteEvent, eventHandlerControl, mentionEvent, submitEvent } from "comps/controls/eventHandlerControl";
import { styleControl } from "comps/controls/styleControl";
import { calculateRemainingHeight, calculateRemainingWidth, CommentStyle, CommentStyleType, formatBoxValuesWithUnit } from "comps/controls/styleControlConstants";
import { hiddenPropertyView } from "comps/utils/propertyUtils";

// 生成器和工具
import { UICompBuilder, withDefault } from "../../generators";
import { valueComp } from "comps/generators/simpleGenerators";
import { NameConfig, NameConfigHidden, withExposingConfigs } from "comps/generators/withExposing";
import { getInitialsAndColorCode } from "util/stringUtils";

// 国际化
import { trans } from "i18n";

// 本地组件
import { checkMentionListData, checkUserInfoData, CommentDataTooltip, commentDataTYPE, commentDate, CommentUserDataTooltip, convertCommentData } from "./commentConstants";
dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

const Wrapper = styled.div<{
  $style: CommentStyleType;
  $autoHeight: boolean;
}>`
  border: 1px solid ${props => props.$style.border};
  margin: ${props => formatBoxValuesWithUnit(props.$style.margin_UNIT, [3, 3, 3, 3])};
  padding: ${props => formatBoxValuesWithUnit(props.$style.padding_UNIT, [20, 10, 0, 10])};
  width: ${props => calculateRemainingWidth(props.$style.margin_UNIT, [3, 3, 3, 3])};
  height: ${props => calculateRemainingHeight(props.$style.margin_UNIT, [3, 3, 3, 3])};
  background: ${props => props.$style.background};
  border-radius: ${props => props.$style.radius};
  display: flex;
  flex-direction: column;
  overflow: hidden;
`;

const CommentHeader = styled.div`
  flex-shrink: 0;
  padding-bottom: 8px;
`;

const CommentContent = styled.div<{ $autoHeight: boolean }>`
  flex: 1;
  overflow: ${props => props.$autoHeight ? 'visible' : 'auto'};
  overflow-x: hidden;
  min-height: 0;
  
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: rgba(139, 143, 163, 0.2);
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb:hover {
    background-color: rgba(139, 143, 163, 0.4);
  }
`;

const CommentFooter = styled.div`
  flex-shrink: 0;
  padding-top: 8px;
`;

const InputContainer = styled.div`
  position: relative;
  width: 100%;
`;

const StyledMentions = styled(Mentions)`
  width: 100%;
  
  .ant-mentions {
    min-height: 40px;
    max-height: 120px; /* 约3行的高度 */
    overflow-y: auto;
    resize: none;
    padding-right: 40px; /* 为右侧图标留出空间 */
    line-height: 1.5;
    height: auto !important;
  }
  
  .ant-mentions textarea {
    min-height: 40px !important;
    max-height: 120px !important;
    height: auto !important;
    line-height: 1.5;
    overflow-y: auto;
    resize: none;
  }
`;

const SendIcon = styled.div<{ disabled: boolean; $inputHeight: number }>`
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  color: ${props => props.disabled ? '#d9d9d9' : '#1890ff'};
  cursor: ${props => props.disabled ? 'not-allowed' : 'pointer'};
  transition: all 0.2s;
  z-index: 10;
  
  &:hover {
    color: ${props => props.disabled ? '#d9d9d9' : '#40a9ff'};
  }
  
  &:active {
    color: ${props => props.disabled ? '#d9d9d9' : '#096dd9'};
  }
`;

const CommentItem = styled.div<{ $hoverBackground?: string }>`
  display: flex;
  margin-bottom: 4px;
  position: relative;
  border-radius: 4px;
  padding: 6px;
  transition: background-color 0.1s;
  
  &:hover {
    background-color: ${props => props.$hoverBackground || 'transparent'};
  }
`;

const CommentAvatar = styled.div`
  flex-shrink: 0;
  margin-right: 12px;
  position: sticky;
  top: 0;
  align-self: flex-start;
`;

const CommentContentWrapper = styled.div`
  flex: 1;
  min-width: 0;
`;

const CommentItemHeader = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 4px;
`;

const CommentText = styled.div`
  line-height: 1.5;
  word-wrap: break-word;
  white-space: pre-wrap;
`;

const CommentActions = styled.div`
  position: absolute;
  top: 0;
  right: 0;
  opacity: 0;
  transition: opacity 0.2s;
  
  ${CommentItem}:hover & {
    opacity: 1;
  }

  .ant-popover-inner {
    padding: 10px!important;
  }
`;

const EventOptions = [
  clickEvent,
  submitEvent,
  deleteEvent,
  mentionEvent,
] as const;

const childrenMap = {
  value: jsonControl(convertCommentData, commentDate),
  title: withDefault(StringControl, trans("comment.titledDefaultValue")),
  placeholder: withDefault(StringControl, trans("comment.placeholder")),
  buttonText: withDefault(StringControl, trans("comment.buttonText")),
  sendCommentAble: BoolControl.DEFAULT_TRUE,
  deleteAble: BoolControl,
  copyAble: BoolControl,
  autoScroll: BoolControl.DEFAULT_TRUE,
  userInfo: jsonControl(checkUserInfoData, {
    name: "{{currentUser.name}}",
    email: "{{currentUser.email}}",
  }),
  mentionList: jsonControl(checkMentionListData, {
    "@": ["Li Lei", "Han Meimei"],
    "#": ["123", "456", "789"],
  }),
  onEvent: eventHandlerControl(EventOptions),
  style: styleControl(CommentStyle),
  autoHeight: AutoHeightControl,
  commentList: jsonValueExposingStateControl("commentList", []),
  deletedItem: jsonValueExposingStateControl("deletedItem", []),
  submitedItem: jsonValueExposingStateControl("submitedItem", []),
  mentionName: valueComp<string>(""),
};

const CommentCompBase = (
  props: RecordConstructorToView<typeof childrenMap> & {
    dispatch: (action: CompAction) => void;
  }
) => {
  const commentContentRef = useRef<HTMLDivElement>(null);
  const {
    value,
    dispatch,
    style,
    title,
    sendCommentAble,
    mentionList,
    userInfo,
    placeholder,
    deleteAble,
    copyAble,
    autoScroll,
    autoHeight,
    onEvent,
    submitedItem,
    deletedItem,
  } = props;
  type PrefixType = "@" | keyof typeof mentionList;
  const [MentionListData, setMentionList] = useState<typeof mentionList>([]);
  const [commentListData, setCommentListData] = useState<commentDataTYPE[]>([]);
  const [prefix, setPrefix] = useState<PrefixType>("@");
  const [context, setContext] = useState<string>("");
  const [inputHeight, setInputHeight] = useState<number>(40);
  useEffect(() => {
    setCommentListData(value);
  }, [value]);

  // 使用useMemo优化提及列表的合并逻辑
  const mergedMentionList = useMemo(() => {
    const userNames = commentListData
      .map(item => item?.user?.name)
      .filter(Boolean);
    
    return _.merge(mentionList, {
      "@": _.union(mentionList["@"] || [], userNames),
    });
  }, [mentionList, commentListData]);

  useEffect(() => {
    setMentionList(mergedMentionList);
  }, [mergedMentionList]);

  useEffect(() => {
    props.commentList.onChange(commentListData);
    if (autoScroll) {
      setTimeout(() => {
        if (commentContentRef.current) {
          commentContentRef.current.scrollTop = commentContentRef.current.scrollHeight;
        }
      }, 50);
    }
  }, [commentListData, props.commentList, autoScroll]);

  // 获取提及搜索关键字
  const onSearch = useCallback((_: string, newPrefix: PrefixType) => {
    setPrefix(newPrefix);
  }, []);

  // 生成评论头像 - 使用useCallback优化
  const generateCommentAvatar = useCallback((item: commentDataTYPE) => {
    const displayName = item?.user?.displayName ?? item?.user?.name;
    const avatarStyle = {
      backgroundColor: item?.user?.avatar
        ? ""
        : getInitialsAndColorCode(displayName)[1],
      verticalAlign: "middle" as const,
    };

    let avatarText = null;
    if (!item?.user?.avatar) {
      if (item?.user?.displayName) {
        avatarText = item.user.displayName;
      } else {
        const name = item?.user?.name;
        if (/^([\u4e00-\u9fa5]{2,4})$/gi.test(name)) {
          avatarText = name.slice(-2);
        } else {
          avatarText = name.split(' ').map(word => word[0]).join('').toUpperCase().slice(0, 2);
        }
      }
    }

    return (
      <Avatar
        onClick={() => onEvent("click")}
        style={avatarStyle}
        src={item?.user?.avatar}
      >
        {avatarText}
      </Avatar>
    );
  }, [onEvent]);
  const adjustTextareaHeight = useCallback((textarea: HTMLTextAreaElement) => {
    if (textarea) {
      textarea.style.height = 'auto';
      const scrollHeight = textarea.scrollHeight;
      const minHeight = 40;
      const maxHeight = 120;
      const newHeight = Math.min(Math.max(scrollHeight, minHeight), maxHeight);
      textarea.style.height = newHeight + 'px';
      setInputHeight(newHeight);
    }
  }, []);

  const onChange = useCallback((value: string) => {
    const subObject = {
      user: userInfo,
      value: value.trim(),
      createdAt: dayjs().format(),
    };
    submitedItem.onChange(subObject);
    setContext(value);
    // 延迟执行以确保DOM已更新
    setTimeout(() => {
      const textarea = document.querySelector('.ant-mentions textarea') as HTMLTextAreaElement;
      if (textarea) {
        adjustTextareaHeight(textarea);
      }
    }, 0);
  }, [adjustTextareaHeight]);

  const handleSubmit = useCallback(() => {
    // 检查输入内容是否为空或只包含空白字符
    const trimmedContext = context.trim();
    if (!trimmedContext) {
      return; // 不允许发送空评论
    }

    const subObject = {
      user: userInfo,
      value: trimmedContext, // 使用去除首尾空白的内容
      createdAt: dayjs().format(),
    };
    submitedItem.onChange(subObject);
    setCommentListData(prev => [...prev, subObject]);
    setContext("");
    setInputHeight(40); // 重置输入框高度

    // 手动重置textarea的高度样式
    setTimeout(() => {
      const textarea = document.querySelector('.ant-mentions textarea') as HTMLTextAreaElement;
      if (textarea) {
        textarea.style.height = '40px';
      }
    }, 0);

    onEvent("submit");
  }, [context, userInfo, submitedItem, onEvent]);

  const handleDelete = useCallback((index: number) => {
    setCommentListData(prev => {
      const newList = [...prev];
      const deletedItemData = newList.splice(index, 1)[0];
      deletedItem.onChange(deletedItemData);
      return newList;
    });
    onEvent("delete");
  }, [deletedItem, onEvent]);

  const handleCopy = useCallback((item: commentDataTYPE) => {
    const success = copy(item?.value || '');
    if (success) {
      message.success(trans("comment.commentCopied"));
      onEvent("click");
    } else {
      message.error(trans("comment.commentCopyFailed"));
    }
  }, [onEvent]);

  const onPressEnter = useCallback((e: React.KeyboardEvent) => {
    if (e.shiftKey) {
      e.preventDefault();
      // 检查是否有有效内容再提交
      if (context.trim()) {
        handleSubmit();
      }
    }
  }, [context, handleSubmit]);
  // 使用useMemo优化标题计算
  const displayTitle = useMemo(() => {
    if (title === "") return "";
    
    const count = commentListData.length;
    let processedTitle = title.replaceAll("%d", count.toString());
    
    if (count > 1) {
      processedTitle = processedTitle.replace("comment", "comments");
    }
    
    return processedTitle;
  }, [title, commentListData.length]);

  // 使用useMemo优化提及选项
  const mentionOptions = useMemo(() => 
    (MentionListData[prefix] || []).map(
      (value: string, index: number) => ({
        key: index.toString(),
        value: value,
        label: value,
      })
    ), [MentionListData, prefix]
  );

  return (
    <Wrapper $style={style} $autoHeight={autoHeight}>
      {displayTitle && (
        <CommentHeader>
          <div>{displayTitle}</div>
        </CommentHeader>
      )}

      <CommentContent ref={commentContentRef} $autoHeight={autoHeight} role="feed" aria-label="评论列表">
        <VirtualList
          data={commentListData}
          itemKey={(item: commentDataTYPE) => item?.createdAt || `comment-${Math.random()}`}
        >
          {(item, index) => (
            <CommentItem 
              key={`${item?.createdAt}-${index}`} 
              $hoverBackground={style.commentHoverBackground}
            >
              <CommentAvatar>
                {generateCommentAvatar(item)}
              </CommentAvatar>
              <CommentContentWrapper>
                <CommentItemHeader>
                  <button
                    style={{
                      background: 'none',
                      border: 'none',
                      padding: 0,
                      color: '#1890ff',
                      cursor: 'pointer',
                      textDecoration: 'underline',
                      marginRight: '8px'
                    }}
                    onClick={() => onEvent("click")}
                    type="button"
                  >
                    {item?.user?.name}
                  </button>
                  <Tooltip
                    title={
                      dayjs(item?.createdAt).isValid()
                        ? dayjs(item?.createdAt).format("YYYY/M/D HH:mm:ss")
                        : trans("comment.dateErr")
                    }
                    placement="bottom"
                  >
                    <span
                      style={{
                        color: "#999",
                        fontSize: "11px",
                      }}
                    >
                      {dayjs(item?.createdAt).isValid()
                        ? dayjs(item?.createdAt).fromNow()
                        : trans("comment.dateErr")}
                    </span>
                  </Tooltip>
                </CommentItemHeader>
                <CommentText>
                  {item?.value}
                </CommentText>
              </CommentContentWrapper>
              {(copyAble || deleteAble) && (
                <CommentActions>
                  {copyAble && (
                    <CopyOutlined
                      style={{
                        color: "#1890ff",
                        cursor: 'pointer',
                        padding: '4px',
                        marginRight: '4px'
                      }}
                      onClick={() => handleCopy(item)}
                      role="button"
                      tabIndex={0}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault();
                          handleCopy(item);
                        }
                      }}
                    />
                  )}
                  {deleteAble && (
                    <CloseOutlined
                      style={{
                        color: "#c32230",
                        cursor: 'pointer',
                        padding: '4px'
                      }}
                      onClick={() => handleDelete(index)}
                      role="button"
                      tabIndex={0}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault();
                          handleDelete(index);
                        }
                      }}
                    />
                  )}
                </CommentActions>
              )}
            </CommentItem>
          )}
        </VirtualList>
      </CommentContent>

      {
        sendCommentAble && (
          <CommentFooter>
            <InputContainer>
              <StyledMentions
                onSearch={onSearch}
                prefix={Object.keys(MentionListData)}
                onChange={onChange}
                onSelect={(option: any) => {
                  dispatch(changeChildAction("mentionName", option?.value, false));
                  onEvent("mention");
                }}
                value={context}
                rows={1}
                onPressEnter={onPressEnter}
                placeholder={placeholder}
                options={mentionOptions}
              />
              <SendIcon
                disabled={!context.trim()}
                $inputHeight={inputHeight}
                onClick={context.trim() ? handleSubmit : undefined}
                role="button"
                tabIndex={context.trim() ? 0 : -1}
                onKeyDown={(e) => {
                  if (context.trim() && (e.key === 'Enter' || e.key === ' ')) {
                    e.preventDefault();
                    handleSubmit();
                  }
                }}
              >
                <SendOutlined />
              </SendIcon>
            </InputContainer>
          </CommentFooter>
        )
      }
    </Wrapper >
  );
};

let CommentBasicComp = (function () {
  return new UICompBuilder(childrenMap, (props, dispatch) => (
    <CommentCompBase {...props} dispatch={dispatch} />
  ))
    .setPropertyViewFn((children) => (
      <>
        <Section name={sectionNames.basic}>
          {children.title.propertyView({
            label: trans("comment.title"),
          })}
          {children.value.propertyView({
            label: trans("comment.value"),
            tooltip: CommentDataTooltip,
            placeholder: "[]",
          })}
          {children.userInfo.propertyView({
            label: trans("comment.userInfo"),
            tooltip: CommentUserDataTooltip,
          })}
          {children.mentionList.propertyView({
            label: trans("comment.mentionList"),
            tooltip: trans("comment.mentionListDec"),
          })}
          {children.sendCommentAble.propertyView({
            label: trans("comment.showSendButton"),
          })}
          {children.sendCommentAble.getView() &&
            children.buttonText.propertyView({
              label: trans("comment.buttonTextDec"),
            })}
          {children.sendCommentAble.getView() &&
            children.placeholder.propertyView({
              label: trans("comment.placeholderDec"),
            })}
          {children.deleteAble.propertyView({
            label: trans("comment.deleteAble"),
          })}
          {children.copyAble.propertyView({
            label: trans("comment.showCopyButton"),
          })}
          {children.autoScroll.propertyView({
            label: trans("comment.autoScroll"),
          })}
        </Section>
        <Section name={sectionNames.layout}>
          {children.autoHeight.getPropertyView()}
          {children.onEvent.getPropertyView()}
          {hiddenPropertyView(children)}
        </Section>
        <Section name={sectionNames.style}>
          {children.style.getPropertyView()}
        </Section>
      </>
    ))
    .build();
})();

CommentBasicComp = class extends CommentBasicComp {
  override autoHeight(): boolean {
    return this.children.autoHeight.getView();
  }
};
export const CommentComp = withExposingConfigs(CommentBasicComp, [
  new NameConfig("commentList", trans("comment.commentList")),
  new NameConfig("deletedItem", trans("comment.deletedItem")),
  new NameConfig("submitedItem", trans("comment.submitedItem")),
  new NameConfig("mentionName", trans("comment.submitedItem")),
  NameConfigHidden,
]);
