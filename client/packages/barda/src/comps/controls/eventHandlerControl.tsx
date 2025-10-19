import {
  closestCenter,
  DndContext,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import type { SyntheticListenerMap } from '@dnd-kit/core/dist/hooks/utilities';
import {
  arrayMove,
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { DispatchType } from "barda-core";
import {
  AddEventIcon,
  AddLine,
  controlItem,
  CustomPopover,
  DragIcon,
  EditPopover,
  EventAction,
  EventContent,
  EventDiv,
  EventTitle,
  InlineEventFormWrapper,
  LinkButton,
  OptionType,
  QueryConfigItemWrapper,
  ValueFromOption,
} from "barda-design";
import EmptyItem from "components/EmptyItem";
import { CustomListAction, list } from "comps/generators/list";
import { simpleMultiComp } from "comps/generators/multi";
import { trans } from "i18n";
import _ from "lodash";
import { Fragment, ReactNode, useContext, useEffect, useState } from "react";
import { memo } from "util/cacheUtils";
import { EditorContext } from "../editorState";
import { ActionSelectorControl } from "./actionSelector/actionSelectorControl";
import { dropdownControl } from "./dropdownControl";

export interface EventConfigType extends OptionType {
  readonly description: string;
}

export type EventConfigsType = readonly EventConfigType[];

interface SingleEventHandlerProperViewProps {
  onCopy: () => void;
  onDelete: () => void;
  inline?: boolean;
  type?: "query";
  popup: boolean;
  eventConfigs: EventConfigsType;
  isFocused?: boolean;
  onFocus?: () => void;
  dragListeners?: SyntheticListenerMap | undefined;
}

const childrenMap = {
  name: dropdownControl<EventConfigsType>([], ""), // event name
  // FIXME: refactor the parameter config more properly
  handler: ActionSelectorControl,
};

class SingleEventHandlerControl<T extends EventConfigsType> extends simpleMultiComp(childrenMap) {
  // view is function (eventName: ValueFromOption<T>) => void, representing a named event
  getView() {
    const name = this.children.name.getView();
    const handler = this.children.handler.getView();
    return (eventName: ValueFromOption<T>) => {
      if (eventName !== name) {
        return;
      }
      if (handler) {
        return handler();
      }
    };
  }

  propertyView(props: SingleEventHandlerProperViewProps) {
    const name = this.children.name.getView();
    const children = this.children;
    const { eventConfigs } = props;

    const eventName = eventConfigs.find((x) => x.value === name)?.label?.toString();

    let content: ReactNode = null;
    if (props.inline && eventConfigs.length === 1) {
      content = (
        <InlineEventFormWrapper>
          <div>
            {trans("eventHandler.inlineEventTitle", { eventName: eventName?.toLowerCase() ?? "" })}
          </div>
          {children.handler.propertyView({
            label: trans("eventHandler.action"),
            placement: props.type,
          })}
        </InlineEventFormWrapper>
      );
    } else {
      content = (
        <>
          {eventConfigs.length > 1 &&
            children.name.propertyView({
              label: trans("eventHandler.event"),
              options: eventConfigs,
            })}
          {children.handler.propertyView({
            label: trans("eventHandler.action"),
            placement: props.type,
          })}
        </>
      );
    }

    const eventAction = this.children.handler.displayName();

    if (props.inline) {
      return content;
    }
    return (
      <EventDiv
        onFocus={props.onFocus}
        style={props.isFocused ? { boxShadow: '0 0 0 2px #d6e4ff', border: '1px solid #3377ff' } : { border: 'none', borderBottom: '1px solid #d7d9e0' }}
      >
        <DragIcon
          style={{ width: '16px', height: '16px', cursor: 'grab', marginLeft: '6px' }}
          {...(props.dragListeners || {})}
        />
        <CustomPopover
          title={trans("edit")}
          content={content}
          type={props.type}
          defaultVisible={props.popup}
          focus={props.isFocused}
        >
          <EventContent>
            {!_.isEmpty(eventName) && <EventTitle>{eventName}</EventTitle>}
            <EventAction>{eventAction}</EventAction>
          </EventContent>
        </CustomPopover>
        <EditPopover copy={props.onCopy} del={props.onDelete} />
      </EventDiv>
    );
  }
}

// SortableItem 组件
interface SortableItemProps {
  id: string;
  children: (dragListeners: SyntheticListenerMap | undefined) => React.ReactNode;
}
function SortableItem({ id, children }: SortableItemProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id });
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    zIndex: isDragging ? 999 : undefined,
    background: isDragging ? '#f0f0f0' : undefined,
  };
  return (
    <div ref={setNodeRef} style={style} {...attributes}>
      {children(listeners as SyntheticListenerMap)}
    </div>
  );
}

const EventHandlerControlPropertyView = (props: {
  dispatch: DispatchType;
  pushAction: (value: any) => CustomListAction<typeof SingleEventHandlerControl>;
  deleteAction: (index: number) => CustomListAction<typeof SingleEventHandlerControl>;
  arrayMoveAction: (oldIndex: number, newIndex: number) => CustomListAction<typeof SingleEventHandlerControl>;
  items: InstanceType<typeof SingleEventHandlerControl>[];
  inline?: boolean;
  title?: ReactNode;
  type?: "query";
  eventConfigs: EventConfigsType;
}) => {
  const { dispatch, pushAction, deleteAction, arrayMoveAction, inline = false, items, eventConfigs, type } = props;
  const editorState = useContext(EditorContext);
  const [showNewCreate, setShowNewCreate] = useState(false);
  // 拖拽排序相关 state
  const [itemsOrder, setItemsOrder] = useState(items.map((_, idx) => String(idx)));
  // 跟踪当前焦点位置
  const [focusedIndex, setFocusedIndex] = useState<number | null>(null);

  useEffect(() => {
    setItemsOrder(items.map((_, idx) => String(idx)));
  }, [items]);
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } })
  );
  const handleDragEnd = (event: any) => {
    const { active, over } = event;
    if (active.id !== over?.id) {
      const oldIndex = itemsOrder.indexOf(active.id);
      const newIndex = itemsOrder.indexOf(over.id);
      const newOrder = arrayMove(itemsOrder, oldIndex, newIndex);
      setItemsOrder(newOrder);
      dispatch(arrayMoveAction(oldIndex, newIndex));
      if (focusedIndex === oldIndex) {
        setFocusedIndex(newIndex);
      }
    }
  };

  useEffect(() => setShowNewCreate(false), [dispatch]);

  const handleAdd = () => {
    if (eventConfigs.length === 0) {
      return;
    }
    const queryExecHandler = {
      compType: "executeQuery",
      comp: {
        queryName: editorState?.selectedOrFirstQueryComp()?.children.name.getView(),
      },
    };
    const messageHandler = {
      compType: "message",
    };
    const isInDevIde = !!window.__BARDA_DEV__;
    const newHandler = {
      name: eventConfigs[0].value,
      handler: isInDevIde ? messageHandler : queryExecHandler,
    } as const;
    dispatch(pushAction(type !== "query" ? newHandler : { name: eventConfigs[0].value }));
    setShowNewCreate(true);
    setFocusedIndex(items.length);
  };

  const renderItems = () =>
    itemsOrder.length > 0 ? (
      <div style={{ 'gap': '0px', border: '1px solid #d7d9e0', borderRadius: '6px' }}>
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext items={itemsOrder} strategy={verticalListSortingStrategy}>
            {itemsOrder.map((orderId, displayIndex) => {
              const orderIdx = Number(orderId);
              const isFocused = focusedIndex === displayIndex;
              return (
                <SortableItem key={orderId} id={orderId}>
                  {(dragListeners) => (
                    <Fragment>
                      {items[orderIdx] && items[orderIdx].propertyView({
                        type,
                        inline,
                        onCopy: () => dispatch(pushAction({ ...items[orderIdx].toJsonValue() })),
                        onDelete: () => dispatch(deleteAction(orderIdx)),
                        popup: showNewCreate && orderIdx === items.length - 1,
                        eventConfigs,
                        isFocused,
                        onFocus: () => setFocusedIndex(displayIndex),
                        dragListeners,
                      })}
                    </Fragment>
                  )}
                </SortableItem>
              );
            })}
          </SortableContext>
        </DndContext>
      </div>
    ) : (
      <EmptyItem onClick={handleAdd}>{trans("eventHandler.emptyEventHandlers")}</EmptyItem>
    );
  if (props.inline) {
    return <div style={{ paddingTop: 8 }}>{renderItems()}</div>;
  }
  if (type === "query") {
    return (
      <QueryConfigItemWrapper>
        <LinkButton
          text={trans("addItem")}
          icon={<AddEventIcon />}
          onClick={() => {
            dispatch(pushAction({ name: eventConfigs[0].value }));
            setShowNewCreate(true);
          }}
        />
        <div style={{ height: "8px" }} />
        {renderItems()}
      </QueryConfigItemWrapper>
    );
  }
  return (
    <>
      <AddLine title={props.title} add={handleAdd} />
      {renderItems()}
    </>
  );
};

class EventHandlerControl<T extends EventConfigsType> extends list(SingleEventHandlerControl) {
  @memo
  // @ts-ignore
  getView() {
    return (eventName: ValueFromOption<T>) => {
      const list: Promise<unknown>[] = [];
      super.getView().forEach((child) => {
        const ret = child.getView()(eventName);
        if (ret) {
          list.push(ret);
        }
      });
      return Promise.all(list);
    };
  }

  isBind(eventName: ValueFromOption<T>) {
    return super.getView().some((child) => child.children.name.getView() === eventName);
  }

  override getPropertyView() {
    return this.propertyView();
  }

  propertyView(options?: { inline?: boolean; title?: ReactNode; type?: "query"; eventConfigs: T }) {
    const title = options?.title ?? trans("eventHandler.eventHandlers");
    return controlItem(
      { filterText: title },
      <EventHandlerControlPropertyView
        type={options?.type}
        eventConfigs={options?.eventConfigs || []}
        dispatch={this.dispatch}
        pushAction={this.pushAction}
        deleteAction={this.deleteAction}
        arrayMoveAction={this.arrayMoveAction}
        items={super.getView() as any}
        inline={options?.inline}
        title={title}
      />
    );
  }
}

export function eventHandlerControl<T extends EventConfigsType>(eventConfigs?: T, type?: "query") {
  class EventHandlerTempControl extends EventHandlerControl<T> {
    getEventNames() {
      return eventConfigs;
    }

    propertyView(options?: { inline?: boolean; title?: ReactNode; eventConfigs?: T }) {
      return super.propertyView({
        ...options,
        type,
        eventConfigs: options?.eventConfigs || eventConfigs || ([] as any),
      });
    }
  }

  return EventHandlerTempControl;
}

export const submitEvent: EventConfigType = {
  label: trans("event.submit"),
  value: "submit",
  description: trans("event.submitDesc"),
};
export const changeEvent: EventConfigType = {
  label: trans("event.change"),
  value: "change",
  description: trans("event.changeDesc"),
};
export const focusEvent: EventConfigType = {
  label: trans("event.focus"),
  value: "focus",
  description: trans("event.focusDesc"),
};
export const blurEvent: EventConfigType = {
  label: trans("event.blur"),
  value: "blur",
  description: trans("event.blurDesc"),
};
export const clickEvent: EventConfigType = {
  label: trans("event.click"),
  value: "click",
  description: trans("event.clickDesc"),
};
export const closeEvent: EventConfigType = {
  label: trans("event.close"),
  value: "close",
  description: trans("event.closeDesc"),
};
export const successEvent: EventConfigType = {
  label: trans("event.success"),
  value: "success",
  description: trans("event.successDesc"),
};

export const deleteEvent: EventConfigType = {
  label: trans("event.delete"),
  value: "delete",
  description: trans("event.deleteDesc"),
};
export const mentionEvent: EventConfigType = {
  label: trans("event.mention"),
  value: "mention",
  description: trans("event.mentionDesc"),
};

export const clearEvent: EventConfigType = {
  label: trans("event.clear"),
  value: "clear",
  description: trans("event.clearDesc"),
};

export const InputEventHandlerControl = eventHandlerControl([
  changeEvent,
  focusEvent,
  blurEvent,
  submitEvent,
  clearEvent,
] as const);

export const ButtonEventHandlerControl = eventHandlerControl([clickEvent] as const);

export const ChangeEventHandlerControl = eventHandlerControl([changeEvent] as const);

export const SelectEventHandlerControl = eventHandlerControl([
  changeEvent,
  focusEvent,
  blurEvent,
  clearEvent,
] as const);

export const ScannerEventHandlerControl = eventHandlerControl([
  clickEvent,
  successEvent,
  closeEvent,
] as const);
