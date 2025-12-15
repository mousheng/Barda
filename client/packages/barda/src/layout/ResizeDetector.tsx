import { ReactNode } from 'react';
import { useResizeDetector, useResizeDetectorProps } from 'react-resize-detector';

interface MyResizeDetectorProps extends useResizeDetectorProps<HTMLDivElement> {
    children?: ReactNode;
}
// @deprecated 准备废弃，使用react-resize-detector代替
export const MyResizeDetector = (props: MyResizeDetectorProps) => {
    const { ref } = useResizeDetector(props);
    return <div ref={ref} style={{ width: '100%', height: "100%" }}>{
        props.children
    }</div>;
};