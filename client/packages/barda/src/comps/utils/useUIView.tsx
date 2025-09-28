import React, { ReactNode, Suspense } from "react";
import { useIsMobile } from "util/hooks";
import Skeleton from "antd/es/skeleton/index";

export function useUIView(mobileView: ReactNode, pcView: ReactNode) {
  return <>{useIsMobile() ? <Suspense fallback={<Skeleton />}>{mobileView}</Suspense> :
    <div onMouseDown={(e) => { e.preventDefault(); e.stopPropagation(); }}>
      {pcView}
    </div>
  }</>;
}
