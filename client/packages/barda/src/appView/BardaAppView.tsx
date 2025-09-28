import React, { useEffect, useImperativeHandle, useRef, useState } from "react";
import {
  AppViewInstance,
  AppViewInstanceOptions,
  EventTriggerHandler,
  OutputChangeHandler,
} from "./AppViewInstance";
import { bootstrapAppAt } from "./bootstrapAt";

export interface BardaAppViewProps<I, O> extends AppViewInstanceOptions<I> {
  appId: string;
  className?: string;
  onModuleOutputChange?: OutputChangeHandler<O>;
  onModuleEventTriggered?: EventTriggerHandler;
}

function BardaAppViewBase<I = any, O = any>(
  props: BardaAppViewProps<I, O>,
  ref: React.Ref<AppViewInstance | undefined>
) {
  const { appId, className, onModuleEventTriggered, onModuleOutputChange, ...options } = props;

  const [instance, setInstance] = useState<AppViewInstance | undefined>();
  const nodeRef = useRef<HTMLDivElement>(null);

  useImperativeHandle(ref, () => instance, [instance]);

  useEffect(() => {
    const node = nodeRef.current;
    if (!node) {
      return;
    }
    bootstrapAppAt<I>(appId, node, options).then(setInstance);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appId]);

  useEffect(() => {
    if (!instance) {
      return;
    }
    instance.on("moduleOutputChange", onModuleOutputChange);
    instance.on("moduleEventTriggered", onModuleEventTriggered);
  }, [instance, onModuleEventTriggered, onModuleOutputChange]);

  useEffect(() => {
    if (options.moduleInputs && instance) {
      instance.setModuleInputs(options.moduleInputs);
    }
  }, [options.moduleInputs, instance]);

  return <div ref={nodeRef} className={className}></div>;
}

export const BardaAppView = React.forwardRef(BardaAppViewBase);
