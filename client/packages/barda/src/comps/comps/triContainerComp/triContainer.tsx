import { HintPlaceHolder } from "barda-design";
import { ContainerStyleType, parseBoxValues } from "comps/controls/styleControlConstants";
import { BackgroundColorContext } from "comps/utils/backgroundColorContext";
import { ReactNode } from "react";
import styled, { css } from "styled-components";
import { gridItemCompToGridItems, InnerGrid } from "../containerComp/containerView";
import { TriContainerViewProps } from "../triContainerComp/triContainerCompBuilder";

const getStyle = (style: ContainerStyleType) => {
  return css`
    border-color: ${style.border};
    border-radius: ${style.radius};
    overflow: hidden;
  `;
};

const Wrapper = styled.div<{ $style: ContainerStyleType }>`
  display: flex;
  flex-flow: column;
  height: 100%;
  border: 1px solid #d7d9e0;
  border-radius: 4px;
  ${(props) => props.$style && getStyle(props.$style)}
`;

const HeaderInnerGrid = styled(InnerGrid) <{ $backgroundColor: string, $autoFullHeight: boolean }>`
  overflow: visible;
  ${(props) => props.$autoFullHeight && `height: 100%;`}
  ${(props) => props.$backgroundColor && `background-color: ${props.$backgroundColor};`}
  border-radius: 0;
`;

const BodyInnerGridWrapper = styled.div<{
  $showBorder: boolean;
  $borderColor: string;
}>`
  flex: 1;
  position: relative;
  border-top: ${props => props.$showBorder ? 1 : 0}px solid ${props => props.$borderColor};
  min-height: 0;
`;

const BodyInnerGrid = styled(InnerGrid) <{
  $backgroundColor: string;
}>`
  ${(props) => props.$backgroundColor && `background-color: ${props.$backgroundColor};`}
  border-radius: 0;
`;

const FooterInnerGrid = styled(InnerGrid) <{
  $showBorder: boolean;
  $backgroundColor: string;
  $borderColor: string;
  $autoFullHeight: boolean;
}>`
  ${(props) => props.$autoFullHeight && `height: 100%;`}
  border-top: ${(props) => `${props.$showBorder ? 1 : 0}px solid ${props.$borderColor}`};
  overflow: visible;
  ${(props) => props.$backgroundColor && `background-color: ${props.$backgroundColor};`}
  border-radius: 0;
`;

export type TriContainerProps = TriContainerViewProps & {
  hintPlaceholder?: ReactNode;
  showScroll?: boolean;
};

export function TriContainer(props: TriContainerProps) {
  const { container } = props;
  const { showHeader, showFooter } = container;
  // When the header and footer are not displayed, the body must be displayed
  const showBody = container.showBody || (!showHeader && !showFooter);

  const { items: headerItems, ...otherHeaderProps } = container.header;
  const { items: bodyItems, ...otherBodyProps } = container.body["0"].children.view.getView();
  const { items: footerItems, ...otherFooterProps } = container.footer;
  const { style } = container;

  const headPadding = parseBoxValues(style.headPadding_UNIT, [3, 19, 3, 19], false) as number[];
  const bodyPadding = parseBoxValues(style.bodyPadding_UNIT, [11, 19, 11, 19], false) as number[];
  const footerPadding = parseBoxValues(style.footerPadding_UNIT, [3, 19, 3, 19], false) as number[];
  return (
    <Wrapper $style={style}>
      {showHeader && (
        <BackgroundColorContext.Provider value={container.style.headerBackground}>
          <HeaderInnerGrid
            {...otherHeaderProps}
            items={gridItemCompToGridItems(headerItems)}
            autoHeight={true}
            emptyRows={5}
            minHeight="46px"
            containerPadding={[headPadding[1], headPadding[0]]}
            showName={{ bottom: showBody || showFooter ? 20 : 0 }}
            $backgroundColor={style?.headerBackground}
            $autoFullHeight={((showBody === showFooter) && !showBody) || (!showBody && showFooter)}
          />
        </BackgroundColorContext.Provider>
      )}
      {showBody && (
        <BackgroundColorContext.Provider value={container.style.background}>
          <BodyInnerGridWrapper
            $showBorder={showHeader}
            $borderColor={style?.border}
          >
            <BodyInnerGrid
              className="BodyInnerGrid"
              {...otherBodyProps}
              items={gridItemCompToGridItems(bodyItems)}
              autoHeight={container.autoHeight}
              emptyRows={14}
              minHeight={showHeader ? "143px" : "142px"}
              containerPadding={
                [bodyPadding[1], bodyPadding[0]]
              }
              hintPlaceholder={props.hintPlaceholder ?? HintPlaceHolder}
              $backgroundColor={style?.background}
              showScroll={props.showScroll}
            />
          </BodyInnerGridWrapper>
        </BackgroundColorContext.Provider>
      )}
      {showFooter && (
        <BackgroundColorContext.Provider value={container.style.footerBackground}>
          <FooterInnerGrid
            $showBorder={showHeader || showBody}
            {...otherFooterProps}
            items={gridItemCompToGridItems(footerItems)}
            autoHeight={true}
            emptyRows={5}
            minHeight={showBody ? "47px" : "46px"}
            containerPadding={[footerPadding[1], footerPadding[0]]}
            showName={{ top: showHeader || showBody ? 20 : 0 }}
            $backgroundColor={style?.footerBackground}
            $borderColor={style?.border}
            $autoFullHeight={((showBody === showHeader) && !showBody)}
          />
        </BackgroundColorContext.Provider>
      )}
    </Wrapper>
  );
}
