import { ReactNode } from 'react';
import { useResizeDetector, useResizeDetectorProps } from 'react-resize-detector';

interface MyResizeDetectorProps extends useResizeDetectorProps<HTMLDivElement> {
    children?: ReactNode;
}
export const MyResizeDetector = (props: MyResizeDetectorProps) => {
    const { ref } = useResizeDetector(props);
    return <div ref={ref} style={{ width: '100%', height: "100%" }} onResize={props.onResize as any}>{
        props.children
    }</div>;
};