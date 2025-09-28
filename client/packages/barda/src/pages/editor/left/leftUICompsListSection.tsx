import { UICompType } from "@barda/comps/uiCompRegistry";
import { trans } from "@barda/i18n";
import { NodeInfo, NodeItem } from "@barda/util/objectUtils";
import { BasicDataNode, DataNode, EventDataNode } from "antd/es/tree";
import { BaseSection, FoldedIcon, LeftClose, LeftCommon, LeftOpen, ScrollBar, Tooltip, UnfoldIcon } from "barda-design";
import { EditorContext, SelectSourceType } from "comps/editorState";
import React, { memo, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { CompStateIcon } from "../editorConstants";
import { CollapseView } from "../LeftContent";
import { CollapseWrapper, DirectoryTreeStyle, Node } from "../styledComponents";

interface CustomTreeNodeProps {
    node: NodeItem;
    showData: NodeInfo[];
    setShowData: (data: NodeInfo[]) => void;
}

const CustomTreeNode = React.memo((props: CustomTreeNodeProps) => {
    const { node, showData, setShowData } = props;
    const editorState = useContext(EditorContext);
    const compInfos = editorState.uiCompInfoList(comp => comp.children.name.getView() === node.title);
    const data = compInfos.length > 0 ? compInfos[0] : undefined;
    const info = showData.find((item: NodeInfo) => item.key === node.key);

    const handleClick = useCallback(
        (e: React.MouseEvent) => {
            e.stopPropagation();
            const index = showData.findIndex((item: NodeInfo) => item.key === node.key);
            let newData: NodeInfo[] = [];
            const info = {
                key: node.key,
                show: true,
                clientX: e.currentTarget.parentElement?.offsetLeft,
            };
            if (index > -1) {
                newData = showData.map((item: NodeInfo) => {
                    if (item.key === node.key) {
                        return info;
                    }
                    return item;
                });
            } else {
                newData = [...showData, info];
            }
            setShowData(newData);
        }, [showData, node.key, setShowData]);

    const handleClose = useCallback((e: React.MouseEvent) => {
        e.stopPropagation();
        const newData = showData.map((item: NodeInfo) => {
            if (item.key === node.key) {
                return {
                    key: item.key,
                    show: false,
                    clientX: undefined,
                };
            }
            return item;
        });
        setShowData(newData);
    }, [showData, node.key, setShowData]);

    return (
        <Node>
            <span>
                <span>{node.title}</span>
                {data &&
                    !!Object.keys(data.data)?.length &&
                    (info?.show ? (
                        <Tooltip
                            placement="right"
                            title={trans("leftPanel.collapseTip", { component: node.title })}
                        >
                            <div
                                title=""
                                className="show-data"
                                onClick={handleClose}
                            >
                                <LeftOpen />
                            </div>
                        </Tooltip>
                    ) : (
                        <Tooltip
                            placement="right"
                            title={trans("leftPanel.expandTip", { component: node.title })}
                        >
                            <div
                                title=""
                                className="no-data"
                                onClick={handleClick}
                            >
                                <LeftClose />
                            </div>
                        </Tooltip>
                    ))}
            </span>
            {info?.show && data && (
                <CollapseWrapper title="" $clientX={info?.clientX} onClick={(e: React.MouseEvent) => e.stopPropagation()}>
                    <ScrollBar style={{ maxHeight: "400px" }}>
                        <CollapseView
                            key={data.name}
                            name={data.name}
                            desc={data.dataDesc}
                            data={data.data}
                            isOpen={true}
                        />
                    </ScrollBar>
                </CollapseWrapper>
            )}
        </Node>
    );
});

interface BottomResSectionProps {
    sectionName: string;
    treeData: NodeItem[];
    selectedKeys: React.Key[];
    clickNode: (selectedCompNames: Set<string>, selectSource?: SelectSourceType) => void;
    height: number;
}

export const UICompsListSection = memo(({ treeData, selectedKeys, clickNode, sectionName, height }: BottomResSectionProps) => {
    const treeRef: any = useRef(null);
    const [expandedKeys, setExpandedKeys] = useState<React.Key[]>(selectedKeys);
    const [showData, setShowData] = useState<NodeInfo[]>([]);
    const icon = useMemo(() => (props: any) => props.type && (CompStateIcon[props.type as UICompType] || <LeftCommon />), [])
    const switcherIcon = useMemo(() => (props: any) => props.expanded ? <FoldedIcon /> : <UnfoldIcon />, [])
    const handleNodeClick = useCallback((e: React.MouseEvent<HTMLSpanElement, MouseEvent>, node: EventDataNode<DataNode>) => {
        clickNode(new Set([node.title as string]), "leftPanel")
    }, [clickNode])
    const onExpand = useCallback((keys: React.Key[]) => setExpandedKeys(keys), [])
    const titleRender = useCallback((nodeData: BasicDataNode) =>
        <CustomTreeNode node={nodeData as NodeItem} showData={showData} setShowData={setShowData} />,
        [showData, setShowData])


    useEffect(() => {
        setExpandedKeys(selectedKeys)
        setTimeout(() => {
            treeRef?.current?.scrollTo({ key: selectedKeys?.[0], behavior: "smooth" })
        }, 100);
    }, [selectedKeys])
    return (
        <BaseSection name={sectionName} width={288} noMargin>
            <span>
                <DirectoryTreeStyle
                    virtual
                    autoExpandParent
                    expandedKeys={expandedKeys}
                    height={height}
                    icon={icon}
                    onClick={handleNodeClick}
                    onExpand={onExpand}
                    ref={treeRef}
                    selectedKeys={selectedKeys}
                    switcherIcon={switcherIcon}
                    titleRender={titleRender}
                    treeData={treeData}
                />
            </span>
        </BaseSection>
    );
}); 