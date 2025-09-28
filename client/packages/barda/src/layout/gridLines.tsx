import { CSSProperties } from "react";
import { calcGridColWidth, PositionParams } from "./calculateUtils";
import { Position, setTransform } from "./utils";

interface GridLineProps {
  position: Position;
  positionParams: PositionParams;
  lineColor: string;
}

function setBackgroundProps(positionParams: PositionParams, lineColor: string): CSSProperties {
  const { rowHeight } = positionParams;
  const colWidth = calcGridColWidth(positionParams);
  return {
    backgroundImage: `linear-gradient(to right, ${lineColor} 1px, transparent 1px),linear-gradient(to bottom, ${lineColor} 1px, transparent 1px)`,
    backgroundSize: `${colWidth}px ${rowHeight}px`,
  };
}

export function GridLines(props: GridLineProps) {
  const style = {
    ...setTransform(props.position),
    ...setBackgroundProps(props.positionParams, props.lineColor),
  };
  return <div style={style} />;
}
